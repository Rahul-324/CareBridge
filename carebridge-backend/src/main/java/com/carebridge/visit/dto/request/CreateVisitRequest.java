package com.carebridge.visit.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateVisitRequest(
        @NotNull(message = "branch ID is required")
        UUID branchId,

        @NotNull(message = "patient ID is required")
        UUID patientId,

        @NotNull(message = "doctor ID is required")
        UUID doctorId,

        UUID appointmentId,

        @Size(max = 1000, message = "chief complaint cannot exceed 1000 characters")
        String chiefComplaint,

        @Size(max = 1000, message = "diagnosis cannot exceed 1000 characters")
        String diagnosis,

        @Size(max = 2000, message = "notes cannot exceed 2000 characters")
        String notes
) {
}
