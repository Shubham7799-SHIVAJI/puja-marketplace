package com.SHIVA.puja.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.CustomerOrderLink;

public interface CustomerOrderLinkRepository extends JpaRepository<CustomerOrderLink, Long> {

    List<CustomerOrderLink> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<CustomerOrderLink> findByOrderIdIn(Collection<Long> orderIds);

    Optional<CustomerOrderLink> findByUserIdAndOrderId(Long userId, Long orderId);

    Optional<CustomerOrderLink> findByOrderId(Long orderId);
}