package com.usp3.security.entity;

import java.util.UUID;

import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table; // Import UUID

@Entity
@Table(name = "api_keys")
public class ApiKey extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private UUID merchantId;

    // Non-secret display fingerprint (e.g., sk_sandbox_xxxx)
    @Column(nullable = false, length = 64)
    private String displayValue;

    protected ApiKey() {}

    public ApiKey(String keyHash, UUID merchantId, String displayValue) {
        this.keyHash = keyHash;
        this.merchantId = merchantId;
        this.displayValue = displayValue;
        this.active = true;
    }

    public String getKeyHash() { return keyHash; }
    public boolean isActive() { return active; }
    public UUID getMerchantId() { return merchantId; }
    public String getDisplayValue() { return displayValue; }
    public void deactivate() { this.active = false; }
}