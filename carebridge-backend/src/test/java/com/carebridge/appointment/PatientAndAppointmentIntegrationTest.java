package com.carebridge.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.carebridge.support.AbstractPostgresIntegrationTest;
import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PatientAndAppointmentIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PASSWORD = "SecurePassword123!";

    private String uniqueSuffix() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    private String getSuffixFromEmail(String email) {
        return email.substring(email.indexOf("+") + 1, email.indexOf("@"));
    }

    private void onboard(String adminEmail) throws Exception {
        String suffix = getSuffixFromEmail(adminEmail);
        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Key", "test-onboarding-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenant": {
                                    "name": "Tenant %s",
                                    "email": "tenant+%s@clinic.test"
                                  },
                                  "clinic": {
                                    "clinicName": "Clinic %s",
                                    "clinicType": "CLINIC"
                                  },
                                  "branch": {
                                    "branchName": "Main %s",
                                    "email": "branch+%s@clinic.test",
                                    "addressLine1": "1 Health St",
                                    "city": "Bengaluru",
                                    "state": "KA",
                                    "postalCode": "560001",
                                    "country": "IN"
                                  },
                                  "admin": {
                                    "firstName": "Ada",
                                    "lastName": "Admin",
                                    "email": "%s",
                                    "password": "%s"
                                  }
                                }
                                """.formatted(suffix, suffix, suffix, suffix, suffix, adminEmail, PASSWORD)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    @Test
    void testPatientRegistrationSearchAndAppointmentWorkflow() throws Exception {
        String adminEmail = "admin+" + uniqueSuffix() + "@clinic.test";
        onboard(adminEmail);
        String token = login(adminEmail, PASSWORD);

        // 1. Get branch ID from login details
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(adminEmail, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        String branchId = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.branchId");
        String doctorUserId = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.userId");

        // 2. Register Patient
        MvcResult registerResult = mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "gender": "MALE",
                                  "dateOfBirth": "1990-05-15",
                                  "phone": "9876543210",
                                  "email": "john.doe@example.test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.mrn").exists())
                .andReturn();

        String patientId = JsonPath.read(registerResult.getResponse().getContentAsString(), "$.data.id");
        String patientMrn = JsonPath.read(registerResult.getResponse().getContentAsString(), "$.data.mrn");

        // 3. Search Patients (Tenant Scoped)
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", "Bearer " + token)
                        .param("query", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(patientId));

        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", "Bearer " + token)
                        .param("query", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].mrn").value(patientMrn));

        // 4. Book Appointment
        LocalDateTime start1 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end1 = start1.plusMinutes(30);

        MvcResult apptResult = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchId": "%s",
                                  "patientId": "%s",
                                  "doctorId": "%s",
                                  "appointmentType": "CONSULTATION",
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "reasonForVisit": "Regular Checkup"
                                }
                                """.formatted(branchId, patientId, doctorUserId, start1, end1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.appointmentNumber").exists())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                .andReturn();

        String appointmentId = JsonPath.read(apptResult.getResponse().getContentAsString(), "$.data.id");

        // 5. Attempt overlapping appointment (Doctor Conflict) -> 409 CONFLICT
        LocalDateTime startOverlap = start1.plusMinutes(15);
        LocalDateTime endOverlap = startOverlap.plusMinutes(30);

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchId": "%s",
                                  "patientId": "%s",
                                  "doctorId": "%s",
                                  "appointmentType": "CONSULTATION",
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "reasonForVisit": "Overlapping Checkup"
                                }
                                """.formatted(branchId, patientId, doctorUserId, startOverlap, endOverlap)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("doctor has a conflicting appointment during this time slot"));

        // 6. Reschedule Appointment
        LocalDateTime start2 = start1.plusHours(2);
        LocalDateTime end2 = start2.plusMinutes(30);

        mockMvc.perform(patch("/api/v1/appointments/" + appointmentId + "/reschedule")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "notes": "Rescheduled to afternoon"
                                }
                                """.formatted(start2, end2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes").value("Rescheduled to afternoon"));

        // 7. Create Visit linked to Appointment
        mockMvc.perform(post("/api/v1/visits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchId": "%s",
                                  "patientId": "%s",
                                  "doctorId": "%s",
                                  "appointmentId": "%s",
                                  "chiefComplaint": "Fever and headaches",
                                  "diagnosis": "Viral flu"
                                }
                                """.formatted(branchId, patientId, doctorUserId, appointmentId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.visitNumber").exists())
                .andExpect(jsonPath("$.data.chiefComplaint").value("Fever and headaches"))
                .andExpect(jsonPath("$.data.appointmentId").value(appointmentId));

        // 8. Verify appointment status automatically set to COMPLETED
        mockMvc.perform(get("/api/v1/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
