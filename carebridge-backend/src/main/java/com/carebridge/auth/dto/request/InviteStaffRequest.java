package com.carebridge.auth.dto.request;

import java.util.UUID;

import com.carebridge.auth.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteStaffRequest(
        @NotBlank(message = "first name is required")
        @Size(max = 100, message = "first name cannot exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "last name cannot exceed 100 characters")
        String lastName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 150, message = "email cannot exceed 150 characters")
        String email,

        @Size(max = 20, message = "phone cannot exceed 20 characters")
        String phone,

        @NotNull(message = "role is required")
        UserRole role,

        UUID branchId
) {
}
