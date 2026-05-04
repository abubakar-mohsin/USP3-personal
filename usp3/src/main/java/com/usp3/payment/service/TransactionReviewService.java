package com.usp3.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usp3.payment.dto.ReviewDecisionRequest;
import com.usp3.payment.entity.PaymentAttempt;
import com.usp3.payment.entity.Transaction;
import com.usp3.payment.entity.enums.AttemptStatus;
import com.usp3.payment.entity.enums.TransactionStatus;
import com.usp3.payment.repository.PaymentAttemptRepository;
import com.usp3.payment.repository.TransactionRepository;
import com.usp3.platform.event.PaymentCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionReviewService {

    private final TransactionRepository transactionRepository;
    private final PaymentAttemptRepository attemptRepository;

    @Transactional
    public Transaction resolve(String transactionReference, ReviewDecisionRequest request) {
        Transaction txn = transactionRepository.findByReference(transactionReference)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionReference));

        if (txn.getStatus() != TransactionStatus.HELD_FOR_REVIEW) {
            throw new IllegalStateException("Transaction is not awaiting review");
        }

        PaymentAttempt latestAttempt = attemptRepository.findTop1ByTransaction_IdOrderByCreatedAtDesc(txn.getId());

        switch (request.getDecision()) {
            case APPROVE -> applyApprove(txn, latestAttempt, request);
            case DECLINE -> applyDecline(txn, latestAttempt, request);
            case BLOCK -> applyBlock(txn, latestAttempt, request);
            case FALSE_POSITIVE -> applyFalsePositive(txn, latestAttempt, request);
            default -> throw new IllegalArgumentException("Unsupported decision");
        }

        txn.setFlaggedForReview(false);
        transactionRepository.save(txn);
        if (latestAttempt != null) {
            attemptRepository.save(latestAttempt);
        }

        emitEvent(new PaymentCompletedEvent(
            txn.getReference(),
            txn.getStatus().name(),
            0,
            latestAttempt != null ? latestAttempt.getBankDecision() : null
        ));

        return txn;
    }

    private void applyApprove(Transaction txn, PaymentAttempt attempt, ReviewDecisionRequest request) {
        txn.setStatus(TransactionStatus.SUCCEEDED);
        if (attempt != null) {
            attempt.setStatus(AttemptStatus.SUCCEEDED);
            if (attempt.getBankDecision() == null || attempt.getBankDecision().isBlank()) {
                attempt.setBankDecision("APPROVED");
            }
            attempt.setFraudDecision("REVIEW_APPROVED");
        }
        log.info("Manual review APPROVE by {}: {}", request.getReviewer(), request.getReason());
    }

    private void applyDecline(Transaction txn, PaymentAttempt attempt, ReviewDecisionRequest request) {
        txn.setStatus(TransactionStatus.FAILED);
        if (attempt != null) {
            attempt.setStatus(AttemptStatus.BANK_FAILED);
            attempt.setBankDecision("DECLINED");
            attempt.setFraudDecision("REVIEW_DECLINED");
        }
        log.info("Manual review DECLINE by {}: {}", request.getReviewer(), request.getReason());
    }

    private void applyBlock(Transaction txn, PaymentAttempt attempt, ReviewDecisionRequest request) {
        txn.setStatus(TransactionStatus.BLOCKED);
        if (attempt != null) {
            attempt.setStatus(AttemptStatus.FRAUD_REJECTED);
            attempt.setBankDecision("DECLINED");
            attempt.setFraudDecision("REVIEW_BLOCKED");
        }
        log.info("Manual review BLOCK by {}: {}", request.getReviewer(), request.getReason());
    }

    private void applyFalsePositive(Transaction txn, PaymentAttempt attempt, ReviewDecisionRequest request) {
        txn.setStatus(TransactionStatus.SUCCEEDED);
        if (attempt != null) {
            attempt.setStatus(AttemptStatus.SUCCEEDED);
            if (attempt.getBankDecision() == null || attempt.getBankDecision().isBlank()) {
                attempt.setBankDecision("APPROVED");
            }
            attempt.setFraudDecision("FALSE_POSITIVE");
        }
        log.info("Manual review FALSE_POSITIVE by {}: {}", request.getReviewer(), request.getReason());
    }

    private void emitEvent(Object event) {
        log.info("Event emitted: {} - {}", event.getClass().getSimpleName(), event);
    }
}
