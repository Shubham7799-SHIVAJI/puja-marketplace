package com.SHIVA.puja.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.OrderShippingDetail;

public interface OrderShippingDetailRepository extends JpaRepository<OrderShippingDetail, Long> {

    Optional<OrderShippingDetail> findByOrderId(Long orderId);

    List<OrderShippingDetail> findByOrderIdIn(Collection<Long> orderIds);
}