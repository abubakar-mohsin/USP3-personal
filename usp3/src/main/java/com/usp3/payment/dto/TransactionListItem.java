package com.usp3.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListItem {
    private String reference;
    private String status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;
    private boolean flaggedForReview;

    // Fraud and policy insights for dashboard tables
    private Integer fraudScore;
    private String fraudDecision;
    private String fraudConfidence;
    private String fraudExplanation;
    private String triggeredRules;
    private String policyDecision;
    private String policyReason;
    private String bankDecision;

    // Context for deep-dive modal
    private String deviceFingerprint;
    private String country;
    private String ipAddress;
    private String binCountry;
    private String ipCountry;

    private String customerEmail;
    private String customerLast4;
    private String cardBrand;
    private String merchantLabel;
}
