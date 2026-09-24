package com.carebridge.lab.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLabTestRequest(
        @NotBlank(message = "test code is required")
        @Size(max = 50, message = "test code cannot exceed 50 characters")
        String testCode,

        @NotBlank(message = "test name is required")
        @Size(max = 150, message = "test name cannot exceed 150 characters")
        String testName,

        @Size(max = 100, message = "category cannot exceed 100 characters")
        String category,

        @Size(max = 500, message = "description cannot exceed 500 characters")
        String description,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.00", message = "price cannot be negative")
        BigDecimal price,

        @Size(max = 255, message = "reference range cannot exceed 255 characters")
        String referenceRange,

        @Size(max = 50, message = "turnaround time cannot exceed 50 characters")
        String turnaroundTime
) {
}
