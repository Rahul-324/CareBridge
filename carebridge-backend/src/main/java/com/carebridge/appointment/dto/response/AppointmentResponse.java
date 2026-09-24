package com.carebridge.appointment.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.appointment.entity.Appointment;
import com.carebridge.appointment.entity.AppointmentStatus;
import com.carebridge.appointment.entity.AppointmentType;

public record AppointmentResponse(
        UUID id,
        UUID tenantId,
        UUID clinicId,
        UUID branchId,
        String branchName,
        UUID patientId,
        String patientMrn,
        String patientFirstName,
        String patientLastName,
        UUID doctorId,
        String doctorFirstName,
        String doctorLastName,
        String appointmentNumber,
        AppointmentType appointmentType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status,
        String reasonForVisit,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getTenant().getId(),
                appointment.getClinic().getId(),
                appointment.getBranch().getId(),
                appointment.getBranch().getBranchName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getMrn(),
                appointment.getPatient().getFirstName(),
                appointment.getPatient().getLastName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentNumber(),
                appointment.getAppointmentType(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getReasonForVisit(),
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
