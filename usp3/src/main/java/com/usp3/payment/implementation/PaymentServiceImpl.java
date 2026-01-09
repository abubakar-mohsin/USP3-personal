package com.usp3.payment.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.usp3.payment.dto.PaymentRequest;
import com.usp3.payment.dto.PaymentResponse;
import com.usp3.payment.entity.PaymentAttempt;
import com.usp3.payment.entity.Transaction;
import com.usp3.payment.repository.PaymentAttemptRepository;
import com.usp3.payment.repository.TransactionRepository;
import com.usp3.payment.service.PaymentService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentAttemptRepository attemptRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, UUID merchantId) {
        
        // --- STEP 3: CREATE TRANSACTION (Immutable Record) ---
        String reference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Convert BigDecimal amount to Long (cents/smallest unit)
        Long amountInCents = request.getAmount()
            .multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
        
        Transaction transaction = new Transaction(
            reference,
            amountInCents,
            request.getCurrency(),
            merchantId,            // Pass UUID directly
            "default-tenant",      // Match your BaseEntity requirements
            request.getTraceId()
        );

        Transaction savedTxn = transactionRepository.save(transaction);

        // --- STEP 4: CREATE PAYMENT ATTEMPT (The First Try) ---
        PaymentAttempt firstAttempt = new PaymentAttempt(
            savedTxn,
            1, // First attempt
            "default-tenant",
            request.getTraceId()
        );
        
        attemptRepository.save(firstAttempt);

        // Return the response as PENDING/CREATED
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(savedTxn.getReference());
        response.setStatus("CREATED");
        return response;
    }
}