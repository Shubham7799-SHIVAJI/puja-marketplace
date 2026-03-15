package com.SHIVA.puja.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SHIVA.puja.dto.SellerWorkspaceResponse;
import com.SHIVA.puja.service.SellerWorkspaceService;

@RestController
@RequestMapping({"/seller-dashboard", "/api/v1/seller-dashboard"})
@CrossOrigin(origins = "http://localhost:4200")
public class SellerWorkspaceController {

    private final SellerWorkspaceService sellerWorkspaceService;

    public SellerWorkspaceController(SellerWorkspaceService sellerWorkspaceService) {
        this.sellerWorkspaceService = sellerWorkspaceService;
    }

    @GetMapping("/workspace")
    public SellerWorkspaceResponse getWorkspace(@RequestParam(required = false) String registrationId) {
        return sellerWorkspaceService.getWorkspace(registrationId);
    }

    @GetMapping("/workspace/{registrationId}")
    public SellerWorkspaceResponse getWorkspaceByRegistration(@PathVariable String registrationId) {
        return sellerWorkspaceService.getWorkspace(registrationId);
    }
}
