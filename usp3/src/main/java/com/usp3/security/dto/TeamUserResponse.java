package com.usp3.security.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamUserResponse {
    UUID id;
    String email;
    String role;
    boolean active;
    Instant lastLoginAt;
}
