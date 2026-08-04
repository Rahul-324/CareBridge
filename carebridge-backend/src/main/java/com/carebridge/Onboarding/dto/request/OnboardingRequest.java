package com.carebridge.Onboarding.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OnboardingRequest(
        @Valid
        @NotNull(message = "Tenant details are required")
        TenantDetailsRequest tenant,

        @Valid
        @NotNull(message = "Clinic details are required")
        ClinicDetailsRequest clinic,

        @Valid
        @NotNull(message = "Branch details are required")
        BranchDetailsRequest branch,

        @Valid
        @NotNull(message = "Administrator details are required")
        AdminDetailsRequest admin
) {

}
