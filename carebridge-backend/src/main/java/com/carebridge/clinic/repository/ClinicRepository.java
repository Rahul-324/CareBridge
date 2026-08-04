package com.carebridge.clinic.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carebridge.clinic.entity.Clinic;

public interface ClinicRepository extends JpaRepository<Clinic,UUID> {
    Optional<Clinic> findByClinicCode(String clinicCode);

    Optional<Clinic> findByTenant_Id(UUID tenantId);

    boolean existsByClinicCode(String clinicCode);

    boolean existsByTenant_Id(UUID tenantId);

}
