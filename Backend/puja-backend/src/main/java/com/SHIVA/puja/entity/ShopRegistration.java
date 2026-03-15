package com.SHIVA.puja.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "shop_registrations")
@Data
public class ShopRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_id", nullable = false, unique = true, length = 64)
    private String registrationId;

    @Column(name = "shop_unique_id", nullable = false, unique = true, length = 64)
    private String shopUniqueId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "current_step")
    private Integer currentStep;

    @Column(name = "owner_full_name", length = 120)
    private String ownerFullName;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "email_otp", length = 12)
    private String emailOtp;

    @Column(name = "email_otp_verified")
    private Boolean emailOtpVerified;

    @Column(name = "phone_otp", length = 12)
    private String phoneOtp;

    @Column(name = "phone_otp_verified")
    private Boolean phoneOtpVerified;

    @Column(name = "profile_photo", length = 255)
    private String profilePhoto;

    @Column(name = "shop_name", length = 180)
    private String shopName;

    @Column(name = "shop_category", length = 100)
    private String shopCategory;

    @Lob
    @Column(name = "shop_description")
    private String shopDescription;

    @Column(name = "address_line_1", length = 200)
    private String addressLine1;

    @Column(name = "address_line_2", length = 200)
    private String addressLine2;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "state", length = 120)
    private String state;

    @Column(name = "pincode", length = 12)
    private String pincode;

    @Column(name = "country", length = 120)
    private String country;

    @Column(name = "landmark", length = 200)
    private String landmark;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "shop_phone_number", length = 20)
    private String shopPhoneNumber;

    @Column(name = "shop_email", length = 160)
    private String shopEmail;

    @Column(name = "whatsapp_number", length = 20)
    private String whatsappNumber;

    @Column(name = "owner_aadhar_number", length = 20)
    private String ownerAadharNumber;

    @Column(name = "owner_pan_number", length = 20)
    private String ownerPanNumber;

    @Column(name = "owner_aadhar_photo", length = 255)
    private String ownerAadharPhoto;

    @Column(name = "owner_pan_photo", length = 255)
    private String ownerPanPhoto;

    @Column(name = "owner_selfie_with_id", length = 255)
    private String ownerSelfieWithId;

    @Column(name = "gst_number", length = 30)
    private String gstNumber;

    @Column(name = "gst_certificate_upload", length = 255)
    private String gstCertificateUpload;

    @Column(name = "business_registration_number", length = 100)
    private String businessRegistrationNumber;

    @Column(name = "account_holder_name", length = 120)
    private String accountHolderName;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "account_number", length = 40)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "upi_id", length = 120)
    private String upiId;

    @Column(name = "cancelled_cheque_photo", length = 255)
    private String cancelledChequePhoto;

    @Column(name = "accept_terms_and_conditions")
    private Boolean acceptTermsAndConditions;

    @Column(name = "accept_privacy_policy")
    private Boolean acceptPrivacyPolicy;

    @Column(name = "accept_commission_policy")
    private Boolean acceptCommissionPolicy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}