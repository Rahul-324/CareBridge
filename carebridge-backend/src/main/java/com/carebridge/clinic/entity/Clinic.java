package com.carebridge.clinic.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.tenant.entity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clinics")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clinic {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_clinics_tenant")
    )
    private Tenant tenant;
    @Column(
        name = "clinic_code",
        nullable = false,
        unique = true,
        length = 50
    )
    private String clinicCode;
    @Column(
        name = "clinic_name",
        nullable = false,
        length = 150
    )
    private String clinicName;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "clinic_type",
            nullable = false,
            length = 40
    )
    private ClinicType clinicType;

    @Column(
            name = "registration_number",
            length = 100
    )
    private String registrationNumber;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String website;

    @Column(
            name = "logo_url",
            length = 500
    )
    private String logoUrl;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    public Clinic(
            Tenant tenant,
            String clinicCode,
            String clinicName,
            ClinicType clinicType,
            String registrationNumber,
            String email,
            String phone
    ) {
        this.tenant = tenant;
        this.clinicCode = clinicCode;
        this.clinicName = clinicName;
        this.clinicType = clinicType;
        this.registrationNumber = registrationNumber;
        this.email = email;
        this.phone = phone;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
