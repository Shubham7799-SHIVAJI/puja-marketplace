package com.SHIVA.puja.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;

import com.SHIVA.puja.entity.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findTop5BySellerIdOrderByAvailableStockAsc(Long sellerId);

    List<InventoryItem> findBySellerId(Long sellerId);

    Optional<InventoryItem> findBySellerIdAndProductId(Long sellerId, Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryItem i where i.sellerId = :sellerId and i.productId = :productId")
    Optional<InventoryItem> findBySellerIdAndProductIdForUpdate(Long sellerId, Long productId);
}
