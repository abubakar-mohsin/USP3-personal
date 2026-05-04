package com.usp3.fraud;

import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "fraud_results")
public class FraudResult extends BaseEntity {

    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private int riskScore; // 0 - 100

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudDecision decision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfidenceLevel confidenceLevel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String triggeredRules; // JSON / readable text

    @Column(nullable = false)
    private String policyApplied;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String explanation;

    protected FraudResult() {}

    public FraudResult(
            String transactionId,
            int riskScore,
            FraudDecision decision,
            ConfidenceLevel confidenceLevel,
            String triggeredRules,
            String policyApplied,
            String explanation,
            String tenantId,
            String traceId
    ) {
        this.transactionId = transactionId;
        this.riskScore = riskScore;
        this.decision = decision;
        this.confidenceLevel = confidenceLevel;
        this.triggeredRules = triggeredRules;
        this.policyApplied = policyApplied;
        this.explanation = explanation;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public String getTransactionId() { return transactionId; }
    public int getRiskScore() { return riskScore; }
    public int getFraudScore() { return riskScore; } // Alias for compatibility
    public FraudDecision getDecision() { return decision; }
    public ConfidenceLevel getConfidenceLevel() { return confidenceLevel; }
    public String getTriggeredRules() { return triggeredRules; }
    public String getPolicyApplied() { return policyApplied; }
    public String getExplanation() { return explanation; }
}
  