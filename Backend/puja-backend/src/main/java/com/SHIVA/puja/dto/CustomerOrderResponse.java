package com.SHIVA.puja.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerOrderResponse {

    private Long orderId;
    private String orderCode;
    private String sellerName;
    private String sellerCode;
    private String orderStatus;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal deliveryCharge;
    private String shippingPartner;
    private String trackingNumber;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryAt;
    private ShippingAddress shippingAddress;
    private List<Item> items;

    @Data
    @Builder
    public static class ShippingAddress {
        private String recipientName;
        private String phoneNumber;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String country;
        private String landmark;
        private String deliveryInstructions;
    }

    @Data
    @Builder
    public static class Item {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}