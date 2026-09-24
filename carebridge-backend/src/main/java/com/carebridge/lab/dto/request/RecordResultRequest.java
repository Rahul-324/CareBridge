package com.carebridge.lab.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordResultRequest(
        @NotNull(message = "order item ID is required")
        UUID orderItemId,

        @NotBlank(message = "result text is required")
        @Size(max = 2000, message = "result text cannot exceed 2000 characters")
        String resultText,

        @Size(max = 255, message = "reference range cannot exceed 255 characters")
        String referenceRange,

        @Size(max = 50, message = "unit cannot exceed 50 characters")
        String unit,

        @Size(max = 500, message = "file URL cannot exceed 500 characters")
        String fileUrl
) {
}
