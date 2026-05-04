package com.usp3.security.dto;

public class LoginRequest {
    private String email;
    private String password;
    private String deviceFingerprint;
    private String country;
    private String traceId;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
