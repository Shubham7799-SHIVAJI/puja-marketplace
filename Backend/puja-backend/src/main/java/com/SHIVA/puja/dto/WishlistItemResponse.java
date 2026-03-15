package com.SHIVA.puja.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistItemResponse {

    private Long id;
    private Long productId;
    private LocalDateTime createdAt;
    private MarketplaceProductCardResponse product;
}