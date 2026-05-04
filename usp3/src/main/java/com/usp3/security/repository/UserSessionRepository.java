package com.usp3.security.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.security.entity.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findByRefreshTokenHashAndRevokedAtIsNull(String refreshTokenHash);
    List<UserSession> findByUserIdAndRevokedAtIsNull(UUID userId);
    long countByUserIdAndCreatedAtAfterAndRevokedAtIsNull(UUID userId, Instant createdAfter);

    List<UserSession> findByMerchantIdAndRevokedAtIsNull(UUID merchantId);

    List<UserSession> findByDeviceFingerprintAndMerchantIdAndRevokedAtIsNull(String deviceFingerprint, UUID merchantId);
}
