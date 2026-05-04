package com.usp3.security.entity;

import com.usp3.platform.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "login_attempts")
public class LoginAttempt extends BaseEntity {

    @Column(nullable = false)
    private java.util.UUID userId;

    @Column(nullable = false)
    private java.util.UUID merchantId;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private int riskScore;

    @Column(nullable = false)
    private String decision; // ALLOW / RESTRICT / BLOCK

    @Column(nullable = false)
    private String triggeredRules;

    @Column(nullable = false)
    private String explanation;

    @Column
    private String deviceFingerprint;

    @Column
    private String ipAddress;

    @Column
    private String country;

    @Column
    private String userAgent;

    protected LoginAttempt() {}

    public LoginAttempt(java.util.UUID userId,
                        java.util.UUID merchantId,
                        boolean success,
                        int riskScore,
                        String decision,
                        String triggeredRules,
                        String explanation,
                        String deviceFingerprint,
                        String ipAddress,
                        String country,
                        String userAgent,
                        String tenantId,
                        String traceId) {
        this.userId = userId;
        this.merchantId = merchantId;
        this.success = success;
        this.riskScore = riskScore;
        this.decision = decision;
        this.triggeredRules = triggeredRules;
        this.explanation = explanation;
        this.deviceFingerprint = deviceFingerprint;
        this.ipAddress = ipAddress;
        this.country = country;
        this.userAgent = userAgent;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public java.util.UUID getUserId() { return userId; }
    public java.util.UUID getMerchantId() { return merchantId; }
    public boolean isSuccess() { return success; }
    public int getRiskScore() { return riskScore; }
    public String getDecision() { return decision; }
    public String getTriggeredRules() { return triggeredRules; }
    public String getExplanation() { return explanation; }
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public String getIpAddress() { return ipAddress; }
    public String getCountry() { return country; }
    public String getUserAgent() { return userAgent; }
}
