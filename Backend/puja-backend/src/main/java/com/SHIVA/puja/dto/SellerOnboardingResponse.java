package com.SHIVA.puja.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerOnboardingResponse {

    private String sellerCode;
    private String registrationId;
    private String shopName;
    private String sellerEmail;
    private String role;
    private String status;
    private String message;
}
