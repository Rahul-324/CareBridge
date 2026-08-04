package com.carebridge.branch.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.carebridge.clinic.entity.Clinic;
import com.carebridge.tenant.entity.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "branches")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Branch {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(name = "tenant_id",
        nullable = false,
        foreignKey = @ForeignKey(name="fk_branches_tenant")
    )
    private Tenant tenant;
    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
)
    @JoinColumn(
            name = "clinic_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_branches_clinic"
            )
    )
    private Clinic clinic;

    @Column(name = "branch_code",
        nullable = false,
        length=50
    )
    private String branchCode;
     @Column(
            name = "branch_name",
            nullable = false,
            length = 150
    )
    private String branchName;
     @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;
    @Column(
            name = "address_line1",
            length = 255
    )
    private String addressLine1;

    @Column(
            name = "address_line2",
            length = 255
    )
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;
     @Column(
            name = "postal_code",
            length = 20
    )
    private String postalCode;

    @Column(length = 100)
    private String country;

    @Column(
            name = "is_main_branch",
            nullable = false
    )
    private boolean mainBranch;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private BranchStatus status;
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

    public Branch(
            Tenant tenant,
            Clinic clinic,
            String branchCode,
            String branchName,
            String email,
            String phone,
            boolean mainBranch
    ) {
        this.tenant = tenant;
        this.clinic = clinic;
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.email = email;
        this.phone = phone;
        this.mainBranch = mainBranch;
        this.status = BranchStatus.ACTIVE;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (status == null) {
            status = BranchStatus.ACTIVE;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
