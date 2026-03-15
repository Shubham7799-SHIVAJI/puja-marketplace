package com.SHIVA.puja.controllers;

import java.util.Arrays;
import java.util.List;

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

import com.SHIVA.puja.dto.CategoryOptionResponse;
import com.SHIVA.puja.dto.CheckoutRequest;
import com.SHIVA.puja.dto.CheckoutResponse;
import com.SHIVA.puja.dto.CustomerAddressRequest;
import com.SHIVA.puja.dto.CustomerAddressResponse;
import com.SHIVA.puja.dto.CustomerNotificationResponse;
import com.SHIVA.puja.dto.CustomerOrderResponse;
import com.SHIVA.puja.dto.CustomerReviewCreateRequest;
import com.SHIVA.puja.dto.MarketplaceHighlightsResponse;
import com.SHIVA.puja.dto.MarketplaceProductCardResponse;
import com.SHIVA.puja.dto.MarketplaceProductDetailResponse;
import com.SHIVA.puja.dto.PageResponse;
import com.SHIVA.puja.dto.ReviewResponse;
import com.SHIVA.puja.dto.WishlistItemResponse;
import com.SHIVA.puja.service.MarketplaceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/marketplace")
@CrossOrigin(origins = "http://localhost:4200")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping("/catalog")
    public PageResponse<MarketplaceProductCardResponse> listCatalog(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(defaultValue = "popular") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return marketplaceService.listCatalog(query, category, brand, minPrice, maxPrice, minRating, inStockOnly, sortBy, page, size);
    }

    @GetMapping("/catalog/suggestions")
    public List<String> listSuggestions(@RequestParam String query) {
        return marketplaceService.listSuggestions(query);
    }

    @GetMapping("/categories")
    public List<CategoryOptionResponse> listCategories() {
        return marketplaceService.listCategories();
    }

    @GetMapping("/highlights")
    public MarketplaceHighlightsResponse getHighlights() {
        return marketplaceService.getHighlights();
    }

    @GetMapping("/products/{productId}")
    public MarketplaceProductDetailResponse getProductDetail(@PathVariable Long productId) {
        return marketplaceService.getProductDetail(productId);
    }

    @GetMapping("/compare")
    public List<MarketplaceProductCardResponse> compareProducts(@RequestParam String ids) {
        List<Long> productIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
        return marketplaceService.compareProducts(productIds);
    }

    @GetMapping("/wishlist")
    public List<WishlistItemResponse> listWishlist() {
        return marketplaceService.listWishlist();
    }

    @PostMapping("/wishlist/{productId}")
    public WishlistItemResponse addToWishlist(@PathVariable Long productId) {
        return marketplaceService.addToWishlist(productId);
    }

    @DeleteMapping("/wishlist/{productId}")
    public void removeFromWishlist(@PathVariable Long productId) {
        marketplaceService.removeFromWishlist(productId);
    }

    @GetMapping("/addresses")
    public List<CustomerAddressResponse> listAddresses() {
        return marketplaceService.listAddresses();
    }

    @PostMapping("/addresses")
    public CustomerAddressResponse createAddress(@Valid @RequestBody CustomerAddressRequest request) {
        return marketplaceService.createAddress(request);
    }

    @PutMapping("/addresses/{addressId}")
    public CustomerAddressResponse updateAddress(@PathVariable Long addressId, @Valid @RequestBody CustomerAddressRequest request) {
        return marketplaceService.updateAddress(addressId, request);
    }

    @DeleteMapping("/addresses/{addressId}")
    public void deleteAddress(@PathVariable Long addressId) {
        marketplaceService.deleteAddress(addressId);
    }

    @PostMapping("/checkout")
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return marketplaceService.checkout(request);
    }

    @GetMapping("/orders")
    public List<CustomerOrderResponse> listOrders() {
        return marketplaceService.listOrders();
    }

    @GetMapping("/orders/{orderId}")
    public CustomerOrderResponse getOrder(@PathVariable Long orderId) {
        return marketplaceService.getOrder(orderId);
    }

    @PostMapping("/reviews")
    public ReviewResponse createReview(@Valid @RequestBody CustomerReviewCreateRequest request) {
        return marketplaceService.createReview(request);
    }

    @GetMapping("/notifications")
    public List<CustomerNotificationResponse> listNotifications() {
        return marketplaceService.listNotifications();
    }
}