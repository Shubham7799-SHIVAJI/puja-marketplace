package com.SHIVA.puja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.CustomerNotification;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {

    List<CustomerNotification> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}