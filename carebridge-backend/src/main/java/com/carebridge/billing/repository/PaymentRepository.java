package com.carebridge.billing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.billing.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndPaymentNumber(UUID tenantId, String paymentNumber);

    List<Payment> findAllByTenant_IdAndInvoice_Id(UUID tenantId, UUID invoiceId);

    @Query("""
            SELECT p FROM Payment p
            JOIN FETCH p.tenant
            JOIN FETCH p.invoice
            WHERE p.tenant.id = :tenantId
            AND (:invoiceId IS NULL OR p.invoice.id = :invoiceId)
            ORDER BY p.paymentDate DESC
            """)
    List<Payment> findPayments(
            @Param("tenantId") UUID tenantId,
            @Param("invoiceId") UUID invoiceId
    );
}
