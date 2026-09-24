package com.carebridge.billing.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.carebridge.billing.entity.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordPaymentRequest(
        @NotNull(message = "invoice ID is required")
        UUID invoiceId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "payment amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "payment method is required")
        PaymentMethod paymentMethod,

        @Size(max = 100, message = "transaction reference cannot exceed 100 characters")
        String transactionReference,

        @Size(max = 500, message = "notes cannot exceed 500 characters")
        String notes
) {
}
