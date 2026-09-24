package com.carebridge.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInviteRequest(
        @NotBlank(message = "invite token is required")
        String token,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "Password must contain between 8 and 100 characters")
        String password
) {
}
