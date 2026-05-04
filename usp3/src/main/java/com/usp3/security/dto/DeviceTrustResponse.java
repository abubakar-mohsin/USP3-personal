package com.usp3.security.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeviceTrustResponse {
    String deviceFingerprint;
    String userAgent;
    String ipAddress;
    String country;
    boolean restricted;
    Instant lastSeenAt;
    Integer riskScore;
    String decision;
    java.util.UUID userId;
    String userEmail;
    String userRole;
}
