package com.usp3.security.entity;

import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_sessions")
public class UserSession extends BaseEntity {

    @Column(nullable = false)
    private java.util.UUID userId;

    @Column(nullable = false)
    private java.util.UUID merchantId;

    @Column(nullable = false)
    private String refreshTokenHash;

    @Column
    private String userAgent;

    @Column
    private String deviceFingerprint;

    @Column
    private String ipAddress;

    @Column
    private String country;

    @Column(nullable = false)
    private boolean restricted = false;

    @Column
    private java.time.Instant revokedAt;

    @Column
    private java.time.Instant lastSeenAt;

    protected UserSession() {}

    public UserSession(java.util.UUID userId, java.util.UUID merchantId, String refreshTokenHash, String userAgent,
                       String deviceFingerprint, String ipAddress, String country, boolean restricted,
                       String tenantId, String traceId) {
        this.userId = userId;
        this.merchantId = merchantId;
        this.refreshTokenHash = refreshTokenHash;
        this.userAgent = userAgent;
        this.deviceFingerprint = deviceFingerprint;
        this.ipAddress = ipAddress;
        this.country = country;
        this.restricted = restricted;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.lastSeenAt = java.time.Instant.now();
    }

    public java.util.UUID getUserId() { return userId; }
    public java.util.UUID getMerchantId() { return merchantId; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public String getUserAgent() { return userAgent; }
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public String getIpAddress() { return ipAddress; }
    public String getCountry() { return country; }
    public boolean isRestricted() { return restricted; }
    public java.time.Instant getRevokedAt() { return revokedAt; }
    public java.time.Instant getLastSeenAt() { return lastSeenAt; }

    public void revoke() { this.revokedAt = java.time.Instant.now(); }
    public void touch() { this.lastSeenAt = java.time.Instant.now(); }
    public void setRestricted(boolean restricted) { this.restricted = restricted; }
}
