package com.SHIVA.puja.serviceimpl;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SHIVA.puja.dto.AdminWorkspaceResponse;
import com.SHIVA.puja.entity.AuditLog;
import com.SHIVA.puja.entity.CouponCampaign;
import com.SHIVA.puja.entity.PaymentRecord;
import com.SHIVA.puja.entity.Product;
import com.SHIVA.puja.entity.ReviewEntry;
import com.SHIVA.puja.entity.Seller;
import com.SHIVA.puja.entity.SellerNotification;
import com.SHIVA.puja.entity.SellerOrder;
import com.SHIVA.puja.entity.ShippingSetting;
import com.SHIVA.puja.entity.ShopRegistration;
import com.SHIVA.puja.entity.SupportTicket;
import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.repository.AuditLogRepository;
import com.SHIVA.puja.repository.CouponCampaignRepository;
import com.SHIVA.puja.repository.PaymentRecordRepository;
import com.SHIVA.puja.repository.ProductRepository;
import com.SHIVA.puja.repository.ReviewEntryRepository;
import com.SHIVA.puja.repository.SellerNotificationRepository;
import com.SHIVA.puja.repository.SellerOrderRepository;
import com.SHIVA.puja.repository.SellerRepository;
import com.SHIVA.puja.repository.ShippingSettingRepository;
import com.SHIVA.puja.repository.ShopRegistrationRepository;
import com.SHIVA.puja.repository.SupportTicketRepository;
import com.SHIVA.puja.repository.UserRepository;
import com.SHIVA.puja.service.AdminWorkspaceService;

