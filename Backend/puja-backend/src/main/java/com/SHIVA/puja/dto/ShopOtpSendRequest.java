package com.SHIVA.puja.dto;

import lombok.Data;

@Data
public class ShopOtpSendRequest {

    private String registrationId;
    private String channel;
    private String contact;
    private String ownerFullName;
}