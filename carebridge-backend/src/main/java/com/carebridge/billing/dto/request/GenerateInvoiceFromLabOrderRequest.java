package com.carebridge.billing.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GenerateInvoiceFromLabOrderRequest(
        @NotNull(message = "lab order ID is required")
        UUID labOrderId,

        @DecimalMin(value = "0.00", message = "discount amount cannot be negative")
        BigDecimal discountAmount,

        @Size(max = 1000, message = "notes cannot exceed 1000 characters")
        String notes
) {
}
