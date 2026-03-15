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
@Table(name = "coupons")
@Data
public class CouponCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "campaign_name", length = 160)
    private String campaignName;

    @Column(name = "discount_type", nullable = false, length = 40)
    private String discountType;

    @Column(name = "discount_value", nullable = false, length = 40)
    private String discountValue;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Column(name = "status", nullable = false, length = 40)
    private String status;
}
