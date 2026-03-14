package com.SHIVA.puja.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.OtpPurpose;
import com.SHIVA.puja.entity.OtpRequest;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {

    Optional<OtpRequest> findTopByEmailAndPurposeAndVerifiedFalseOrderByIdDesc(String email, OtpPurpose purpose);
}