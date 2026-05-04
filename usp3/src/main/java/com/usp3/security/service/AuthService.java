package com.usp3.security.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.usp3.platform.Merchant;
import com.usp3.platform.repository.MerchantRepository;
import com.usp3.security.dto.AcceptInviteRequest;
import com.usp3.security.dto.AcceptInviteResponse;
import com.usp3.security.dto.CreateInvitationRequest;
import com.usp3.security.dto.LoginRequest;
import com.usp3.security.dto.LoginResponse;
import com.usp3.security.dto.RefreshRequest;
import com.usp3.security.dto.SignupRequest;
import com.usp3.security.dto.SignupResponse;
import com.usp3.security.entity.Invitation;
import com.usp3.security.entity.LoginAttempt;
import com.usp3.security.entity.MerchantUser;
import com.usp3.security.entity.UserSession;
import com.usp3.security.model.LoginRiskDecision;
import com.usp3.security.repository.InvitationRepository;
import com.usp3.security.repository.LoginAttemptRepository;
import com.usp3.security.repository.MerchantUserRepository;
import com.usp3.security.repository.UserSessionRepository;
import com.usp3.security.util.ApiKeyUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final MerchantRepository merchantRepository;
    private final MerchantUserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRiskEngine loginRiskEngine;
    private final UserSessionRepository sessionRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final ApiKeyService apiKeyService;

    private static final long ACCESS_TTL_SECONDS = 15 * 60;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (request.getAdminEmail() == null || request.getAdminEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (request.getMerchantName() == null || request.getMerchantName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant name is required");
        }
        if (request.getMerchantCode() == null || request.getMerchantCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant code is required");
        }

        String merchantName = normalize(request.getMerchantName(), request.getAdminEmail());
        String merchantCode = normalizeCode(request.getMerchantCode(), merchantName);
        String role = normalizeRole(request.getRole());

        merchantRepository.findByName(merchantName).ifPresent(m -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Merchant name already exists");
        });

        merchantRepository.findByMerchantCode(merchantCode).ifPresent(m -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Merchant code already exists");
        });

        userRepository.findByEmailIgnoreCase(request.getAdminEmail()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        });

        Merchant merchant = new Merchant(
            merchantName,
            merchantCode,
            "default-tenant",
            request.getTraceId()
        );
        merchantRepository.save(merchant);

        String passwordHash = passwordEncoder.encode(request.getPassword());
        MerchantUser admin = new MerchantUser(
            request.getAdminEmail().trim(),
            role,
            passwordHash,
            merchant,
            merchant.getTenantId(),
            request.getTraceId()
        );
        userRepository.save(admin);

        String rawKey = apiKeyService.createNewSandboxKey(merchant.getId(), merchant.getTenantId(), request.getTraceId());

        return new SignupResponse(merchant.getId(), admin.getId(), rawKey);
    }

    @Transactional
    public Invitation invite(CreateInvitationRequest request, String inviterUserId) {
        MerchantUser inviter = userRepository.findById(UUID.fromString(inviterUserId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inviter not found"));

        String inviterRole = inviter.getRole() == null ? "" : inviter.getRole().trim().toUpperCase();
        if (inviterRole.startsWith("ROLE_")) {
            inviterRole = inviterRole.substring("ROLE_".length());
        }
        log.info("Invite attempt by user {} ({}) role={} merchantId={}", inviter.getId(), inviter.getEmail(), inviterRole, inviter.getMerchant().getId());
        if (!"ADMIN".equals(inviterRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can invite team members");
        }

        String email = sanitizeEmail(request.getEmail());
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        String normalizedRole = normalizeRole(request.getRole(), "ANALYST");
        Integer expiresInDays = request.getExpiresInDays();
        Integer daysObj = (expiresInDays != null && expiresInDays.compareTo(0) > 0) ? expiresInDays : 7;
        Instant expiresAt = Instant.now().plus(daysObj.longValue(), ChronoUnit.DAYS);
        String token = UUID.randomUUID().toString();
        String traceId = request.getTraceId();
        if (traceId == null || traceId.isBlank()) {
            traceId = inviter.getTraceId() != null ? inviter.getTraceId() : UUID.randomUUID().toString();
        }

        Invitation invitation = new Invitation(
            email,
            normalizedRole,
            inviter.getMerchant().getId(),
            token,
            expiresAt,
            inviter.getId(),
            inviter.getTenantId(),
            traceId
        );
        Invitation saved = invitationRepository.save(invitation);
        log.info("Invite saved: id={} email={} role={} merchantId={} inviterId={}", saved.getId(), saved.getEmail(), saved.getRole(), saved.getMerchantId(), inviter.getId());
        return saved;
    }

    @Transactional
    public AcceptInviteResponse acceptInvite(AcceptInviteRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation token is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        Invitation invitation = invitationRepository.findByTokenAndAcceptedAtIsNullAndRevokedAtIsNull(request.getToken())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found or already used"));

        if (invitation.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation expired");
        }

        if (userRepository.findByEmailIgnoreCase(invitation.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        Merchant merchant = merchantRepository.findById(invitation.getMerchantId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Merchant not found"));

        String traceId = request.getTraceId();
        if (traceId == null || traceId.isBlank()) {
            traceId = invitation.getTraceId() != null ? invitation.getTraceId() : UUID.randomUUID().toString();
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        MerchantUser newUser = new MerchantUser(
            invitation.getEmail(),
            invitation.getRole(),
            passwordHash,
            merchant,
            merchant.getTenantId(),
            traceId
        );
        userRepository.save(newUser);

        invitation.accept();
        invitationRepository.save(invitation);

        return new AcceptInviteResponse(merchant.getId(), newUser.getId());
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        MerchantUser user = userRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptRepository.save(new LoginAttempt(
                user.getId(),
                user.getMerchant().getId(),
                false,
                0,
                LoginRiskDecision.BLOCK.name(),
                "PASSWORD_MISMATCH",
                "Bad credentials",
                request.getDeviceFingerprint(),
                ip,
                request.getCountry(),
                userAgent,
                user.getTenantId(),
                request.getTraceId()
            ));
            throw new IllegalArgumentException("Invalid credentials");
        }

        var risk = loginRiskEngine.evaluate(
            user.getId(),
            user.getMerchant().getId(),
            request.getDeviceFingerprint(),
            request.getCountry(),
            ip,
            userAgent,
            user.getTenantId(),
            request.getTraceId()
        );

        boolean restricted = risk.decision() == LoginRiskDecision.RESTRICT;
        if (risk.decision() == LoginRiskDecision.BLOCK) {
            loginAttemptRepository.save(new LoginAttempt(
                user.getId(),
                user.getMerchant().getId(),
                false,
                risk.riskScore(),
                risk.decision().name(),
                risk.triggeredRules() != null ? risk.triggeredRules() : "BLOCKED",
                "Login blocked by risk engine",
                request.getDeviceFingerprint(),
                ip,
                request.getCountry(),
                userAgent,
                user.getTenantId(),
                request.getTraceId()
            ));
            throw new IllegalStateException("Login blocked by risk engine");
        }

        loginAttemptRepository.save(new LoginAttempt(
            user.getId(),
            user.getMerchant().getId(),
            true,
            risk.riskScore(),
            risk.decision().name(),
            risk.triggeredRules() != null ? risk.triggeredRules() : "ALLOW",
            "Login successful",
            request.getDeviceFingerprint(),
            ip,
            request.getCountry(),
            userAgent,
            user.getTenantId(),
            request.getTraceId()
        ));

        String rawRefresh = UUID.randomUUID().toString();
        String refreshHash = ApiKeyUtils.hashKey(rawRefresh);
        UserSession session = new UserSession(
            user.getId(),
            user.getMerchant().getId(),
            refreshHash,
            userAgent,
            request.getDeviceFingerprint(),
            ip,
            request.getCountry(),
            restricted,
            user.getTenantId(),
            request.getTraceId()
        );
        sessionRepository.save(session);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("merchantId", user.getMerchant().getId().toString());
        claims.put("tenantId", user.getTenantId());
        claims.put("restricted", restricted);
        claims.put("sessionId", session.getId().toString());

        String accessToken = jwtService.generateAccessToken(user.getId().toString(), claims, ACCESS_TTL_SECONDS);

        user.setLastLoginAt(Instant.now());
        user.setLastLoginIp(ip);
        user.setLastLoginCountry(request.getCountry());
        userRepository.save(user);

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            log.info("Security Heartbeat: {}", formatHeartbeat("Access Granted", risk, userAgent, request.getCountry(), ip, restricted));
        }

        return new LoginResponse(accessToken, rawRefresh, risk.riskScore(), risk.decision().name(), risk.triggeredRules(), restricted);
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request, String userAgent, String ip) {
        String hash = ApiKeyUtils.hashKey(request.getRefreshToken());
        UserSession session = sessionRepository.findByRefreshTokenHashAndRevokedAtIsNull(hash)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh"));

        if (session.getRevokedAt() != null) {
            throw new IllegalArgumentException("Session revoked");
        }

        session.touch();

        MerchantUser user = userRepository.findById(session.getUserId())
            .orElseThrow(() -> new IllegalStateException("User missing"));

        String newRefreshRaw = UUID.randomUUID().toString();
        String newRefreshHash = ApiKeyUtils.hashKey(newRefreshRaw);
        session.revoke();
        sessionRepository.save(session);

        UserSession newSession = new UserSession(
            user.getId(),
            user.getMerchant().getId(),
            newRefreshHash,
            userAgent,
            session.getDeviceFingerprint(),
            ip,
            session.getCountry(),
            session.isRestricted(),
            user.getTenantId(),
            user.getTraceId()
        );
        sessionRepository.save(newSession);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("merchantId", user.getMerchant().getId().toString());
        claims.put("tenantId", user.getTenantId());
        claims.put("restricted", session.isRestricted());
        claims.put("sessionId", newSession.getId().toString());

        String newAccess = jwtService.generateAccessToken(user.getId().toString(), claims, ACCESS_TTL_SECONDS);

        return new LoginResponse(newAccess, newRefreshRaw, 0, session.isRestricted() ? "RESTRICT" : "ALLOW", session.isRestricted() ? "SESSION_RESTRICTED" : "NONE", session.isRestricted());
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = ApiKeyUtils.hashKey(refreshToken);
        sessionRepository.findByRefreshTokenHashAndRevokedAtIsNull(hash).ifPresent(s -> {
            s.revoke();
            sessionRepository.save(s);
        });
    }

    private String formatHeartbeat(String accessState, LoginRiskEngine.Result risk, String userAgent, String country, String ip, boolean restricted) {
        String reason = (risk.triggeredRules() == null || risk.triggeredRules().isBlank())
            ? "NONE"
            : risk.triggeredRules().split(",")[0].trim().replace('_', ' ');
        String ua = userAgent == null || userAgent.isBlank() ? "unknown device" : userAgent;
        String geo = country == null || country.isBlank() ? "unknown location" : country;
        String ipAddr = ip == null || ip.isBlank() ? "unknown ip" : ip;
        return String.format("%s. Risk Score: %d (Reason: %s). Device: %s. Location: %s. IP: %s. Restricted: %s.",
            accessState,
            risk.riskScore(),
            reason,
            ua,
            geo,
            ipAddr,
            restricted ? "YES" : "NO");
    }

    private String normalize(String value, String fallback) {
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            int at = fallback.indexOf('@');
            return (at > 0 ? fallback.substring(0, at) : fallback).trim();
        }
        return "merchant";
    }

    private String normalizeCode(String value, String name) {
        String base = (value != null && !value.isBlank()) ? value : name;
        base = base.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "m-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return base;
    }

    private String normalizeRole(String value) {
        if (value == null || value.isBlank()) {
            return "ADMIN"; // first user defaults to admin
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN", "MERCHANT", "USER", "ANALYST", "DEV", "DEVELOPER" -> normalized;
            default -> "ADMIN";
        };
    }

    private String normalizeRole(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN", "MERCHANT", "USER", "ANALYST", "DEV", "DEVELOPER" -> normalized;
            default -> fallback;
        };
    }

    private String sanitizeEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase();
    }
}
