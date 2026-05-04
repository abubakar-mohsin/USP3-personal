package com.usp3.security.service;

import java.util.UUID;

public interface ApiKeyService {

    /**
     * Validates API key and returns merchantId
     */
    UUID validateAndGetMerchantId(String rawApiKey);

    /**
     * Lists active keys for a merchant.
     */
    java.util.List<com.usp3.security.entity.ApiKey> listForMerchant(UUID merchantId);

    /**
     * Creates (rotates) a new sandbox key for the merchant, deactivating old ones.
     * Returns the raw key so caller can display/copy it once.
     */
    String createNewSandboxKey(UUID merchantId, String tenantId, String traceId);
}
