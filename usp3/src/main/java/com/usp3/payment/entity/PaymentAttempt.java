package com.usp3.payment.entity;

import com.usp3.payment.entity.enums.AttemptStatus;
import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status;

    @Column(nullable = false)
    private String fraudDecision; // ALLOW / REVIEW / BLOCK

    @Column(nullable = false)
    private String bankDecision; // APPROVED / DECLINED / TIMEOUT

    protected PaymentAttempt() {}

    public PaymentAttempt(Transaction transaction,
                          int attemptNumber,
                          String tenantId,
                          String traceId) {
        this.transaction = transaction;
        this.attemptNumber = attemptNumber;
        this.status = AttemptStatus.INITIATED;
        this.fraudDecision = "PENDING";
        this.bankDecision = "PENDING";
        this.tenantId = tenantId;
        this.traceId = traceId;
    }
}
