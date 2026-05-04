package com.usp3.security.implementation;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service; // Import the utility
import org.springframework.transaction.annotation.Transactional;

import com.usp3.exception.UnauthorizedException;
import com.usp3.security.entity.ApiKey;
import com.usp3.security.repository.ApiKeyRepository;
import com.usp3.security.service.ApiKeyService;
import com.usp3.security.util.ApiKeyUtils;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyServiceImpl(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public UUID validateAndGetMerchantId(String rawApiKey) {
        System.out.println("Incoming API Key: " + rawApiKey);
        String hashedKey = ApiKeyUtils.hashKey(rawApiKey);
        System.out.println("Hashed Key: " + hashedKey);

        return apiKeyRepository.findByKeyHashAndActiveTrue(hashedKey)
                .orElseThrow(() -> new UnauthorizedException("Invalid API Key"))
                .getMerchantId();
    }

    @Override
    public List<ApiKey> listForMerchant(UUID merchantId) {
        return apiKeyRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    @Override
    @Transactional
    public String createNewSandboxKey(UUID merchantId, String tenantId, String traceId) {
        // deactivate existing keys for safety
        List<ApiKey> existing = apiKeyRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
        existing.forEach(ApiKey::deactivate);
        apiKeyRepository.saveAll(existing);

        String rawKey = "sk_sandbox_" + UUID.randomUUID();
        String hash = ApiKeyUtils.hashKey(rawKey);
        String display = rawKey.substring(0, Math.min(rawKey.length(), 12)) + "***" + rawKey.substring(rawKey.length() - 4);
        ApiKey apiKey = new ApiKey(hash, merchantId, display);
        apiKey.setTenantId(tenantId != null ? tenantId : "default-tenant");
        apiKey.setTraceId(traceId != null && !traceId.isBlank() ? traceId : UUID.randomUUID().toString());
        apiKeyRepository.save(apiKey);
        return rawKey;
    }

}