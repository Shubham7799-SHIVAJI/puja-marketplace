package com.SHIVA.puja.service;

import com.SHIVA.puja.dto.LoginRequest;
import com.SHIVA.puja.dto.ResendOtpRequest;
import com.SHIVA.puja.dto.SetPasswordRequest;
import com.SHIVA.puja.dto.SignInRequest;
import com.SHIVA.puja.dto.VerifyOtpRequest;

public interface UserService {

    void loginUser(LoginRequest request);

    void resendEmailOtp(ResendOtpRequest request);

    void verifyEmailOtp(VerifyOtpRequest request);

    void setPassword(SetPasswordRequest request);

    void signIn(SignInRequest request);

}