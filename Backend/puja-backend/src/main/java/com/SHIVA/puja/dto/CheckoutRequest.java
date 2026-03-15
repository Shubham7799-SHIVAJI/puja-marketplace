package com.SHIVA.puja.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotNull(message = "Address is required.")
    private Long addressId;

    @NotBlank(message = "Payment method is required.")
    private String paymentMethod;

    private String couponCode;

    @Valid
    @NotEmpty(message = "At least one item is required.")
    private List<Item> items;

    @Data
    public static class Item {

        @NotNull(message = "Product is required.")
        private Long productId;

        private Long variantId;

        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        private Integer quantity;
    }
}