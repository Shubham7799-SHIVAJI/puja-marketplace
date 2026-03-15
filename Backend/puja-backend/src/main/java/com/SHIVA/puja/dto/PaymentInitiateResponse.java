package com.SHIVA.puja.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInitiateResponse {

    private String provider;
    private String razorpayOrderId;
    private String keyId;
    private Long amountPaise;
    private String currency;
    private Long orderId;
    private String orderCode;
    private String message;
}
