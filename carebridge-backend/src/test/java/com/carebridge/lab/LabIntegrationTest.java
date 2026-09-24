package com.carebridge.lab;

import java.math.BigDecimal;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LabIntegrationTest extends AbstractPostgresIntegrationTest {

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
                                    "email": "tenant+%s@lab.test"
                                  },
                                  "clinic": {
                                    "clinicName": "Diagnostic %s",
                                    "clinicType": "DIAGNOSTIC_CENTER"
                                  },
                                  "branch": {
                                    "branchName": "Main %s",
                                    "email": "branch+%s@lab.test",
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
    void testCompleteLabWorkflow() throws Exception {
        String adminEmail = "admin+" + uniqueSuffix() + "@lab.test";
        onboard(adminEmail);
        String token = login(adminEmail, PASSWORD);

        // 1. Get branch ID & doctor user ID
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

        // 2. Add Test Catalog Item
        MvcResult catalogResult = mockMvc.perform(post("/api/v1/lab/catalog")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "testCode": "CBC-001",
                                  "testName": "Complete Blood Count",
                                  "category": "HEMATOLOGY",
                                  "description": "Measures red blood cells, white blood cells, and platelets",
                                  "price": 450.00,
                                  "referenceRange": "4.5 - 11.0 x10^3 / uL",
                                  "turnaroundTime": "24 hours"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.testCode").value("CBC-001"))
                .andReturn();

        String catalogId = JsonPath.read(catalogResult.getResponse().getContentAsString(), "$.data.id");

        // 3. Register Patient
        MvcResult patientResult = mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Smith",
                                  "gender": "FEMALE",
                                  "dateOfBirth": "1992-08-20",
                                  "phone": "9123456789",
                                  "email": "jane.smith@lab.test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String patientId = JsonPath.read(patientResult.getResponse().getContentAsString(), "$.data.id");

        // 4. Create Lab Order
        MvcResult orderResult = mockMvc.perform(post("/api/v1/lab/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchId": "%s",
                                  "patientId": "%s",
                                  "doctorId": "%s",
                                  "testCatalogIds": ["%s"],
                                  "notes": "Routine screening"
                                }
                                """.formatted(branchId, patientId, doctorUserId, catalogId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderNumber").exists())
                .andExpect(jsonPath("$.data.status").value("PLACED"))
                .andReturn();

        String orderId = JsonPath.read(orderResult.getResponse().getContentAsString(), "$.data.id");
        String orderItemId = JsonPath.read(orderResult.getResponse().getContentAsString(), "$.data.items[0].id");

        // 5. Collect Sample
        MvcResult sampleResult = mockMvc.perform(post("/api/v1/lab/samples/collect")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderItemId": "%s",
                                  "sampleType": "BLOOD",
                                  "notes": "Collected in EDTA tube"
                                }
                                """.formatted(orderItemId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sampleNumber").exists())
                .andExpect(jsonPath("$.data.status").value("COLLECTED"))
                .andReturn();

        String sampleId = JsonPath.read(sampleResult.getResponse().getContentAsString(), "$.data.id");

        // 6. Update Sample Status to RECEIVED
        mockMvc.perform(patch("/api/v1/lab/samples/" + sampleId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RECEIVED",
                                  "notes": "Sample received in good condition"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RECEIVED"));

        // 7. Upload Report File
        MvcResult fileUploadResult = mockMvc.perform(post("/api/v1/lab/files/upload")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fileName": "CBC_Jane_Smith.pdf",
                                  "contentType": "application/pdf"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String reportFileUrl = JsonPath.read(fileUploadResult.getResponse().getContentAsString(), "$.data.fileUrl");

        // 8. Record Result
        mockMvc.perform(post("/api/v1/lab/results")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderItemId": "%s",
                                  "resultText": "WBC: 6.8, RBC: 4.5, Hb: 13.5, Platelets: 250",
                                  "referenceRange": "Normal",
                                  "unit": "g/dL",
                                  "fileUrl": "%s"
                                }
                                """.formatted(orderItemId, reportFileUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.resultText").value("WBC: 6.8, RBC: 4.5, Hb: 13.5, Platelets: 250"))
                .andExpect(jsonPath("$.data.fileUrl").value(reportFileUrl));

        // 9. Verify overall Order status is COMPLETED
        mockMvc.perform(get("/api/v1/lab/orders/" + orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
