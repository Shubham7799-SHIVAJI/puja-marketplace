package com.SHIVA.puja.service;

import com.SHIVA.puja.dto.AdminLoginChallengeRequest;
import com.SHIVA.puja.dto.AdminLoginChallengeResponse;
import com.SHIVA.puja.dto.AdminLoginVerifyRequest;
import com.SHIVA.puja.dto.AuthTokenResponse;

public interface AdminAuthService {

    AdminLoginChallengeResponse startChallenge(AdminLoginChallengeRequest request, String clientIp);

    AuthTokenResponse verifyChallenge(AdminLoginVerifyRequest request, String clientIp);
}
