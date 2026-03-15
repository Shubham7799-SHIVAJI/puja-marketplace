package com.SHIVA.puja.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketplaceProductCardResponse {

    private Long id;
    private String name;
    private String sellerName;
    private String sellerCode;
    private String categoryName;
    private String description;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private BigDecimal discountPercent;
    private BigDecimal ratingAverage;
    private Integer reviewCount;
    private Integer stockQuantity;
    private String status;
    private String imageUrl;
    private String deliveryLabel;
    private Boolean wishlisted;
}