package com.carebridge.branch.dto.request;

import com.carebridge.branch.entity.BranchStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateBranchStatusRequest(
        @NotNull(message = "status is required")
        BranchStatus status
) {
}
