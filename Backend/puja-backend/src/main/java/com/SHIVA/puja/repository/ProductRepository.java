package com.SHIVA.puja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    long countBySellerId(Long sellerId);

    List<Product> findTop5BySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Product> findTop5BySellerIdOrderByReviewCountDesc(Long sellerId);

    List<Product> findBySellerId(Long sellerId);
}
