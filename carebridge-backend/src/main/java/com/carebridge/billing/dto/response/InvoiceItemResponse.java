package com.carebridge.billing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.billing.entity.InvoiceItem;
import com.carebridge.billing.entity.ItemType;

public record InvoiceItemResponse(
        UUID id,
        String itemName,
        ItemType itemType,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InvoiceItemResponse from(InvoiceItem item) {
        return new InvoiceItemResponse(
                item.getId(),
                item.getItemName(),
                item.getItemType(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTotalPrice(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
