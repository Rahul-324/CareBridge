package com.carebridge.billing.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.billing.dto.request.CreatePriceListItemRequest;
import com.carebridge.billing.dto.response.PriceListItemResponse;
import com.carebridge.billing.service.PriceListService;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/billing/price-list")
@RequiredArgsConstructor
public class PriceListController {

    private final PriceListService priceListService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PriceListItemResponse>> createItem(
            @Valid @RequestBody CreatePriceListItemRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("price list item created successfully", priceListService.createItem(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PriceListItemResponse>>> listItems(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("price list items retrieved successfully", priceListService.listItems(status))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PriceListItemResponse>> getItem(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("price list item retrieved successfully", priceListService.getItem(id))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PriceListItemResponse>> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePriceListItemRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("price list item updated successfully", priceListService.updateItem(id, request))
        );
    }
}
