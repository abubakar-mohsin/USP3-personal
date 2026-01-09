package com.usp3.security;

import com.usp3.platform.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "merchant_users")
public class MerchantUser extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role; // ADMIN, DEV, ANALYST

    @Column(nullable = false)
    private boolean active = true;

    protected MerchantUser() {}

    public MerchantUser(String email, String role, String tenantId, String traceId) {
        this.email = email;
        this.role = role;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}
