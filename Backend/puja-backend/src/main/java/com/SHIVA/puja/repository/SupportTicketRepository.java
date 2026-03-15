package com.SHIVA.puja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.SupportTicket;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findTop10BySellerIdOrderByUpdatedAtDesc(Long sellerId);

    List<SupportTicket> findBySellerId(Long sellerId);
}
