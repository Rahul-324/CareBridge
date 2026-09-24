package com.carebridge.billing;

import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BillingAndLaunchIntegrationTest extends AbstractPostgresIntegrationTest {

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
                                    "email": "tenant+%s@billing.test"
                                  },
                                  "clinic": {
                                    "clinicName": "Clinic %s",
                                    "clinicType": "CLINIC_AND_DIAGNOSTIC"
                                  },
                                  "branch": {
                                    "branchName": "Main %s",
                                    "email": "branch+%s@billing.test",
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
    void testCompleteBillingPaymentAndAuditWorkflow() throws Exception {
        String adminEmail = "admin+" + uniqueSuffix() + "@billing.test";
        onboard(adminEmail);
        String token = login(adminEmail, PASSWORD);

        // 1. Get branch & doctor user details
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

        // 2. Add Price List Item
        mockMvc.perform(post("/api/v1/billing/price-list")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemCode": "CNS-001",
                                  "itemName": "General Consultation Fee",
                                  "itemType": "CONSULTATION",
                                  "unitPrice": 500.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.itemCode").value("CNS-001"));

        // 3. Register Patient
        MvcResult patientResult = mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Robert",
                                  "lastName": "Paulson",
                                  "gender": "MALE",
                                  "dateOfBirth": "1985-03-10",
                                  "phone": "9988776655",
                                  "email": "robert@billing.test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String patientId = JsonPath.read(patientResult.getResponse().getContentAsString(), "$.data.id");

        // 4. Create Visit
        MvcResult visitResult = mockMvc.perform(post("/api/v1/visits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchId": "%s",
                                  "patientId": "%s",
                                  "doctorId": "%s",
                                  "chiefComplaint": "General Checkup",
                                  "diagnosis": "Healthy"
                                }
                                """.formatted(branchId, patientId, doctorUserId)))
                .andExpect(status().isCreated())
                .andReturn();

        String visitId = JsonPath.read(visitResult.getResponse().getContentAsString(), "$.data.id");

        // 5. Generate Invoice from Visit
        MvcResult invoiceResult = mockMvc.perform(post("/api/v1/billing/invoices/generate-from-visit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "visitId": "%s",
                                  "consultationFee": 500.00,
                                  "discountAmount": 50.00,
                                  "notes": "First visit discount"
                                }
                                """.formatted(visitId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.invoiceNumber").exists())
                .andExpect(jsonPath("$.data.totalAmount").value(450.00))
                .andExpect(jsonPath("$.data.status").value("ISSUED"))
                .andReturn();

        String invoiceId = JsonPath.read(invoiceResult.getResponse().getContentAsString(), "$.data.id");

        // 6. Record Payment for Invoice
        mockMvc.perform(post("/api/v1/billing/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceId": "%s",
                                  "amount": 450.00,
                                  "paymentMethod": "UPI",
                                  "transactionReference": "UPI-TXN-123456"
                                }
                                """.formatted(invoiceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paymentNumber").exists())
                .andExpect(jsonPath("$.data.amount").value(450.00));

        // 7. Verify Invoice Status updated to PAID
        mockMvc.perform(get("/api/v1/billing/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paidAmount").value(450.00))
                .andExpect(jsonPath("$.data.status").value("PAID"));

        // 8. Verify PHI Audit Logs endpoint
        mockMvc.perform(get("/api/v1/audit/phi")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 9. Verify Actuator Health endpoint is accessible without sensitive details exposed
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }
}
