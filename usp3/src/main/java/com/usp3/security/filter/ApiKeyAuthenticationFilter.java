package com.usp3.security.filter;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.filter.OncePerRequestFilter;

import com.usp3.exception.UnauthorizedException;
import com.usp3.security.service.ApiKeyService;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        if (path.startsWith("/api/v1/payments")) {
            String keyPreview = request.getHeader("X-API-KEY");
            if (keyPreview != null && keyPreview.length() > 8) {
                keyPreview = keyPreview.substring(0, 8) + "…";
            }
            log.info("API key filter hit: {} {} key={}", method, path, keyPreview);
        }

        // 1. Pull the key from the HTTP Header (The "Handover")
        String rawApiKey = request.getHeader("X-API-KEY");

        if (rawApiKey == null || rawApiKey.isEmpty()) {
            log.warn("Missing API key for {} {}", method, path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing API Key");
            return;
        }

        try {
            // 2. Use your Step 2 logic to validate and get the Merchant ID
            UUID merchantId = apiKeyService.validateAndGetMerchantId(rawApiKey);

            // 3. Success! Store the Merchant ID in the request context
            // This allows the PaymentService (Step 3) to know WHO the merchant is.
            request.setAttribute("authenticatedMerchantId", merchantId);

            // 4. Let the request continue to the Controller
            filterChain.doFilter(request, response);

        } catch (UnauthorizedException e) {
            log.warn("Invalid API key for {} {}", method, path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Only enforce API key on payment ingestion; skip auth/signup/login and other endpoints
        if (path.startsWith("/api/v1/auth")) {
            return true;
        }
        // Allow non-payment endpoints to go through without API key
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        boolean skip = !("/api/v1/payments".equals(path) || "/api/v1/payments/".equals(path));
        if (path.startsWith("/api/v1/payments")) {
            log.info("API key filter check: {} {} skip={}", request.getMethod(), path, skip);
        }
        return skip;
    }
}