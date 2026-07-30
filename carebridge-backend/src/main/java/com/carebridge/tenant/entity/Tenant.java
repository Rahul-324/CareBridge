package com.carebridge.tenant.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
//import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    private UUID id;

    @Column(
        name="tenant_code",
        nullable = false,
        unique = true,
        length = 50
    )
    private String tenantCode;
    @Column(nullable = false,length = -150)
    private String name;
    @Column(nullable = false,length=150)
    private String email;
    @Column(nullable = false,length = 20)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 30)
    private TenantStatus status;
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    public Tenant(
            UUID id,
            String tenantCode,
            String name,
            String email,
            String phone
    ) {
        this.id = id;
        this.tenantCode = tenantCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = TenantStatus.TRIAL;
    }
    @PrePersist
    public void prePersist(){
        LocalDateTime now=LocalDateTime.now();
        if(id==null){
            id=UUID.randomUUID();

        }
        createdAt=now;
        updatedAt=now;
    }
    @PreUpdate
    public void preUpdate(){
        updatedAt=LocalDateTime.now();
    }


}
