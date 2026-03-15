package com.SHIVA.puja.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import com.SHIVA.puja.service.SellerCrudService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/seller-api")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyRole('SELLER','ADMIN')")
public class SellerOperationsController {

    private final SellerCrudService sellerCrudService;

    public SellerOperationsController(SellerCrudService sellerCrudService) {
        this.sellerCrudService = sellerCrudService;
    }

    @GetMapping("/products")
    public PageResponse<ProductResponse> listProducts(@RequestParam(required = false) String sellerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return sellerCrudService.listProducts(sellerCode, page, size, query, status, category);
    }

    @GetMapping("/products/{productId}")
    public ProductResponse getProduct(@RequestParam(required = false) String sellerCode, @PathVariable Long productId) {
        return sellerCrudService.getProduct(sellerCode, productId);
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestParam(required = false) String sellerCode,
            @Valid @RequestBody ProductUpsertRequest request) {
        return sellerCrudService.createProduct(sellerCode, request);
    }

    @PutMapping("/products/{productId}")
    public ProductResponse updateProduct(@RequestParam(required = false) String sellerCode, @PathVariable Long productId,
            @Valid @RequestBody ProductUpsertRequest request) {
        return sellerCrudService.updateProduct(sellerCode, productId, request);
    }

    @DeleteMapping("/products/{productId}")
    public void deleteProduct(@RequestParam(required = false) String sellerCode, @PathVariable Long productId) {
        sellerCrudService.deleteProduct(sellerCode, productId);
    }

    @GetMapping("/inventory")
    public PageResponse<InventoryResponse> listInventory(@RequestParam(required = false) String sellerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return sellerCrudService.listInventory(sellerCode, page, size, lowStockOnly);
    }

    @PostMapping("/inventory")
    public InventoryResponse createInventory(@RequestParam(required = false) String sellerCode,
            @Valid @RequestBody InventoryRequest request) {
        return sellerCrudService.createInventory(sellerCode, request);
    }

    @PutMapping("/inventory/{inventoryId}")
    public InventoryResponse updateInventory(@RequestParam(required = false) String sellerCode, @PathVariable Long inventoryId,
            @Valid @RequestBody InventoryRequest request) {
        return sellerCrudService.updateInventory(sellerCode, inventoryId, request);
    }

    @PostMapping("/inventory/bulk-update")
    public PageResponse<InventoryResponse> bulkUpdateInventory(@RequestParam(required = false) String sellerCode,
            @Valid @RequestBody InventoryBulkUpdateRequest request) {
        return sellerCrudService.bulkUpdateInventory(sellerCode, request);
    }

    @DeleteMapping("/inventory/{inventoryId}")
    public void deleteInventory(@RequestParam(required = false) String sellerCode, @PathVariable Long inventoryId) {
        sellerCrudService.deleteInventory(sellerCode, inventoryId);
    }

    @GetMapping("/orders")
    public PageResponse<OrderResponse> listOrders(@RequestParam(required = false) String sellerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return sellerCrudService.listOrders(sellerCode, page, size, status);
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponse getOrder(@RequestParam(required = false) String sellerCode, @PathVariable Long orderId) {
        return sellerCrudService.getOrder(sellerCode, orderId);
    }

    @PostMapping("/orders")
    public OrderResponse createOrder(@RequestParam(required = false) String sellerCode,
            @Valid @RequestBody OrderRequest request) {
        return sellerCrudService.createOrder(sellerCode, request);
    }

    @PutMapping("/orders/{orderId}")
    public OrderResponse updateOrder(@RequestParam(required = false) String sellerCode, @PathVariable Long orderId,
            @Valid @RequestBody OrderRequest request) {
        return sellerCrudService.updateOrder(sellerCode, orderId, request);
    }

    @DeleteMapping("/orders/{orderId}")
    public void deleteOrder(@RequestParam(required = false) String sellerCode, @PathVariable Long orderId) {
        sellerCrudService.deleteOrder(sellerCode, orderId);
    }

    @GetMapping("/coupons")
    public PageResponse<CouponResponse> listCoupons(@RequestParam(required = false) String sellerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return sellerCrudService.listCoupons(sellerCode, page, size, status);
    }

    @PostMapping("/coupons")
    public CouponResponse createCoupon(@RequestParam(required = false) String sellerCode,
            @Valid @RequestBody CouponRequest request) {
        return sellerCrudService.createCoupon(sellerCode, request);
    }

    @PutMapping("/coupons/{couponId}")
    public CouponResponse updateCoupon(@RequestParam(required = false) String sellerCode, @PathVariable Long couponId,
            @Valid @RequestBody CouponRequest request) {
        return sellerCrudService.updateCoupon(sellerCode, couponId, request);
    }

    @DeleteMapping("/coupons/{couponId}")
    public void deleteCoupon(@RequestParam(required = false) String sellerCode, @PathVariable Long couponId) {
        sellerCrudService.deleteCoupon(sellerCode, couponId);
    }

    @GetMapping("/reviews")
    public PageResponse<ReviewResponse> listReviews(@RequestParam(required = false) String sellerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean abusive) {
        return sellerCrudService.listReviews(sellerCode, page, size, abusive);
    }

    @PostMapping("/reviews")
    public ReviewResponse createReview(@RequestParam(required = false) String sellerCode,
            @Valid @RequestBody ReviewRequest request) {
        return sellerCrudService.createReview(sellerCode, request);
    }

    @PutMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(@RequestParam(required = false) String sellerCode, @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {
        return sellerCrudService.updateReview(sellerCode, reviewId, request);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(@RequestParam(required = false) String sellerCode, @PathVariable Long reviewId) {
        sellerCrudService.deleteReview(sellerCode, reviewId);
    }

    @GetMapping("/support-tickets")
    public PageResponse<SupportTicketResponse> listSupportTickets(@RequestParam(required = false) String sellerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return sellerCrudService.listSupportTickets(sellerCode, page, size, status);
    }

    @PostMapping("/support-tickets")
    public SupportTicketResponse createSupportTicket(@RequestParam(required = false) String sellerCode,
            @Valid @RequestBody SupportTicketRequest request) {
        return sellerCrudService.createSupportTicket(sellerCode, request);
    }

    @PutMapping("/support-tickets/{ticketId}")
    public SupportTicketResponse updateSupportTicket(@RequestParam(required = false) String sellerCode, @PathVariable Long ticketId,
            @Valid @RequestBody SupportTicketRequest request) {
        return sellerCrudService.updateSupportTicket(sellerCode, ticketId, request);
    }

    @DeleteMapping("/support-tickets/{ticketId}")
    public void deleteSupportTicket(@RequestParam(required = false) String sellerCode, @PathVariable Long ticketId) {
        sellerCrudService.deleteSupportTicket(sellerCode, ticketId);
    }
}
