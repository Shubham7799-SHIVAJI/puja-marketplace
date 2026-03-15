package com.SHIVA.puja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.CustomerProfile;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    long countBySellerId(Long sellerId);

    List<CustomerProfile> findTop10BySellerIdOrderByTotalPurchasesDesc(Long sellerId);

    java.util.Optional<CustomerProfile> findBySellerIdAndEmailIgnoreCase(Long sellerId, String email);
}
