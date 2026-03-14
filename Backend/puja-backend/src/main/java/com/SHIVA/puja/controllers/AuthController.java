package com.SHIVA.puja.controllers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import com.SHIVA.puja.dto.LoginRequest;
import com.SHIVA.puja.dto.ResendOtpRequest;
import com.SHIVA.puja.dto.SetPasswordRequest;
import com.SHIVA.puja.dto.SignInRequest;
import com.SHIVA.puja.dto.VerifyOtpRequest;
import com.SHIVA.puja.service.UserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {

        userService.loginUser(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User saved successfully");
        response.put("otpSent", "true");

        return response;
    }

    @PostMapping("/resend-otp")
    public Map<String, String> resendOtp(@Valid @RequestBody ResendOtpRequest request) {

        userService.resendEmailOtp(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP resent successfully");

        return response;
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {

        userService.verifyEmailOtp(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP verified successfully");

        return response;
    }

    @PostMapping("/set-password")
    public Map<String, String> setPassword(@Valid @RequestBody SetPasswordRequest request) {

        userService.setPassword(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password set successfully");

        return response;
    }

    @PostMapping("/signin")
    public Map<String, String> signIn(@Valid @RequestBody SignInRequest request) {

        userService.signIn(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Sign-in successful");

        return response;
    }

}