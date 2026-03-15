package com.SHIVA.puja.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequest {

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotNull(message = "Available stock is required.")
    @Min(value = 0, message = "Available stock cannot be negative.")
    private Integer availableStock;

    @NotNull(message = "Reserved stock is required.")
    @Min(value = 0, message = "Reserved stock cannot be negative.")
    private Integer reservedStock;

    @NotNull(message = "Low stock threshold is required.")
    @Min(value = 0, message = "Low stock threshold cannot be negative.")
    private Integer lowStockThreshold;

    private String stockHistory;
}
