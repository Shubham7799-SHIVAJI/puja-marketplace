package com.SHIVA.puja.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String customerName;
    private BigDecimal rating;
    private String reviewText;
    private String replyText;
    private Boolean abusive;
    private LocalDateTime createdAt;
}
