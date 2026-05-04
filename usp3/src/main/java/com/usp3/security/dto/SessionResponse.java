package com.usp3.security.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SessionResponse {
    UUID id;
    UUID userId;
    String userEmail;
    String userRole;
    String deviceFingerprint;
    String userAgent;
    String ipAddress;
    String country;
    boolean restricted;
    Instant lastSeenAt;
}