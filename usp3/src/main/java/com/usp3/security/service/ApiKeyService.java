package com.usp3.security.service;

import java.util.UUID;

public interface ApiKeyService {

    /**
     * Validates API key and returns merchantId
     */
    UUID validateAndGetMerchantId(String rawApiKey);
}
