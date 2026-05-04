package com.usp3.payment.dto;

import java.math.BigDecimal;

public class PaymentResponse {

    private String transactionId = "";
    private String status = "PROCESSING"; // default
    private BigDecimal amount = BigDecimal.ZERO;
    private String currency = "USD";

    // explainability
    private int fraudScore = 0;
    private String fraudDecision = "";
    private String fraudConfidence = "";
    private String triggeredRules = "";
    private String explanation = "";

    // policy and bank
    private String policyDecision = "";
    private String policyReason = "";
    private String bankDecision = "";

    // case management
    private boolean flaggedForReview = false;

    // traceability
    private String traceId = "";

    // getters & setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId != null ? transactionId : "";
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status != null ? status : "PROCESSING"; 
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount != null ? amount : BigDecimal.ZERO; 
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { 
        this.currency = currency != null ? currency : "USD"; 
    }

    public int getFraudScore() { return fraudScore; }
    public void setFraudScore(int fraudScore) { this.fraudScore = fraudScore; }

    public String getFraudDecision() { return fraudDecision; }
    public void setFraudDecision(String fraudDecision) {
        this.fraudDecision = fraudDecision != null ? fraudDecision : "";
    }

    public String getFraudConfidence() { return fraudConfidence; }
    public void setFraudConfidence(String fraudConfidence) {
        this.fraudConfidence = fraudConfidence != null ? fraudConfidence : "";
    }

    public String getTriggeredRules() { return triggeredRules; }
    public void setTriggeredRules(String triggeredRules) {
        this.triggeredRules = triggeredRules != null ? triggeredRules : "";
    }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) {
        this.explanation = explanation != null ? explanation : "";
    }

    public String getPolicyDecision() { return policyDecision; }
    public void setPolicyDecision(String policyDecision) {
        this.policyDecision = policyDecision != null ? policyDecision : "";
    }

    public String getPolicyReason() { return policyReason; }
    public void setPolicyReason(String policyReason) {
        this.policyReason = policyReason != null ? policyReason : "";
    }

    public String getBankDecision() { return bankDecision; }
    public void setBankDecision(String bankDecision) {
        this.bankDecision = bankDecision != null ? bankDecision : "";
    }

    public boolean isFlaggedForReview() { return flaggedForReview; }
    public void setFlaggedForReview(boolean flaggedForReview) {
        this.flaggedForReview = flaggedForReview;
    }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { 
        this.traceId = traceId != null ? traceId : ""; 
    }
}
