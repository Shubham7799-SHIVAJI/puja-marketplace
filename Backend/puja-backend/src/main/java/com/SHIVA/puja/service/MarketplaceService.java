package com.SHIVA.puja.service;

import java.util.List;

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

public interface MarketplaceService {

    PageResponse<MarketplaceProductCardResponse> listCatalog(String query, String category, String brand, Double minPrice,
            Double maxPrice, Double minRating, Boolean inStockOnly, String sortBy, int page, int size);

    List<String> listSuggestions(String query);

    List<CategoryOptionResponse> listCategories();

    MarketplaceHighlightsResponse getHighlights();

    MarketplaceProductDetailResponse getProductDetail(Long productId);

    List<MarketplaceProductCardResponse> compareProducts(List<Long> productIds);

    List<WishlistItemResponse> listWishlist();

    WishlistItemResponse addToWishlist(Long productId);

    void removeFromWishlist(Long productId);

    List<CustomerAddressResponse> listAddresses();

    CustomerAddressResponse createAddress(CustomerAddressRequest request);

    CustomerAddressResponse updateAddress(Long addressId, CustomerAddressRequest request);

    void deleteAddress(Long addressId);

    CheckoutResponse checkout(CheckoutRequest request);

    List<CustomerOrderResponse> listOrders();

    CustomerOrderResponse getOrder(Long orderId);

    ReviewResponse createReview(CustomerReviewCreateRequest request);

    List<CustomerNotificationResponse> listNotifications();
}