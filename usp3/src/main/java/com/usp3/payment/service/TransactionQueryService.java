package com.usp3.payment.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.usp3.fraud.FraudResult;
import com.usp3.fraud.repository.FraudResultRepository;
import com.usp3.payment.dto.TransactionDetailResponse;
import com.usp3.payment.dto.TransactionListItem;
import com.usp3.payment.dto.TransactionStoryResponse;
import com.usp3.payment.entity.PaymentAttempt;
import com.usp3.payment.entity.Transaction;
import com.usp3.payment.entity.enums.TransactionStatus;
import com.usp3.payment.repository.PaymentAttemptRepository;
import com.usp3.payment.repository.TransactionRepository;
import com.usp3.policy.entity.PolicyResult;
import com.usp3.policy.repository.PolicyResultRepository;

import lombok.RequiredArgsConstructor;

/**
 * STEP 11: Transaction Retrieval Service
 * 
 * Provides explainable transaction history
 */
@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final FraudResultRepository fraudResultRepository;
    private final PolicyResultRepository policyResultRepository;

    public Page<TransactionListItem> listTransactions(java.util.UUID merchantId, String status, String query, int page, int size) {
        if (merchantId == null) {
            throw new IllegalArgumentException("merchantId is required");
        }
        var pageable = PageRequest.of(
            Math.max(page, 0),
            Math.max(size, 1),
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );
        Page<Transaction> txPage;
        String safeStatus = status == null ? "" : status;
        String safeQuery = query == null ? "" : query;
        boolean hasStatus = !safeStatus.isBlank();
        boolean hasQuery = !safeQuery.isBlank();

        if (hasStatus && hasQuery) {
            TransactionStatus parsedStatus = TransactionStatus.valueOf(safeStatus.toUpperCase());
            txPage = transactionRepository.findByMerchantIdAndStatusAndReferenceContainingIgnoreCase(
                merchantId,
                parsedStatus,
                safeQuery.trim(),
                pageable
            );
        } else if (hasStatus) {
            TransactionStatus parsedStatus = TransactionStatus.valueOf(safeStatus.toUpperCase());
            txPage = transactionRepository.findByMerchantIdAndStatus(merchantId, parsedStatus, pageable);
        } else if (hasQuery) {
            txPage = transactionRepository.findByMerchantIdAndReferenceContainingIgnoreCase(
                merchantId,
                safeQuery.trim(),
                pageable
            );
        } else {
            txPage = transactionRepository.findByMerchantId(merchantId, pageable);
        }

        return txPage.map(tx -> {
            FraudResult fraud = fraudResultRepository.findByTransactionId(tx.getReference());
            PolicyResult policy = policyResultRepository.findFirstByTransactionIdOrderByCreatedAtDesc(tx.getId());
            PaymentAttempt latestAttempt = attemptRepository.findTop1ByTransaction_IdOrderByCreatedAtDesc(tx.getId());

            return TransactionListItem.builder()
                .reference(tx.getReference())
                .status(tx.getStatus().name())
                .amount(BigDecimal.valueOf(tx.getAmount()).divide(BigDecimal.valueOf(100)))
                .currency(tx.getCurrency())
                .createdAt(tx.getCreatedAt() != null ? tx.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .flaggedForReview(tx.isFlaggedForReview())
                .fraudScore(fraud != null ? fraud.getFraudScore() : null)
                .fraudDecision(fraud != null ? fraud.getDecision().name() : null)
                .fraudConfidence(fraud != null ? fraud.getConfidenceLevel().name() : null)
                .fraudExplanation(fraud != null ? fraud.getExplanation() : null)
                .triggeredRules(fraud != null ? fraud.getTriggeredRules() : null)
                .policyDecision(policy != null ? policy.getDecision().name() : null)
                .policyReason(policy != null ? policy.getReason() : null)
                .bankDecision(latestAttempt != null ? latestAttempt.getBankDecision() : null)
                .deviceFingerprint(latestAttempt != null ? latestAttempt.getDeviceFingerprint() : null)
                .country(latestAttempt != null ? latestAttempt.getCountry() : null)
                .binCountry(latestAttempt != null ? latestAttempt.getBinCountry() : null)
                .ipCountry(latestAttempt != null ? latestAttempt.getIpCountry() : null)
                .ipAddress(latestAttempt != null ? latestAttempt.getIpAddress() : null)
                .customerEmail(latestAttempt != null ? latestAttempt.getCustomerEmail() : null)
                .customerLast4(latestAttempt != null ? latestAttempt.getCustomerLast4() : null)
                .cardBrand(latestAttempt != null ? latestAttempt.getCardBrand() : null)
                .merchantLabel(latestAttempt != null ? latestAttempt.getMerchantLabel() : null)
                .build();
        });
    }

    public java.util.Optional<Transaction> findByReference(String reference) {
        return transactionRepository.findByReference(reference);
    }

    /**
     * GET /transactions/{id}
     * Returns complete transaction details
     */
    public TransactionDetailResponse getTransactionDetails(String transactionReference) {
        Transaction transaction = transactionRepository.findByReference(transactionReference)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionReference));

        FraudResult fraudResult = fraudResultRepository.findByTransactionId(transactionReference);
        PolicyResult policyResult = policyResultRepository.findFirstByTransactionIdOrderByCreatedAtDesc(transaction.getId());
        List<PaymentAttempt> attempts = attemptRepository.findAll().stream()
            .filter(pa -> pa.getTransaction().getId().equals(transaction.getId()))
            .collect(Collectors.toList());

        PaymentAttempt latestAttempt = attempts.isEmpty() ? null : attempts.get(attempts.size() - 1);

        return TransactionDetailResponse.builder()
            .transactionId(transaction.getReference())
            .status(transaction.getStatus().name())
            .amount(BigDecimal.valueOf(transaction.getAmount()).divide(BigDecimal.valueOf(100)))
            .currency(transaction.getCurrency())
            .createdAt(transaction.getCreatedAt() != null ? 
                transaction.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
            .updatedAt(transaction.getUpdatedAt() != null ? 
                transaction.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
            .fraudScore(fraudResult != null ? fraudResult.getFraudScore() : null)
            .fraudDecision(fraudResult != null ? fraudResult.getDecision().name() : null)
        .fraudConfidence(fraudResult != null ? fraudResult.getConfidenceLevel().name() : null)
            .fraudExplanation(fraudResult != null ? fraudResult.getExplanation() : null)
            .triggeredRules(fraudResult != null ? fraudResult.getTriggeredRules() : null)
            .policyDecision(policyResult != null ? policyResult.getDecision().name() : null)
            .policyReason(policyResult != null ? policyResult.getReason() : null)
            .bankDecision(latestAttempt != null ? latestAttempt.getBankDecision() : null)
        .flaggedForReview(transaction.isFlaggedForReview())
            .build();
    }

    /**
     * GET /transactions/{id}/story
     * Returns timeline of transaction journey
     */
    public TransactionStoryResponse getTransactionStory(String transactionReference) {
        Transaction transaction = transactionRepository.findByReference(transactionReference)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionReference));

        FraudResult fraudResult = fraudResultRepository.findByTransactionId(transactionReference);
        PolicyResult policyResult = policyResultRepository.findFirstByTransactionIdOrderByCreatedAtDesc(transaction.getId());
        List<PaymentAttempt> attempts = attemptRepository.findAll().stream()
            .filter(pa -> pa.getTransaction().getId().equals(transaction.getId()))
            .collect(Collectors.toList());

        PaymentAttempt latestAttempt = attempts.isEmpty() ? null : attempts.get(attempts.size() - 1);

        List<TransactionStoryResponse.StoryEvent> timeline = new ArrayList<>();

        // Helper to convert Instant to LocalDateTime
        java.util.function.Function<Instant, java.time.LocalDateTime> toLocalDateTime = 
            instant -> instant != null ? instant.atZone(ZoneId.systemDefault()).toLocalDateTime() : null;

        // 1. Payment Initiated
        timeline.add(TransactionStoryResponse.StoryEvent.builder()
            .timestamp(toLocalDateTime.apply(transaction.getCreatedAt()))
            .eventType("PAYMENT_INITIATED")
            .description("Payment transaction created")
            .details(String.format("Amount: %s %s", 
                BigDecimal.valueOf(transaction.getAmount()).divide(BigDecimal.valueOf(100)), 
                transaction.getCurrency()))
            .build());

        // 2. Fraud Rules Triggered
        if (fraudResult != null) {
            timeline.add(TransactionStoryResponse.StoryEvent.builder()
                .timestamp(toLocalDateTime.apply(fraudResult.getCreatedAt()))
                .eventType("FRAUD_EVALUATED")
                .description("Fraud evaluation completed")
                .details(String.format("Score: %d, Decision: %s, Confidence: %s, Rules: %s", 
                    fraudResult.getFraudScore(),
                    fraudResult.getDecision(),
                    fraudResult.getConfidenceLevel(),
                    fraudResult.getTriggeredRules()))
                .build());
        }

        // 3. Policy Decision
        if (policyResult != null) {
            timeline.add(TransactionStoryResponse.StoryEvent.builder()
                .timestamp(policyResult.getCreatedAt())
                .eventType("POLICY_DECISION")
                .description("Policy evaluation completed")
                .details(String.format("Decision: %s, Reason: %s", 
                    policyResult.getDecision(),
                    policyResult.getReason()))
                .build());

            if (policyResult.getDecision() == com.usp3.policy.model.PolicyDecisionType.REVIEW) {
                timeline.add(TransactionStoryResponse.StoryEvent.builder()
                    .timestamp(policyResult.getCreatedAt())
                    .eventType("CASE_FLAGGED")
                    .description("Transaction flagged for manual review")
                    .details("Flagged for review by policy layer")
                    .build());
            }
        }

        // 4. Bank Response
        if (latestAttempt != null && !"PENDING".equals(latestAttempt.getBankDecision())) {
            Instant attemptTime = latestAttempt.getUpdatedAt() != null ? 
                latestAttempt.getUpdatedAt() : latestAttempt.getCreatedAt();
            timeline.add(TransactionStoryResponse.StoryEvent.builder()
                .timestamp(toLocalDateTime.apply(attemptTime))
                .eventType("BANK_RESPONDED")
                .description("Bank processing completed")
                .details("Decision: " + latestAttempt.getBankDecision())
                .build());
        }

        // 5. Final Status
        Instant finalTime = transaction.getUpdatedAt() != null ? 
            transaction.getUpdatedAt() : transaction.getCreatedAt();
        timeline.add(TransactionStoryResponse.StoryEvent.builder()
            .timestamp(toLocalDateTime.apply(finalTime))
            .eventType("FINAL_STATUS")
            .description("Transaction finalized")
            .details("Status: " + transaction.getStatus())
            .build());

        return TransactionStoryResponse.builder()
            .transactionId(transaction.getReference())
            .status(transaction.getStatus().name())
            .amount(transaction.getAmount())
            .currency(transaction.getCurrency())
            .timeline(timeline)
            .build();
    }
}
