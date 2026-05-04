package com.usp3.policy.model;

import java.time.LocalDateTime;

public class PolicyDecision {

    private PolicyDecisionType decision;
    private String policyName;
    private String reason;
    private LocalDateTime evaluatedAt;

    public PolicyDecision(
            PolicyDecisionType decision,
            String policyName,
            String reason
    ) {
        this.decision = decision;
        this.policyName = policyName;
        this.reason = reason;
        this.evaluatedAt = LocalDateTime.now();
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

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }
}
