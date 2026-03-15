package com.SHIVA.puja.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginChallengeResponse {

    private boolean mfaRequired;
    private String challengeToken;
    private Long expiresInMinutes;
    private String message;
}
