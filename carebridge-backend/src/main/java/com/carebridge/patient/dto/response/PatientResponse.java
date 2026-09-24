package com.carebridge.patient.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.patient.entity.Gender;
import com.carebridge.patient.entity.Patient;
import com.carebridge.patient.entity.PatientStatus;

public record PatientResponse(
        UUID id,
        UUID tenantId,
        String mrn,
        String firstName,
        String lastName,
        Gender gender,
        LocalDate dateOfBirth,
        String phone,
        String email,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        String emergencyContactName,
        String emergencyContactPhone,
        PatientStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getTenant().getId(),
                patient.getMrn(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getGender(),
                patient.getDateOfBirth(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAddressLine1(),
                patient.getAddressLine2(),
                patient.getCity(),
                patient.getState(),
                patient.getPostalCode(),
                patient.getCountry(),
                patient.getEmergencyContactName(),
                patient.getEmergencyContactPhone(),
                patient.getStatus(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}
