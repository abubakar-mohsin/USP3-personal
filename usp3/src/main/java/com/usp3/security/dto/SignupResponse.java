package com.usp3.security.dto;

import java.util.UUID;

public class SignupResponse {
    private final UUID merchantId;
    private final UUID adminUserId;
    private final String sandboxApiKey;

    public SignupResponse(UUID merchantId, UUID adminUserId, String sandboxApiKey) {
        this.merchantId = merchantId;
        this.adminUserId = adminUserId;
        this.sandboxApiKey = sandboxApiKey;
    }

    public UUID getMerchantId() { return merchantId; }
    public UUID getAdminUserId() { return adminUserId; }
    public String getSandboxApiKey() { return sandboxApiKey; }
}
