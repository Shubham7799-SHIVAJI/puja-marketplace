package com.SHIVA.puja.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {

    private LocalDateTime placedAt;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private List<PlacedOrder> orders;

    @Data
    @Builder
    public static class PlacedOrder {
        private Long orderId;
        private String orderCode;
        private String sellerName;
        private String status;
        private BigDecimal totalAmount;
        private String trackingNumber;
    }
}