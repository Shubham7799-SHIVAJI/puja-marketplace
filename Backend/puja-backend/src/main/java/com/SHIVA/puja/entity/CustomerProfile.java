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
@Table(name = "customers")
@Data
public class CustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Column(name = "total_purchases", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPurchases;

    @Column(name = "last_order_at")
    private LocalDateTime lastOrderAt;
}
