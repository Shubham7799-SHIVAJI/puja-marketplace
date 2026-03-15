package com.SHIVA.puja.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketplaceProductDetailResponse {

    private MarketplaceProductCardResponse product;
    private String sellerDescription;
    private String returnPolicy;
    private String shippingPartners;
    private String deliveryRegions;
    private String estimatedDelivery;
    private List<ProductResponse.ImageItem> images;
    private List<ProductResponse.VariantItem> variants;
    private List<ReviewResponse> reviews;
}