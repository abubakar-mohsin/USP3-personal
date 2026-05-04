package com.usp3.payment.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.usp3.payment.entity.PaymentAttempt;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {
    
    // Change the parameter from LocalDateTime to Instant
    long countByTransaction_MerchantIdAndCreatedAtAfter(UUID merchantId, Instant createdAt);
    
    @Query("SELECT COUNT(DISTINCT pa.transaction.id) FROM PaymentAttempt pa WHERE pa.transaction.merchantId = :merchantId AND pa.deviceFingerprint = :deviceFingerprint AND pa.deviceFingerprint IS NOT NULL")
    long countByTransaction_MerchantIdAndDeviceFingerprint(@Param("merchantId") UUID merchantId, @Param("deviceFingerprint") String deviceFingerprint);

    // Recent bank declines for repeated failure rule
    long countByTransaction_MerchantIdAndBankDecisionAndCreatedAtAfter(UUID merchantId, String bankDecision, Instant createdAt);

    // Recent fraud blocks for repeated failure rule
    long countByTransaction_MerchantIdAndFraudDecisionAndCreatedAtAfter(UUID merchantId, String fraudDecision, Instant createdAt);

    // Latest attempt for country/device comparison
    PaymentAttempt findTop1ByTransaction_MerchantIdOrderByCreatedAtDesc(UUID merchantId);

    // Latest attempt by transaction id
    PaymentAttempt findTop1ByTransaction_IdOrderByCreatedAtDesc(UUID transactionId);
}