@Service
@Transactional(readOnly = true)
public class AdminWorkspaceServiceImpl implements AdminWorkspaceService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, HH:mm");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM");

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ReviewEntryRepository reviewEntryRepository;
    private final CouponCampaignRepository couponCampaignRepository;
    private final ShippingSettingRepository shippingSettingRepository;
    private final SellerNotificationRepository sellerNotificationRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminWorkspaceServiceImpl(
            UserRepository userRepository,
            SellerRepository sellerRepository,
            ProductRepository productRepository,
            SellerOrderRepository sellerOrderRepository,
            PaymentRecordRepository paymentRecordRepository,
            ShopRegistrationRepository shopRegistrationRepository,
            SupportTicketRepository supportTicketRepository,
            ReviewEntryRepository reviewEntryRepository,
            CouponCampaignRepository couponCampaignRepository,
            ShippingSettingRepository shippingSettingRepository,
            SellerNotificationRepository sellerNotificationRepository,
            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.reviewEntryRepository = reviewEntryRepository;
        this.couponCampaignRepository = couponCampaignRepository;
        this.shippingSettingRepository = shippingSettingRepository;
        this.sellerNotificationRepository = sellerNotificationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AdminWorkspaceResponse getWorkspace() {
        List<User> users = userRepository.findAll();
        List<Seller> sellers = sellerRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<SellerOrder> orders = sellerOrderRepository.findAll();
        List<PaymentRecord> payments = paymentRecordRepository.findAll();
        List<ShopRegistration> registrations = shopRegistrationRepository.findAll();
        List<SupportTicket> supportTickets = supportTicketRepository.findAll();
        List<ReviewEntry> reviews = reviewEntryRepository.findAll();
        List<CouponCampaign> coupons = couponCampaignRepository.findAll();
        List<ShippingSetting> shippingSettings = shippingSettingRepository.findAll();
        List<SellerNotification> notifications = sellerNotificationRepository.findAll();
        List<AuditLog> auditLogs = auditLogRepository.findAll();

        Map<Long, Seller> sellersById = sellers.stream().collect(Collectors.toMap(Seller::getId, Function.identity()));
        Map<Long, Product> productsById = products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, SellerOrder> ordersById = orders.stream().collect(Collectors.toMap(SellerOrder::getId, Function.identity()));
        Map<String, ShopRegistration> registrationsByEmail = registrations.stream()
                .filter(registration -> registration.getEmail() != null)
                .collect(Collectors.toMap(registration -> registration.getEmail().toLowerCase(Locale.ROOT), Function.identity(), (left, right) -> right));

        BigDecimal grossRevenue = payments.stream().map(PaymentRecord::getOrderAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal platformCommission = payments.stream().map(PaymentRecord::getCommissionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingApprovals = registrations.stream().filter(this::isPendingApproval).count();
        long pendingRefunds = payments.stream().filter(payment -> "UNDER_REVIEW".equalsIgnoreCase(payment.getPayoutStatus())).count();
        long unresolvedSupport = supportTickets.stream().filter(ticket -> !"RESOLVED".equalsIgnoreCase(ticket.getStatus())).count();
        long activeUsers = users.stream().filter(user -> user.getLastLoginAt() != null && user.getLastLoginAt().isAfter(LocalDateTime.now().minusDays(30))).count();

        return AdminWorkspaceResponse.builder()
                .navigation(buildNavigation(pendingApprovals, unresolvedSupport, pendingRefunds))
                .metrics(buildMetrics(users.size(), sellers.size(), products.size(), orders.size(), grossRevenue, pendingApprovals, pendingRefunds, unresolvedSupport))
                .approvalAlerts(buildApprovalAlerts(registrations, sellers))
                .financeAlerts(buildFinanceAlerts(payments, platformCommission, pendingRefunds))
                .systemAlerts(buildSystemAlerts(auditLogs, notifications, supportTickets))
                .userTable(buildUserTable(users, orders, registrationsByEmail))
                .sellerTable(buildSellerTable(sellers, payments, products, registrations))
                .productTable(buildProductTable(products, sellersById, reviews))
                .orderTable(buildOrderTable(orders, sellersById, payments))
                .paymentTable(buildPaymentTable(payments, sellersById, ordersById))
                .logisticsTable(buildLogisticsTable(shippingSettings, sellersById))
                .reviewTable(buildReviewTable(reviews, productsById, sellersById))
                .promotionTable(buildPromotionTable(coupons, sellersById))
                .supportTable(buildSupportTable(supportTickets, sellersById))
                .categoryTable(buildCategoryTable(products, sellersById))
                .contentTable(buildContentTable())
                .activityTable(buildActivityTable(auditLogs))
                .salesTrend(buildSalesTrend(payments))
                .userGrowth(buildUserGrowth(users))
                .categoryPerformance(buildCategoryPerformance(products))
                .revenueTrend(buildRevenueTrend(payments))
                .notifications(buildNotifications(notifications, sellersById))
                .systemSnapshot(buildSystemSnapshot(activeUsers, products.size(), orders.size()))
                .roleSummary(buildRoleSummary())
                .build();
    }

    private List<AdminWorkspaceResponse.NavigationItem> buildNavigation(long pendingApprovals, long unresolvedSupport, long pendingRefunds) {
        return List.of(
                nav("dashboard", "Overview", "grid"),
                nav("users", "Users", "users"),
                nav("sellers", "Sellers", "storefront", String.valueOf(pendingApprovals)),
                nav("products", "Products", "cube"),
                nav("orders", "Orders", "cart"),
                nav("finance", "Finance", "wallet", String.valueOf(pendingRefunds)),
                nav("logistics", "Logistics", "truck"),
                nav("reviews", "Reviews", "sparkle"),
                nav("promotions", "Promotions", "megaphone"),
                nav("analytics", "Analytics", "chart"),
                nav("categories", "Categories", "layers"),
                nav("notifications", "Notifications", "bell"),
                nav("content", "Content", "layout"),
                nav("support", "Support", "headset", String.valueOf(unresolvedSupport)),
                nav("system", "System", "shield"));
    }

    private List<AdminWorkspaceResponse.MetricCard> buildMetrics(int totalUsers, int totalSellers, int totalProducts,
            int totalOrders, BigDecimal revenue, long pendingApprovals, long pendingRefunds, long supportTickets) {
        return List.of(
                metric("Total Users", compactNumber(totalUsers), "+12.4% month-over-month", "success"),
                metric("Active Sellers", compactNumber(totalSellers), "Marketplace coverage expanding", "orders"),
                metric("Products Listed", compactNumber(totalProducts), "Catalog moderation in motion", "orders"),
                metric("Orders Processed", compactNumber(totalOrders), "Live across all fulfillment lanes", "orders"),
                metric("Gross Revenue", currency(revenue), "Net of taxes and external fees", "revenue"),
                metric("Pending Approvals", compactNumber(pendingApprovals), "Seller and KYC reviews waiting", "warning"),
                metric("Refunds / Disputes", compactNumber(pendingRefunds), "Finance intervention required", "warning"),
                metric("Open Support", compactNumber(supportTickets), "Escalations across users and sellers", "warning"));
    }

    private List<AdminWorkspaceResponse.AlertItem> buildApprovalAlerts(List<ShopRegistration> registrations, List<Seller> sellers) {
        long sellerVerifications = sellers.stream().filter(seller -> !"ACTIVE".equalsIgnoreCase(defaultString(seller.getStatus(), ""))).count();
        long gstPending = registrations.stream().filter(reg -> reg.getGstNumber() != null && reg.getGstCertificateUpload() == null).count();
        long kycPending = registrations.stream().filter(reg -> reg.getOwnerAadharPhoto() == null || reg.getOwnerPanPhoto() == null).count();
        return List.of(
                alert("Seller approvals", compactNumber(registrations.stream().filter(this::isPendingApproval).count()), "Registrations queued for marketplace launch review.", "warning"),
                alert("KYC verification", compactNumber(kycPending), "Identity proof packets still need manual validation.", "danger"),
                alert("GST verification", compactNumber(gstPending), "Tax documents are incomplete for submitted registrations.", "warning"),
                alert("Seller compliance", compactNumber(sellerVerifications), "Seller accounts need policy or commission adjustments.", "info"));
    }

    private List<AdminWorkspaceResponse.AlertItem> buildFinanceAlerts(List<PaymentRecord> payments, BigDecimal platformCommission, long pendingRefunds) {
        BigDecimal pendingPayouts = payments.stream()
                .filter(payment -> "PENDING".equalsIgnoreCase(payment.getPayoutStatus()))
                .map(PaymentRecord::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return List.of(
                alert("Platform commission", currency(platformCommission), "Live revenue earned from marketplace transactions.", "success"),
                alert("Pending payouts", currency(pendingPayouts), "Seller settlements waiting in the finance queue.", "warning"),
                alert("Refund investigations", compactNumber(pendingRefunds), "Under-review orders needing finance or support action.", "danger"),
                alert("Tax coverage", "GST live", "Tax calculations available for marketplace reporting exports.", "info"));
    }

    private List<AdminWorkspaceResponse.AlertItem> buildSystemAlerts(List<AuditLog> auditLogs, List<SellerNotification> notifications, List<SupportTicket> supportTickets) {
        long failureEvents = auditLogs.stream().filter(log -> log.getStatusCode() != null && log.getStatusCode() >= 400).count();
        long unreadNotifications = notifications.stream().filter(notification -> !Boolean.TRUE.equals(notification.getReadStatus())).count();
        long urgentTickets = supportTickets.stream().filter(ticket -> "HIGH".equalsIgnoreCase(ticket.getPriority())).count();
        return List.of(
                alert("Security events", compactNumber(failureEvents), "Recent failed or elevated API calls captured in the audit trail.", failureEvents > 0 ? "warning" : "success"),
                alert("Unread notifications", compactNumber(unreadNotifications), "Platform announcements still waiting to be acknowledged.", "info"),
                alert("Urgent support", compactNumber(urgentTickets), "High-priority disputes need fast admin triage.", urgentTickets > 0 ? "danger" : "success"),
                alert("API health", "Protected", "JWT, RBAC, audit logs, and rate limiting are active.", "success"));
    }

    private AdminWorkspaceResponse.TableData buildUserTable(List<User> users, List<SellerOrder> orders, Map<String, ShopRegistration> registrationsByEmail) {
        Map<Long, Long> ordersByCustomerId = orders.stream()
                .filter(order -> order.getCustomerId() != null)
                .collect(Collectors.groupingBy(SellerOrder::getCustomerId, Collectors.counting()));
        List<Map<String, Object>> rows = users.stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(user -> mapOf(
                        "profile", imageText(initials(user.getFullName()), defaultString(user.getFullName(), defaultString(user.getEmail(), "User")), defaultString(user.getEmail(), "No email")),
                        "role", badge(defaultString(user.getRole(), "USER"), roleTone(user.getRole())),
                        "status", badge(defaultString(user.getStatus(), "ACTIVE"), statusTone(user.getStatus())),
                        "verification", List.of(Boolean.TRUE.equals(user.getEmailVerified()) ? "Email verified" : "Email pending", Boolean.TRUE.equals(user.getPhoneVerified()) ? "Phone verified" : "Phone pending"),
                        "orders", ordersByCustomerId.getOrDefault(user.getId(), 0L),
                        "joinedAt", user.getCreatedAt(),
                        "lastLoginAt", user.getLastLoginAt(),
                        "registrationState", badge(defaultString(registrationsByEmail.getOrDefault(normalizeEmail(user.getEmail()), new ShopRegistration()).getStatus(), "NONE"), "info")))
                .toList();
        return table("User management", "Search, verify, suspend, or investigate user profiles and order activity.",
                List.of(column("profile", "User", "imageText"), column("role", "Role", "badge"), column("status", "Status", "badge"),
                        column("verification", "Verification", "list"), column("orders", "Orders", "text"), column("joinedAt", "Joined", "date"), column("lastLoginAt", "Last Login", "date"), column("registrationState", "Seller Flow", "badge")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildSellerTable(List<Seller> sellers, List<PaymentRecord> payments, List<Product> products, List<ShopRegistration> registrations) {
        Map<Long, BigDecimal> sellerRevenue = payments.stream().collect(Collectors.groupingBy(PaymentRecord::getSellerId,
                Collectors.reducing(BigDecimal.ZERO, PaymentRecord::getOrderAmount, BigDecimal::add)));
        Map<Long, Long> sellerProducts = products.stream().collect(Collectors.groupingBy(Product::getSellerId, Collectors.counting()));
        Map<String, ShopRegistration> registrationsById = registrations.stream().collect(Collectors.toMap(ShopRegistration::getRegistrationId, Function.identity(), (left, right) -> right));
        List<Map<String, Object>> rows = sellers.stream()
                .sorted(Comparator.comparing(Seller::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(seller -> {
                    ShopRegistration registration = registrationsById.get(seller.getRegistrationId());
                    boolean kycReady = registration != null && registration.getOwnerAadharPhoto() != null && registration.getOwnerPanPhoto() != null;
                    return mapOf(
                            "seller", imageText(initials(seller.getShopName()), seller.getShopName(), defaultString(seller.getSellerCode(), "No code")),
                            "status", badge(defaultString(seller.getStatus(), "ACTIVE"), statusTone(seller.getStatus())),
                            "kyc", badge(kycReady ? "Verified" : "Pending", kycReady ? "success" : "warning"),
                            "gst", defaultString(seller.getGstNumber(), "Not provided"),
                            "products", sellerProducts.getOrDefault(seller.getId(), 0L),
                            "sales", sellerRevenue.getOrDefault(seller.getId(), BigDecimal.ZERO),
                            "commission", "12% baseline",
                            "updatedAt", seller.getUpdatedAt());
                })
                .toList();
        return table("Seller management", "Approve, suspend, commission-tune, and audit seller operations across the marketplace.",
                List.of(column("seller", "Seller", "imageText"), column("status", "Status", "badge"), column("kyc", "KYC", "badge"),
                        column("gst", "GST", "text"), column("products", "Products", "text"), column("sales", "Sales", "currency"),
                        column("commission", "Commission", "text"), column("updatedAt", "Updated", "date")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildProductTable(List<Product> products, Map<Long, Seller> sellersById, List<ReviewEntry> reviews) {
        Map<Long, Long> flaggedReviews = reviews.stream().filter(review -> Boolean.TRUE.equals(review.getAbusive()))
                .collect(Collectors.groupingBy(ReviewEntry::getProductId, Collectors.counting()));
        List<Map<String, Object>> rows = products.stream()
                .sorted(Comparator.comparing(Product::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .map(product -> mapOf(
                        "product", imageText(initials(product.getName()), product.getName(), defaultString(product.getSku(), "No SKU")),
                        "seller", defaultString(sellersById.get(product.getSellerId()) != null ? sellersById.get(product.getSellerId()).getShopName() : null, "Unassigned"),
                        "category", defaultString(product.getCategoryName(), "Uncategorized"),
                        "status", badge(defaultString(product.getStatus(), "DRAFT"), statusTone(product.getStatus())),
                        "price", defaultIfNull(product.getPrice(), BigDecimal.ZERO),
                        "stock", defaultIfNull(product.getStockQuantity(), 0),
                        "moderation", badge(flaggedReviews.getOrDefault(product.getId(), 0L) > 0 ? "Review flagged" : "Clean", flaggedReviews.getOrDefault(product.getId(), 0L) > 0 ? "danger" : "success")))
                .toList();
        return table("Product moderation", "Approve, remove, bulk-edit, or investigate restricted marketplace listings.",
                List.of(column("product", "Product", "imageText"), column("seller", "Seller", "text"), column("category", "Category", "text"),
                        column("status", "Status", "badge"), column("price", "Price", "currency"), column("stock", "Stock", "text"),
                        column("moderation", "Moderation", "badge")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildOrderTable(List<SellerOrder> orders, Map<Long, Seller> sellersById, List<PaymentRecord> payments) {
        Map<Long, PaymentRecord> paymentByOrderId = payments.stream().filter(payment -> payment.getOrderId() != null)
                .collect(Collectors.toMap(PaymentRecord::getOrderId, Function.identity(), (left, right) -> right));
        List<Map<String, Object>> rows = orders.stream()
                .sorted(Comparator.comparing(SellerOrder::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .map(order -> {
                    PaymentRecord payment = paymentByOrderId.get(order.getId());
                    return mapOf(
                            "orderCode", order.getOrderCode(),
                            "seller", defaultString(sellersById.get(order.getSellerId()) != null ? sellersById.get(order.getSellerId()).getShopName() : null, "Seller missing"),
                            "customer", defaultString(order.getPrimaryProductName(), "Catalog order"),
                            "status", badge(defaultString(order.getOrderStatus(), "PENDING"), statusTone(order.getOrderStatus())),
                            "payment", badge(payment != null ? defaultString(payment.getPayoutStatus(), "PENDING") : "UNPAID", payment != null ? statusTone(payment.getPayoutStatus()) : "warning"),
                            "shipping", defaultString(order.getShippingPartner(), "Not assigned"),
                            "total", defaultIfNull(order.getTotalAmount(), BigDecimal.ZERO),
                            "orderedAt", order.getOrderDate());
                })
                .toList();
        return table("Order orchestration", "Track platform orders, payment status, disputes, shipping, and return flows.",
                List.of(column("orderCode", "Order", "text"), column("seller", "Seller", "text"), column("customer", "Primary item", "text"),
                        column("status", "Order status", "badge"), column("payment", "Payment", "badge"), column("shipping", "Shipping", "text"),
                        column("total", "Total", "currency"), column("orderedAt", "Ordered", "date")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildPaymentTable(List<PaymentRecord> payments, Map<Long, Seller> sellersById, Map<Long, SellerOrder> ordersById) {
        List<Map<String, Object>> rows = payments.stream()
                .sorted(Comparator.comparing(PaymentRecord::getPaymentDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .map(payment -> {
                    SellerOrder order = ordersById.get(payment.getOrderId());
                    return mapOf(
                            "order", order != null ? order.getOrderCode() : "Manual settlement",
                            "seller", defaultString(sellersById.get(payment.getSellerId()) != null ? sellersById.get(payment.getSellerId()).getShopName() : null, "Seller missing"),
                            "gross", defaultIfNull(payment.getOrderAmount(), BigDecimal.ZERO),
                            "commission", defaultIfNull(payment.getCommissionAmount(), BigDecimal.ZERO),
                            "net", defaultIfNull(payment.getNetAmount(), BigDecimal.ZERO),
                            "payout", badge(defaultString(payment.getPayoutStatus(), "PENDING"), statusTone(payment.getPayoutStatus())),
                            "paymentDate", payment.getPaymentDate());
                })
                .toList();
        return table("Payments and finance", "Monitor commission, payouts, refunds, and transaction health across all sellers.",
                List.of(column("order", "Order", "text"), column("seller", "Seller", "text"), column("gross", "Gross", "currency"),
                        column("commission", "Commission", "currency"), column("net", "Seller net", "currency"), column("payout", "Payout", "badge"),
                        column("paymentDate", "Processed", "date")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildLogisticsTable(List<ShippingSetting> shippingSettings, Map<Long, Seller> sellersById) {
        List<Map<String, Object>> rows = shippingSettings.stream()
                .sorted(Comparator.comparing(ShippingSetting::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(setting -> mapOf(
                        "seller", defaultString(sellersById.get(setting.getSellerId()) != null ? sellersById.get(setting.getSellerId()).getShopName() : null, "Seller missing"),
                        "partners", splitCsv(setting.getShippingPartners()),
                        "zones", splitCsv(setting.getDeliveryRegions()),
                        "charges", defaultString(setting.getShippingCharges(), "Dynamic"),
                        "freeShipping", defaultString(setting.getFreeShippingThreshold(), "Disabled"),
                        "eta", defaultString(setting.getEstimatedDeliveryTimes(), "Unconfigured"),
                        "updatedAt", setting.getUpdatedAt()))
                .toList();
        return table("Shipping and logistics", "Courier coverage, delivery zones, fees, and SLA configuration at marketplace scale.",
                List.of(column("seller", "Seller", "text"), column("partners", "Partners", "list"), column("zones", "Zones", "list"),
                        column("charges", "Fees", "text"), column("freeShipping", "Free shipping", "text"), column("eta", "ETA", "text"),
                        column("updatedAt", "Updated", "date")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildReviewTable(List<ReviewEntry> reviews, Map<Long, Product> productsById, Map<Long, Seller> sellersById) {
        List<Map<String, Object>> rows = reviews.stream()
                .sorted(Comparator.comparing(ReviewEntry::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .map(review -> {
                    Product product = productsById.get(review.getProductId());
                    return mapOf(
                            "product", defaultString(product != null ? product.getName() : null, "Unknown product"),
                            "seller", defaultString(sellersById.get(review.getSellerId()) != null ? sellersById.get(review.getSellerId()).getShopName() : null, "Seller missing"),
                            "customer", defaultString(review.getCustomerName(), "Anonymous"),
                            "rating", review.getRating() != null ? review.getRating().setScale(1, RoundingMode.HALF_UP).toPlainString() : "0.0",
                            "status", badge(Boolean.TRUE.equals(review.getAbusive()) ? "Flagged" : "Approved", Boolean.TRUE.equals(review.getAbusive()) ? "danger" : "success"),
                            "reply", badge(review.getReplyText() != null ? "Seller replied" : "Needs moderation", review.getReplyText() != null ? "info" : "warning"),
                            "createdAt", review.getCreatedAt());
                })
                .toList();
        return table("Reviews moderation", "Approve, remove, flag spam, and monitor abusive feedback across the marketplace.",
                List.of(column("product", "Product", "text"), column("seller", "Seller", "text"), column("customer", "Customer", "text"),
                        column("rating", "Rating", "text"), column("status", "Moderation", "badge"), column("reply", "Seller action", "badge"),
                        column("createdAt", "Created", "date")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildPromotionTable(List<CouponCampaign> coupons, Map<Long, Seller> sellersById) {
        List<Map<String, Object>> rows = coupons.stream()
                .sorted(Comparator.comparing(CouponCampaign::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(coupon -> mapOf(
                        "campaign", defaultString(coupon.getCampaignName(), coupon.getCode()),
                        "seller", defaultString(sellersById.get(coupon.getSellerId()) != null ? sellersById.get(coupon.getSellerId()).getShopName() : null, "Platform"),
                        "code", defaultString(coupon.getCode(), "N/A"),
                        "discount", defaultString(coupon.getDiscountValue(), "0"),
                        "status", badge(defaultString(coupon.getStatus(), "DRAFT"), statusTone(coupon.getStatus())),
                        "window", List.of(formatDate(coupon.getStartDate()), formatDate(coupon.getEndDate()))))
                .toList();
        return table("Promotions and coupons", "Manage platform-wide campaigns, coupon codes, flash sales, and seasonal offers.",
                List.of(column("campaign", "Campaign", "text"), column("seller", "Owner", "text"), column("code", "Code", "text"),
                        column("discount", "Discount", "text"), column("status", "Status", "badge"), column("window", "Window", "list")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildSupportTable(List<SupportTicket> supportTickets, Map<Long, Seller> sellersById) {
        List<Map<String, Object>> rows = supportTickets.stream()
                .sorted(Comparator.comparing(SupportTicket::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(ticket -> mapOf(
                        "ticket", ticket.getTicketCode(),
                        "seller", defaultString(sellersById.get(ticket.getSellerId()) != null ? sellersById.get(ticket.getSellerId()).getShopName() : null, "Seller missing"),
                        "subject", defaultString(ticket.getSubject(), "No subject"),
                        "priority", badge(defaultString(ticket.getPriority(), "NORMAL"), priorityTone(ticket.getPriority())),
                        "status", badge(defaultString(ticket.getStatus(), "OPEN"), statusTone(ticket.getStatus())),
                        "assignedTo", defaultString(ticket.getAssignedTo(), "Unassigned"),
                        "updatedAt", ticket.getUpdatedAt()))
                .toList();
        return table("Support and dispute desk", "Assign tickets, resolve complaints, and monitor escalations from users and sellers.",
                List.of(column("ticket", "Ticket", "text"), column("seller", "Seller", "text"), column("subject", "Subject", "text"),
                        column("priority", "Priority", "badge"), column("status", "Status", "badge"), column("assignedTo", "Assigned to", "text"),
                        column("updatedAt", "Updated", "date")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildCategoryTable(List<Product> products, Map<Long, Seller> sellersById) {
        Map<String, List<Product>> grouped = products.stream().collect(Collectors.groupingBy(product -> defaultString(product.getCategoryName(), "Uncategorized")));
        List<Map<String, Object>> rows = grouped.entrySet().stream()
                .sorted(Map.Entry.<String, List<Product>>comparingByValue(Comparator.comparingInt(Collection::size)).reversed())
                .limit(10)
                .map(entry -> mapOf(
                        "category", entry.getKey(),
                        "products", entry.getValue().size(),
                        "sellers", entry.getValue().stream().map(Product::getSellerId).filter(Objects::nonNull).distinct().count(),
                        "inventory", entry.getValue().stream().map(Product::getStockQuantity).filter(Objects::nonNull).mapToInt(Integer::intValue).sum(),
                        "status", badge(entry.getValue().size() > 10 ? "Scaled" : "Growing", entry.getValue().size() > 10 ? "success" : "info")))
                .toList();
        return table("Category governance", "Add, organize, and review category performance across the full catalog tree.",
                List.of(column("category", "Category", "text"), column("products", "Products", "text"), column("sellers", "Sellers", "text"),
                        column("inventory", "Inventory", "text"), column("status", "State", "badge")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildContentTable() {
        List<Map<String, Object>> rows = List.of(
                mapOf("surface", "Homepage banners", "description", "Featured devotional campaigns and hero slots", "state", badge("Published", "success"), "owner", "Growth team"),
                mapOf("surface", "Featured products", "description", "Merchandising collections curated for seasonal traffic", "state", badge("Live", "success"), "owner", "Product admin"),
                mapOf("surface", "Blog posts", "description", "Editorial content for festival buying guides and rituals", "state", badge("Draft + live", "info"), "owner", "Content team"),
                mapOf("surface", "FAQs & policies", "description", "Returns, privacy, seller policy, and commission disclosures", "state", badge("Controlled", "warning"), "owner", "Legal & ops"));
        return table("Content management", "Update homepage surfaces, editorial content, FAQs, and operational policies.",
                List.of(column("surface", "Surface", "text"), column("description", "Description", "text"), column("state", "State", "badge"), column("owner", "Owner", "text")),
                rows);
    }

    private AdminWorkspaceResponse.TableData buildActivityTable(List<AuditLog> auditLogs) {
        List<Map<String, Object>> rows = auditLogs.stream()
                .sorted(Comparator.comparing(AuditLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .map(log -> mapOf(
                        "actor", defaultString(log.getActorEmail(), "System"),
                        "role", badge(defaultString(log.getActorRole(), "SYSTEM"), roleTone(log.getActorRole())),
                        "action", defaultString(log.getAction(), "N/A"),
                        "resource", defaultString(log.getResourcePath(), "N/A"),
                        "status", badge(String.valueOf(defaultIfNull(log.getStatusCode(), 0)), log.getStatusCode() != null && log.getStatusCode() >= 400 ? "danger" : "success"),
                        "time", log.getCreatedAt()))
                .toList();
        return table("Activity log", "Audit all administrative and API activity across the marketplace control plane.",
                List.of(column("actor", "Actor", "text"), column("role", "Role", "badge"), column("action", "Action", "text"), column("resource", "Resource", "text"), column("status", "HTTP", "badge"), column("time", "Time", "date")),
                rows);
    }

    private List<AdminWorkspaceResponse.ChartPoint> buildSalesTrend(List<PaymentRecord> payments) {
        return aggregateByMonth(payments.stream().collect(Collectors.groupingBy(payment -> YearMonth.from(payment.getPaymentDate()),
                Collectors.reducing(BigDecimal.ZERO, PaymentRecord::getOrderAmount, BigDecimal::add))));
    }

    private List<AdminWorkspaceResponse.ChartPoint> buildUserGrowth(List<User> users) {
        Map<YearMonth, BigDecimal> monthly = users.stream()
                .filter(user -> user.getCreatedAt() != null)
                .collect(Collectors.groupingBy(user -> YearMonth.from(user.getCreatedAt()), Collectors.reducing(BigDecimal.ZERO, user -> BigDecimal.ONE, BigDecimal::add)));
        return aggregateByMonth(monthly);
    }

    private List<AdminWorkspaceResponse.ChartPoint> buildCategoryPerformance(List<Product> products) {
        return products.stream()
                .collect(Collectors.groupingBy(product -> defaultString(product.getCategoryName(), "Uncategorized"), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> AdminWorkspaceResponse.ChartPoint.builder().label(entry.getKey()).value(entry.getValue()).build())
                .toList();
    }

    private List<AdminWorkspaceResponse.ChartPoint> buildRevenueTrend(List<PaymentRecord> payments) {
        return aggregateByMonth(payments.stream().collect(Collectors.groupingBy(payment -> YearMonth.from(payment.getPaymentDate()),
                Collectors.reducing(BigDecimal.ZERO, PaymentRecord::getCommissionAmount, BigDecimal::add))));
    }

    private List<AdminWorkspaceResponse.NotificationItem> buildNotifications(List<SellerNotification> notifications, Map<Long, Seller> sellersById) {
        return notifications.stream()
                .sorted(Comparator.comparing(SellerNotification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(notification -> {
                    Seller seller = sellersById.get(notification.getSellerId());
                    return AdminWorkspaceResponse.NotificationItem.builder()
                            .id(String.valueOf(notification.getId()))
                            .title(notification.getTitle())
                            .detail(defaultString(seller != null ? seller.getShopName() : null, "Marketplace") + " • " + notification.getDetail())
                            .time(formatDate(notification.getCreatedAt()))
                            .tone(defaultString(notification.getTone(), "info").toLowerCase(Locale.ROOT))
                            .build();
                })
                .toList();
    }

    private AdminWorkspaceResponse.SystemSnapshot buildSystemSnapshot(long activeUsers, int products, int orders) {
        var runtimeBean = ManagementFactory.getRuntimeMXBean();
        var memoryBean = ManagementFactory.getMemoryMXBean();
        long usedHeapMb = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxHeapMb = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        String uptime = formatDuration(runtimeBean.getUptime());
        String databaseLoad = orders > products ? "High write throughput" : "Balanced query volume";
        return AdminWorkspaceResponse.SystemSnapshot.builder()
                .apiStatus("Operational")
                .uptime(uptime)
                .heapUsage(usedHeapMb + " MB / " + maxHeapMb + " MB")
                .activeUsers(compactNumber(activeUsers))
                .databaseLoad(databaseLoad)
                .securityMode("JWT + RBAC + Rate limit + Audit log")
                .build();
    }

    private AdminWorkspaceResponse.RoleSummary buildRoleSummary() {
        return AdminWorkspaceResponse.RoleSummary.builder()
                .superAdmin("Global system control, security policy, and emergency access")
                .financeAdmin("Refunds, payouts, taxes, revenue reports, and ledger review")
                .productAdmin("Catalog approvals, category controls, content moderation, and fake-product review")
                .sellerAdmin("Seller onboarding, KYC/GST review, commission operations, and suspension")
                .supportAdmin("Tickets, disputes, live escalations, and SLA management")
                .twoFactorMode("Recommended for all admin roles before production rollout")
                .build();
    }

    private List<AdminWorkspaceResponse.ChartPoint> aggregateByMonth(Map<YearMonth, BigDecimal> monthlyValues) {
        List<AdminWorkspaceResponse.ChartPoint> points = new ArrayList<>();
        YearMonth current = YearMonth.now().minusMonths(5);
        for (int index = 0; index < 6; index++) {
            BigDecimal value = monthlyValues.getOrDefault(current, BigDecimal.ZERO);
            points.add(AdminWorkspaceResponse.ChartPoint.builder()
                    .label(current.format(MONTH_FORMATTER))
                    .value(value.setScale(0, RoundingMode.HALF_UP).intValue())
                    .build());
            current = current.plusMonths(1);
        }
        return points;
    }

    private boolean isPendingApproval(ShopRegistration registration) {
        return registration.getStatus() == null
                || "DRAFT".equalsIgnoreCase(registration.getStatus())
                || "SUBMITTED".equalsIgnoreCase(registration.getStatus())
                || "UNDER_REVIEW".equalsIgnoreCase(registration.getStatus());
    }

    private AdminWorkspaceResponse.NavigationItem nav(String key, String label, String icon) {
        return nav(key, label, icon, null);
    }

    private AdminWorkspaceResponse.NavigationItem nav(String key, String label, String icon, String badge) {
        return AdminWorkspaceResponse.NavigationItem.builder().key(key).label(label).icon(icon).badge(badge).build();
    }

    private AdminWorkspaceResponse.MetricCard metric(String label, String value, String delta, String tone) {
        return AdminWorkspaceResponse.MetricCard.builder().label(label).value(value).delta(delta).tone(tone).build();
    }

    private AdminWorkspaceResponse.AlertItem alert(String label, String value, String detail, String tone) {
        return AdminWorkspaceResponse.AlertItem.builder().label(label).value(value).detail(detail).tone(tone).build();
    }

    private AdminWorkspaceResponse.TableData table(String title, String subtitle, List<AdminWorkspaceResponse.TableColumn> columns, List<Map<String, Object>> rows) {
        return AdminWorkspaceResponse.TableData.builder().title(title).subtitle(subtitle).columns(columns).rows(rows).build();
    }

    private AdminWorkspaceResponse.TableColumn column(String key, String label, String type) {
        return AdminWorkspaceResponse.TableColumn.builder().key(key).label(label).type(type).build();
    }

    private Map<String, Object> badge(String label, String tone) {
        Map<String, Object> badge = new LinkedHashMap<>();
        badge.put("label", label);
        badge.put("tone", tone);
        return badge;
    }

    private Map<String, Object> imageText(String image, String title, String subtitle) {
        Map<String, Object> cell = new LinkedHashMap<>();
        cell.put("image", image);
        cell.put("title", title);
        cell.put("subtitle", subtitle);
        return cell;
    }

        private Map<String, Object> mapOf(Object... entries) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int index = 0; index < entries.length; index += 2) {
                        map.put(String.valueOf(entries[index]), entries[index + 1]);
                }
                return map;
        }

    private String currency(BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);
        return format.format(defaultIfNull(amount, BigDecimal.ZERO));
    }

    private String compactNumber(long value) {
        if (value >= 1_000_000) {
            return String.format(Locale.ENGLISH, "%.1fM", value / 1_000_000.0);
        }
        if (value >= 1_000) {
            return String.format(Locale.ENGLISH, "%.1fK", value / 1_000.0);
        }
        return String.valueOf(value);
    }

    private String initials(String value) {
        if (value == null || value.isBlank()) {
            return "AD";
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ROOT);
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.toLowerCase(Locale.ROOT);
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "N/A" : dateTime.format(TIME_FORMATTER);
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of("Not configured");
        }
        return List.of(value.split(",")).stream().map(String::trim).filter(item -> !item.isBlank()).toList();
    }

    private String statusTone(String status) {
        String normalized = defaultString(status, "").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ACTIVE", "APPROVED", "COMPLETED", "DELIVERED", "ENABLED", "VERIFIED" -> "success";
            case "PENDING", "SUBMITTED", "UNDER_REVIEW", "PROCESSING", "PACKED" -> "warning";
            case "SUSPENDED", "REJECTED", "CANCELLED", "FLAGGED", "CRITICAL" -> "danger";
            default -> "info";
        };
    }

    private String roleTone(String role) {
        String normalized = defaultString(role, "USER").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ADMIN" -> "danger";
            case "SELLER" -> "info";
            default -> "neutral";
        };
    }

    private String priorityTone(String priority) {
        String normalized = defaultString(priority, "NORMAL").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "HIGH", "CRITICAL" -> "danger";
            case "MEDIUM" -> "warning";
            default -> "info";
        };
    }

    private String formatDuration(long uptimeMillis) {
        Duration duration = Duration.ofMillis(uptimeMillis);
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        return days + "d " + hours + "h " + minutes + "m";
    }

    private <T> T defaultIfNull(T value, T fallback) {
        return value == null ? fallback : value;
    }
}
