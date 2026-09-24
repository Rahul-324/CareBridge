package com.carebridge.lab.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLabOrderRequest(
        @NotNull(message = "branch ID is required")
        UUID branchId,

        @NotNull(message = "patient ID is required")
        UUID patientId,

        UUID visitId,

        @NotNull(message = "doctor ID is required")
        UUID doctorId,

        @NotEmpty(message = "at least one test catalog ID is required")
        List<UUID> testCatalogIds,

        @Size(max = 1000, message = "notes cannot exceed 1000 characters")
        String notes
) {
}
