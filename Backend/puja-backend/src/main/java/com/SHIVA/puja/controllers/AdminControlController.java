package com.SHIVA.puja.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SHIVA.puja.dto.AdminStatusUpdateRequest;
import com.SHIVA.puja.service.AdminControlService;

@RestController
@RequestMapping("/admin-control")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('ADMIN')")
public class AdminControlController {

    private final AdminControlService adminControlService;

    public AdminControlController(AdminControlService adminControlService) {
        this.adminControlService = adminControlService;
    }

    @PutMapping("/products/{productId}/status")
    public void updateProductStatus(@PathVariable Long productId, @RequestBody AdminStatusUpdateRequest request) {
        adminControlService.updateProductStatus(productId, request);
    }

    @PutMapping("/orders/{orderId}/status")
    public void updateOrderStatus(@PathVariable Long orderId, @RequestBody AdminStatusUpdateRequest request) {
        adminControlService.updateOrderStatus(orderId, request);
    }

    @PutMapping("/reviews/{reviewId}/moderation")
    public void moderateReview(@PathVariable Long reviewId, @RequestBody AdminStatusUpdateRequest request) {
        adminControlService.moderateReview(reviewId, request);
    }

    @PutMapping("/sellers/{sellerId}/status")
    public void updateSellerStatus(@PathVariable Long sellerId, @RequestBody AdminStatusUpdateRequest request) {
        adminControlService.updateSellerStatus(sellerId, request);
    }
}