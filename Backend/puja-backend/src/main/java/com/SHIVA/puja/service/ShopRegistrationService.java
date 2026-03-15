package com.SHIVA.puja.service;

import org.springframework.web.multipart.MultipartFile;

import com.SHIVA.puja.dto.ShopFileUploadResponse;
import com.SHIVA.puja.dto.ShopOtpResponse;
import com.SHIVA.puja.dto.ShopPhoneVerificationConfirmRequest;
import com.SHIVA.puja.dto.ShopOtpSendRequest;
import com.SHIVA.puja.dto.ShopOtpVerifyRequest;
import com.SHIVA.puja.dto.ShopRegistrationRequest;
import com.SHIVA.puja.dto.ShopRegistrationResponse;

public interface ShopRegistrationService {

    ShopRegistrationResponse saveDraft(ShopRegistrationRequest request);

    ShopRegistrationResponse getDraft(String registrationId);

    ShopRegistrationResponse submit(ShopRegistrationRequest request);

    ShopOtpResponse sendOtp(ShopOtpSendRequest request);

    ShopOtpResponse verifyOtp(ShopOtpVerifyRequest request);

    ShopOtpResponse confirmPhoneVerification(ShopPhoneVerificationConfirmRequest request);

    ShopFileUploadResponse uploadFile(String registrationId, String fieldName, MultipartFile file, String registrationSessionToken);
}