package com.SHIVA.puja.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.OrderPayment;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    Optional<OrderPayment> findByOrderId(Long orderId);

    Optional<OrderPayment> findByGatewayReference(String gatewayReference);

    List<OrderPayment> findByOrderIdIn(Collection<Long> orderIds);
}