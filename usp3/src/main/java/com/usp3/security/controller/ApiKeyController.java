package com.usp3.security.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usp3.security.service.ApiKeyService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public record ApiKeyResponse(UUID id, String displayValue, boolean active, java.time.Instant createdAt) {}
    public record CreateKeyResponse(String apiKey, String displayValue) {}

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list(HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(401).build();
        }
        List<ApiKeyResponse> payload = apiKeyService.listForMerchant(merchantId).stream()
            .map(k -> new ApiKeyResponse(k.getId(), k.getDisplayValue(), k.isActive(), k.getCreatedAt()))
            .toList();
        return ResponseEntity.ok(payload);
    }

    @PostMapping
    public ResponseEntity<CreateKeyResponse> rotate(HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        String tenantId = (String) request.getAttribute("authenticatedTenantId");
        if (merchantId == null) {
            return ResponseEntity.status(401).build();
        }
        String traceId = UUID.randomUUID().toString();
        String raw = apiKeyService.createNewSandboxKey(merchantId, tenantId != null ? tenantId : "default-tenant", traceId);
        String display = raw.substring(0, Math.min(raw.length(), 12)) + "***" + raw.substring(raw.length() - 4);
        return ResponseEntity.ok(new CreateKeyResponse(raw, display));
    }
}
