package com.SHIVA.puja.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CouponRequest {

    @NotBlank(message = "Coupon code is required.")
    private String code;

    private String campaignName;

    @NotBlank(message = "Discount type is required.")
    private String discountType;

    @NotBlank(message = "Discount value is required.")
    private String discountValue;

    @NotNull(message = "Start date is required.")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required.")
    private LocalDateTime endDate;

    @NotNull(message = "Usage limit is required.")
    @Min(value = 1, message = "Usage limit must be at least 1.")
    private Integer usageLimit;

    @NotBlank(message = "Status is required.")
    private String status;
}
