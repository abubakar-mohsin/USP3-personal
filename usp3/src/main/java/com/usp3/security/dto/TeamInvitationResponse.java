package com.usp3.security.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamInvitationResponse {
    UUID id;
    String email;
    String role;
    Instant expiresAt;
    Instant acceptedAt;
    Instant revokedAt;
    String status;
}
