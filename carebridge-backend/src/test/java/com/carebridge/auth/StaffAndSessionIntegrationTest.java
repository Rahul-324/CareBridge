package com.carebridge.auth;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class StaffAndSessionIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Password1";
    private static final String STAFF_PASSWORD = "StaffPass1";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onboardingWithoutRegistrationKeyIsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(uniqueSuffix(), "admin@clinic.test")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("onboarding is not permitted"));
    }

    @Test
    void refreshRotatesTokenAndLogoutRevokesIt() throws Exception {
        String adminEmail = "admin+" + uniqueSuffix() + "@clinic.test";
        onboard(adminEmail);

        MvcResult login = login(adminEmail, PASSWORD);
        String accessToken = read(login, "$.data.accessToken");
        String refreshToken = read(login, "$.data.refreshToken");

        MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", not(refreshToken)))
                .andReturn();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid refresh token"));

        String nextRefresh = read(refreshed, "$.data.refreshToken");
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(nextRefresh)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(nextRefresh)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void clinicAdminCanInviteStaffWhoThenAcceptsAndLogsIn() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";
        String staffEmail = "doctor+" + suffix + "@clinic.test";
        onboard(adminEmail);

        String adminToken = read(login(adminEmail, PASSWORD), "$.data.accessToken");

        MvcResult invite = mockMvc.perform(post("/api/v1/staff/invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Dana",
                                  "lastName": "Doctor",
                                  "email": "%s",
                                  "role": "DOCTOR"
                                }
                                """.formatted(staffEmail)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.inviteToken", notNullValue()))
                .andExpect(jsonPath("$.data.status").value("INVITED"))
                .andReturn();

        String inviteToken = read(invite, "$.data.inviteToken");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(staffEmail, STAFF_PASSWORD)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s", "password": "%s" }
                                """.formatted(inviteToken, STAFF_PASSWORD)))
                .andExpect(status().isOk());

        String staffToken = read(login(staffEmail, STAFF_PASSWORD), "$.data.accessToken");

        mockMvc.perform(get("/api/v1/staff")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/staff")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void clinicAdminCanInviteStaffWithVariousRoles() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";
        onboard(adminEmail);

        String adminToken = read(login(adminEmail, PASSWORD), "$.data.accessToken");

        String[] roles = {"RECEPTIONIST", "LAB_TECHNICIAN", "BILLING_STAFF"};
        for (String role : roles) {
            String staffEmail = role.toLowerCase() + "+" + suffix + "@clinic.test";
            mockMvc.perform(post("/api/v1/staff/invites")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "Staff",
                                      "lastName": "%s",
                                      "email": "%s",
                                      "role": "%s"
                                    }
                                    """.formatted(role, staffEmail, role)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.role").value(role))
                    .andExpect(jsonPath("$.data.status").value("INVITED"));
        }
    }

    private void onboard(String adminEmail) throws Exception {
        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header(OnboardingKeyFilter.HEADER_NAME, AuthAndOnboardingIntegrationTest.ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(uniqueSuffix(), adminEmail)))
                .andExpect(status().isCreated());
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private static String read(MvcResult result, String path) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), path);
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
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
