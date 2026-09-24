package com.carebridge.billing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.billing.entity.Payment;
import com.carebridge.billing.entity.PaymentMethod;
import com.carebridge.billing.entity.PaymentStatus;

public record PaymentResponse(
        UUID id,
        UUID tenantId,
        UUID invoiceId,
        String invoiceNumber,
        String paymentNumber,
        LocalDateTime paymentDate,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String transactionReference,
        PaymentStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTenant().getId(),
                payment.getInvoice().getId(),
                payment.getInvoice().getInvoiceNumber(),
                payment.getPaymentNumber(),
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getStatus(),
                payment.getNotes(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
