package com.SHIVA.puja.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerReviewCreateRequest {

    @NotNull(message = "Product is required.")
    private Long productId;

    @NotNull(message = "Rating is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rating must be positive.")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.")
    private BigDecimal rating;

    private String reviewText;
}