package com.carebridge.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.auth.dto.request.InviteStaffRequest;
import com.carebridge.auth.dto.response.StaffInviteResponse;
import com.carebridge.auth.dto.response.StaffMemberResponse;
import com.carebridge.auth.service.StaffService;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.carebridge.auth.dto.request.AssignBranchRequest;
import com.carebridge.auth.dto.request.UpdateStaffStatusRequest;
import com.carebridge.auth.entity.UserStatus;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/invites")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<StaffInviteResponse>> invite(
            @Valid @RequestBody InviteStaffRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("staff invited successfully", staffService.invite(request)));
    }

    @GetMapping
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<List<StaffMemberResponse>>> listStaff(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UserStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("staff retrieved successfully", staffService.listStaff(branchId, status))
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<StaffMemberResponse>> updateStaffStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStaffStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("staff status updated successfully", staffService.updateStaffStatus(id, request))
        );
    }

    @PatchMapping("/{id}/branch")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ApiResponse<StaffMemberResponse>> assignStaffBranch(
            @PathVariable UUID id,
            @Valid @RequestBody AssignBranchRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("staff branch updated successfully", staffService.assignStaffBranch(id, request))
        );
    }
}
