package com.usp3.security.dto;

import java.util.UUID;

public class AcceptInviteResponse {
    private final UUID merchantId;
    private final UUID userId;

    public AcceptInviteResponse(UUID merchantId, UUID userId) {
        this.merchantId = merchantId;
        this.userId = userId;
    }

    public UUID getMerchantId() { return merchantId; }
    public UUID getUserId() { return userId; }
}
