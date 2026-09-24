package com.carebridge.clinic;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.carebridge.auth.security.OnboardingKeyFilter;
import com.carebridge.support.AbstractPostgresIntegrationTest;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ClinicOpsIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Password1";
    private static final String STAFF_PASSWORD = "StaffPass1";
    private static final String ONBOARDING_KEY = "test-onboarding-key";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAndUpdateClinicProfileSucceeds() throws Exception {
        String adminEmail = "admin+" + uniqueSuffix() + "@clinic.test";
        onboard(adminEmail);
        String token = login(adminEmail, PASSWORD);

        mockMvc.perform(get("/api/v1/clinic")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clinicName").value("Clinic " + getSuffixFromEmail(adminEmail)));

        mockMvc.perform(put("/api/v1/clinic")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clinicName": "Updated Clinic Name",
                                  "clinicType": "DIAGNOSTIC_CENTER",
                                  "registrationNumber": "REG-12345",
                                  "email": "contact@updatedclinic.test",
                                  "phone": "9876543210",
                                  "website": "https://updatedclinic.test",
                                  "logoUrl": "https://updatedclinic.test/logo.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clinicName").value("Updated Clinic Name"))
                .andExpect(jsonPath("$.data.clinicType").value("DIAGNOSTIC_CENTER"))
                .andExpect(jsonPath("$.data.registrationNumber").value("REG-12345"))
                .andExpect(jsonPath("$.data.email").value("contact@updatedclinic.test"));
    }

    @Test
    void createExtraBranchListBranchesAndDeactivateExtraBranch() throws Exception {
        String adminEmail = "admin+" + uniqueSuffix() + "@clinic.test";
        onboard(adminEmail);
        String token = login(adminEmail, PASSWORD);

        MvcResult branchesResult = mockMvc.perform(get("/api/v1/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andReturn();

        String mainBranchId = JsonPath.read(branchesResult.getResponse().getContentAsString(), "$.data[0].id");

        mockMvc.perform(patch("/api/v1/branches/" + mainBranchId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "INACTIVE" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("cannot deactivate main branch"));

        MvcResult createBranchResult = mockMvc.perform(post("/api/v1/branches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchName": "Downtown Branch",
                                  "email": "downtown@clinic.test",
                                  "phone": "1234567890",
                                  "addressLine1": "100 Main St",
                                  "city": "Bengaluru",
                                  "state": "KA",
                                  "postalCode": "560002",
                                  "country": "IN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.branchName").value("Downtown Branch"))
                .andExpect(jsonPath("$.data.mainBranch").value(false))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        String extraBranchId = JsonPath.read(createBranchResult.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/v1/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        mockMvc.perform(patch("/api/v1/branches/" + extraBranchId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "INACTIVE" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void suspendStaffAndAssignBranchSucceeds() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";
        String doctorEmail = "doc+" + suffix + "@clinic.test";
        onboard(adminEmail);
        String adminToken = login(adminEmail, PASSWORD);

        MvcResult createBranchResult = mockMvc.perform(post("/api/v1/branches")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchName": "East Wing",
                                  "addressLine1": "20 East St",
                                  "city": "Bengaluru",
                                  "state": "KA",
                                  "postalCode": "560003",
                                  "country": "IN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String newBranchId = JsonPath.read(createBranchResult.getResponse().getContentAsString(), "$.data.id");

        MvcResult inviteResult = mockMvc.perform(post("/api/v1/staff/invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Gregory",
                                  "lastName": "House",
                                  "email": "%s",
                                  "role": "DOCTOR"
                                }
                                """.formatted(doctorEmail)))
                .andExpect(status().isCreated())
                .andReturn();

        String staffId = JsonPath.read(inviteResult.getResponse().getContentAsString(), "$.data.userId");
        String inviteToken = JsonPath.read(inviteResult.getResponse().getContentAsString(), "$.data.inviteToken");

        mockMvc.perform(post("/api/v1/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s", "password": "%s" }
                                """.formatted(inviteToken, STAFF_PASSWORD)))
                .andExpect(status().isOk());

        MvcResult doctorLogin = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(doctorEmail, STAFF_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = JsonPath.read(doctorLogin.getResponse().getContentAsString(), "$.data.refreshToken");

        mockMvc.perform(patch("/api/v1/staff/" + staffId + "/branch")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "branchId": "%s" }
                                """.formatted(newBranchId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.branchId").value(newBranchId));

        mockMvc.perform(patch("/api/v1/staff/" + staffId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "SUSPENDED" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(doctorEmail, STAFF_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("account is not active"));
    }

    private void onboard(String adminEmail) throws Exception {
        String suffix = getSuffixFromEmail(adminEmail);
        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header(OnboardingKeyFilter.HEADER_NAME, ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(suffix, adminEmail)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String getSuffixFromEmail(String email) {
        int plusIndex = email.indexOf("+");
        int atIndex = email.indexOf("@");
        return email.substring(plusIndex + 1, atIndex);
    }

    private static String onboardingPayload(String suffix, String adminEmail) {
        return """
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
                """.formatted(suffix, suffix, suffix, suffix, adminEmail, PASSWORD);
    }
}
