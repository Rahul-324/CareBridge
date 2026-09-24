package com.carebridge.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.auth.entity.RefreshToken;
import com.carebridge.auth.entity.User;
import com.carebridge.auth.repository.RefreshTokenRepository;
import com.carebridge.auth.security.SecureTokens;
import com.carebridge.common.exception.InvalidTokenException;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration}") long refreshExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    @Transactional
    public String issue(User user) {
        String rawToken = SecureTokens.randomToken();
        RefreshToken refreshToken = new RefreshToken(
                user,
                user.getTenant().getId(),
                SecureTokens.sha256(rawToken),
                LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs))
        );
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RotatedRefreshToken rotate(String rawToken) {
        RefreshToken existing = requireUsable(rawToken);
        existing.revoke();

        User user = existing.getUser();
        String nextRawToken = SecureTokens.randomToken();
        RefreshToken replacement = new RefreshToken(
                user,
                existing.getTenantId(),
                SecureTokens.sha256(nextRawToken),
                LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs))
        );
        refreshTokenRepository.save(replacement);
        existing.setReplacedBy(replacement.getId());

        return new RotatedRefreshToken(user, nextRawToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(SecureTokens.sha256(rawToken))
                .ifPresent(RefreshToken::revoke);
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.findAllByUser_IdAndRevokedAtIsNull(userId)
                .forEach(RefreshToken::revoke);
    }

    private RefreshToken requireUsable(String rawToken) {
        RefreshToken token = refreshTokenRepository
                .findByTokenHash(SecureTokens.sha256(rawToken))
                .orElseThrow(() -> new InvalidTokenException("invalid refresh token"));

        if (token.isRevoked()) {
            revokeAllForUser(token.getUser().getId());
            throw new InvalidTokenException("invalid refresh token");
        }

        if (token.isExpired()) {
            token.revoke();
            throw new InvalidTokenException("invalid refresh token");
        }

        return token;
    }

    public record RotatedRefreshToken(User user, String refreshToken) {
    }
}
