package com.SHIVA.puja.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CouponResponse {

    private Long id;
    private String code;
    private String campaignName;
    private String discountType;
    private String discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private String status;
}
