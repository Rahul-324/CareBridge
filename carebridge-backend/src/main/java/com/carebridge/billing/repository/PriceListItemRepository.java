package com.carebridge.billing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.billing.entity.PriceListItem;

public interface PriceListItemRepository extends JpaRepository<PriceListItem, UUID> {

    Optional<PriceListItem> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndItemCode(UUID tenantId, String itemCode);

    List<PriceListItem> findAllByTenant_Id(UUID tenantId);

    List<PriceListItem> findAllByTenant_IdAndStatus(UUID tenantId, String status);
}
