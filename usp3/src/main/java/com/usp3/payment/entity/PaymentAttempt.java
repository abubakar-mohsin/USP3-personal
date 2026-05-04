package com.usp3.payment.entity;

import com.usp3.payment.entity.enums.AttemptStatus;
import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_attempts")
@Getter
@Setter
public class PaymentAttempt extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status;

    @Column(nullable = false)
    private String fraudDecision; // ALLOW / REVIEW / BLOCK

    @Column(nullable = false)
    private String bankDecision; // APPROVED / DECLINED / TIMEOUT

    @Column
    private String deviceFingerprint;

    @Column
    private String ipAddress;

    @Column
    private String country;

    @Column
    private String binCountry;

    @Column
    private String ipCountry;

    @Column
    private String customerEmail;

    @Column
    private String customerLast4;

    @Column
    private String cardBrand;

    @Column
    private String merchantLabel;

    protected PaymentAttempt() {}

    public PaymentAttempt(Transaction transaction,
                          int attemptNumber,
                          String tenantId,
                          String traceId,
                          String deviceFingerprint,
                          String country,
                          String ipAddress) {
        this.transaction = transaction;
        this.attemptNumber = attemptNumber;
        this.status = AttemptStatus.INITIATED;
        this.fraudDecision = "PENDING";
        this.bankDecision = "PENDING";
        this.deviceFingerprint = deviceFingerprint;
        this.country = country;
        this.binCountry = null;
        this.ipCountry = null;
        this.customerEmail = null;
        this.customerLast4 = null;
        this.cardBrand = null;
        this.merchantLabel = null;
        this.ipAddress = ipAddress;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public void setBinCountry(String binCountry) { this.binCountry = binCountry; }
    public void setIpCountry(String ipCountry) { this.ipCountry = ipCountry; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCustomerLast4(String customerLast4) { this.customerLast4 = customerLast4; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    public void setMerchantLabel(String merchantLabel) { this.merchantLabel = merchantLabel; }

    public String getBinCountry() { return binCountry; }
    public String getIpCountry() { return ipCountry; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerLast4() { return customerLast4; }
    public String getCardBrand() { return cardBrand; }
    public String getMerchantLabel() { return merchantLabel; }
}
