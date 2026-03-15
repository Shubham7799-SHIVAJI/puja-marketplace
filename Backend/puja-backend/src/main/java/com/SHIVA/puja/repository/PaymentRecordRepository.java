package com.SHIVA.puja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.PaymentRecord;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findTop10BySellerIdOrderByPaymentDateDesc(Long sellerId);

    List<PaymentRecord> findBySellerId(Long sellerId);
}
