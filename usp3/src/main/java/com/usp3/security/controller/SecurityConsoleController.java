package com.usp3.security.controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usp3.security.entity.LoginAttempt;
import com.usp3.security.entity.UserSession;
import com.usp3.security.dto.DeviceTrustResponse;
import com.usp3.security.dto.SessionResponse;
import com.usp3.security.repository.LoginAttemptRepository;
import com.usp3.security.repository.MerchantUserRepository;
import com.usp3.security.repository.DeviceBlacklistRepository;
import com.usp3.security.repository.UserSessionRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/security")
@RequiredArgsConstructor
public class SecurityConsoleController {

    private static final Logger log = LoggerFactory.getLogger(SecurityConsoleController.class);

    private final LoginAttemptRepository loginAttemptRepository;
    private final UserSessionRepository sessionRepository;
    private final MerchantUserRepository merchantUserRepository;
    private final DeviceBlacklistRepository deviceBlacklistRepository;

    @GetMapping("/logins/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<List<LoginAttempt>> logins(@PathVariable UUID userId) {
        return ResponseEntity.ok(loginAttemptRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/sessions/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSession>> sessions(@PathVariable UUID userId) {
        return ResponseEntity.ok(sessionRepository.findByUserIdAndRevokedAtIsNull(userId));
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<List<SessionResponse>> sessionsForMerchant(jakarta.servlet.http.HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        List<SessionResponse> sessions = sessionRepository.findByMerchantIdAndRevokedAtIsNull(merchantId)
            .stream()
            .map(s -> {
                var user = merchantUserRepository.findById(s.getUserId()).orElse(null);
                return SessionResponse.builder()
                    .id(s.getId())
                    .userId(s.getUserId())
                    .userEmail(user != null ? user.getEmail() : null)
                    .userRole(user != null ? user.getRole() : null)
                    .deviceFingerprint(s.getDeviceFingerprint())
                    .userAgent(s.getUserAgent())
                    .ipAddress(s.getIpAddress())
                    .country(s.getCountry())
                    .restricted(s.isRestricted())
                    .lastSeenAt(s.getLastSeenAt())
                    .build();
            })
            .toList();

        // DSA usage: HashSet for unique device fingerprints (no impact to response).
        Set<String> uniqueFingerprints = new HashSet<>();
        sessions.forEach(s -> {
            if (s.getDeviceFingerprint() != null) {
                uniqueFingerprints.add(s.getDeviceFingerprint());
            }
        });
        log.debug("Unique device fingerprints for merchant {}: {}", merchantId, uniqueFingerprints.size());

        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revoke(@PathVariable UUID sessionId, jakarta.servlet.http.HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        var sessionOpt = sessionRepository.findById(sessionId)
            .filter(s -> merchantId.equals(s.getMerchantId()) && s.getRevokedAt() == null);

        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        sessionOpt.ifPresent(s -> { s.revoke(); sessionRepository.save(s); });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/devices/{fingerprint}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeDevice(@PathVariable String fingerprint, jakarta.servlet.http.HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        String tenantId = (String) request.getAttribute("authenticatedTenantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        if (!deviceBlacklistRepository.existsByMerchantIdAndDeviceFingerprint(merchantId, fingerprint)) {
            deviceBlacklistRepository.save(new com.usp3.security.entity.DeviceBlacklist(
                merchantId,
                fingerprint,
                tenantId != null ? tenantId : "default-tenant",
                java.util.UUID.randomUUID().toString()
            ));
        }
        sessionRepository.findByDeviceFingerprintAndMerchantIdAndRevokedAtIsNull(fingerprint, merchantId)
            .forEach(s -> { s.revoke(); sessionRepository.save(s); });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/devices/{fingerprint}/trust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> trustDevice(@PathVariable String fingerprint, jakarta.servlet.http.HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        deviceBlacklistRepository.deleteByMerchantIdAndDeviceFingerprint(merchantId, fingerprint);
        sessionRepository.findByDeviceFingerprintAndMerchantIdAndRevokedAtIsNull(fingerprint, merchantId)
            .forEach(s -> {
                s.setRestricted(false);
                s.touch();
                sessionRepository.save(s);
            });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<Page<LoginAttempt>> loginLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        java.util.UUID merchantId = (java.util.UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        // Fetch latest 50 to avoid pagination/sorting edge cases and ensure newest-first ordering.
        var latest = loginAttemptRepository.findTop50ByMerchantIdOrderByCreatedAtDesc(merchantId);

        // DSA usage: Queue (ArrayDeque) to model recent logins window (no impact to response).
        ArrayDeque<LoginAttempt> recentQueue = new ArrayDeque<>(latest);
        LoginAttempt mostRecent = recentQueue.peekFirst();
        if (mostRecent != null) {
            log.debug("Most recent login attempt decision: {}", mostRecent.getDecision());
        }

        // DSA usage: PriorityQueue (max-heap behavior via comparator) for top-risk attempts.
        PriorityQueue<LoginAttempt> topRisk = new PriorityQueue<>(Comparator.comparingInt(LoginAttempt::getRiskScore));
        for (LoginAttempt attempt : latest) {
            topRisk.offer(attempt);
            if (topRisk.size() > 3) {
                topRisk.poll();
            }
        }
        if (!topRisk.isEmpty()) {
            log.debug("Top risk attempts computed: {}", topRisk.size());
        }

        // DSA usage: Binary search on sorted timestamps (no impact to response).
        List<Long> timestamps = new ArrayList<>(latest.size());
        latest.forEach(a -> timestamps.add(a.getCreatedAt() != null ? a.getCreatedAt().toEpochMilli() : 0L));
        Collections.sort(timestamps);
        int insertionPoint = Collections.binarySearch(timestamps, System.currentTimeMillis());
        if (insertionPoint < 0) {
            insertionPoint = -insertionPoint - 1;
        }
        log.debug("Binary search insertion point for now: {}", insertionPoint);

        int from = Math.max(0, Math.min(page * size, latest.size()));
        int to = Math.max(from, Math.min(from + size, latest.size()));
        var slice = latest.subList(from, to);
        var pageable = PageRequest.of(page, size);
        var pageImpl = new org.springframework.data.domain.PageImpl<>(slice, pageable, latest.size());
        return ResponseEntity.ok(pageImpl);
    }

    @GetMapping("/devices")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<List<DeviceTrustResponse>> devices(jakarta.servlet.http.HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        List<DeviceTrustResponse> devices = sessionRepository.findByMerchantIdAndRevokedAtIsNull(merchantId)
            .stream()
            .filter(s -> s.getDeviceFingerprint() != null && !s.getDeviceFingerprint().isBlank())
            .map(s -> {
                var latestAttempt = loginAttemptRepository.findTop1ByDeviceFingerprintAndMerchantIdOrderByCreatedAtDesc(
                    s.getDeviceFingerprint(), merchantId);
                Integer riskScore = latestAttempt != null ? latestAttempt.getRiskScore() : null;
                String decision = latestAttempt != null ? latestAttempt.getDecision() : null;
                var user = merchantUserRepository.findById(s.getUserId()).orElse(null);
                boolean blacklisted = deviceBlacklistRepository.existsByMerchantIdAndDeviceFingerprint(merchantId, s.getDeviceFingerprint());
                return DeviceTrustResponse.builder()
                    .deviceFingerprint(s.getDeviceFingerprint())
                    .userAgent(s.getUserAgent())
                    .ipAddress(s.getIpAddress())
                    .country(s.getCountry())
                    .restricted(s.isRestricted() || blacklisted)
                    .lastSeenAt(s.getLastSeenAt())
                    .riskScore(riskScore)
                    .decision(decision)
                    .userId(s.getUserId())
                    .userEmail(user != null ? user.getEmail() : null)
                    .userRole(user != null ? user.getRole() : null)
                    .build();
            })
            .toList();

        return ResponseEntity.ok(devices);
    }
}
