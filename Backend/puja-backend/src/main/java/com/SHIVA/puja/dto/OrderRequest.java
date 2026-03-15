package com.SHIVA.puja.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

    private Long customerId;

    @NotBlank(message = "Order code is required.")
    private String orderCode;

    @NotBlank(message = "Order status is required.")
    private String orderStatus;

    @NotBlank(message = "Payment method is required.")
    private String paymentMethod;

    @NotNull(message = "Total amount is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive.")
    private BigDecimal totalAmount;

    private String shippingPartner;

    private String trackingNumber;

    @NotBlank(message = "Primary product name is required.")
    private String primaryProductName;

    @NotNull(message = "Total quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private Integer totalQuantity;
}
