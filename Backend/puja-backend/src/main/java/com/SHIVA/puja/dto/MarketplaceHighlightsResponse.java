package com.SHIVA.puja.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketplaceHighlightsResponse {

    private List<CategoryOptionResponse> categories;
    private List<MarketplaceProductCardResponse> featuredProducts;
    private List<MarketplaceProductCardResponse> trendingProducts;
    private List<MarketplaceProductCardResponse> budgetProducts;
}