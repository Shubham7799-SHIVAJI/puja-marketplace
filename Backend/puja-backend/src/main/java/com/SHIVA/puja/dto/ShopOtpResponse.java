package com.SHIVA.puja.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopOtpResponse {

    private String registrationId;
    private String channel;
    private String contact;
    private boolean verified;
    private String message;
    private String previewOtp;
}