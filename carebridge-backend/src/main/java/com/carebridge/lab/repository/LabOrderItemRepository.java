package com.carebridge.lab.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.lab.entity.LabOrderItem;

public interface LabOrderItemRepository extends JpaRepository<LabOrderItem, UUID> {

    @Query("""
            SELECT item FROM LabOrderItem item
            JOIN FETCH item.labOrder o
            JOIN FETCH item.testCatalog
            WHERE item.id = :id AND o.tenant.id = :tenantId
            """)
    Optional<LabOrderItem> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
