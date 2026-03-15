package com.SHIVA.puja.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginChallengeRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String contact;

    @NotBlank(message = "Password is required")
    private String password;
}
