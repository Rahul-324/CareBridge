package com.carebridge.auth.dto.request;

import com.carebridge.auth.entity.UserStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateStaffStatusRequest(
        @NotNull(message = "status is required")
        UserStatus status
) {
}
