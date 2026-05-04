package com.usp3.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.payment.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Optional<Transaction> findByReference(String reference);

    // For fraud amount deviation analysis
    java.util.List<Transaction> findTop10ByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    Page<Transaction> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<Transaction> findByMerchantIdAndStatus(UUID merchantId, com.usp3.payment.entity.enums.TransactionStatus status, Pageable pageable);

    Page<Transaction> findByMerchantIdAndReferenceContainingIgnoreCase(UUID merchantId, String reference, Pageable pageable);

    Page<Transaction> findByMerchantIdAndStatusAndReferenceContainingIgnoreCase(UUID merchantId, com.usp3.payment.entity.enums.TransactionStatus status, String reference, Pageable pageable);
}

