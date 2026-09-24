package com.carebridge.auth.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignBranchRequest(
        @NotNull(message = "branch ID is required")
        UUID branchId
) {
}
