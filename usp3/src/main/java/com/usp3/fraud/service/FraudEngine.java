package com.usp3.fraud.service;

import java.time.Instant; 
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.usp3.fraud.ConfidenceLevel;
import com.usp3.fraud.FraudDecision;
import com.usp3.fraud.FraudResult;
import com.usp3.payment.entity.PaymentAttempt;
import com.usp3.payment.entity.Transaction;
import com.usp3.payment.repository.PaymentAttemptRepository;
import com.usp3.payment.repository.TransactionRepository;
import com.usp3.security.repository.DeviceBlacklistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FraudEngine {

    private final PaymentAttemptRepository attemptRepository;
    private final TransactionRepository transactionRepository;
    private final DeviceBlacklistRepository deviceBlacklistRepository;

    private static final long HIGH_AMOUNT_THRESHOLD_CENTS = 5_000_00L; // $5k and above triggers
    private static final long HARD_BLOCK_AMOUNT_CENTS = 10_000_00L; // $10k and above is hard-blocked
    private static final int VELOCITY_WINDOW_MINUTES = 5;
    private static final int VELOCITY_THRESHOLD_ATTEMPTS = 3;
    private static final int VELOCITY_HARD_CAP_ATTEMPTS = 5; // heavy penalty when hit

    private static final int FAILURE_WINDOW_MINUTES = 10;
    private static final int FAILURE_THRESHOLD = 3;

    private static final double AMOUNT_DEVIATION_MEDIUM = 2.0; // 2x recent avg
    private static final double AMOUNT_DEVIATION_HIGH = 3.0;   // 3x recent avg

    public FraudResult evaluate(Transaction transaction, PaymentAttempt currentAttempt, 
                                String deviceFingerprint, String binCountry, String ipAddress) {
        
        int riskScore = 0;
        List<String> triggeredRules = new ArrayList<>();
        List<String> ruleExplanations = new ArrayList<>();

        if (deviceFingerprint != null && !deviceFingerprint.isEmpty()
            && deviceBlacklistRepository.existsByMerchantIdAndDeviceFingerprint(transaction.getMerchantId(), deviceFingerprint)) {
            riskScore = 100;
            triggeredRules.add("BLACKLISTED_DEVICE");
            ruleExplanations.add("Device fingerprint is blacklisted for this merchant");
            FraudResult result = new FraudResult(
                transaction.getReference(),
                riskScore,
                FraudDecision.REJECTED,
                ConfidenceLevel.HIGH,
                String.join(", ", triggeredRules),
                "STANDARD_FRAUD_POLICY",
                buildExplanation(riskScore, triggeredRules, ruleExplanations),
                transaction.getTenantId(),
                transaction.getTraceId()
            );
            return result;
        }

        // --- RULE 0: HARD AMOUNT BLOCK ---
        if (transaction.getAmount() >= HARD_BLOCK_AMOUNT_CENTS) {
            riskScore = 100;
            triggeredRules.add("HIGH_AMOUNT_HARD_BLOCK");
            ruleExplanations.add(String.format(
                "Transaction amount (%s) exceeds hard block threshold (%s)",
                formatAmount(transaction.getAmount()),
                formatAmount(HARD_BLOCK_AMOUNT_CENTS)
            ));
            return new FraudResult(
                transaction.getReference(),
                riskScore,
                FraudDecision.REJECTED,
                ConfidenceLevel.HIGH,
                String.join(", ", triggeredRules),
                "STANDARD_FRAUD_POLICY",
                buildExplanation(riskScore, triggeredRules, ruleExplanations),
                transaction.getTenantId(),
                transaction.getTraceId()
            );
        }

        // --- RULE 1: HIGH AMOUNT RULE ---
        if (transaction.getAmount() >= HIGH_AMOUNT_THRESHOLD_CENTS) {
            riskScore += 50;
            triggeredRules.add("HIGH_AMOUNT");
            ruleExplanations.add(String.format(
                "Transaction amount (%s) meets/exceeds high-risk threshold (%s)", 
                formatAmount(transaction.getAmount()), 
                formatAmount(HIGH_AMOUNT_THRESHOLD_CENTS)
            ));
        }

        // --- RULE 2: VELOCITY RULE ---
        Instant fiveMinutesAgo = Instant.now().minus(VELOCITY_WINDOW_MINUTES, ChronoUnit.MINUTES);
        long recentAttempts = attemptRepository.countByTransaction_MerchantIdAndCreatedAtAfter(
            transaction.getMerchantId(), fiveMinutesAgo);
        if (recentAttempts >= VELOCITY_HARD_CAP_ATTEMPTS) {
            riskScore += 70;
            triggeredRules.add("VELOCITY_EXTREME");
            ruleExplanations.add(String.format(
                "Detected %d payment attempts in the last %d minutes (hard cap: %d)", 
                recentAttempts, VELOCITY_WINDOW_MINUTES, VELOCITY_HARD_CAP_ATTEMPTS
            ));
        } else if (recentAttempts >= VELOCITY_THRESHOLD_ATTEMPTS) {
            riskScore += 45;
            triggeredRules.add("VELOCITY");
            ruleExplanations.add(String.format(
                "Detected %d payment attempts in the last %d minutes (threshold: %d)", 
                recentAttempts, VELOCITY_WINDOW_MINUTES, VELOCITY_THRESHOLD_ATTEMPTS
            ));
        }

        boolean trustedDevice = false;

        // --- RULE 3: DEVICE CHANGE RULE ---
        if (deviceFingerprint != null && !deviceFingerprint.isEmpty()) {
            long deviceUsageCount = attemptRepository.countByTransaction_MerchantIdAndDeviceFingerprint(
                transaction.getMerchantId(),
                deviceFingerprint
            );

            // Current attempt is already saved, so first-time devices show as count=1
            if (deviceUsageCount <= 1) {
                riskScore += 20;
                triggeredRules.add("NEW_DEVICE");
                ruleExplanations.add("First payment attempt from this device fingerprint");
            } else if (deviceUsageCount >= 3) {
                trustedDevice = true;
                riskScore -= 20;
                triggeredRules.add("TRUSTED_DEVICE");
                ruleExplanations.add("Device has multiple successful attempts and is considered trusted");
            }
        }

        // --- RULE 4: COUNTRY MISMATCH RULE ---
        PaymentAttempt lastAttempt = attemptRepository.findTop1ByTransaction_MerchantIdOrderByCreatedAtDesc(
            transaction.getMerchantId());
        PaymentAttempt priorAttempt = (lastAttempt != null && !lastAttempt.getId().equals(currentAttempt.getId()))
            ? lastAttempt : null;

        if (binCountry != null && !binCountry.isBlank() && priorAttempt != null && priorAttempt.getCountry() != null
                && !binCountry.equalsIgnoreCase(priorAttempt.getCountry())) {
            riskScore += 15;
            triggeredRules.add("COUNTRY_MISMATCH");
            ruleExplanations.add(String.format(
                "Country mismatch: current %s vs last %s",
                binCountry,
                priorAttempt.getCountry()
            ));
        }

        // --- RULE 4B: GEO-IP & BIN MISMATCH ---
        String ipCountry = currentAttempt.getIpCountry();
        if (binCountry != null && ipCountry != null && !binCountry.isBlank() && !ipCountry.isBlank()
                && !binCountry.equalsIgnoreCase(ipCountry)) {
            int geoPoints = 40;
            if (trustedDevice) {
                geoPoints = 15; // soften when device is trusted
            }
            riskScore += geoPoints;
            triggeredRules.add("GEO_BIN_MISMATCH");
            ruleExplanations.add(String.format(
                "BIN country %s differs from IP country %s (risk +%d)",
                binCountry,
                ipCountry,
                geoPoints
            ));
        }

        // --- RULE 5: AMOUNT DEVIATION RULE ---
        List<Transaction> recentTransactions = transactionRepository
            .findTop10ByMerchantIdOrderByCreatedAtDesc(transaction.getMerchantId());
        if (!recentTransactions.isEmpty()) {
            double avg = recentTransactions.stream()
                .mapToLong(Transaction::getAmount)
                .average()
                .orElse(0);
            if (avg > 0) {
                double ratio = transaction.getAmount() / avg;
                if (ratio >= AMOUNT_DEVIATION_HIGH) {
                    riskScore += 30;
                    triggeredRules.add("AMOUNT_DEVIATION_HIGH");
                    ruleExplanations.add(String.format(
                        "Amount is %.1fx recent average (avg %s)", ratio, formatAmount((long) avg)
                    ));
                } else if (ratio >= AMOUNT_DEVIATION_MEDIUM) {
                    riskScore += 20;
                    triggeredRules.add("AMOUNT_DEVIATION_MEDIUM");
                    ruleExplanations.add(String.format(
                        "Amount is %.1fx recent average (avg %s)", ratio, formatAmount((long) avg)
                    ));
                }
            }
        }

        // --- RULE 6: REPEATED FAILURES RULE ---
        Instant tenMinutesAgo = Instant.now().minus(FAILURE_WINDOW_MINUTES, ChronoUnit.MINUTES);
        long recentDeclines = attemptRepository.countByTransaction_MerchantIdAndBankDecisionAndCreatedAtAfter(
            transaction.getMerchantId(), "DECLINED", tenMinutesAgo);
        long recentBlocks = attemptRepository.countByTransaction_MerchantIdAndFraudDecisionAndCreatedAtAfter(
            transaction.getMerchantId(), "BLOCK", tenMinutesAgo);
        long recentFailures = recentDeclines + recentBlocks;
        if (recentFailures >= FAILURE_THRESHOLD) {
            riskScore += 25;
            triggeredRules.add("REPEATED_FAILURES");
            ruleExplanations.add(String.format(
                "%d recent failures (declines/blocks) in last %d minutes", recentFailures, FAILURE_WINDOW_MINUTES
            ));
        }

        // --- RULE 7: IP CHANGE ANOMALY ---
        if (ipAddress != null && !ipAddress.isBlank() && priorAttempt != null && priorAttempt.getIpAddress() != null
                && !ipAddress.equals(priorAttempt.getIpAddress())) {
            riskScore += 10;
            triggeredRules.add("IP_CHANGE");
            ruleExplanations.add(String.format(
                "Client IP changed from %s to %s",
                priorAttempt.getIpAddress(), ipAddress
            ));
        }

        if (riskScore < 0) {
            riskScore = 0;
        }

        FraudDecision decision;
        ConfidenceLevel confidence;
        
        if (riskScore >= 70) {
            decision = FraudDecision.REJECTED;
            confidence = ConfidenceLevel.HIGH;
        } else if (riskScore >= 40) {
            decision = FraudDecision.REVIEW;
            confidence = ConfidenceLevel.MEDIUM;
        } else {
            decision = FraudDecision.APPROVED;
            confidence = riskScore > 0 ? ConfidenceLevel.MEDIUM : ConfidenceLevel.HIGH;
        }

        // Boost confidence when multiple independent rules fire
        if (triggeredRules.size() >= 3 && confidence == ConfidenceLevel.MEDIUM) {
            confidence = ConfidenceLevel.HIGH;
        }

        String explanation = buildExplanation(riskScore, triggeredRules, ruleExplanations);
        String triggeredRulesJson = String.join(", ", triggeredRules);

        return new FraudResult(
            transaction.getReference(),
            riskScore,
            decision,
            confidence,
            triggeredRulesJson,
            "STANDARD_FRAUD_POLICY",
            explanation,
            transaction.getTenantId(),
            transaction.getTraceId()
        );
    }

    private String buildExplanation(int score, List<String> rules, List<String> explanations) {
        if (rules.isEmpty()) {
            return String.format("No fraud rules triggered. Risk score: %d", score);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Risk score: %d. ", score));
        sb.append("Triggered rules: ").append(String.join(", ", rules)).append(". ");
        sb.append("Details: ").append(String.join("; ", explanations));
        return sb.toString();
    }

    private String formatAmount(Long cents) {
        return String.format("$%.2f", cents / 100.0);
    }
}