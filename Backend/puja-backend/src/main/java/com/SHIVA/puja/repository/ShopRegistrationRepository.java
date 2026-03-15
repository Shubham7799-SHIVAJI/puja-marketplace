package com.SHIVA.puja.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.ShopRegistration;

public interface ShopRegistrationRepository extends JpaRepository<ShopRegistration, Long> {

    Optional<ShopRegistration> findByRegistrationId(String registrationId);
}