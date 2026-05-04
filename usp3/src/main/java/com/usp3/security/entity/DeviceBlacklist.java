package com.usp3.security.entity;

import java.util.UUID;

import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "device_blacklist", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"merchant_id", "device_fingerprint"})
})
public class DeviceBlacklist extends BaseEntity {

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "device_fingerprint", nullable = false)
    private String deviceFingerprint;

    protected DeviceBlacklist() {}

    public DeviceBlacklist(UUID merchantId, String deviceFingerprint, String tenantId, String traceId) {
        this.merchantId = merchantId;
        this.deviceFingerprint = deviceFingerprint;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public UUID getMerchantId() { return merchantId; }
    public String getDeviceFingerprint() { return deviceFingerprint; }
}
