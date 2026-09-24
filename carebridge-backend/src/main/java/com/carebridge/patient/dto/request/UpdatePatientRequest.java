package com.carebridge.patient.dto.request;

import java.time.LocalDate;

import com.carebridge.patient.entity.Gender;
import com.carebridge.patient.entity.PatientStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record UpdatePatientRequest(
        @NotBlank(message = "first name is required")
        @Size(max = 100, message = "first name cannot exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "last name cannot exceed 100 characters")
        String lastName,

        @NotNull(message = "gender is required")
        Gender gender,

        @NotNull(message = "date of birth is required")
        @Past(message = "date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "phone is required")
        @Size(max = 20, message = "phone cannot exceed 20 characters")
        String phone,

        @Email(message = "email must be valid")
        @Size(max = 150, message = "email cannot exceed 150 characters")
        String email,

        @Size(max = 255, message = "address line 1 cannot exceed 255 characters")
        String addressLine1,

        @Size(max = 255, message = "address line 2 cannot exceed 255 characters")
        String addressLine2,

        @Size(max = 100, message = "city cannot exceed 100 characters")
        String city,

        @Size(max = 100, message = "state cannot exceed 100 characters")
        String state,

        @Size(max = 20, message = "postal code cannot exceed 20 characters")
        String postalCode,

        @Size(max = 100, message = "country cannot exceed 100 characters")
        String country,

        @Size(max = 150, message = "emergency contact name cannot exceed 150 characters")
        String emergencyContactName,

        @Size(max = 20, message = "emergency contact phone cannot exceed 20 characters")
        String emergencyContactPhone,

        @NotNull(message = "status is required")
        PatientStatus status
) {
}
