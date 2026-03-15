package com.SHIVA.puja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.CouponCampaign;

public interface CouponCampaignRepository extends JpaRepository<CouponCampaign, Long> {

    List<CouponCampaign> findTop10BySellerIdOrderByStartDateDesc(Long sellerId);

    List<CouponCampaign> findBySellerId(Long sellerId);
}
