package com.usp3.platform.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentInitiatedEvent {
    private UUID transactionId;
    private String transactionReference;
    private UUID merchantId;
    private Long amount;
    private String currency;
}
