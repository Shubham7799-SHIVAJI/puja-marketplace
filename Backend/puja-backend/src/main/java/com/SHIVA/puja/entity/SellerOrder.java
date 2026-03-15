package com.SHIVA.puja.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class SellerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "order_code", nullable = false, unique = true, length = 64)
    private String orderCode;

    @Column(name = "order_status", nullable = false, length = 40)
    private String orderStatus;

    @Column(name = "payment_method", nullable = false, length = 40)
    private String paymentMethod;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_partner", length = 80)
    private String shippingPartner;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "primary_product_name", length = 180)
    private String primaryProductName;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
}
