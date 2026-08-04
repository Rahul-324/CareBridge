package com.carebridge.Onboarding.dto.response;

import java.util.UUID;

public record OnboardingResponse(
        UUID tenantId,
        String tenantCode,
        String tenantName,

        UUID clinicId,
        String clinicCode,
        String clinicName,

        UUID branchId,
        String branchCode,
        String branchName,

        UUID adminUserId,
        String adminEmail
) {

}
