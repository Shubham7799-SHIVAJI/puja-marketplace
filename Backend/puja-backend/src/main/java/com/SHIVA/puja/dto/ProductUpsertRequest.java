package com.SHIVA.puja.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductUpsertRequest {

    @NotBlank(message = "Product name is required.")
    private String name;

    @NotBlank(message = "SKU is required.")
    private String sku;

    @NotBlank(message = "Category is required.")
    private String categoryName;

    private String description;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive.")
    private BigDecimal price;

    @NotNull(message = "Discount is required.")
    @DecimalMin(value = "0.0", message = "Discount cannot be negative.")
    private BigDecimal discountPercent;

    @NotNull(message = "Stock quantity is required.")
    @Min(value = 0, message = "Stock quantity cannot be negative.")
    private Integer stockQuantity;

    @NotBlank(message = "Status is required.")
    private String status;

    private String weight;

    private String dimensions;

    @Valid
    private List<ImagePayload> images;

    @Valid
    private List<VariantPayload> variants;

    @Data
    public static class ImagePayload {
        @NotBlank(message = "Image URL is required.")
        private String imageUrl;
        private Integer sortOrder;
        private Boolean primaryImage;
    }

    @Data
    public static class VariantPayload {
        @NotBlank(message = "Variant name is required.")
        private String variantName;
        @NotBlank(message = "Variant value is required.")
        private String variantValue;
        private String skuSuffix;
        @DecimalMin(value = "0.0", message = "Additional price cannot be negative.")
        private BigDecimal additionalPrice;
        @Min(value = 0, message = "Variant stock cannot be negative.")
        private Integer stockQuantity;
    }
}
