package com.carebridge.tenant.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.tenant.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant,UUID> {
    Optional<Tenant> findByTenantCode(
            String tenantCode
    );

    Optional<Tenant> findByEmailIgnoreCase(
            String email
    );

    boolean existsByTenantCode(
            String tenantCode
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

}
