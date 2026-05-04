package com.usp3.security.dto;

public class SignupRequest {
    private String merchantName;
    private String merchantCode;
    private String adminEmail;
    private String password;
    private String traceId;
    private String role;

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String merchantCode) { this.merchantCode = merchantCode; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
