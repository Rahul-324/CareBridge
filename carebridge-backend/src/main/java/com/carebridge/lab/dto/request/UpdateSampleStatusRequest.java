package com.carebridge.lab.dto.request;

import com.carebridge.lab.entity.SampleStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSampleStatusRequest(
        @NotNull(message = "status is required")
        SampleStatus status,

        @Size(max = 500, message = "notes cannot exceed 500 characters")
        String notes
) {
}
