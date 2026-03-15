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
@Table(name = "reviews")
@Data
public class ReviewEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "customer_name", nullable = false, length = 160)
    private String customerName;

    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "review_text")
    private String reviewText;

    @Column(name = "reply_text")
    private String replyText;

    @Column(name = "abusive", nullable = false)
    private Boolean abusive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
