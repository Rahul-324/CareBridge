package com.carebridge.auth.dto.response;

import java.util.UUID;

import com.carebridge.auth.entity.UserRole;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        UUID userId,
        UUID tenantId,
        UUID branchId,
        String firstName,
        String lastName,
        String email,
        UserRole role
) {
}
