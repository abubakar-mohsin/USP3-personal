package com.usp3.security.entity;

import com.usp3.platform.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID; // Import UUID

@Entity
@Table(name = "api_keys")
public class ApiKey extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false)
    private boolean active = true;

    // FIX: Rename from tenantId to merchantId to match reviewer feedback
    @Column(nullable = false)
    private UUID merchantId; 

    protected ApiKey() {}

    public ApiKey(String keyHash, UUID merchantId) {
        this.keyHash = keyHash;
        this.merchantId = merchantId;
        this.active = true;
    }

    // Getters
    public String getKeyHash() { return keyHash; }
    public boolean isActive() { return active; }
    
    // This is the method your Service calls!
    public UUID getMerchantId() { return merchantId; }
}