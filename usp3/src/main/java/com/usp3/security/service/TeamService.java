package com.usp3.security.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.usp3.security.dto.TeamInvitationResponse;
import com.usp3.security.dto.TeamOverviewResponse;
import com.usp3.security.dto.TeamUserResponse;
import com.usp3.security.entity.Invitation;
import com.usp3.security.entity.MerchantUser;
import com.usp3.security.repository.InvitationRepository;
import com.usp3.security.repository.MerchantUserRepository;
import com.usp3.security.repository.UserSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final InvitationRepository invitationRepository;
    private final MerchantUserRepository merchantUserRepository;
    private final UserSessionRepository sessionRepository;

    public TeamOverviewResponse overview(UUID merchantId) {
        List<TeamInvitationResponse> invites = invitationRepository.findByMerchantId(merchantId)
            .stream()
            .map(this::toInvitationDto)
            .toList();

        List<TeamUserResponse> users = merchantUserRepository.findByMerchant_Id(merchantId)
            .stream()
            .map(this::toUserDto)
            .toList();

        return TeamOverviewResponse.builder()
            .invitations(invites)
            .users(users)
            .build();
    }

    @Transactional
    public TeamUserResponse updateRole(UUID userId, String role, UUID merchantId) {
        MerchantUser user = merchantUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ensureSameMerchant(user, merchantId);

        String normalizedRole = normalizeRole(role);
        user.setActive(true);
        user.setRole(normalizedRole);
        merchantUserRepository.save(user);
        return toUserDto(user);
    }

    @Transactional
    public void revokeUser(UUID userId, UUID merchantId) {
        MerchantUser user = merchantUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ensureSameMerchant(user, merchantId);

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot revoke an admin user");
        }

        user.setActive(false);
        merchantUserRepository.save(user);

        // Revoke any active sessions for this user to enforce access removal immediately.
        sessionRepository.findByUserIdAndRevokedAtIsNull(user.getId())
            .forEach(session -> {
                session.revoke();
                sessionRepository.save(session);
            });
    }

    private TeamInvitationResponse toInvitationDto(Invitation inv) {
        String status;
        if (inv.getRevokedAt() != null) {
            status = "REVOKED";
        } else if (inv.getAcceptedAt() != null) {
            status = "ACCEPTED";
        } else if (inv.isExpired()) {
            status = "EXPIRED";
        } else {
            status = "PENDING";
        }

        return TeamInvitationResponse.builder()
            .id(inv.getId())
            .email(inv.getEmail())
            .role(inv.getRole())
            .expiresAt(inv.getExpiresAt())
            .acceptedAt(inv.getAcceptedAt())
            .revokedAt(inv.getRevokedAt())
            .status(status)
            .build();
    }

    private TeamUserResponse toUserDto(MerchantUser user) {
        return TeamUserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .active(user.isActive())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    private void ensureSameMerchant(MerchantUser user, UUID merchantId) {
        if (!user.getMerchant().getId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cross-merchant access denied");
        }
    }

    private String normalizeRole(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN", "MERCHANT", "USER", "ANALYST", "DEV", "DEVELOPER" -> normalized;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        };
    }
}
