package com.carebridge.clinic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.clinic.dto.request.UpdateClinicRequest;
import com.carebridge.clinic.dto.response.ClinicResponse;
import com.carebridge.clinic.service.ClinicService;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clinic")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClinicResponse>> getClinicProfile() {
        return ResponseEntity.ok(
                ApiResponse.success("clinic profile retrieved successfully", clinicService.getClinicProfile())
        );
    }

    @PutMapping
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<ClinicResponse>> updateClinicProfile(
            @Valid @RequestBody UpdateClinicRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("clinic profile updated successfully", clinicService.updateClinicProfile(request))
        );
    }
}
