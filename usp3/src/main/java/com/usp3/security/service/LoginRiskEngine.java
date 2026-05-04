package com.usp3.security.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.usp3.security.entity.LoginAttempt;
import com.usp3.security.model.LoginRiskDecision;
import com.usp3.security.repository.LoginAttemptRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginRiskEngine {

    private final LoginAttemptRepository loginAttemptRepository;

    private static final int BRUTE_FORCE_THRESHOLD = 5;
    private static final int BRUTE_FORCE_WINDOW_MINUTES = 2;

    public Result evaluate(java.util.UUID userId, java.util.UUID merchantId, String deviceFingerprint, String country, String ip, String userAgent, String tenantId, String traceId) {
        int risk = 0;
        List<String> rules = new ArrayList<>();
        List<String> details = new ArrayList<>();

        // Brute force rule
        Instant windowStart = Instant.now().minus(BRUTE_FORCE_WINDOW_MINUTES, ChronoUnit.MINUTES);
        long recentFails = loginAttemptRepository.countByUserIdAndSuccessIsFalseAndCreatedAtAfter(userId, windowStart);
        if (recentFails >= BRUTE_FORCE_THRESHOLD) {
            risk += 100;
            rules.add("BRUTE_FORCE");
            details.add(String.format("%d failed attempts in last %d minutes", recentFails, BRUTE_FORCE_WINDOW_MINUTES));
        }

        if (deviceFingerprint == null || deviceFingerprint.isBlank()) {
            risk += 10;
            rules.add("NO_DEVICE");
            details.add("Missing device fingerprint");
        } else {
            // simple heuristic: if no prior attempts recorded, treat as new
            long priorAttempts = loginAttemptRepository.countByUserIdAndSuccessIsFalseAndCreatedAtAfter(userId, Instant.EPOCH);
            if (priorAttempts == 0) {
                risk += 30;
                rules.add("NEW_DEVICE");
                details.add("Device not seen before for this user");
            }
        }

        if (country == null || country.isBlank()) {
            risk += 5;
            rules.add("NO_COUNTRY");
            details.add("No country provided");
        }

        LoginRiskDecision decision;
        if (risk >= 100) {
            decision = LoginRiskDecision.BLOCK;
        } else if (risk >= 50) {
            decision = LoginRiskDecision.RESTRICT;
        } else {
            decision = LoginRiskDecision.ALLOW;
        }

        String explanation = String.format("Risk %d, rules: %s, details: %s", risk, String.join(", ", rules), String.join("; ", details));
        String rulesJoined = String.join(", ", rules);

        LoginAttempt attempt = new LoginAttempt(
            userId,
            merchantId,
            decision != LoginRiskDecision.BLOCK,
            risk,
            decision.name(),
            rulesJoined,
            explanation,
            deviceFingerprint,
            ip,
            country,
            userAgent,
            tenantId,
            traceId
        );
        loginAttemptRepository.save(attempt);

        return new Result(risk, decision, rulesJoined, explanation);
    }

    public record Result(int riskScore, LoginRiskDecision decision, String triggeredRules, String explanation) {}
}
