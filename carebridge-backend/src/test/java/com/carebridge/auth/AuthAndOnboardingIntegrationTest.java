package com.carebridge.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.entity.BranchStatus;
import com.carebridge.branch.repository.BranchRepository;
import com.carebridge.support.AbstractPostgresIntegrationTest;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.entity.TenantStatus;
import com.carebridge.tenant.repository.TenantRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndOnboardingIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Password1";
    private static final String ORIGIN = "http://localhost:5173";
    static final String ONBOARDING_KEY = "test-onboarding-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Test
    void onboardLoginAndCurrentUserSucceed() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";

        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Key", ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(suffix, adminEmail, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tenantId").isNotEmpty())
                .andExpect(jsonPath("$.data.adminEmail").value(adminEmail));

        String accessToken = loginAndGetToken(adminEmail);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(adminEmail))
                .andExpect(jsonPath("$.data.tenantId").isNotEmpty());
    }

    @Test
    void onboardAllowsMissingOptionalBranchEmail() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";

        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Key", ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(suffix, adminEmail, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void onboardWithInviteTokenHeaderSucceeds() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";

        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Invite-Token", ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(suffix, adminEmail, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void onboardWithInviteTokenQueryParamSucceeds() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";

        mockMvc.perform(post("/api/v1/onboarding/clinic?inviteToken=" + ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(suffix, adminEmail, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void duplicateAdminEmailReturnsConflict() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";
        String payload = onboardingPayload(suffix, adminEmail, true);

        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Key", ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        String secondSuffix = uniqueSuffix();
        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Key", ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(secondSuffix, adminEmail, true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";

        mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Key", ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(suffix, adminEmail, true)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "WrongPass1"
                                }
                                """.formatted(adminEmail)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid email or password"));
    }

    @Test
    void currentUserWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginRejectedWhenTenantIsSuspended() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";

        MvcResult onboard = mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Key", ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(suffix, adminEmail, true)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID tenantId = UUID.fromString(
                JsonPath.read(onboard.getResponse().getContentAsString(), "$.data.tenantId")
        );

        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow();
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenantRepository.save(tenant);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(adminEmail, PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("account is not active"));
    }

    @Test
    void loginRejectedWhenBranchIsInactive() throws Exception {
        String suffix = uniqueSuffix();
        String adminEmail = "admin+" + suffix + "@clinic.test";

        MvcResult onboard = mockMvc.perform(post("/api/v1/onboarding/clinic")
                        .header("X-Onboarding-Key", ONBOARDING_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingPayload(suffix, adminEmail, true)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID branchId = UUID.fromString(
                JsonPath.read(onboard.getResponse().getContentAsString(), "$.data.branchId")
        );

        Branch branch = branchRepository.findById(branchId).orElseThrow();
        branch.setStatus(BranchStatus.INACTIVE);
        branchRepository.save(branch);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(adminEmail, PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("account is not active"));
    }

    @Test
    void corsPreflightIsAllowedForConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String onboardingPayload(
            String suffix,
            String adminEmail,
            boolean includeBranchEmail
    ) {
        String branchEmailJson = includeBranchEmail
                ? "\"email\": \"branch+" + suffix + "@clinic.test\","
                : "";

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
                    %s
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
                """.formatted(
                suffix,
                suffix,
                suffix,
                suffix,
                branchEmailJson,
                adminEmail,
                PASSWORD
        );
    }
}
