package com.usp3.payment.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.usp3.bank.service.BankSimulator;
import com.usp3.fraud.FraudResult;
import com.usp3.fraud.repository.FraudResultRepository;
import com.usp3.fraud.service.FraudEngine;
import com.usp3.payment.dto.PaymentRequest;
import com.usp3.payment.dto.PaymentResponse;
import com.usp3.payment.entity.PaymentAttempt;
import com.usp3.payment.entity.Transaction;
import com.usp3.payment.entity.enums.AttemptStatus;
import com.usp3.payment.entity.enums.TransactionStatus;
import com.usp3.payment.repository.PaymentAttemptRepository;
import com.usp3.payment.repository.TransactionRepository;
import com.usp3.payment.service.PaymentService;
import com.usp3.platform.event.BankRespondedEvent;
import com.usp3.platform.event.FraudEvaluatedEvent;
import com.usp3.platform.event.PaymentCompletedEvent;
import com.usp3.platform.event.PaymentInitiatedEvent;
import com.usp3.policy.entity.PolicyResult;
import com.usp3.policy.model.PolicyDecision;
import com.usp3.policy.model.PolicyDecisionType;
import com.usp3.policy.repository.PolicyResultRepository;
import com.usp3.policy.service.PolicyEngine;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * COMPLETE PAYMENT PIPELINE (Steps 3-10)
 * 
 * Pipeline Flow:
 * 1. Create Transaction (PENDING)
 * 2. Create PaymentAttempt
 * 3. Fraud Evaluation → FraudResult
 * 4. Policy Evaluation → PolicyDecision
 * 5. Bank Simulation → Bank Decision
 * 6. Final Transaction Status
 * 7. Emit Events
 * 8. Return Explainable Response
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final FraudEngine fraudEngine;
    private final FraudResultRepository fraudResultRepository;
    private final PolicyEngine policyEngine;
    private final PolicyResultRepository policyResultRepository;
    private final BankSimulator bankSimulator;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, UUID merchantId, String clientIpAddress) {
        String deviceFingerprint = request.getDeviceFingerprint();
        if (deviceFingerprint != null && deviceFingerprint.isBlank()) {
            deviceFingerprint = null; // allow risk evaluation without a fingerprint
        }

        String binCountry = request.getCountry();
        if (binCountry != null && binCountry.isBlank()) {
            binCountry = null;
        }

        String ipCountry = request.getIpCountry();
        if (ipCountry != null && ipCountry.isBlank()) {
            ipCountry = null;
        }

        if (ipCountry == null) {
            ipCountry = binCountry; // default to BIN country when geo country is unknown
        }
        
        // --- STEP 3: CREATE TRANSACTION (Immutable Record) ---
        String reference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Long amountInCents = request.getAmount()
            .multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
        
        Transaction transaction = new Transaction(
            reference,
            amountInCents,
            request.getCurrency(),
            merchantId,
            "default-tenant",
            request.getTraceId()
        );
        transaction.setStatus(TransactionStatus.PROCESSING);

        Transaction savedTxn = transactionRepository.save(transaction);

        // Emit PaymentInitiatedEvent
        emitEvent(new PaymentInitiatedEvent(
            savedTxn.getId(),
            savedTxn.getReference(),
            merchantId,
            amountInCents,
            request.getCurrency()
        ));

        // --- STEP 4: CREATE PAYMENT ATTEMPT ---
        PaymentAttempt attempt = new PaymentAttempt(
            savedTxn,
            1,
            "default-tenant",
            request.getTraceId(),
            deviceFingerprint,
            ipCountry,
            clientIpAddress
        );
        attempt.setBinCountry(binCountry);
        attempt.setIpCountry(ipCountry);
        attempt.setCustomerEmail(request.getCustomerEmail());
        attempt.setCustomerLast4(request.getCustomerLast4());
        attempt.setCardBrand(request.getCardBrand());
        attempt.setMerchantLabel(request.getMerchantLabel());
        attemptRepository.save(attempt);

        // --- STEP 5: FRAUD ENGINE ---
        FraudResult fraudResult = fraudEngine.evaluate(
            savedTxn,
            attempt,
            deviceFingerprint,
            binCountry,
            clientIpAddress
        );
        fraudResultRepository.save(fraudResult);

        // Emit FraudEvaluatedEvent
        emitEvent(new FraudEvaluatedEvent(
            savedTxn.getReference(),
            fraudResult.getFraudScore(),
            fraudResult.getDecision().name(),
            fraudResult.getExplanation()
        ));

        // --- STEP 6: POLICY LAYER ---
        PolicyDecision policyDecision = policyEngine.evaluate(fraudResult, savedTxn);
        
        // Persist PolicyResult
        PolicyResult policyResult = new PolicyResult(
            savedTxn.getId(),
            policyDecision.getDecision(),
            policyDecision.getPolicyName(),
            policyDecision.getReason()
        );
        policyResultRepository.save(policyResult);

        // Update attempt with fraud decision
        attempt.setFraudDecision(policyDecision.getDecision().name());
        if (policyDecision.getDecision() == PolicyDecisionType.BLOCK) {
            attempt.setStatus(AttemptStatus.FRAUD_REJECTED);
        }

        // --- STEP 7: BANK SIMULATOR ---
        String bankDecision = bankSimulator.simulateBankResponse(
            fraudResult.getDecision(),
            savedTxn.getAmount()
        );
        attempt.setBankDecision(bankDecision);
        attempt.setStatus(resolveAttemptStatus(policyDecision.getDecision(), bankDecision));
        attemptRepository.save(attempt);

        // Emit BankRespondedEvent
        long bankLatency = bankSimulator.simulateLatency();
        emitEvent(new BankRespondedEvent(
            savedTxn.getReference(),
            bankDecision,
            bankLatency
        ));

        // --- STEP 8: FINAL TRANSACTION DECISION ---
        TransactionStatus finalStatus = determineFinalStatus(
            policyDecision.getDecision(),
            bankDecision
        );
        savedTxn.setStatus(finalStatus);
        // flag reviews for later case management
        if (policyDecision.getDecision() == PolicyDecisionType.REVIEW) {
            savedTxn.setFlaggedForReview(true);
        }
        transactionRepository.save(savedTxn);

        // Emit PaymentCompletedEvent
        emitEvent(new PaymentCompletedEvent(
            savedTxn.getReference(),
            finalStatus.name(),
            fraudResult.getFraudScore(),
            bankDecision
        ));

        // --- STEP 10: RETURN EXPLAINABLE RESPONSE ---
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(savedTxn.getReference());
        response.setStatus(mapStatusToResponse(finalStatus));
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setFraudScore(fraudResult.getFraudScore());
        response.setFraudDecision(fraudResult.getDecision().name());
        response.setFraudConfidence(fraudResult.getConfidenceLevel().name());
        response.setTriggeredRules(fraudResult.getTriggeredRules());
        response.setExplanation(fraudResult.getExplanation());
        response.setPolicyDecision(policyDecision.getDecision().name());
        response.setPolicyReason(policyDecision.getReason());
        response.setBankDecision(bankDecision);
        response.setFlaggedForReview(savedTxn.isFlaggedForReview());
        response.setTraceId(request.getTraceId());
        
        return response;
    }

    /**
     * STEP 8: Determine final transaction status based on fraud + bank decisions
     */
    private TransactionStatus determineFinalStatus(PolicyDecisionType fraudDecision, String bankDecision) {
        if (fraudDecision == PolicyDecisionType.BLOCK) {
            return TransactionStatus.BLOCKED;
        }
        
        if (fraudDecision == PolicyDecisionType.ALLOW && "APPROVED".equals(bankDecision)) {
            return TransactionStatus.SUCCEEDED;
        }
        
        if (fraudDecision == PolicyDecisionType.ALLOW && "DECLINED".equals(bankDecision)) {
            return TransactionStatus.FAILED;
        }
        
        // REVIEW case - hold until manual decision
        if (fraudDecision == PolicyDecisionType.REVIEW) {
            return TransactionStatus.HELD_FOR_REVIEW;
        }

        return TransactionStatus.FAILED;
    }

    private AttemptStatus resolveAttemptStatus(PolicyDecisionType policyDecision, String bankDecision) {
        if (policyDecision == PolicyDecisionType.BLOCK) {
            return AttemptStatus.FRAUD_REJECTED;
        }
        if (policyDecision == PolicyDecisionType.REVIEW) {
            return AttemptStatus.HELD_FOR_REVIEW;
        }
        if ("DECLINED".equals(bankDecision)) {
            return AttemptStatus.BANK_FAILED;
        }
        return AttemptStatus.SUCCEEDED;
    }

    private String mapStatusToResponse(TransactionStatus status) {
        return switch (status) {
            case SUCCEEDED -> "APPROVED";
            case FAILED -> "DECLINED";
            case BLOCKED -> "BLOCKED";
            case HELD_FOR_REVIEW -> "REVIEW";
            default -> "PROCESSING";
        };
    }

    /**
     * STEP 9: Emit internal events (simple logging for now)
     */
    private void emitEvent(Object event) {
        // For Phase 3, we just log. Later this can be Kafka/EventBus
        log.info("Event emitted: {} - {}", event.getClass().getSimpleName(), event);
    }
}
