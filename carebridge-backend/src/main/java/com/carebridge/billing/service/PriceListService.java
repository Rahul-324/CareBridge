package com.carebridge.billing.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.billing.dto.request.CreatePriceListItemRequest;
import com.carebridge.billing.dto.response.PriceListItemResponse;
import com.carebridge.billing.entity.PriceListItem;
import com.carebridge.billing.repository.PriceListItemRepository;
import com.carebridge.common.exception.DuplicateResourceException;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceListService {

    private final PriceListItemRepository priceListItemRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public PriceListItemResponse createItem(CreatePriceListItemRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        if (priceListItemRepository.existsByTenant_IdAndItemCode(tenantId, request.itemCode().trim())) {
            throw new DuplicateResourceException("item code already exists in price list");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));

        PriceListItem item = new PriceListItem(
                tenant,
                request.itemCode().trim(),
                request.itemName().trim(),
                request.itemType(),
                request.unitPrice()
        );

        PriceListItem saved = priceListItemRepository.save(item);
        return PriceListItemResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PriceListItemResponse> listItems(String status) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        List<PriceListItem> list = (status != null && !status.isBlank())
                ? priceListItemRepository.findAllByTenant_IdAndStatus(tenantId, status.trim())
                : priceListItemRepository.findAllByTenant_Id(tenantId);
        return list.stream().map(PriceListItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PriceListItemResponse getItem(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        PriceListItem item = priceListItemRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("price list item not found"));
        return PriceListItemResponse.from(item);
    }

    @Transactional
    public PriceListItemResponse updateItem(UUID id, CreatePriceListItemRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        PriceListItem item = priceListItemRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("price list item not found"));

        item.setItemName(request.itemName().trim());
        item.setItemType(request.itemType());
        item.setUnitPrice(request.unitPrice());

        PriceListItem saved = priceListItemRepository.save(item);
        return PriceListItemResponse.from(saved);
    }
}
