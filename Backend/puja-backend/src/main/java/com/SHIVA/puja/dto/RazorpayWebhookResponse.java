package com.SHIVA.puja.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RazorpayWebhookResponse {

    private boolean accepted;
    private String event;
    private String message;
}
