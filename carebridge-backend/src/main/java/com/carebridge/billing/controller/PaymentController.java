package com.carebridge.billing.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.billing.dto.request.RecordPaymentRequest;
import com.carebridge.billing.dto.response.PaymentResponse;
import com.carebridge.billing.service.PaymentService;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/billing/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @Valid @RequestBody RecordPaymentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("payment recorded successfully", paymentService.recordPayment(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> listPayments(
            @RequestParam(required = false) UUID invoiceId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("payments retrieved successfully", paymentService.listPayments(invoiceId))
        );
    }
}
