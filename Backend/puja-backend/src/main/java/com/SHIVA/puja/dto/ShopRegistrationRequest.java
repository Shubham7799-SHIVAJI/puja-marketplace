package com.SHIVA.puja.dto;

import lombok.Data;

@Data
public class ShopRegistrationRequest {

    private String registrationId;
    private Integer currentStep;
    private String ownerFullName;
    private String email;
    private String phoneNumber;
    private String password;
    private String confirmPassword;
    private String emailOtp;
    private String phoneOtp;
    private String profilePhoto;
    private String shopName;
    private String shopUniqueId;
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
    private String confirmAccountNumber;
    private String ifscCode;
    private String upiId;
    private String cancelledChequePhoto;
    private Boolean acceptTermsAndConditions;
    private Boolean acceptPrivacyPolicy;
    private Boolean acceptCommissionPolicy;
}