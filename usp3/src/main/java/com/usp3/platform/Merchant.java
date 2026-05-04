package com.usp3.platform;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "merchants")
public class Merchant extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String merchantCode;

    @Column(nullable = false)
    private boolean active = true;

    protected Merchant() {}

    public Merchant(String name, String merchantCode, String tenantId, String traceId) {
        this.name = name;
        this.merchantCode = merchantCode;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public String getName() { return name; }
    public String getMerchantCode() { return merchantCode; }
    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
