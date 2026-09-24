package com.carebridge.appointment.dto.request;

import com.carebridge.appointment.entity.AppointmentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAppointmentStatusRequest(
        @NotNull(message = "status is required")
        AppointmentStatus status,

        @Size(max = 1000, message = "notes cannot exceed 1000 characters")
        String notes
) {
}
