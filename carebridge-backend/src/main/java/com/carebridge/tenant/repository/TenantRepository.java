package com.carebridge.tenant.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.tenant.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant,UUID> {
    

}
