package com.SHIVA.puja.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentInitiateRequest {

    @NotNull(message = "Order id is required")
    private Long orderId;

    private String provider;
}
