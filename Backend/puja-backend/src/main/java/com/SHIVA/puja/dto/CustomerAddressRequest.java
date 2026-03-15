package com.SHIVA.puja.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerAddressRequest {

    @NotBlank(message = "Recipient name is required.")
    private String fullName;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

    @NotBlank(message = "Address line 1 is required.")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "State is required.")
    private String state;

    @NotBlank(message = "Pincode is required.")
    private String pincode;

    @NotBlank(message = "Country is required.")
    private String country;

    private String landmark;

    private String deliveryInstructions;

    private Boolean defaultAddress;
}