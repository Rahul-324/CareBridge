package com.carebridge.Onboarding.dto.request;

import com.carebridge.clinic.entity.ClinicType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClinicDetailsRequest(
    @NotBlank(message = "Clinic name is required")
        @Size(
                max = 150,
                message = "Clinic name cannot exceed 150 characters"
        )
        String clinicName,

        @NotNull(message = "Clinic type is required")
        ClinicType clinicType,

        @Size(
                max = 100,
                message = "Registration number cannot exceed 100 characters"
        )
        String registrationNumber,

        @Email(message = "Clinic email must be valid")
        @Size(
                max = 150,
                message = "Clinic email cannot exceed 150 characters"
        )
        String email,

        @Size(
                max = 20,
                message = "Clinic phone cannot exceed 20 characters"
        )
        String phone,

        @Size(
                max = 255,
                message = "Website URL cannot exceed 255 characters"
        )
        String website
) {

}
