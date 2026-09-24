package com.carebridge.lab.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CollectSampleRequest(
        @NotNull(message = "order item ID is required")
        UUID orderItemId,

        @NotBlank(message = "sample type is required")
        @Size(max = 50, message = "sample type cannot exceed 50 characters")
        String sampleType,

        @Size(max = 500, message = "notes cannot exceed 500 characters")
        String notes
) {
}
