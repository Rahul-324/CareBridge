package com.carebridge.common.tenant;

import com.carebridge.auth.security.CustomUserDetails;
import com.carebridge.branch.entity.BranchStatus;
import com.carebridge.tenant.entity.TenantStatus;

public final class AccountEligibility {

    private AccountEligibility() {
    }

    public static boolean canAccess(CustomUserDetails userDetails) {
        return userDetails.isEnabled()
                && userDetails.isAccountNonLocked()
                && isTenantAccessible(userDetails.getTenantStatus())
                && isBranchAccessible(userDetails.getBranchStatus());
    }

    public static boolean isTenantAccessible(TenantStatus status) {
        return status == TenantStatus.TRIAL || status == TenantStatus.ACTIVE;
    }

    public static boolean isBranchAccessible(BranchStatus status) {
        return status == null || status == BranchStatus.ACTIVE;
    }
}
