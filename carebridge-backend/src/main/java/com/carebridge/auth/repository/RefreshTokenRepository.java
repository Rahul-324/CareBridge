package com.carebridge.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("""
            SELECT token FROM RefreshToken token
            JOIN FETCH token.user user
            JOIN FETCH user.tenant
            LEFT JOIN FETCH user.branch
            WHERE token.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    List<RefreshToken> findAllByUser_IdAndRevokedAtIsNull(UUID userId);
}
