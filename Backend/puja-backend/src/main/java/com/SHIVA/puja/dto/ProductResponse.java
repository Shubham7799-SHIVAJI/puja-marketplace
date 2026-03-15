package com.SHIVA.puja.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private String categoryName;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private Integer stockQuantity;
    private String status;
    private String weight;
    private String dimensions;
    private BigDecimal ratingAverage;
    private Integer reviewCount;
    private List<ImageItem> images;
    private List<VariantItem> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class ImageItem {
        private Long id;
        private String imageUrl;
        private Integer sortOrder;
        private Boolean primaryImage;
    }

    @Data
    @Builder
    public static class VariantItem {
        private Long id;
        private String variantName;
        private String variantValue;
        private String skuSuffix;
        private BigDecimal additionalPrice;
        private Integer stockQuantity;
    }
}
