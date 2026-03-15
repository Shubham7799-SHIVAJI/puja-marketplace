package com.SHIVA.puja.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.SHIVA.puja.dto.PaymentInitiateRequest;
import com.SHIVA.puja.dto.PaymentInitiateResponse;
import com.SHIVA.puja.dto.RazorpayWebhookResponse;
import com.SHIVA.puja.service.PaymentGatewayService;

import jakarta.validation.Valid;

@RestController
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    public PaymentController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping({"/payments/initiate", "/api/v1/payments/initiate"})
    public PaymentInitiateResponse initiatePayment(@Valid @RequestBody PaymentInitiateRequest request) {
        return paymentGatewayService.initiatePayment(request);
    }

    @PostMapping({"/webhooks/razorpay", "/api/v1/webhooks/razorpay"})
    @ResponseStatus(HttpStatus.OK)
    public RazorpayWebhookResponse handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signatureHeader) {
        return paymentGatewayService.handleRazorpayWebhook(payload, signatureHeader);
    }
}
