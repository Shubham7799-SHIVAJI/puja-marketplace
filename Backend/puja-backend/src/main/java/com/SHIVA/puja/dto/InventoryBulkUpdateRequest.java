package com.SHIVA.puja.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class InventoryBulkUpdateRequest {

    @Valid
    @NotEmpty(message = "At least one inventory item is required.")
    private List<InventoryRequest> items;
}
