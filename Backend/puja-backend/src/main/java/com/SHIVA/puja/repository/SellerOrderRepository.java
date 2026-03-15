package com.SHIVA.puja.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.SellerOrder;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, Long> {

    long countBySellerId(Long sellerId);

    long countBySellerIdAndOrderStatusIn(Long sellerId, Collection<String> orderStatuses);

    List<SellerOrder> findTop10BySellerIdOrderByOrderDateDesc(Long sellerId);

    List<SellerOrder> findBySellerId(Long sellerId);
}
