package com.usp3.security.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.security.entity.LoginAttempt;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
    long countByUserIdAndSuccessIsFalseAndCreatedAtAfter(UUID userId, Instant createdAfter);
    List<LoginAttempt> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<LoginAttempt> findByMerchantId(UUID merchantId, Pageable pageable);

    List<LoginAttempt> findTop50ByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    LoginAttempt findTop1ByDeviceFingerprintAndMerchantIdOrderByCreatedAtDesc(String deviceFingerprint, UUID merchantId);
}
