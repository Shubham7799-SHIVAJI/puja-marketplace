package com.SHIVA.puja.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopRegistrationResponse {

    private String registrationId;
    private String shopUniqueId;
    private String status;
    private Integer currentStep;
    private String ownerFullName;
    private String email;
    private String phoneNumber;
    private String emailOtp;
    private Boolean emailOtpVerified;
    private String phoneOtp;
    private Boolean phoneOtpVerified;
    private String profilePhoto;
    private String shopName;
    private String shopCategory;
    private String shopDescription;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String landmark;
    private Double latitude;
    private Double longitude;
    private String shopPhoneNumber;
    private String shopEmail;
    private String whatsappNumber;
    private String ownerAadharNumber;
    private String ownerPanNumber;
    private String ownerAadharPhoto;
    private String ownerPanPhoto;
    private String ownerSelfieWithId;
    private String gstNumber;
    private String gstCertificateUpload;
    private String businessRegistrationNumber;
    private String accountHolderName;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String upiId;
    private String cancelledChequePhoto;
    private Boolean acceptTermsAndConditions;
    private Boolean acceptPrivacyPolicy;
    private Boolean acceptCommissionPolicy;
    private LocalDateTime lastSavedAt;
    private LocalDateTime submittedAt;
}