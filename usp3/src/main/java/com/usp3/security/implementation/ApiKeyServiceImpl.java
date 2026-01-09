package com.usp3.security.implementation;

import com.usp3.security.repository.ApiKeyRepository;
import com.usp3.security.util.ApiKeyUtils; // Import the utility
import com.usp3.exception.UnauthorizedException;
import com.usp3.security.service.ApiKeyService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyServiceImpl(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public UUID validateAndGetMerchantId(String rawApiKey) {
        // 1. Hash the incoming key
        String hashedKey = ApiKeyUtils.hashKey(rawApiKey);

        // 2. Query and return the merchantId
        return apiKeyRepository.findByKeyHashAndActiveTrue(hashedKey)
                .orElseThrow(() -> new UnauthorizedException("Invalid API Key"))
                .getMerchantId(); // This now matches the Entity getter above!
    }
}