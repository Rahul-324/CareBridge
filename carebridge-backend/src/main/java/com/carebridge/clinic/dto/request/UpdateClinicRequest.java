package com.carebridge.clinic.dto.request;

import com.carebridge.clinic.entity.ClinicType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateClinicRequest(
        @NotBlank(message = "clinic name is required")
        @Size(max = 150, message = "clinic name cannot exceed 150 characters")
        String clinicName,

        @NotNull(message = "clinic type is required")
        ClinicType clinicType,

        @Size(max = 100, message = "registration number cannot exceed 100 characters")
        String registrationNumber,

        @Email(message = "email must be valid")
        @Size(max = 150, message = "email cannot exceed 150 characters")
        String email,

        @Size(max = 20, message = "phone cannot exceed 20 characters")
        String phone,

        @Size(max = 255, message = "website cannot exceed 255 characters")
        String website,

        @Size(max = 500, message = "logo URL cannot exceed 500 characters")
        String logoUrl
) {
}
