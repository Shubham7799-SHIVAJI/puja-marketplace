package com.SHIVA.puja.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.Seller;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByRegistrationId(String registrationId);

    Optional<Seller> findBySellerCode(String sellerCode);

    Optional<Seller> findTopByEmailOrderByIdDesc(String email);
}
