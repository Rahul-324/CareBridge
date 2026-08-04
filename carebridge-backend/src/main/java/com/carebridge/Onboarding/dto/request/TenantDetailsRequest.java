package com.carebridge.Onboarding.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantDetailsRequest( 
    @NotBlank(message="Tenant name is required")
    @Size(
        max=150,
        message="Tenant name cannot exceed 150 characters"
    )
    String name,
    @NotBlank(message = "Tenant email is required")
        @Email(message = "Tenant email must be valid")
        @Size(
                max = 150,
                message = "Tenant email cannot exceed 150 characters"
        )
    String email,
     @Size(
                max = 20,
                message = "Tenant phone cannot exceed 20 characters"
        )
    String phone
) {

}
