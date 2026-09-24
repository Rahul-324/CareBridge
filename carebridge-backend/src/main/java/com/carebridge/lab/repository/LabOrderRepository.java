package com.carebridge.lab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.lab.entity.LabOrder;
import com.carebridge.lab.entity.LabOrderStatus;

public interface LabOrderRepository extends JpaRepository<LabOrder, UUID> {

    Optional<LabOrder> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndOrderNumber(UUID tenantId, String orderNumber);

    @Query("""
            SELECT o FROM LabOrder o
            JOIN FETCH o.tenant
            JOIN FETCH o.clinic
            JOIN FETCH o.branch
            JOIN FETCH o.patient
            JOIN FETCH o.doctor
            LEFT JOIN FETCH o.visit
            WHERE o.tenant.id = :tenantId
            AND (:patientId IS NULL OR o.patient.id = :patientId)
            AND (:visitId IS NULL OR o.visit.id = :visitId)
            AND (:status IS NULL OR o.status = :status)
            ORDER BY o.orderDate DESC
            """)
    List<LabOrder> findOrders(
            @Param("tenantId") UUID tenantId,
            @Param("patientId") UUID patientId,
            @Param("visitId") UUID visitId,
            @Param("status") LabOrderStatus status
    );
}
