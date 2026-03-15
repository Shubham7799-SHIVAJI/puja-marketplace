package com.SHIVA.puja.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private String orderCode;
    private String orderStatus;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private String shippingPartner;
    private String trackingNumber;
    private String primaryProductName;
    private Integer totalQuantity;
    private LocalDateTime orderDate;
}
