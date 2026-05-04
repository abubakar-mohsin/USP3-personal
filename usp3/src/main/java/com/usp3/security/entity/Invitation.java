package com.usp3.security.entity;

import com.usp3.platform.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitations")
public class Invitation extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant acceptedAt;

    @Column
    private Instant revokedAt;

    @Column(nullable = false)
    private UUID invitedBy;

    protected Invitation() {}

    public Invitation(String email,
                      String role,
                      UUID merchantId,
                      String token,
                      Instant expiresAt,
                      UUID invitedBy,
                      String tenantId,
                      String traceId) {
        this.email = email;
        this.role = role;
        this.merchantId = merchantId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.invitedBy = invitedBy;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public String getEmail() { return email; }
    public String getRole() { return role; }
    public UUID getMerchantId() { return merchantId; }
    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public UUID getInvitedBy() { return invitedBy; }

    public void accept() { this.acceptedAt = Instant.now(); }
    public void revoke() { this.revokedAt = Instant.now(); }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
