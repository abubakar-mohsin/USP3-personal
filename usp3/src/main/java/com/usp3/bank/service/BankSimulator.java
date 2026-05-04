package com.usp3.bank.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.usp3.fraud.FraudDecision;

import lombok.RequiredArgsConstructor;

/**
 * STEP 7: BANK SIMULATOR (CONTROLLED REALISM)
 * * Logic updated:
 * - If Fraud REJECTED -> Returns "DECLINED"
 * - If Fraud APPROVED/REVIEW -> Returns "APPROVED" (Randomness removed for testing)
 */
@Service
@RequiredArgsConstructor
public class BankSimulator {

    private final Random random = new Random();

    /**
     * Simulate bank response
     * * @param fraudDecision The fraud engine decision
     * @param amount Transaction amount in cents
     * @return Bank decision: "APPROVED" or "DECLINED"
     */
    public String simulateBankResponse(FraudDecision fraudDecision, Long amount) {
        
        // 1. If fraud blocked, bank is never called
        if (fraudDecision == FraudDecision.REJECTED) {
            return "DECLINED";
        }

        // 2. For testing purposes, we force "APPROVED" for any transaction 
        // that wasn't blocked by fraud. 
        // (Previously this had a random.nextDouble() check that caused your DECLINES)
        
        return "APPROVED"; 
    }

    /**
     * Simulate bank latency (50ms - 200ms)
     */
    public long simulateLatency() {
        return 50 + random.nextInt(150);
    }
}