package com.carebridge.billing.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInvoiceRequest(
        @NotNull(message = "branch ID is required")
        UUID branchId,

        @NotNull(message = "patient ID is required")
        UUID patientId,

        UUID visitId,

        UUID labOrderId,

        LocalDateTime dueDate,

        @DecimalMin(value = "0.00", message = "tax amount cannot be negative")
        BigDecimal taxAmount,

        @DecimalMin(value = "0.00", message = "discount amount cannot be negative")
        BigDecimal discountAmount,

        @NotEmpty(message = "at least one invoice item is required")
        @Valid
        List<CreateInvoiceItemRequest> items,

        @Size(max = 1000, message = "notes cannot exceed 1000 characters")
        String notes
) {
}
