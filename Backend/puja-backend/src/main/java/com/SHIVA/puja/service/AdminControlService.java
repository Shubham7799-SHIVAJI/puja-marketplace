package com.SHIVA.puja.service;

import com.SHIVA.puja.dto.AdminStatusUpdateRequest;

public interface AdminControlService {

    void updateProductStatus(Long productId, AdminStatusUpdateRequest request);

    void updateOrderStatus(Long orderId, AdminStatusUpdateRequest request);

    void moderateReview(Long reviewId, AdminStatusUpdateRequest request);

    void updateSellerStatus(Long sellerId, AdminStatusUpdateRequest request);
}