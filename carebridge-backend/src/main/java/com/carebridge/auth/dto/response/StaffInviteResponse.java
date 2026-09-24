package com.carebridge.auth.dto.response;

import java.util.UUID;

import com.carebridge.auth.entity.UserRole;
import com.carebridge.auth.entity.UserStatus;

public record StaffInviteResponse(
        UUID userId,
        UUID tenantId,
        UUID branchId,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        UserStatus status,
        String inviteToken,
        long expiresInMs
) {
}
