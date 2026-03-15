package com.SHIVA.puja.service;

import com.SHIVA.puja.dto.PaymentInitiateRequest;
import com.SHIVA.puja.dto.PaymentInitiateResponse;
import com.SHIVA.puja.dto.RazorpayWebhookResponse;

public interface PaymentGatewayService {

    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request);

    RazorpayWebhookResponse handleRazorpayWebhook(String payload, String signatureHeader);
}
