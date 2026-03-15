package com.SHIVA.puja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.ReviewEntry;

public interface ReviewEntryRepository extends JpaRepository<ReviewEntry, Long> {

    List<ReviewEntry> findTop10BySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<ReviewEntry> findBySellerId(Long sellerId);

    List<ReviewEntry> findByProductIdOrderByCreatedAtDesc(Long productId);
}
