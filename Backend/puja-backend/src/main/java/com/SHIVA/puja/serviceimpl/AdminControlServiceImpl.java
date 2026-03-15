package com.SHIVA.puja.serviceimpl;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SHIVA.puja.dto.AdminStatusUpdateRequest;
import com.SHIVA.puja.entity.CustomerNotification;
import com.SHIVA.puja.entity.CustomerOrderLink;
import com.SHIVA.puja.entity.Product;
import com.SHIVA.puja.entity.ReviewEntry;
import com.SHIVA.puja.entity.Seller;
import com.SHIVA.puja.entity.SellerOrder;
import com.SHIVA.puja.entity.ShopRegistration;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.CustomerNotificationRepository;
import com.SHIVA.puja.repository.CustomerOrderLinkRepository;
import com.SHIVA.puja.repository.ProductRepository;
import com.SHIVA.puja.repository.ReviewEntryRepository;
import com.SHIVA.puja.repository.SellerOrderRepository;
import com.SHIVA.puja.repository.SellerRepository;
import com.SHIVA.puja.repository.ShopRegistrationRepository;
import com.SHIVA.puja.service.AdminControlService;

@Service
@Transactional
public class AdminControlServiceImpl implements AdminControlService {

    private final ProductRepository productRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final ReviewEntryRepository reviewEntryRepository;
    private final SellerRepository sellerRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;
    private final CustomerOrderLinkRepository customerOrderLinkRepository;
    private final CustomerNotificationRepository customerNotificationRepository;

    public AdminControlServiceImpl(ProductRepository productRepository,
            SellerOrderRepository sellerOrderRepository,
            ReviewEntryRepository reviewEntryRepository,
            SellerRepository sellerRepository,
            ShopRegistrationRepository shopRegistrationRepository,
            CustomerOrderLinkRepository customerOrderLinkRepository,
            CustomerNotificationRepository customerNotificationRepository) {
        this.productRepository = productRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.reviewEntryRepository = reviewEntryRepository;
        this.sellerRepository = sellerRepository;
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.customerOrderLinkRepository = customerOrderLinkRepository;
        this.customerNotificationRepository = customerNotificationRepository;
    }

    @Override
    public void updateProductStatus(Long productId, AdminStatusUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found."));
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Status is required.");
        }
        product.setStatus(request.getStatus().trim().toUpperCase());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    public void updateOrderStatus(Long orderId, AdminStatusUpdateRequest request) {
        SellerOrder order = sellerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found."));
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            order.setOrderStatus(request.getStatus().trim().toUpperCase());
        }
        if (request.getShippingPartner() != null && !request.getShippingPartner().isBlank()) {
            order.setShippingPartner(request.getShippingPartner().trim());
        }
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            order.setTrackingNumber(request.getTrackingNumber().trim());
        }
        sellerOrderRepository.save(order);
        customerOrderLinkRepository.findByOrderId(orderId).ifPresent(link -> notifyCustomer(link, order));
    }

    @Override
    public void moderateReview(Long reviewId, AdminStatusUpdateRequest request) {
        ReviewEntry review = reviewEntryRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "Review not found."));
        if (request.getAbusive() != null) {
            review.setAbusive(request.getAbusive());
        }
        if (request.getReplyText() != null) {
            review.setReplyText(request.getReplyText().trim());
        }
        reviewEntryRepository.save(review);
    }

    @Override
    public void updateSellerStatus(Long sellerId, AdminStatusUpdateRequest request) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SELLER_NOT_FOUND", "Seller not found."));
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Status is required.");
        }
        String status = request.getStatus().trim().toUpperCase();
        seller.setStatus(status);
        seller.setUpdatedAt(LocalDateTime.now());
        sellerRepository.save(seller);

        if (seller.getRegistrationId() != null) {
            shopRegistrationRepository.findByRegistrationId(seller.getRegistrationId()).ifPresent(registration -> {
                registration.setStatus(status);
                registration.setUpdatedAt(LocalDateTime.now());
                shopRegistrationRepository.save(registration);
            });
        }
    }

    private void notifyCustomer(CustomerOrderLink link, SellerOrder order) {
        CustomerNotification notification = new CustomerNotification();
        notification.setUserId(link.getUserId());
        notification.setNotificationType("ORDER_STATUS");
        notification.setTitle("Order status updated");
        notification.setDetail(order.getOrderCode() + " is now " + order.getOrderStatus() + (order.getTrackingNumber() == null ? "" : " with tracking " + order.getTrackingNumber()) + '.');
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        customerNotificationRepository.save(notification);
    }
}