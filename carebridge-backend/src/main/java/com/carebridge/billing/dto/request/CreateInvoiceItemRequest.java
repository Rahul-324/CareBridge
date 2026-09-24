package com.carebridge.billing.dto.request;

import java.math.BigDecimal;

import com.carebridge.billing.entity.ItemType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInvoiceItemRequest(
        @NotBlank(message = "item name is required")
        @Size(max = 150, message = "item name cannot exceed 150 characters")
        String itemName,

        @NotNull(message = "item type is required")
        ItemType itemType,

        @NotNull(message = "unit price is required")
        @DecimalMin(value = "0.00", message = "unit price cannot be negative")
        BigDecimal unitPrice,

        @Min(value = 1, message = "quantity must be at least 1")
        int quantity
) {
}
