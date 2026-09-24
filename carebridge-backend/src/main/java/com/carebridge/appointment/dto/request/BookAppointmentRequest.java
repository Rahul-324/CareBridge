package com.carebridge.appointment.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.appointment.entity.AppointmentType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookAppointmentRequest(
        @NotNull(message = "branch ID is required")
        UUID branchId,

        @NotNull(message = "patient ID is required")
        UUID patientId,

        @NotNull(message = "doctor ID is required")
        UUID doctorId,

        @NotNull(message = "appointment type is required")
        AppointmentType appointmentType,

        @NotNull(message = "start time is required")
        @Future(message = "start time must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "end time is required")
        @Future(message = "end time must be in the future")
        LocalDateTime endTime,

        @Size(max = 500, message = "reason for visit cannot exceed 500 characters")
        String reasonForVisit,

        @Size(max = 1000, message = "notes cannot exceed 1000 characters")
        String notes
) {
}
