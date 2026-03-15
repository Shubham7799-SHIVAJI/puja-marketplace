package com.SHIVA.puja.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "shipping_settings")
@Data
public class ShippingSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false, unique = true)
    private Long sellerId;

    @Column(name = "shipping_partners", nullable = false, length = 255)
    private String shippingPartners;

    @Column(name = "shipping_charges", nullable = false, length = 120)
    private String shippingCharges;

    @Column(name = "delivery_regions", nullable = false, length = 255)
    private String deliveryRegions;

    @Column(name = "free_shipping_threshold", nullable = false, length = 80)
    private String freeShippingThreshold;

    @Column(name = "estimated_delivery_times", nullable = false, length = 120)
    private String estimatedDeliveryTimes;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
