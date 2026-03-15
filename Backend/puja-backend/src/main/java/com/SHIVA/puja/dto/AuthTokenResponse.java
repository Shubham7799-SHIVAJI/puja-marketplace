package com.SHIVA.puja.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthTokenResponse {

    private String token;
    private String refreshToken;
    private String email;
    private String role;
    private Long expiresInMinutes;
    private Long refreshExpiresInDays;
    private String message;
}
