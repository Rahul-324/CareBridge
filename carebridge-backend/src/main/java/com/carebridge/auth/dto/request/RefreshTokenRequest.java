package com.carebridge.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "refresh token is required")
        String refreshToken
) {
}
