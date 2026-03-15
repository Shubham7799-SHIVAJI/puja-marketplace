package com.SHIVA.puja.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.SHIVA.puja.dto.AdminStatusUpdateRequest;
import com.SHIVA.puja.service.AdminControlService;

@RestController
@RequestMapping({"/admin-control", "/api/v1/admin-control", "/api/admin/control"})
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
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

    @PutMapping("/sellers/{sellerId}/commission")
    public void updateSellerCommission(@PathVariable Long sellerId, @RequestBody AdminStatusUpdateRequest request) {
        adminControlService.updateSellerCommission(sellerId, request);
    }

    @PutMapping("/users/{userId}/status")
    public void updateUserStatus(@PathVariable Long userId, @RequestBody AdminStatusUpdateRequest request) {
        adminControlService.updateUserStatus(userId, request);
    }

    @PostMapping("/users/{userId}/reset-password")
    public Map<String, String> resetUserPassword(@PathVariable Long userId) {
        String temporaryPassword = adminControlService.resetUserPassword(userId);
        return Map.of("temporaryPassword", temporaryPassword, "message", "Temporary password generated");
    }
}