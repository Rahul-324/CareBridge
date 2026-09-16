package com.carebridge.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.auth.entity.User;

public interface UserRepository extends JpaRepository<User,UUID>{
    @Query("""
            SELECT u FROM User u
            JOIN FETCH u.tenant
            LEFT JOIN FETCH u.branch
            WHERE LOWER(u.email) = LOWER(:email)
            """)
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByIdAndTenant_Id(UUID userId,
        UUID tenantId
    );

    @Query("""
            SELECT u FROM User u
            JOIN FETCH u.tenant
            LEFT JOIN FETCH u.branch
            WHERE u.id = :id
            """)
    Optional<User> findWithTenantAndBranchById(@Param("id") UUID id);

    List<User> findAllByTenant_Id(UUID tenanrId);
    List<User> findAllByTenant_IdAndBranch_Id(
            UUID tenantId,
            UUID branchId
    );
}



