package com.usp3.security.dto;

public class LoginResponse {
    private final String accessToken;
    private final String refreshToken;
    private final int riskScore;
    private final String decision;
    private final String triggeredRules;
    private final boolean restricted;

    public LoginResponse(String accessToken, String refreshToken, int riskScore, String decision, String triggeredRules, boolean restricted) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.riskScore = riskScore;
        this.decision = decision;
        this.triggeredRules = triggeredRules;
        this.restricted = restricted;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public int getRiskScore() { return riskScore; }
    public String getDecision() { return decision; }
    public String getTriggeredRules() { return triggeredRules; }
    public boolean isRestricted() { return restricted; }
}
