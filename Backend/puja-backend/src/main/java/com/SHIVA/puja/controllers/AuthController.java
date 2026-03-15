package com.SHIVA.puja.controllers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import com.SHIVA.puja.dto.LoginRequest;
import com.SHIVA.puja.dto.AdminLoginChallengeRequest;
import com.SHIVA.puja.dto.AdminLoginChallengeResponse;
import com.SHIVA.puja.dto.AdminLoginVerifyRequest;
import com.SHIVA.puja.dto.RefreshTokenRequest;
import com.SHIVA.puja.dto.ResendOtpRequest;
import com.SHIVA.puja.dto.SetPasswordRequest;
import com.SHIVA.puja.dto.SignInRequest;
import com.SHIVA.puja.dto.VerifyOtpRequest;
import com.SHIVA.puja.dto.AuthTokenResponse;
import com.SHIVA.puja.service.AdminAuthService;
import com.SHIVA.puja.service.UserService;

@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserService userService;
    private final AdminAuthService adminAuthService;

    public AuthController(UserService userService, AdminAuthService adminAuthService) {
        this.userService = userService;
        this.adminAuthService = adminAuthService;
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
        String resetToken = userService.verifyEmailOtp(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP verified successfully");
        response.put("resetToken", resetToken);

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
    public AuthTokenResponse signIn(@Valid @RequestBody SignInRequest request) {
        return userService.signIn(request);
    }

    @PostMapping("/admin/challenge")
    public AdminLoginChallengeResponse startAdminChallenge(
            @Valid @RequestBody AdminLoginChallengeRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "X-Real-IP", required = false) String realIp,
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        String clientIp = forwardedFor != null && !forwardedFor.isBlank()
                ? forwardedFor.split(",")[0].trim()
                : (realIp != null && !realIp.isBlank() ? realIp.trim() : servletRequest.getRemoteAddr());
        return adminAuthService.startChallenge(request, clientIp);
    }

    @PostMapping("/admin/verify-otp")
    public AuthTokenResponse verifyAdminOtp(
            @Valid @RequestBody AdminLoginVerifyRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "X-Real-IP", required = false) String realIp,
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        String clientIp = forwardedFor != null && !forwardedFor.isBlank()
                ? forwardedFor.split(",")[0].trim()
                : (realIp != null && !realIp.isBlank() ? realIp.trim() : servletRequest.getRemoteAddr());
        return adminAuthService.verifyChallenge(request, clientIp);
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return userService.refreshAccessToken(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        userService.revokeRefreshToken(request.getRefreshToken());
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            userService.blacklistAccessToken(authorizationHeader.substring(7));
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Signed out successfully");
        return response;
    }

}