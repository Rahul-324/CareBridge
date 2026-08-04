package com.carebridge.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.auth.entity.User;

public interface UserRepository extends JpaRepository<User,UUID>{
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByIdAndTenant_Id(UUID userId,
        UUID tenantId
    );
    List<User> findAllByTenant_Id(UUID tenanrId);
    List<User> findAllByTenant_IdAndBranch_Id(
            UUID tenantId,
            UUID branchId
    );
}



