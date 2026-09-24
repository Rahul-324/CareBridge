package com.carebridge.visit.dto.request;

import com.carebridge.visit.entity.VisitStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateVisitRequest(
        @Size(max = 1000, message = "chief complaint cannot exceed 1000 characters")
        String chiefComplaint,

        @Size(max = 1000, message = "diagnosis cannot exceed 1000 characters")
        String diagnosis,

        @Size(max = 2000, message = "notes cannot exceed 2000 characters")
        String notes,

        @NotNull(message = "status is required")
        VisitStatus status
) {
}
