package com.usp3.policy.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.policy.entity.PolicyResult;

public interface PolicyResultRepository extends JpaRepository<PolicyResult, UUID> {
    
    List<PolicyResult> findByTransactionIdOrderByCreatedAtDesc(UUID transactionId);
    
    PolicyResult findFirstByTransactionIdOrderByCreatedAtDesc(UUID transactionId);
}
