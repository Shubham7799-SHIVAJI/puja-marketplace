package com.SHIVA.puja.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotBlank(message = "Customer name is required.")
    private String customerName;

    @NotNull(message = "Rating is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rating must be positive.")
    @DecimalMax(value = "5.0", message = "Rating cannot be more than 5.")
    private BigDecimal rating;

    private String reviewText;

    private String replyText;

    private Boolean abusive;
}
