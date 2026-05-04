package com.usp3.security.dto;

import java.time.Instant;

public class InvitationResponse {
    private final String token;
    private final String email;
    private final String role;
    private final Instant expiresAt;

    public InvitationResponse(String token, String email, String role, Instant expiresAt) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Instant getExpiresAt() { return expiresAt; }
}
