package com.carebridge.Onboarding.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminDetailsRequest(
    @NotBlank(message = "Admin first name is required")
        @Size(
                max = 100,
                message = "Admin first name cannot exceed 100 characters"
        )
        String firstName,

        @Size(
                max = 100,
                message = "Admin last name cannot exceed 100 characters"
        )
        String lastName,

        @NotBlank(message = "Admin email is required")
        @Email(message = "Admin email must be valid")
        @Size(
                max = 150,
                message = "Admin email cannot exceed 150 characters"
        )
        String email,

        @Size(
                max = 20,
                message = "Admin phone cannot exceed 20 characters"
        )
        String phone,

        @NotBlank(message = "Admin password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must contain between 8 and 100 characters"
        )
        String password
) {

}
