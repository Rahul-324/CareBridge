package com.carebridge.billing.controller;

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

import com.carebridge.billing.dto.request.CreateInvoiceRequest;
import com.carebridge.billing.dto.request.GenerateInvoiceFromLabOrderRequest;
import com.carebridge.billing.dto.request.GenerateInvoiceFromVisitRequest;
import com.carebridge.billing.dto.response.InvoiceResponse;
import com.carebridge.billing.entity.InvoiceStatus;
import com.carebridge.billing.service.InvoiceService;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/billing/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("invoice created successfully", invoiceService.createInvoice(request)));
    }

    @PostMapping("/generate-from-visit")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateFromVisit(
            @Valid @RequestBody GenerateInvoiceFromVisitRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("invoice generated from visit successfully", invoiceService.generateFromVisit(request)));
    }

    @PostMapping("/generate-from-lab-order")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateFromLabOrder(
            @Valid @RequestBody GenerateInvoiceFromLabOrderRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("invoice generated from lab order successfully", invoiceService.generateFromLabOrder(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> listInvoices(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID visitId,
            @RequestParam(required = false) UUID labOrderId,
            @RequestParam(required = false) InvoiceStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("invoices retrieved successfully", invoiceService.listInvoices(patientId, visitId, labOrderId, status))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("invoice retrieved successfully", invoiceService.getInvoice(id))
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoiceStatus(
            @PathVariable UUID id,
            @RequestParam InvoiceStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("invoice status updated successfully", invoiceService.updateInvoiceStatus(id, status))
        );
    }
}
