package com.SHIVA.puja.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer availableStock;
    private Integer reservedStock;
    private Integer lowStockThreshold;
    private String stockHistory;
    private LocalDateTime updatedAt;
}
