package com.usp3.payment.dto;

import com.usp3.payment.model.ReviewDecision;

public class ReviewDecisionRequest {
    private ReviewDecision decision;
    private String reason;
    private String reviewer;
    private String traceId;

    public ReviewDecision getDecision() { return decision; }
    public void setDecision(ReviewDecision decision) { this.decision = decision; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
