package com.SHIVA.puja.service;

import com.SHIVA.puja.dto.LoginRequest;
import com.SHIVA.puja.dto.ResendOtpRequest;
import com.SHIVA.puja.dto.SetPasswordRequest;
import com.SHIVA.puja.dto.SignInRequest;
import com.SHIVA.puja.dto.VerifyOtpRequest;
import com.SHIVA.puja.dto.AuthTokenResponse;

public interface UserService {

    void loginUser(LoginRequest request);

    void resendEmailOtp(ResendOtpRequest request);

    String verifyEmailOtp(VerifyOtpRequest request);

    void setPassword(SetPasswordRequest request);

    AuthTokenResponse signIn(SignInRequest request);

    AuthTokenResponse refreshAccessToken(String refreshToken);

    void revokeRefreshToken(String refreshToken);

    void blacklistAccessToken(String token);

}