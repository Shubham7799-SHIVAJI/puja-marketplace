package com.SHIVA.puja.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.SHIVA.puja.dto.ShopFileUploadResponse;
import com.SHIVA.puja.dto.ShopOtpResponse;
import com.SHIVA.puja.dto.ShopPhoneVerificationConfirmRequest;
import com.SHIVA.puja.dto.ShopOtpSendRequest;
import com.SHIVA.puja.dto.ShopOtpVerifyRequest;
import com.SHIVA.puja.dto.ShopRegistrationRequest;
import com.SHIVA.puja.dto.ShopRegistrationResponse;
import com.SHIVA.puja.service.ShopRegistrationService;

@RestController
@RequestMapping({"/shop-registration", "/api/v1/shop-registration"})
@CrossOrigin(origins = "http://localhost:4200")
public class ShopRegistrationController {

    private final ShopRegistrationService shopRegistrationService;

    public ShopRegistrationController(ShopRegistrationService shopRegistrationService) {
        this.shopRegistrationService = shopRegistrationService;
    }

    @PostMapping("/draft")
    @ResponseStatus(HttpStatus.OK)
    public ShopRegistrationResponse saveDraft(@RequestBody ShopRegistrationRequest request) {
        return shopRegistrationService.saveDraft(request);
    }

    @GetMapping("/draft/{registrationId}")
    public ShopRegistrationResponse getDraft(@PathVariable String registrationId) {
        return shopRegistrationService.getDraft(registrationId);
    }

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.OK)
    public ShopRegistrationResponse submit(@RequestBody ShopRegistrationRequest request) {
        return shopRegistrationService.submit(request);
    }

    @PostMapping("/otp/send")
    @ResponseStatus(HttpStatus.OK)
    public ShopOtpResponse sendOtp(@RequestBody ShopOtpSendRequest request) {
        return shopRegistrationService.sendOtp(request);
    }

    @PostMapping("/otp/verify")
    @ResponseStatus(HttpStatus.OK)
    public ShopOtpResponse verifyOtp(@RequestBody ShopOtpVerifyRequest request) {
        return shopRegistrationService.verifyOtp(request);
    }

    @PostMapping("/otp/phone/firebase/confirm")
    @ResponseStatus(HttpStatus.OK)
    public ShopOtpResponse confirmPhoneVerification(@RequestBody ShopPhoneVerificationConfirmRequest request) {
        return shopRegistrationService.confirmPhoneVerification(request);
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.OK)
    public ShopFileUploadResponse uploadFile(
            @RequestParam(required = false) String registrationId,
            @RequestParam String fieldName,
            @RequestHeader("X-Registration-Session") String registrationSessionToken,
            @RequestParam("file") MultipartFile file) {
        return shopRegistrationService.uploadFile(registrationId, fieldName, file, registrationSessionToken);
    }
}