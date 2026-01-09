package com.usp3.payment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.payment.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}

