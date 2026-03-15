package com.SHIVA.puja.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SHIVA.puja.dto.AdminStatusUpdateRequest;
import com.SHIVA.puja.entity.CustomerNotification;
import com.SHIVA.puja.entity.CustomerOrderLink;
import com.SHIVA.puja.entity.Product;
import com.SHIVA.puja.entity.ReviewEntry;
import com.SHIVA.puja.entity.Seller;
import com.SHIVA.puja.entity.SellerOrder;
import com.SHIVA.puja.entity.OrderStatusHistory;
import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.CustomerNotificationRepository;
import com.SHIVA.puja.repository.CustomerOrderLinkRepository;
import com.SHIVA.puja.repository.ProductRepository;
import com.SHIVA.puja.repository.ReviewEntryRepository;
import com.SHIVA.puja.repository.SellerOrderRepository;
import com.SHIVA.puja.repository.SellerRepository;
import com.SHIVA.puja.repository.ShopRegistrationRepository;
import com.SHIVA.puja.repository.OrderStatusHistoryRepository;
import com.SHIVA.puja.repository.UserRepository;
import com.SHIVA.puja.service.AdminActivityLogService;
import com.SHIVA.puja.service.RedisTokenBlacklistService;
import com.SHIVA.puja.service.AdminControlService;

@Service
@Transactional
public class AdminControlServiceImpl implements AdminControlService {

    private static final Set<String> ORDER_STATUSES = Set.of("PENDING", "CONFIRMED", "PACKED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED");

    private final ProductRepository productRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final ReviewEntryRepository reviewEntryRepository;
    private final SellerRepository sellerRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;
    private final CustomerOrderLinkRepository customerOrderLinkRepository;
    private final CustomerNotificationRepository customerNotificationRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final UserRepository userRepository;
    private final RedisTokenBlacklistService redisTokenBlacklistService;
    private final AdminActivityLogService adminActivityLogService;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminControlServiceImpl(ProductRepository productRepository,
            SellerOrderRepository sellerOrderRepository,
            ReviewEntryRepository reviewEntryRepository,
            SellerRepository sellerRepository,
            ShopRegistrationRepository shopRegistrationRepository,
            CustomerOrderLinkRepository customerOrderLinkRepository,
            CustomerNotificationRepository customerNotificationRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            UserRepository userRepository,
            RedisTokenBlacklistService redisTokenBlacklistService,
            AdminActivityLogService adminActivityLogService,
            JavaMailSender mailSender) {
        this.productRepository = productRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.reviewEntryRepository = reviewEntryRepository;
        this.sellerRepository = sellerRepository;
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.customerOrderLinkRepository = customerOrderLinkRepository;
        this.customerNotificationRepository = customerNotificationRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.userRepository = userRepository;
        this.redisTokenBlacklistService = redisTokenBlacklistService;
        this.adminActivityLogService = adminActivityLogService;
        this.mailSender = mailSender;
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
        adminActivityLogService.log("PRODUCT_MODERATION", "PRODUCT", productId,
            "Product status set to " + product.getStatus());
    }

    @Override
    public void updateOrderStatus(Long orderId, AdminStatusUpdateRequest request) {
        SellerOrder order = sellerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found."));
        String previousStatus = order.getOrderStatus();
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String nextStatus = request.getStatus().trim().toUpperCase();
            if (!ORDER_STATUSES.contains(nextStatus)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ORDER_STATUS", "Unsupported order status provided.");
            }
            order.setOrderStatus(nextStatus);
        }
        if (request.getShippingPartner() != null && !request.getShippingPartner().isBlank()) {
            order.setShippingPartner(request.getShippingPartner().trim());
        }
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            order.setTrackingNumber(request.getTrackingNumber().trim());
        }
        sellerOrderRepository.save(order);
        if (!Objects.equals(previousStatus, order.getOrderStatus())) {
            recordOrderStatusChange(order.getId(), previousStatus, order.getOrderStatus(), "admin@system", "ADMIN",
                    request.getReason() == null || request.getReason().isBlank() ? "Admin updated order status" : request.getReason().trim());
        }
        adminActivityLogService.log("ORDER_UPDATE", "ORDER", orderId,
            "Order status updated to " + order.getOrderStatus());
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
        adminActivityLogService.log("REVIEW_MODERATION", "REVIEW", reviewId,
            "Review moderation updated; abusive=" + review.getAbusive());
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

        if (seller.getEmail() != null && "SUSPENDED".equals(status)) {
            userRepository.findTopByEmailOrderByIdDesc(seller.getEmail()).ifPresent(user -> {
                user.setStatus("SUSPENDED");
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            });
            redisTokenBlacklistService.blacklistUser(seller.getEmail(), 86400 * 7L);
        }

        if (seller.getRegistrationId() != null) {
            shopRegistrationRepository.findByRegistrationId(seller.getRegistrationId()).ifPresent(registration -> {
                registration.setStatus(status);
                registration.setUpdatedAt(LocalDateTime.now());
                shopRegistrationRepository.save(registration);
            });
        }

        String actionType = switch (status) {
            case "APPROVED" -> "SELLER_APPROVAL";
            case "REJECTED" -> "SELLER_REJECTION";
            case "SUSPENDED", "BLOCKED" -> "SELLER_SUSPENSION";
            default -> "SELLER_STATUS_UPDATE";
        };
        adminActivityLogService.log(actionType, "SELLER", sellerId, "Seller status set to " + status);
    }

    @Override
    public void updateSellerCommission(Long sellerId, AdminStatusUpdateRequest request) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SELLER_NOT_FOUND", "Seller not found."));
        if (request.getCommissionRate() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Commission rate is required.");
        }
        if (request.getCommissionRate() < 0 || request.getCommissionRate() > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Commission rate must be between 0 and 100.");
        }

        seller.setCommissionRate(BigDecimal.valueOf(request.getCommissionRate()));
        seller.setUpdatedAt(LocalDateTime.now());
        sellerRepository.save(seller);
        adminActivityLogService.log("SELLER_COMMISSION_UPDATE", "SELLER", sellerId,
                "Commission updated to " + request.getCommissionRate() + "%");
    }

    @Override
    public void updateUserStatus(Long userId, AdminStatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Status is required.");
        }
        String status = request.getStatus().trim().toUpperCase();
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        if ("SUSPENDED".equals(status) || "BANNED".equals(status) || "BLOCKED".equals(status)) {
            redisTokenBlacklistService.blacklistUser(user.getEmail(), 86400L * 14L);
        }

        adminActivityLogService.log("USER_STATUS_UPDATE", "USER", userId, "User status set to " + status);
    }

    @Override
    public String resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));

        String temporaryPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Password reset by admin");
                message.setText("Your temporary password is: " + temporaryPassword + "\nPlease sign in and change it immediately.");
                mailSender.send(message);
            } catch (RuntimeException ignored) {
                // Fallback: return temp password in response if email dispatch fails.
            }
        }

        adminActivityLogService.log("USER_PASSWORD_RESET", "USER", userId, "Temporary password issued by admin.");
        return temporaryPassword;
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

    private void recordOrderStatusChange(Long orderId, String previousStatus, String newStatus, String actorEmail, String actorRole,
            String reason) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(orderId);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setActorEmail(actorEmail);
        history.setActorRole(actorRole);
        history.setReason(reason);
        history.setChangedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);
    }
}