package com.SHIVA.puja.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;

import com.SHIVA.puja.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    long countBySellerId(Long sellerId);

    List<Product> findTop5BySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Product> findTop5BySellerIdOrderByReviewCountDesc(Long sellerId);

    List<Product> findBySellerId(Long sellerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId")
    Optional<Product> findByIdForUpdate(Long productId);
}
