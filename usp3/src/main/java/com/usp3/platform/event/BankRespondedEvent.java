package com.usp3.platform.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BankRespondedEvent {
    private String transactionId;
    private String bankDecision;
    private long latencyMs;
}
