package com.usp3.payment.dto;

import java.math.BigDecimal;

public class PaymentResponse {

    private String transactionId;
    private String status; // APPROVED, DECLINED, REVIEW

    private BigDecimal amount;
    private String currency;

    // explainability (THIS IS USP³’s DIFFERENTIATOR)
    private int fraudScore;
    private String fraudDecision;
    private String explanation;

    // traceability
    private String traceId;

    // getters & setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getFraudScore() { return fraudScore; }
    public void setFraudScore(int fraudScore) { this.fraudScore = fraudScore; }

    public String getFraudDecision() { return fraudDecision; }
    public void setFraudDecision(String fraudDecision) {
        this.fraudDecision = fraudDecision;
    }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}

