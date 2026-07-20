package com.carebridge.tenant;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@RequiredArgsConstructor
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


}
