package com.carebridge.visit.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.visit.entity.Visit;
import com.carebridge.visit.entity.VisitStatus;

public record VisitResponse(
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
        UUID appointmentId,
        String visitNumber,
        LocalDateTime visitDate,
        String chiefComplaint,
        String diagnosis,
        String notes,
        VisitStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static VisitResponse from(Visit visit) {
        return new VisitResponse(
                visit.getId(),
                visit.getTenant().getId(),
                visit.getClinic().getId(),
                visit.getBranch().getId(),
                visit.getBranch().getBranchName(),
                visit.getPatient().getId(),
                visit.getPatient().getMrn(),
                visit.getPatient().getFirstName(),
                visit.getPatient().getLastName(),
                visit.getDoctor().getId(),
                visit.getDoctor().getFirstName(),
                visit.getDoctor().getLastName(),
                visit.getAppointment() == null ? null : visit.getAppointment().getId(),
                visit.getVisitNumber(),
                visit.getVisitDate(),
                visit.getChiefComplaint(),
                visit.getDiagnosis(),
                visit.getNotes(),
                visit.getStatus(),
                visit.getCreatedAt(),
                visit.getUpdatedAt()
        );
    }
}
