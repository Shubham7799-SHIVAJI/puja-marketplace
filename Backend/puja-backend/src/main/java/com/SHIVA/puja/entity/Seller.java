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
@Table(name = "sellers")
@Data
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_id", length = 64)
    private String registrationId;

    @Column(name = "seller_code", nullable = false, unique = true, length = 64)
    private String sellerCode;

    @Column(name = "shop_name", nullable = false, length = 180)
    private String shopName;

    @Column(name = "owner_name", length = 120)
    private String ownerName;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "gst_number", length = 32)
    private String gstNumber;

    @Column(name = "shop_address", length = 255)
    private String shopAddress;

    @Column(name = "shop_logo", length = 255)
    private String shopLogo;

    @Column(name = "shop_banner", length = 255)
    private String shopBanner;

    @Column(name = "return_policy")
    private String returnPolicy;

    @Column(name = "bank_account_masked", length = 64)
    private String bankAccountMasked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
