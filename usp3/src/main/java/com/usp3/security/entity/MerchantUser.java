package com.usp3.security.entity;

import java.time.Instant;

import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "merchant_users")
public class MerchantUser extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role; // ADMIN, DEV, ANALYST

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private String passwordHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "merchant_id")
    private com.usp3.platform.Merchant merchant;

    private Instant lastLoginAt;
    private String lastLoginIp;
    private String lastLoginCountry;

    protected MerchantUser() {}

    public MerchantUser(String email, String role, String passwordHash, com.usp3.platform.Merchant merchant, String tenantId, String traceId) {
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
        this.merchant = merchant;
        this.tenantId = tenantId;
        // Ensure traceId never violates NOT NULL constraint.
        this.traceId = (traceId == null || traceId.isBlank()) ? java.util.UUID.randomUUID().toString() : traceId;
    }

    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public String getPasswordHash() { return passwordHash; }
    public com.usp3.platform.Merchant getMerchant() { return merchant; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public String getLastLoginIp() { return lastLoginIp; }
    public String getLastLoginCountry() { return lastLoginCountry; }

    public void setActive(boolean active) { this.active = active; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setLastLoginIp(String lastLoginIp) { this.lastLoginIp = lastLoginIp; }
    public void setLastLoginCountry(String lastLoginCountry) { this.lastLoginCountry = lastLoginCountry; }

    public void setRole(String role) { this.role = role; }
}
