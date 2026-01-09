package com.usp3.payment.service;

import java.util.UUID;

import com.usp3.payment.dto.PaymentRequest;
import com.usp3.payment.dto.PaymentResponse;

public interface PaymentService {
    // No more String apiKey here. We use the UUID merchantId from the Gate.
    PaymentResponse processPayment(PaymentRequest request, UUID merchantId);
}
