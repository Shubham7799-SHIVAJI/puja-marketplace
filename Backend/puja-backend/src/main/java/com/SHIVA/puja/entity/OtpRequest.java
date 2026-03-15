package com.SHIVA.puja.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "otp_requests")
@Data
public class OtpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "otp_hash")
    private String otpHash;

    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private Boolean verified;

    private Integer attempts;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}