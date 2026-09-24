package com.carebridge.billing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.billing.entity.Invoice;
import com.carebridge.billing.entity.InvoiceStatus;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndInvoiceNumber(UUID tenantId, String invoiceNumber);

    Optional<Invoice> findByTenant_IdAndVisit_Id(UUID tenantId, UUID visitId);

    Optional<Invoice> findByTenant_IdAndLabOrder_Id(UUID tenantId, UUID labOrderId);

    @Query("""
            SELECT i FROM Invoice i
            JOIN FETCH i.tenant
            JOIN FETCH i.clinic
            JOIN FETCH i.branch
            JOIN FETCH i.patient
            LEFT JOIN FETCH i.visit
            LEFT JOIN FETCH i.labOrder
            WHERE i.tenant.id = :tenantId
            AND (:patientId IS NULL OR i.patient.id = :patientId)
            AND (:visitId IS NULL OR i.visit.id = :visitId)
            AND (:labOrderId IS NULL OR i.labOrder.id = :labOrderId)
            AND (:status IS NULL OR i.status = :status)
            ORDER BY i.invoiceDate DESC
            """)
    List<Invoice> findInvoices(
            @Param("tenantId") UUID tenantId,
            @Param("patientId") UUID patientId,
            @Param("visitId") UUID visitId,
            @Param("labOrderId") UUID labOrderId,
            @Param("status") InvoiceStatus status
    );
}
