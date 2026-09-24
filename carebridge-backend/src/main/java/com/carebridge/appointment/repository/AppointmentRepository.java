package com.carebridge.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carebridge.appointment.entity.Appointment;
import com.carebridge.appointment.entity.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByIdAndTenant_Id(UUID id, UUID tenantId);

    boolean existsByTenant_IdAndAppointmentNumber(UUID tenantId, String appointmentNumber);

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Appointment a
            WHERE a.tenant.id = :tenantId
            AND a.doctor.id = :doctorId
            AND a.status NOT IN (com.carebridge.appointment.entity.AppointmentStatus.CANCELLED, com.carebridge.appointment.entity.AppointmentStatus.NO_SHOW)
            AND (:excludeId IS NULL OR a.id != :excludeId)
            AND (a.startTime < :endTime AND a.endTime > :startTime)
            """)
    boolean hasDoctorConflict(
            @Param("tenantId") UUID tenantId,
            @Param("doctorId") UUID doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") UUID excludeId
    );

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Appointment a
            WHERE a.tenant.id = :tenantId
            AND a.patient.id = :patientId
            AND a.status NOT IN (com.carebridge.appointment.entity.AppointmentStatus.CANCELLED, com.carebridge.appointment.entity.AppointmentStatus.NO_SHOW)
            AND (:excludeId IS NULL OR a.id != :excludeId)
            AND (a.startTime < :endTime AND a.endTime > :startTime)
            """)
    boolean hasPatientConflict(
            @Param("tenantId") UUID tenantId,
            @Param("patientId") UUID patientId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") UUID excludeId
    );

    @Query("""
            SELECT a FROM Appointment a
            JOIN FETCH a.tenant
            JOIN FETCH a.clinic
            JOIN FETCH a.branch
            JOIN FETCH a.patient
            JOIN FETCH a.doctor
            WHERE a.tenant.id = :tenantId
            AND (:branchId IS NULL OR a.branch.id = :branchId)
            AND (:doctorId IS NULL OR a.doctor.id = :doctorId)
            AND (:patientId IS NULL OR a.patient.id = :patientId)
            AND (:status IS NULL OR a.status = :status)
            AND (:startDate IS NULL OR a.startTime >= :startDate)
            AND (:endDate IS NULL OR a.startTime <= :endDate)
            ORDER BY a.startTime ASC
            """)
    List<Appointment> findAppointments(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId,
            @Param("doctorId") UUID doctorId,
            @Param("patientId") UUID patientId,
            @Param("status") AppointmentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
