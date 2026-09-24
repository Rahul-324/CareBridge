package com.carebridge.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.auth.dto.request.AcceptInviteRequest;
import com.carebridge.auth.dto.request.LoginRequest;
import com.carebridge.auth.dto.request.RefreshTokenRequest;
import com.carebridge.auth.dto.response.CurrentUserResponse;
import com.carebridge.auth.dto.response.LoginResponse;
import com.carebridge.auth.security.CustomUserDetails;
import com.carebridge.auth.service.AuthService;
import com.carebridge.auth.service.StaffService;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final StaffService staffService;

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("login successful", authService.login(request))
        );
    }

    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("token refreshed", authService.refresh(request))
        );
    }

    @PostMapping("/logout")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("logged out successfully"));
    }

    @PostMapping("/accept-invite")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Void>> acceptInvite(
            @Valid @RequestBody AcceptInviteRequest request
    ) {
        staffService.acceptInvite(request);
        return ResponseEntity.ok(ApiResponse.success("invite accepted successfully"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> currentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "current user retrieved successfully",
                        authService.currentUser(userDetails)
                )
        );
    }
}
