package com.carebridge.branch.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(
        @NotBlank(message = "branch name is required")
        @Size(max = 150, message = "branch name cannot exceed 150 characters")
        String branchName,

        @Email(message = "email must be valid")
        @Size(max = 150, message = "email cannot exceed 150 characters")
        String email,

        @Size(max = 20, message = "phone cannot exceed 20 characters")
        String phone,

        @NotBlank(message = "address line 1 is required")
        @Size(max = 255, message = "address line 1 cannot exceed 255 characters")
        String addressLine1,

        @Size(max = 255, message = "address line 2 cannot exceed 255 characters")
        String addressLine2,

        @NotBlank(message = "city is required")
        @Size(max = 100, message = "city cannot exceed 100 characters")
        String city,

        @NotBlank(message = "state is required")
        @Size(max = 100, message = "state cannot exceed 100 characters")
        String state,

        @NotBlank(message = "postal code is required")
        @Size(max = 20, message = "postal code cannot exceed 20 characters")
        String postalCode,

        @NotBlank(message = "country is required")
        @Size(max = 100, message = "country cannot exceed 100 characters")
        String country
) {
}
