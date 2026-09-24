package com.carebridge.visit.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.visit.entity.Visit;
import com.carebridge.visit.entity.VisitStatus;

public interface VisitRepository extends JpaRepository<Visit, UUID> {

    Optional<Visit> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndVisitNumber(UUID tenantId, String visitNumber);

    @Query("""
            SELECT v FROM Visit v
            JOIN FETCH v.tenant
            JOIN FETCH v.clinic
            JOIN FETCH v.branch
            JOIN FETCH v.patient
            JOIN FETCH v.doctor
            LEFT JOIN FETCH v.appointment
            WHERE v.tenant.id = :tenantId
            AND (:branchId IS NULL OR v.branch.id = :branchId)
            AND (:doctorId IS NULL OR v.doctor.id = :doctorId)
            AND (:patientId IS NULL OR v.patient.id = :patientId)
            AND (:status IS NULL OR v.status = :status)
            ORDER BY v.visitDate DESC
            """)
    List<Visit> findVisits(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId,
            @Param("doctorId") UUID doctorId,
            @Param("patientId") UUID patientId,
            @Param("status") VisitStatus status
    );
}
