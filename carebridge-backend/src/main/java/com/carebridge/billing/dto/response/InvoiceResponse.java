package com.carebridge.billing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.carebridge.billing.entity.Invoice;
import com.carebridge.billing.entity.InvoiceStatus;

public record InvoiceResponse(
        UUID id,
        UUID tenantId,
        UUID clinicId,
        UUID branchId,
        String branchName,
        UUID patientId,
        String patientMrn,
        String patientFirstName,
        String patientLastName,
        UUID visitId,
        UUID labOrderId,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        LocalDateTime dueDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        InvoiceStatus status,
        String notes,
        List<InvoiceItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getTenant().getId(),
                invoice.getClinic().getId(),
                invoice.getBranch().getId(),
                invoice.getBranch().getBranchName(),
                invoice.getPatient().getId(),
                invoice.getPatient().getMrn(),
                invoice.getPatient().getFirstName(),
                invoice.getPatient().getLastName(),
                invoice.getVisit() == null ? null : invoice.getVisit().getId(),
                invoice.getLabOrder() == null ? null : invoice.getLabOrder().getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getDiscountAmount(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getStatus(),
                invoice.getNotes(),
                invoice.getItems().stream().map(InvoiceItemResponse::from).toList(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
