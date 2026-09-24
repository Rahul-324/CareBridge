package com.carebridge.patient.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.patient.entity.Patient;
import com.carebridge.patient.entity.PatientStatus;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndMrn(UUID tenantId, String mrn);

    List<Patient> findAllByTenant_Id(UUID tenantId);

    @Query("""
            SELECT p FROM Patient p
            WHERE p.tenant.id = :tenantId
            AND (:status IS NULL OR p.status = :status)
            AND (:query IS NULL OR :query = '' OR (
                LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                LOWER(COALESCE(p.lastName, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                LOWER(p.mrn) LIKE LOWER(CONCAT('%', :query, '%')) OR
                p.phone LIKE CONCAT('%', :query, '%') OR
                LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            ))
            ORDER BY p.createdAt DESC
            """)
    List<Patient> searchPatients(
            @Param("tenantId") UUID tenantId,
            @Param("query") String query,
            @Param("status") PatientStatus status
    );
}
