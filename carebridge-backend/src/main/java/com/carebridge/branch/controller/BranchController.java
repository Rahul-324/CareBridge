package com.carebridge.branch.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.branch.dto.request.CreateBranchRequest;
import com.carebridge.branch.dto.request.UpdateBranchStatusRequest;
import com.carebridge.branch.dto.response.BranchResponse;
import com.carebridge.branch.service.BranchService;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody CreateBranchRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("branch created successfully", branchService.createBranch(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> listBranches() {
        return ResponseEntity.ok(
                ApiResponse.success("branches retrieved successfully", branchService.listBranches())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranch(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("branch retrieved successfully", branchService.getBranch(id))
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranchStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBranchStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("branch status updated successfully", branchService.updateBranchStatus(id, request))
        );
    }
}
