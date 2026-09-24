package com.carebridge.auth.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.auth.dto.request.LoginRequest;
import com.carebridge.auth.dto.request.RefreshTokenRequest;
import com.carebridge.auth.dto.response.CurrentUserResponse;
import com.carebridge.auth.dto.response.LoginResponse;
import com.carebridge.auth.entity.User;
import com.carebridge.auth.repository.UserRepository;
import com.carebridge.auth.security.CustomUserDetails;
import com.carebridge.auth.security.JwtService;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.AccountEligibility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        assertAccountCanAccess(userDetails);

        User user = userRepository.findWithTenantAndBranchById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        return issueSession(userDetails, refreshTokenService.issue(user));
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.RotatedRefreshToken rotated =
                refreshTokenService.rotate(request.refreshToken());

        CustomUserDetails userDetails = CustomUserDetails.from(rotated.user());
        assertAccountCanAccess(userDetails);

        return issueSession(userDetails, rotated.refreshToken());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    public CurrentUserResponse currentUser(CustomUserDetails userDetails) {
        return new CurrentUserResponse(
                userDetails.getUserId(),
                userDetails.getTenantId(),
                userDetails.getBranchId(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getEmail(),
                userDetails.getRole()
        );
    }

    private LoginResponse issueSession(CustomUserDetails userDetails, String refreshToken) {
        return new LoginResponse(
                jwtService.generateToken(userDetails),
                refreshToken,
                "Bearer",
                jwtService.getExpiration(),
                refreshTokenService.getRefreshExpirationMs(),
                userDetails.getUserId(),
                userDetails.getTenantId(),
                userDetails.getBranchId(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getEmail(),
                userDetails.getRole()
        );
    }

    private void assertAccountCanAccess(CustomUserDetails userDetails) {
        if (!AccountEligibility.isTenantAccessible(userDetails.getTenantStatus())
                || !AccountEligibility.isBranchAccessible(userDetails.getBranchStatus())) {
            throw new DisabledException("account is not active");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
