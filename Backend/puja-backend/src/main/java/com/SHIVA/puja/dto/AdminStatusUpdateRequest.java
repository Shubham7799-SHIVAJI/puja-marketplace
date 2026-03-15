package com.SHIVA.puja.dto;

import lombok.Data;

@Data
public class AdminStatusUpdateRequest {

    private String status;
    private String reason;
    private String trackingNumber;
    private String shippingPartner;
    private String replyText;
    private Boolean abusive;
    private Double commissionRate;
}