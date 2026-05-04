package com.usp3.security.entity;

import java.time.Instant;

import com.usp3.platform.BaseEntity;
import com.usp3.security.entity.enums.DeviceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "devices")
public class Device extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String deviceFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    private String os;
    private String browser;
    private String country;

    @Column(nullable = false)
    private boolean trusted = false;

    @Column(nullable = false)
    private int riskScore; // 0 - 100

    @Column(nullable = false, updatable = false)
    private Instant firstSeenAt;

    @Column(nullable = false)
    private Instant lastSeenAt;

    protected Device() {}

    public Device(
            String deviceFingerprint,
            DeviceType deviceType,
            String os,
            String browser,
            String country,
            int riskScore,
            String tenantId,
            String traceId
    ) {
        this.deviceFingerprint = deviceFingerprint;
        this.deviceType = deviceType;
        this.os = os;
        this.browser = browser;
        this.country = country;
        this.riskScore = riskScore;
        this.firstSeenAt = Instant.now();
        this.lastSeenAt = Instant.now();
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public String getDeviceFingerprint() { return deviceFingerprint; }
    public DeviceType getDeviceType() { return deviceType; }
    public String getOs() { return os; }
    public String getBrowser() { return browser; }
    public String getCountry() { return country; }
    public boolean isTrusted() { return trusted; }
    public int getRiskScore() { return riskScore; }
    public Instant getFirstSeenAt() { return firstSeenAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
}
