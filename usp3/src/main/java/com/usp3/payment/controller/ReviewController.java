package com.usp3.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usp3.payment.dto.ReviewDecisionRequest;
import com.usp3.payment.entity.Transaction;
import com.usp3.payment.service.TransactionReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final TransactionReviewService reviewService;

    @PostMapping("/{transactionReference}")
    public ResponseEntity<Transaction> resolve(
        @PathVariable String transactionReference,
        @RequestBody ReviewDecisionRequest request
    ) {
        Transaction updated = reviewService.resolve(transactionReference, request);
        return ResponseEntity.ok(updated);
    }
}
