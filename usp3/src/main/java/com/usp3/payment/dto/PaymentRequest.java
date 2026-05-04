package com.usp3.payment.dto;

import java.math.BigDecimal;

public class PaymentRequest {

    private BigDecimal amount = BigDecimal.ZERO; // default to 0
    private String currency = "USD";             // default currency

    // merchant identification
    private String merchantCode = "";

    // simulated token (card already tokenized)
    private String paymentMethodToken = "";

    // optional customer + method display fields
    private String customerEmail = "";
    private String customerLast4 = "";
    private String cardBrand = "";
    private String merchantLabel = "";

    // risk inputs
    private String deviceFingerprint = "";
    // BIN/card country (issuer country)
    private String country = "";
    // optional IP geo country (from edge/geo resolver)
    private String ipCountry = "";

    // traceability
    private String traceId = "";

    // getters & setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount != null ? amount : BigDecimal.ZERO; 
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { 
        this.currency = currency != null ? currency : "USD"; 
    }

    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String merchantCode) { 
        this.merchantCode = merchantCode != null ? merchantCode : ""; 
    }

    public String getPaymentMethodToken() { return paymentMethodToken; }
    public void setPaymentMethodToken(String paymentMethodToken) {
        this.paymentMethodToken = paymentMethodToken != null ? paymentMethodToken : "";
    }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail != null ? customerEmail : "";
    }

    public String getCustomerLast4() { return customerLast4; }
    public void setCustomerLast4(String customerLast4) {
        this.customerLast4 = customerLast4 != null ? customerLast4 : "";
    }

    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand != null ? cardBrand : "";
    }

    public String getMerchantLabel() { return merchantLabel; }
    public void setMerchantLabel(String merchantLabel) {
        this.merchantLabel = merchantLabel != null ? merchantLabel : "";
    }

    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint != null ? deviceFingerprint : "";
    }

    public String getCountry() { return country; }
    public void setCountry(String country) { 
        this.country = country != null ? country : ""; 
    }

    public String getIpCountry() { return ipCountry; }
    public void setIpCountry(String ipCountry) {
        this.ipCountry = ipCountry != null ? ipCountry : "";
    }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { 
        this.traceId = traceId != null ? traceId : ""; 
    }
}
