package com.SHIVA.puja.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SHIVA.puja.dto.SellerOnboardingRequest;
import com.SHIVA.puja.dto.SellerOnboardingResponse;
import com.SHIVA.puja.service.SellerAdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/seller-admin", "/api/v1/seller-admin"})
@CrossOrigin(origins = "http://localhost:4200")
public class SellerAdminController {

    private final SellerAdminService sellerAdminService;

    public SellerAdminController(SellerAdminService sellerAdminService) {
        this.sellerAdminService = sellerAdminService;
    }

    @PostMapping("/onboard")
    @PreAuthorize("hasRole('ADMIN')")
    public SellerOnboardingResponse onboardSeller(@Valid @RequestBody SellerOnboardingRequest request) {
        return sellerAdminService.onboardSeller(request);
    }
}
