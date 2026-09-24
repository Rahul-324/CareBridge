package com.carebridge.appointment.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RescheduleAppointmentRequest(
        @NotNull(message = "start time is required")
        @Future(message = "start time must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "end time is required")
        @Future(message = "end time must be in the future")
        LocalDateTime endTime,

        @Size(max = 1000, message = "notes cannot exceed 1000 characters")
        String notes
) {
}
