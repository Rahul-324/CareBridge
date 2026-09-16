package com.carebridge.auth.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OnboardingKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Onboarding-Key";
    public static final String INVITE_HEADER_NAME = "X-Onboarding-Invite-Token";
    public static final String INVITE_PARAM_NAME = "inviteToken";

    private final SecurityErrorWriter securityErrorWriter;
    private final byte[] expectedKey;

    public OnboardingKeyFilter(
            SecurityErrorWriter securityErrorWriter,
            @Value("${onboarding.registration-key:}") String registrationKey
    ) {
        this.securityErrorWriter = securityErrorWriter;
        this.expectedKey = registrationKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/onboarding")
                || HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth != null && existingAuth.isAuthenticated() && isBootstrapOrAdmin(existingAuth)) {
            filterChain.doFilter(request, response);
            return;
        }

        String provided = resolveProvidedToken(request);
        byte[] providedBytes = provided == null
                ? new byte[0]
                : provided.getBytes(StandardCharsets.UTF_8);

        if (expectedKey.length > 0 && MessageDigest.isEqual(expectedKey, providedBytes)) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "bootstrap-user",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_BOOTSTRAP"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
            return;
        }

        securityErrorWriter.write(
                response,
                HttpStatus.FORBIDDEN,
                "onboarding is not permitted"
        );
    }

    private String resolveProvidedToken(HttpServletRequest request) {
        String keyHeader = request.getHeader(HEADER_NAME);
        if (keyHeader != null && !keyHeader.isBlank()) {
            return keyHeader;
        }
        String inviteHeader = request.getHeader(INVITE_HEADER_NAME);
        if (inviteHeader != null && !inviteHeader.isBlank()) {
            return inviteHeader;
        }
        String inviteParam = request.getParameter(INVITE_PARAM_NAME);
        if (inviteParam != null && !inviteParam.isBlank()) {
            return inviteParam;
        }
        return null;
    }

    private boolean isBootstrapOrAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_BOOTSTRAP".equals(a.getAuthority())
                        || "ROLE_SYSTEM_ADMIN".equals(a.getAuthority())
        );
    }
}
