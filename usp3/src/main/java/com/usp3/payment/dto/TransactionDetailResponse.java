package com.usp3.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STEP 11: Transaction Detail Response
 * 
 * Returns complete transaction information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResponse {
    
    private String transactionId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Fraud information
    private Integer fraudScore;
    private String fraudDecision;
    private String fraudConfidence;
    private String fraudExplanation;
    private String triggeredRules;
    
    // Policy information
    private String policyDecision;
    private String policyReason;
    
    // Bank information
    private String bankDecision;

    // Case flag
    private boolean flaggedForReview;
}
