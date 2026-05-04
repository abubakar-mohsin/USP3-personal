package com.usp3.policy.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.usp3.policy.model.PolicyDecisionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "policy_results")
public class PolicyResult {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyDecisionType decision;

    @Column(nullable = false)
    private String policyName;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PolicyResult() {}

    public PolicyResult(
            UUID transactionId,
            PolicyDecisionType decision,
            String policyName,
            String reason
    ) {
        this.transactionId = transactionId;
        this.decision = decision;
        this.policyName = policyName;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public PolicyDecisionType getDecision() {
        return decision;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
