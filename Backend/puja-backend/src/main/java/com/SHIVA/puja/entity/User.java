package com.SHIVA.puja.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String role;

    private String status;

    @Column(name = "phone_verified")
    private Boolean phoneVerified;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "admin_two_factor_enabled")
    private Boolean adminTwoFactorEnabled;

    @Column(name = "admin_allowed_ips", length = 512)
    private String adminAllowedIps;
}