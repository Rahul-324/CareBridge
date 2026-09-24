package com.carebridge.billing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.billing.dto.request.RecordPaymentRequest;
import com.carebridge.billing.dto.response.PaymentResponse;
import com.carebridge.billing.entity.Invoice;
import com.carebridge.billing.entity.Payment;
import com.carebridge.billing.repository.InvoiceRepository;
import com.carebridge.billing.repository.PaymentRepository;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public PaymentResponse recordPayment(RecordPaymentRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        Invoice invoice = invoiceRepository.findByIdAndTenant_Id(request.invoiceId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("invoice not found"));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));

        String paymentNumber = generateUniquePaymentNumber(tenantId);
        LocalDateTime now = LocalDateTime.now();

        Payment payment = new Payment(
                tenant,
                invoice,
                paymentNumber,
                now,
                request.amount(),
                request.paymentMethod(),
                normalizeNullable(request.transactionReference())
        );
        payment.setNotes(normalizeNullable(request.notes()));

        BigDecimal currentPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        invoice.setPaidAmount(currentPaid.add(request.amount()));
        invoice.updateStatusBasedOnPayments();

        invoiceRepository.save(invoice);
        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments(UUID invoiceId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return paymentRepository.findPayments(tenantId, invoiceId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    private String generateUniquePaymentNumber(UUID tenantId) {
        String number;
        do {
            number = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (paymentRepository.existsByTenant_IdAndPaymentNumber(tenantId, number));
        return number;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
