package com.carebridge.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.auth.entity.UserInvite;

public interface UserInviteRepository extends JpaRepository<UserInvite, UUID> {

    Optional<UserInvite> findByUser_Id(UUID userId);

    @Query("""
            SELECT invite FROM UserInvite invite
            JOIN FETCH invite.user user
            JOIN FETCH user.tenant
            LEFT JOIN FETCH user.branch
            JOIN FETCH invite.tenant
            WHERE invite.tokenHash = :tokenHash
            """)
    Optional<UserInvite> findByTokenHash(@Param("tokenHash") String tokenHash);
}
