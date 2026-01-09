package com.usp3.payment.dto;

import java.math.BigDecimal;

public class PaymentRequest {

    private BigDecimal amount;
    private String currency;

    // merchant identification
    private String merchantCode;

    // simulated token (card already tokenized)
    private String paymentMethodToken;

    // risk inputs
    private String deviceFingerprint;
    private String country;

    // traceability
    private String traceId;

    // getters & setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String merchantCode) { this.merchantCode = merchantCode; }

    public String getPaymentMethodToken() { return paymentMethodToken; }
    public void setPaymentMethodToken(String paymentMethodToken) {
        this.paymentMethodToken = paymentMethodToken;
    }

    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
