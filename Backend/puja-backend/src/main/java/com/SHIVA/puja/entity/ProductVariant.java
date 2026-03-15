package com.SHIVA.puja.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "product_variants")
@Data
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_name", nullable = false, length = 80)
    private String variantName;

    @Column(name = "variant_value", nullable = false, length = 120)
    private String variantValue;

    @Column(name = "sku_suffix", length = 40)
    private String skuSuffix;

    @Column(name = "additional_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal additionalPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
}
