package com.carebridge.clinic.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.clinic.entity.Clinic;
import com.carebridge.clinic.entity.ClinicType;

public record ClinicResponse(
        UUID id,
        UUID tenantId,
        String clinicCode,
        String clinicName,
        ClinicType clinicType,
        String registrationNumber,
        String email,
        String phone,
        String website,
        String logoUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClinicResponse from(Clinic clinic) {
        return new ClinicResponse(
                clinic.getId(),
                clinic.getTenant().getId(),
                clinic.getClinicCode(),
                clinic.getClinicName(),
                clinic.getClinicType(),
                clinic.getRegistrationNumber(),
                clinic.getEmail(),
                clinic.getPhone(),
                clinic.getWebsite(),
                clinic.getLogoUrl(),
                clinic.getCreatedAt(),
                clinic.getUpdatedAt()
        );
    }
}
