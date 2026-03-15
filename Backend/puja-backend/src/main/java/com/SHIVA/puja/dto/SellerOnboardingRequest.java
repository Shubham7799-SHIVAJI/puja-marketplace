package com.SHIVA.puja.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SellerOnboardingRequest {

    @NotBlank(message = "Registration ID is required.")
    private String registrationId;

    private String sellerEmail;

    private String temporaryPassword;

    private Boolean activateSellerUser;
}
