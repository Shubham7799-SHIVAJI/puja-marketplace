package com.SHIVA.puja.service;

import com.SHIVA.puja.dto.CouponRequest;
import com.SHIVA.puja.dto.CouponResponse;
import com.SHIVA.puja.dto.InventoryBulkUpdateRequest;
import com.SHIVA.puja.dto.InventoryRequest;
import com.SHIVA.puja.dto.InventoryResponse;
import com.SHIVA.puja.dto.OrderRequest;
import com.SHIVA.puja.dto.OrderResponse;
import com.SHIVA.puja.dto.PageResponse;
import com.SHIVA.puja.dto.ProductResponse;
import com.SHIVA.puja.dto.ProductUpsertRequest;
import com.SHIVA.puja.dto.ReviewRequest;
import com.SHIVA.puja.dto.ReviewResponse;
import com.SHIVA.puja.dto.SupportTicketRequest;
import com.SHIVA.puja.dto.SupportTicketResponse;

public interface SellerCrudService {

    PageResponse<ProductResponse> listProducts(String sellerCode, int page, int size, String query, String status, String category);

    ProductResponse getProduct(String sellerCode, Long productId);

    ProductResponse createProduct(String sellerCode, ProductUpsertRequest request);

    ProductResponse updateProduct(String sellerCode, Long productId, ProductUpsertRequest request);

    void deleteProduct(String sellerCode, Long productId);

    PageResponse<InventoryResponse> listInventory(String sellerCode, int page, int size, boolean lowStockOnly);

    InventoryResponse createInventory(String sellerCode, InventoryRequest request);

    InventoryResponse updateInventory(String sellerCode, Long inventoryId, InventoryRequest request);

    PageResponse<InventoryResponse> bulkUpdateInventory(String sellerCode, InventoryBulkUpdateRequest request);

    void deleteInventory(String sellerCode, Long inventoryId);

    PageResponse<OrderResponse> listOrders(String sellerCode, int page, int size, String status);

    OrderResponse getOrder(String sellerCode, Long orderId);

    OrderResponse createOrder(String sellerCode, OrderRequest request);

    OrderResponse updateOrder(String sellerCode, Long orderId, OrderRequest request);

    void deleteOrder(String sellerCode, Long orderId);

    PageResponse<CouponResponse> listCoupons(String sellerCode, int page, int size, String status);

    CouponResponse createCoupon(String sellerCode, CouponRequest request);

    CouponResponse updateCoupon(String sellerCode, Long couponId, CouponRequest request);

    void deleteCoupon(String sellerCode, Long couponId);

    PageResponse<ReviewResponse> listReviews(String sellerCode, int page, int size, Boolean abusive);

    ReviewResponse createReview(String sellerCode, ReviewRequest request);

    ReviewResponse updateReview(String sellerCode, Long reviewId, ReviewRequest request);

    void deleteReview(String sellerCode, Long reviewId);

    PageResponse<SupportTicketResponse> listSupportTickets(String sellerCode, int page, int size, String status);

    SupportTicketResponse createSupportTicket(String sellerCode, SupportTicketRequest request);

    SupportTicketResponse updateSupportTicket(String sellerCode, Long ticketId, SupportTicketRequest request);

    void deleteSupportTicket(String sellerCode, Long ticketId);
}
