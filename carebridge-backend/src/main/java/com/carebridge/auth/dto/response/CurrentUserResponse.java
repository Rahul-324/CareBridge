package com.carebridge.auth.dto.response;

import java.util.UUID;

import com.carebridge.auth.entity.UserRole;

public record CurrentUserResponse(
        UUID userId,
        UUID tenantId,
        UUID branchId,
        String firstName,
        String lastName,
        String email,
        UserRole role
) {
}
