package com.carebridge.billing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.billing.entity.ItemType;
import com.carebridge.billing.entity.PriceListItem;

public record PriceListItemResponse(
        UUID id,
        UUID tenantId,
        String itemCode,
        String itemName,
        ItemType itemType,
        BigDecimal unitPrice,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PriceListItemResponse from(PriceListItem item) {
        return new PriceListItemResponse(
                item.getId(),
                item.getTenant().getId(),
                item.getItemCode(),
                item.getItemName(),
                item.getItemType(),
                item.getUnitPrice(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
