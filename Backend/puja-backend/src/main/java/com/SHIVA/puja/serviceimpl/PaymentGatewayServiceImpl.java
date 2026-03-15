package com.SHIVA.puja.serviceimpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.SHIVA.puja.config.AppProperties;
import com.SHIVA.puja.dto.PaymentInitiateRequest;
import com.SHIVA.puja.dto.PaymentInitiateResponse;
import com.SHIVA.puja.dto.RazorpayWebhookResponse;
import com.SHIVA.puja.entity.OrderPayment;
import com.SHIVA.puja.entity.OrderStatusHistory;
import com.SHIVA.puja.entity.SellerOrder;
import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.CustomerOrderLinkRepository;
import com.SHIVA.puja.repository.OrderPaymentRepository;
import com.SHIVA.puja.repository.OrderStatusHistoryRepository;
import com.SHIVA.puja.repository.SellerOrderRepository;
import com.SHIVA.puja.repository.UserRepository;
import com.SHIVA.puja.service.MarketplaceEventPublisher;
import com.SHIVA.puja.service.PaymentGatewayService;

@Service
@Transactional
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private static final String RAZORPAY_PROVIDER = "RAZORPAY";

    private final SellerOrderRepository sellerOrderRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final CustomerOrderLinkRepository customerOrderLinkRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final MarketplaceEventPublisher marketplaceEventPublisher;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public PaymentGatewayServiceImpl(
            SellerOrderRepository sellerOrderRepository,
            OrderPaymentRepository orderPaymentRepository,
            CustomerOrderLinkRepository customerOrderLinkRepository,
            UserRepository userRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            MarketplaceEventPublisher marketplaceEventPublisher,
            AppProperties appProperties,
            ObjectMapper objectMapper) {
        this.sellerOrderRepository = sellerOrderRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.customerOrderLinkRepository = customerOrderLinkRepository;
        this.userRepository = userRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.marketplaceEventPublisher = marketplaceEventPublisher;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        User user = currentUser();
        customerOrderLinkRepository.findByUserIdAndOrderId(user.getId(), request.getOrderId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", "Order does not belong to the current customer."));

        SellerOrder order = sellerOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found."));

        OrderPayment payment = orderPaymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Order payment record not found."));

        String provider = normalizeProvider(request.getProvider(), payment.getGatewayProvider());
        if (!RAZORPAY_PROVIDER.equals(provider)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_PROVIDER", "Only Razorpay is currently supported.");
        }
        if ("PAID".equalsIgnoreCase(payment.getPaymentStatus())) {
            return PaymentInitiateResponse.builder()
                    .provider(provider)
                    .razorpayOrderId(payment.getGatewayReference())
                    .keyId(appProperties.getPayment().getKeyId())
                    .amountPaise(toPaise(order.getTotalAmount()))
                    .currency(currency())
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .message("Payment already completed for this order")
                    .build();
        }

        JsonNode gatewayOrder = createRazorpayOrder(order, user);
        String razorpayOrderId = gatewayOrder.path("id").asText();
        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY_ERROR", "Unable to create gateway order.");
        }

        payment.setGatewayProvider(RAZORPAY_PROVIDER);
        payment.setGatewayReference(razorpayOrderId);
        payment.setPaymentStatus("PENDING_GATEWAY");
        payment.setTransactionLog("Payment initiated at gateway. Awaiting webhook confirmation.");
        payment.setUpdatedAt(LocalDateTime.now());
        orderPaymentRepository.save(payment);

        marketplaceEventPublisher.publish(
                appProperties.getPayment().getKafkaPaymentTopic(),
                "payment.initiated",
                Map.of(
                        "orderId", order.getId(),
                        "orderCode", order.getOrderCode(),
                        "provider", provider,
                        "gatewayOrderId", razorpayOrderId,
                        "amount", order.getTotalAmount(),
                        "currency", currency(),
                        "userId", user.getId()));

        return PaymentInitiateResponse.builder()
                .provider(provider)
                .razorpayOrderId(razorpayOrderId)
                .keyId(appProperties.getPayment().getKeyId())
                .amountPaise(gatewayOrder.path("amount").asLong(toPaise(order.getTotalAmount())))
                .currency(gatewayOrder.path("currency").asText(currency()))
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .message("Payment initiated")
                .build();
    }

    @Override
    public RazorpayWebhookResponse handleRazorpayWebhook(String payload, String signatureHeader) {
        if (payload == null || payload.isBlank()) {
            return RazorpayWebhookResponse.builder()
                    .accepted(false)
                    .event("UNKNOWN")
                    .message("Empty webhook payload")
                    .build();
        }

        String webhookSecret = trimToNull(appProperties.getPayment().getWebhookSecret());
        if (webhookSecret != null && !isValidSignature(payload, signatureHeader, webhookSecret)) {
            return RazorpayWebhookResponse.builder()
                    .accepted(false)
                    .event("UNKNOWN")
                    .message("Invalid webhook signature")
                    .build();
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (IOException exception) {
            return RazorpayWebhookResponse.builder()
                    .accepted(false)
                    .event("UNKNOWN")
                    .message("Malformed webhook payload")
                    .build();
        }

        String event = root.path("event").asText("UNKNOWN");
        JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
        String gatewayOrderId = paymentEntity.path("order_id").asText(null);
        String paymentId = paymentEntity.path("id").asText(null);

        if (gatewayOrderId == null || gatewayOrderId.isBlank()) {
            return RazorpayWebhookResponse.builder()
                    .accepted(true)
                    .event(event)
                    .message("No gateway order id found in payload")
                    .build();
        }

        OrderPayment payment = orderPaymentRepository.findByGatewayReference(gatewayOrderId).orElse(null);
        if (payment == null) {
            return RazorpayWebhookResponse.builder()
                    .accepted(true)
                    .event(event)
                    .message("No matching local payment found")
                    .build();
        }

        SellerOrder order = sellerOrderRepository.findById(payment.getOrderId()).orElse(null);
        if (order == null) {
            return RazorpayWebhookResponse.builder()
                    .accepted(true)
                    .event(event)
                    .message("No matching local order found")
                    .build();
        }

        if ("payment.captured".equalsIgnoreCase(event) || "order.paid".equalsIgnoreCase(event)) {
            markPaymentCaptured(order, payment, paymentId);
            return RazorpayWebhookResponse.builder()
                    .accepted(true)
                    .event(event)
                    .message("Payment captured and order confirmed")
                    .build();
        }

        if ("payment.failed".equalsIgnoreCase(event)) {
            markPaymentFailed(order, payment, paymentId);
            return RazorpayWebhookResponse.builder()
                    .accepted(true)
                    .event(event)
                    .message("Payment failure recorded")
                    .build();
        }

        return RazorpayWebhookResponse.builder()
                .accepted(true)
                .event(event)
                .message("Webhook event acknowledged")
                .build();
    }

    private void markPaymentCaptured(SellerOrder order, OrderPayment payment, String paymentId) {
        LocalDateTime now = LocalDateTime.now();
        payment.setPaymentStatus("PAID");
        payment.setPaidAt(now);
        payment.setTransactionLog("Payment captured" + (paymentId == null ? "" : " [" + paymentId + "]"));
        payment.setUpdatedAt(now);
        orderPaymentRepository.save(payment);

        String previousStatus = order.getOrderStatus();
        if ("PENDING".equalsIgnoreCase(previousStatus)) {
            order.setOrderStatus("CONFIRMED");
            sellerOrderRepository.save(order);
            recordOrderStatusChange(order.getId(), previousStatus, "CONFIRMED", "system@webhook", "SYSTEM",
                    "Auto-confirmed after successful online payment.");
        }

        marketplaceEventPublisher.publish(
                appProperties.getPayment().getKafkaPaymentTopic(),
                "payment.captured",
                Map.of(
                        "orderId", order.getId(),
                        "orderCode", order.getOrderCode(),
                        "gatewayOrderId", payment.getGatewayReference(),
                        "paymentStatus", payment.getPaymentStatus(),
                        "paymentId", paymentId == null ? "" : paymentId));

        marketplaceEventPublisher.publish(
                appProperties.getPayment().getKafkaOrderTopic(),
                "order.confirmed",
                Map.of(
                        "orderId", order.getId(),
                        "orderCode", order.getOrderCode(),
                        "orderStatus", order.getOrderStatus()));
    }

    private void markPaymentFailed(SellerOrder order, OrderPayment payment, String paymentId) {
        LocalDateTime now = LocalDateTime.now();
        payment.setPaymentStatus("FAILED");
        payment.setPaidAt(null);
        payment.setTransactionLog("Payment failed" + (paymentId == null ? "" : " [" + paymentId + "]"));
        payment.setUpdatedAt(now);
        orderPaymentRepository.save(payment);

        marketplaceEventPublisher.publish(
                appProperties.getPayment().getKafkaPaymentTopic(),
                "payment.failed",
                Map.of(
                        "orderId", order.getId(),
                        "orderCode", order.getOrderCode(),
                        "gatewayOrderId", payment.getGatewayReference(),
                        "paymentStatus", payment.getPaymentStatus(),
                        "paymentId", paymentId == null ? "" : paymentId));
    }

    private JsonNode createRazorpayOrder(SellerOrder order, User user) {
        String keyId = trimToNull(appProperties.getPayment().getKeyId());
        String keySecret = trimToNull(appProperties.getPayment().getKeySecret());
        if (keyId == null || keySecret == null) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_CONFIG_MISSING",
                    "Razorpay credentials are not configured.");
        }

        Map<String, Object> requestBody = Map.of(
                "amount", toPaise(order.getTotalAmount()),
                "currency", currency(),
                "receipt", order.getOrderCode(),
                "notes", Map.of(
                        "orderId", order.getId(),
                        "userId", user.getId()));

        String body;
        try {
            body = objectMapper.writeValueAsString(requestBody);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_SERIALIZATION_FAILED",
                    "Unable to prepare payment request body.");
        }

        String basicToken = Base64.getEncoder().encodeToString((keyId + ':' + keySecret).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.razorpay.com/v1/orders"))
                .header("Authorization", "Basic " + basicToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY_UNREACHABLE", "Unable to reach Razorpay.");
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY_ERROR",
                    "Razorpay order creation failed with status " + response.statusCode() + '.');
        }

        try {
            return objectMapper.readTree(response.body());
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY_ERROR", "Invalid response received from Razorpay.");
        }
    }

    private boolean isValidSignature(String payload, String signatureHeader, String webhookSecret) {
        String signature = trimToNull(signatureHeader);
        if (signature == null) {
            return false;
        }

        try {
            Mac sha256 = Mac.getInstance("HmacSHA256");
            sha256.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = sha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(digest);
            return Objects.equals(expected, signature);
        } catch (GeneralSecurityException exception) {
            return false;
        }
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

    private long toPaise(BigDecimal amount) {
        return amount == null
                ? 0L
                : amount.setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).longValueExact();
    }

    private String normalizeProvider(String requestProvider, String fallbackProvider) {
        String resolved = trimToNull(requestProvider);
        if (resolved == null) {
            resolved = trimToNull(fallbackProvider);
        }
        return resolved == null ? RAZORPAY_PROVIDER : resolved.toUpperCase();
    }

    private String currency() {
        String configured = trimToNull(appProperties.getPayment().getCurrency());
        return configured == null ? "INR" : configured.toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Authentication is required for payment operations.");
        }

        return userRepository.findTopByEmailOrderByIdDesc(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Authenticated user not found."));
    }
}
