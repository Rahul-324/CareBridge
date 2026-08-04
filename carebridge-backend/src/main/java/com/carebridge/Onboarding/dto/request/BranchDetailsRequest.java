package com.carebridge.Onboarding.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BranchDetailsRequest(
    @NotBlank(message = "Branch name is required")
        @Size(
                max = 150,
                message = "Branch name cannot exceed 150 characters"
        )
        String branchName,

        @Email(message = "Branch email must be valid")
        @Size(
                max = 150,
                message = "Branch email cannot exceed 150 characters"
        )
        String email,

        @Size(
                max = 20,
                message = "Branch phone cannot exceed 20 characters"
        )
        String phone,

        @NotBlank(message = "Address line 1 is required")
        @Size(
                max = 255,
                message = "Address line 1 cannot exceed 255 characters"
        )
        String addressLine1,

        @Size(
                max = 255,
                message = "Address line 2 cannot exceed 255 characters"
        )
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State cannot exceed 100 characters")
        String state,

        @NotBlank(message = "Postal code is required")
        @Size(
                max = 20,
                message = "Postal code cannot exceed 20 characters"
        )
        String postalCode,

        @NotBlank(message = "Country is required")
        @Size(
                max = 100,
                message = "Country cannot exceed 100 characters"
        )
        String country
) {

}
