package com.usp3.policy.service;

import org.springframework.stereotype.Component;

import com.usp3.fraud.FraudResult;
import com.usp3.payment.entity.Transaction;
import com.usp3.policy.model.PolicyDecision;
import com.usp3.policy.model.PolicyDecisionType;

@Component
public class PolicyEngine {

    private static final int BLOCK_THRESHOLD = 70;
    private static final int REVIEW_THRESHOLD = 40;

    public PolicyDecision evaluate(FraudResult fraudResult, Transaction transaction) {

        int score = fraudResult.getFraudScore();

        if (score > BLOCK_THRESHOLD) {
            return new PolicyDecision(
                    PolicyDecisionType.BLOCK,
                    "STANDARD_RISK_POLICY",
                    "High fraud risk detected"
            );
        }

        if (score >= REVIEW_THRESHOLD) {
            return new PolicyDecision(
                    PolicyDecisionType.REVIEW,
                    "STANDARD_RISK_POLICY",
                    "Medium fraud risk — manual review required"
            );
        }

        return new PolicyDecision(
                PolicyDecisionType.ALLOW,
                "STANDARD_RISK_POLICY",
                "Low fraud risk"
        );
    }
}

