package com.usp3.payment.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STEP 11: Transaction Story Response
 * 
 * Returns the complete timeline of a transaction's journey
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStoryResponse {
    
    private String transactionId;
    private String status;
    private Long amount;
    private String currency;
    
    private List<StoryEvent> timeline;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoryEvent {
        private LocalDateTime timestamp;
        private String eventType;
        private String description;
        private String details;
    }
}
