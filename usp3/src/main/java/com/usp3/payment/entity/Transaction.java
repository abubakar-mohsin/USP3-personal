package com.usp3.payment.entity;

import java.util.UUID;

import com.usp3.payment.entity.enums.TransactionStatus;
import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String reference; // public-facing ID

    @Column(nullable = false)
    private Long amount; // in smallest unit (cents)

    @Column(nullable = false)
    private String currency; // ISO (USD, PKR, etc.)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private UUID merchantId; // logical reference

    @Column(nullable = false)
    private int attemptCount = 0;

    protected Transaction() {}

    public Transaction(String reference, Long amount, String currency, 
        UUID merchantId, String tenantId, String traceId) {
            this.reference = reference;
            this.amount = amount;
            this.currency = currency;
            this.merchantId = merchantId; 
            this.status = TransactionStatus.CREATED; 
            this.tenantId = tenantId;
            this.traceId = traceId;
        }

    public void incrementAttempt() {
        this.attemptCount++;
    }

    public String getReference() {
        return reference;
    }
}

