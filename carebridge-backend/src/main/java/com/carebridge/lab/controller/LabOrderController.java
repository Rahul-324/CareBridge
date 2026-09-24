package com.carebridge.lab.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.common.response.ApiResponse;
import com.carebridge.lab.dto.request.CreateLabOrderRequest;
import com.carebridge.lab.dto.response.LabOrderResponse;
import com.carebridge.lab.entity.LabOrderStatus;
import com.carebridge.lab.service.LabOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lab/orders")
@RequiredArgsConstructor
public class LabOrderController {

    private final LabOrderService labOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'DOCTOR', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabOrderResponse>> createOrder(
            @Valid @RequestBody CreateLabOrderRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("lab order created successfully", labOrderService.createOrder(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LabOrderResponse>>> listOrders(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID visitId,
            @RequestParam(required = false) LabOrderStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("lab orders retrieved successfully", labOrderService.listOrders(patientId, visitId, status))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabOrderResponse>> getOrder(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("lab order retrieved successfully", labOrderService.getOrder(id))
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabOrderResponse>> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam LabOrderStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("lab order status updated successfully", labOrderService.updateOrderStatus(id, status))
        );
    }
}
