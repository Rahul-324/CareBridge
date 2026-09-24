package com.carebridge.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carebridge.auth.dto.request.AcceptInviteRequest;
import com.carebridge.auth.dto.request.InviteStaffRequest;
import com.carebridge.auth.dto.response.StaffInviteResponse;
import com.carebridge.auth.dto.response.StaffMemberResponse;
import com.carebridge.auth.entity.User;
import com.carebridge.auth.entity.UserInvite;
import com.carebridge.auth.entity.UserStatus;
import com.carebridge.auth.repository.UserInviteRepository;
import com.carebridge.auth.repository.UserRepository;
import com.carebridge.auth.security.SecureTokens;
import com.carebridge.branch.entity.Branch;
import com.carebridge.branch.entity.BranchStatus;
import com.carebridge.branch.repository.BranchRepository;
import com.carebridge.common.exception.DuplicateResourceException;
import com.carebridge.common.exception.InvalidTokenException;
import com.carebridge.common.exception.ResourceNotFoundException;
import com.carebridge.common.tenant.TenantContext;
import com.carebridge.common.tenant.TenantContextHolder;
import com.carebridge.tenant.entity.Tenant;
import com.carebridge.tenant.repository.TenantRepository;

import com.carebridge.auth.dto.request.AssignBranchRequest;
import com.carebridge.auth.dto.request.UpdateStaffStatusRequest;

@Service
public class StaffService {

    private final UserRepository userRepository;
    private final UserInviteRepository userInviteRepository;
    private final TenantRepository tenantRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final long inviteExpirationMs;

    public StaffService(
            UserRepository userRepository,
            UserInviteRepository userInviteRepository,
            TenantRepository tenantRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            @Value("${staff.invite-expiration}") long inviteExpirationMs
    ) {
        this.userRepository = userRepository;
        this.userInviteRepository = userInviteRepository;
        this.tenantRepository = tenantRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.inviteExpirationMs = inviteExpirationMs;
    }

    @Transactional
    public StaffInviteResponse invite(InviteStaffRequest request) {
        TenantContext context = TenantContextHolder.require();
        UUID tenantId = context.requireTenantId();
        String email = normalizeEmail(request.email());

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenant not found"));
        Branch branch = resolveBranch(request.branchId(), context.branchId(), tenantId);

        User user = userRepository.findByEmailIgnoreCase(email)
                .map(existing -> reuseInvitedUser(existing, tenantId, request, branch))
                .orElseGet(() -> createInvitedUser(tenant, branch, request, email));

        String rawToken = SecureTokens.randomToken();
        LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMillis(inviteExpirationMs));

        UserInvite invite = userInviteRepository.findByUser_Id(user.getId())
                .orElseGet(() -> new UserInvite(user, tenant, SecureTokens.sha256(rawToken), expiresAt));

        invite.setTokenHash(SecureTokens.sha256(rawToken));
        invite.setExpiresAt(expiresAt);
        invite.setAcceptedAt(null);
        userInviteRepository.save(invite);

        return new StaffInviteResponse(
                user.getId(),
                tenantId,
                user.getBranch() == null ? null : user.getBranch().getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                rawToken,
                inviteExpirationMs
        );
    }

    @Transactional(readOnly = true)
    public List<StaffMemberResponse> listStaff() {
        return listStaff(null, null);
    }

    @Transactional(readOnly = true)
    public List<StaffMemberResponse> listStaff(UUID branchIdFilter, UserStatus statusFilter) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        List<User> users = (branchIdFilter != null)
                ? userRepository.findAllByTenant_IdAndBranch_Id(tenantId, branchIdFilter)
                : userRepository.findAllByTenant_Id(tenantId);

        return users.stream()
                .filter(u -> statusFilter == null || u.getStatus() == statusFilter)
                .map(user -> new StaffMemberResponse(
                        user.getId(),
                        tenantId,
                        user.getBranch() == null ? null : user.getBranch().getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus()
                ))
                .toList();
    }

    @Transactional
    public StaffMemberResponse updateStaffStatus(UUID staffId, UpdateStaffStatusRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        UUID currentUserId = TenantContextHolder.require().userId();

        if (currentUserId != null && currentUserId.equals(staffId) && request.status() == UserStatus.SUSPENDED) {
            throw new IllegalArgumentException("cannot suspend your own account");
        }

        User user = userRepository.findByIdAndTenant_Id(staffId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("staff member not found"));

        user.setStatus(request.status());
        User savedUser = userRepository.save(user);

        if (request.status() == UserStatus.SUSPENDED) {
            refreshTokenService.revokeAllForUser(staffId);
        }

        return new StaffMemberResponse(
                savedUser.getId(),
                tenantId,
                savedUser.getBranch() == null ? null : savedUser.getBranch().getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getStatus()
        );
    }

    @Transactional
    public StaffMemberResponse assignStaffBranch(UUID staffId, AssignBranchRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        User user = userRepository.findByIdAndTenant_Id(staffId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("staff member not found"));

        Branch branch = branchRepository.findByIdAndTenant_Id(request.branchId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("branch not found"));

        if (branch.getStatus() != BranchStatus.ACTIVE) {
            throw new IllegalArgumentException("cannot assign staff to an inactive branch");
        }

        user.setBranch(branch);
        User savedUser = userRepository.save(user);

        return new StaffMemberResponse(
                savedUser.getId(),
                tenantId,
                savedUser.getBranch().getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getStatus()
        );
    }

    @Transactional
    public void acceptInvite(AcceptInviteRequest request) {
        UserInvite invite = userInviteRepository
                .findByTokenHash(SecureTokens.sha256(request.token()))
                .orElseThrow(() -> new InvalidTokenException("invite is invalid or expired"));

        if (invite.isAccepted() || invite.isExpired()) {
            throw new InvalidTokenException("invite is invalid or expired");
        }

        User user = invite.getUser();
        if (user.getStatus() != UserStatus.INVITED) {
            throw new InvalidTokenException("invite is invalid or expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        invite.accept();
    }

    private User createInvitedUser(
            Tenant tenant,
            Branch branch,
            InviteStaffRequest request,
            String email
    ) {
        User user = new User(
                tenant,
                branch,
                request.firstName().trim(),
                normalizeNullable(request.lastName()),
                email,
                normalizeNullable(request.phone()),
                passwordEncoder.encode(SecureTokens.randomToken()),
                request.role()
        );
        user.setStatus(UserStatus.INVITED);
        return userRepository.save(user);
    }

    private User reuseInvitedUser(
            User existing,
            UUID tenantId,
            InviteStaffRequest request,
            Branch branch
    ) {
        if (!existing.getTenant().getId().equals(tenantId)
                || existing.getStatus() != UserStatus.INVITED) {
            throw new DuplicateResourceException(
                    "A user already exists with email: " + existing.getEmail()
            );
        }

        existing.setFirstName(request.firstName().trim());
        existing.setLastName(normalizeNullable(request.lastName()));
        existing.setPhone(normalizeNullable(request.phone()));
        existing.setRole(request.role());
        existing.setBranch(branch);
        return existing;
    }

    private Branch resolveBranch(UUID requestedBranchId, UUID contextBranchId, UUID tenantId) {
        UUID branchId = requestedBranchId != null ? requestedBranchId : contextBranchId;
        if (branchId == null) {
            return null;
        }

        Branch branch = branchRepository.findByIdAndTenant_Id(branchId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("branch not found"));

        if (branch.getStatus() != BranchStatus.ACTIVE) {
            throw new ResourceNotFoundException("branch not found");
        }

        return branch;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
