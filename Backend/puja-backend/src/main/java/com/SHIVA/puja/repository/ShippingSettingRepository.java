package com.SHIVA.puja.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.ShippingSetting;

public interface ShippingSettingRepository extends JpaRepository<ShippingSetting, Long> {

    Optional<ShippingSetting> findBySellerId(Long sellerId);
}
