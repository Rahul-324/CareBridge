package com.carebridge.auth.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.carebridge.auth.entity.User;
import com.carebridge.auth.entity.UserRole;
import com.carebridge.auth.entity.UserStatus;
import com.carebridge.branch.entity.BranchStatus;
import com.carebridge.tenant.entity.TenantStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final UUID userId;
    private final UUID tenantId;
    private final UUID branchId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final UserStatus status;
    private final TenantStatus tenantStatus;
    private final BranchStatus branchStatus;

    public static CustomUserDetails from(User user) {
        UUID branchId = user.getBranch() == null
                ? null
                : user.getBranch().getId();

        BranchStatus branchStatus = user.getBranch() == null
                ? null
                : user.getBranch().getStatus();

        return new CustomUserDetails(
                user.getId(),
                user.getTenant().getId(),
                branchId,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus(),
                user.getTenant().getStatus(),
                branchStatus
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
