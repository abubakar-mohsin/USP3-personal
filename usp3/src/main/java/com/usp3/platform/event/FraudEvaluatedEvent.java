package com.usp3.platform.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FraudEvaluatedEvent {
    private String transactionId;
    private int fraudScore;
    private String decision;
    private String explanation;
}
