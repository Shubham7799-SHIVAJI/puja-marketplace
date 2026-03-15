package com.SHIVA.puja.dto;

import lombok.Data;

@Data
public class ShopPhoneVerificationConfirmRequest {

    private String registrationId;
    private String phoneNumber;
    private String provider;
    private String providerUid;
    private String idToken;
}