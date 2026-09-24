package com.carebridge.lab.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.lab.entity.LabSample;

public interface LabSampleRepository extends JpaRepository<LabSample, UUID> {

    @Query("""
            SELECT s FROM LabSample s
            JOIN FETCH s.labOrderItem item
            JOIN FETCH item.labOrder o
            WHERE s.id = :id AND o.tenant.id = :tenantId
            """)
    Optional<LabSample> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM LabSample s
            JOIN s.labOrderItem item
            JOIN item.labOrder o
            WHERE o.tenant.id = :tenantId AND s.sampleNumber = :sampleNumber
            """)
    boolean existsByTenantIdAndSampleNumber(@Param("tenantId") UUID tenantId, @Param("sampleNumber") String sampleNumber);
}
