package com.SHIVA.puja.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findTop5BySellerIdOrderByAvailableStockAsc(Long sellerId);

    List<InventoryItem> findBySellerId(Long sellerId);

    Optional<InventoryItem> findBySellerIdAndProductId(Long sellerId, Long productId);
}
