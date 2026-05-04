package com.usp3.payment.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usp3.payment.dto.PaymentRequest;
import com.usp3.payment.dto.PaymentResponse;
import com.usp3.payment.dto.TransactionDetailResponse;
import com.usp3.payment.dto.TransactionListItem;
import com.usp3.payment.dto.TransactionStoryResponse;
import com.usp3.payment.service.PaymentService;
import com.usp3.payment.service.TransactionQueryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TransactionQueryService transactionQueryService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest) {

        // 1. Get the Merchant ID that the Filter "saved" in the request
        UUID authenticatedMerchantId = (UUID) httpRequest.getAttribute("authenticatedMerchantId");
        if (authenticatedMerchantId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Missing or invalid API key"
            );
        }

        // 1.a Capture client IP (favor X-Forwarded-For when present)
        String clientIp = extractClientIp(httpRequest);

        // 2. Pass the DTO and the Merchant ID to the Service
        PaymentResponse response = paymentService.processPayment(request, authenticatedMerchantId, clientIp);

        return ResponseEntity.ok(response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // In case of multiple IPs, take the first
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * STEP 11: GET /transactions/{id}
     * Returns complete transaction details
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionDetailResponse> getTransaction(@PathVariable String id) {
        TransactionDetailResponse response = transactionQueryService.getTransactionDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{id}/peek")
    public ResponseEntity<java.util.Map<String, Object>> peekTransaction(
        @PathVariable String id,
        HttpServletRequest request
    ) {
        UUID authenticatedMerchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        var txn = transactionQueryService.findByReference(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("reference", txn.getReference());
        body.put("merchantId", txn.getMerchantId());
        body.put("status", txn.getStatus().name());
        body.put("createdAt", txn.getCreatedAt());
        body.put("yourMerchantId", authenticatedMerchantId);
        body.put("matches", authenticatedMerchantId != null && authenticatedMerchantId.equals(txn.getMerchantId()));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionListItem>> listTransactions(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        UUID authenticatedMerchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (authenticatedMerchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(transactionQueryService.listTransactions(authenticatedMerchantId, status, q, page, size));
    }

    /**
     * STEP 11: GET /transactions/{id}/story
     * Returns timeline of transaction journey
     */
    @GetMapping("/transactions/{id}/story")
    public ResponseEntity<TransactionStoryResponse> getTransactionStory(@PathVariable String id) {
        TransactionStoryResponse response = transactionQueryService.getTransactionStory(id);
        return ResponseEntity.ok(response);
    }
}
