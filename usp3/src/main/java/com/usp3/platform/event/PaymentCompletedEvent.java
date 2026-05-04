package com.usp3.platform.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCompletedEvent {
    private String transactionId;
    private String finalStatus;
    private int fraudScore;
    private String bankDecision;
}
