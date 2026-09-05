package com.carebridge.common.tenant;

import java.util.UUID;

import com.carebridge.auth.entity.UserRole;
import com.carebridge.auth.security.CustomUserDetails;

public record TenantContext(
        UUID userId,
        UUID tenantId,
        UUID branchId,
        UserRole role,
        String email
) {
    public static TenantContext from(CustomUserDetails userDetails) {
        return new TenantContext(
                userDetails.getUserId(),
                userDetails.getTenantId(),
                userDetails.getBranchId(),
                userDetails.getRole(),
                userDetails.getEmail()
        );
    }

    public UUID requireTenantId() {
        return tenantId;
    }
}
