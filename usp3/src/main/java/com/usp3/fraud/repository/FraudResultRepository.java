package com.usp3.fraud.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.fraud.FraudResult;

public interface FraudResultRepository extends JpaRepository<FraudResult, UUID> {
    
    FraudResult findByTransactionId(String transactionId);
}
