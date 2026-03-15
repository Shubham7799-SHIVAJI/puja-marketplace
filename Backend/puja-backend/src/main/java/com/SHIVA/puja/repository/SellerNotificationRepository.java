package com.SHIVA.puja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.SellerNotification;

public interface SellerNotificationRepository extends JpaRepository<SellerNotification, Long> {

    List<SellerNotification> findTop8BySellerIdOrderByCreatedAtDesc(Long sellerId);
}
