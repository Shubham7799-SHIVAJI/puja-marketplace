package com.SHIVA.puja.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SHIVA.puja.dto.SellerWorkspaceResponse;
import com.SHIVA.puja.entity.CouponCampaign;
import com.SHIVA.puja.entity.CustomerProfile;
import com.SHIVA.puja.entity.InventoryItem;
import com.SHIVA.puja.entity.PaymentRecord;
import com.SHIVA.puja.entity.Product;
import com.SHIVA.puja.entity.ReviewEntry;
import com.SHIVA.puja.entity.Seller;
import com.SHIVA.puja.entity.SellerNotification;
import com.SHIVA.puja.entity.SellerOrder;
import com.SHIVA.puja.entity.ShippingSetting;
import com.SHIVA.puja.entity.ShopRegistration;
import com.SHIVA.puja.entity.SupportTicket;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.CouponCampaignRepository;
import com.SHIVA.puja.repository.CustomerProfileRepository;
import com.SHIVA.puja.repository.InventoryItemRepository;
import com.SHIVA.puja.repository.PaymentRecordRepository;
import com.SHIVA.puja.repository.ProductRepository;
import com.SHIVA.puja.repository.ReviewEntryRepository;
import com.SHIVA.puja.repository.SellerNotificationRepository;
import com.SHIVA.puja.repository.SellerOrderRepository;
import com.SHIVA.puja.repository.SellerRepository;
import com.SHIVA.puja.repository.ShippingSettingRepository;
import com.SHIVA.puja.repository.ShopRegistrationRepository;
import com.SHIVA.puja.repository.SupportTicketRepository;
import com.SHIVA.puja.security.SellerAccessService;
import com.SHIVA.puja.service.SellerWorkspaceService;

@Service
@Transactional
public class SellerWorkspaceServiceImpl implements SellerWorkspaceService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, HH:mm");
    private static final List<String> PENDING_STATUSES = List.of("PENDING", "ACCEPTED", "PROCESSING", "PACKED");

    private final SellerRepository sellerRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ReviewEntryRepository reviewEntryRepository;
    private final CouponCampaignRepository couponCampaignRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ShippingSettingRepository shippingSettingRepository;
    private final SellerNotificationRepository sellerNotificationRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final SellerAccessService sellerAccessService;

    public SellerWorkspaceServiceImpl(
            SellerRepository sellerRepository,
            ShopRegistrationRepository shopRegistrationRepository,
            ProductRepository productRepository,
            InventoryItemRepository inventoryItemRepository,
            SellerOrderRepository sellerOrderRepository,
            CustomerProfileRepository customerProfileRepository,
            ReviewEntryRepository reviewEntryRepository,
            CouponCampaignRepository couponCampaignRepository,
            PaymentRecordRepository paymentRecordRepository,
            ShippingSettingRepository shippingSettingRepository,
            SellerNotificationRepository sellerNotificationRepository,
            SupportTicketRepository supportTicketRepository,
            SellerAccessService sellerAccessService) {
        this.sellerRepository = sellerRepository;
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.reviewEntryRepository = reviewEntryRepository;
        this.couponCampaignRepository = couponCampaignRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.shippingSettingRepository = shippingSettingRepository;
        this.sellerNotificationRepository = sellerNotificationRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.sellerAccessService = sellerAccessService;
    }

    @Override
    public SellerWorkspaceResponse getWorkspace(String registrationId) {
        Seller seller = findOrCreateSeller(registrationId);

        List<Product> products = productRepository.findBySellerId(seller.getId());
        List<InventoryItem> inventoryItems = inventoryItemRepository.findBySellerId(seller.getId());
        List<SellerOrder> orders = sellerOrderRepository.findBySellerId(seller.getId());
        List<CustomerProfile> customers = customerProfileRepository.findTop10BySellerIdOrderByTotalPurchasesDesc(seller.getId());
        List<ReviewEntry> reviews = reviewEntryRepository.findTop10BySellerIdOrderByCreatedAtDesc(seller.getId());
        List<CouponCampaign> coupons = couponCampaignRepository.findTop10BySellerIdOrderByStartDateDesc(seller.getId());
        List<PaymentRecord> payments = paymentRecordRepository.findBySellerId(seller.getId());
        ShippingSetting shippingSetting = shippingSettingRepository.findBySellerId(seller.getId()).orElse(null);
        List<SellerNotification> notifications = sellerNotificationRepository.findTop8BySellerIdOrderByCreatedAtDesc(seller.getId());
        List<SupportTicket> supportTickets = supportTicketRepository.findTop10BySellerIdOrderByUpdatedAtDesc(seller.getId());

        BigDecimal grossRevenue = payments.stream()
                .map(PaymentRecord::getOrderAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long orderCount = sellerOrderRepository.countBySellerId(seller.getId());
        long pendingOrders = sellerOrderRepository.countBySellerIdAndOrderStatusIn(seller.getId(), PENDING_STATUSES);
        long productCount = productRepository.countBySellerId(seller.getId());

        return SellerWorkspaceResponse.builder()
                .navigation(buildNavigation())
                .metrics(buildMetrics(grossRevenue, orderCount, pendingOrders, productCount))
                .lowStockAlerts(buildLowStockAlerts(products, inventoryItems))
                .recentOrders(buildRecentOrders(orders, customers))
                .topProducts(buildTopProducts(products))
                .productTable(buildProductTable(products))
                .inventoryTable(buildInventoryTable(products, inventoryItems))
                .orderTable(buildOrdersTable(orders, customers))
                .customerTable(buildCustomerTable(customers))
                .reviewTable(buildReviewTable(products, reviews))
                .promotionsTable(buildPromotionTable(coupons))
                .paymentTable(buildPaymentTable(orders, payments))
                .shippingAlerts(buildShippingAlerts(shippingSetting, pendingOrders))
                .supportTable(buildSupportTable(supportTickets))
                .revenueTrend(buildRevenueTrend(orders))
                .categoryPerformance(buildCategoryPerformance(products))
                .salesPerDay(buildSalesPerDay(orders))
                .customerGrowth(buildCustomerGrowth(customers))
                .notifications(buildNotifications(notifications))
                .shippingSettings(buildShippingSettings(shippingSetting))
                .shopSettings(buildShopSettings(seller))
                .messages(buildMessages(customers, reviews))
                .build();
    }

    private Seller findOrCreateSeller(String registrationId) {
        if (hasText(registrationId)) {
            return sellerAccessService.resolveSellerByRegistrationId(registrationId);
        }

        return sellerAccessService.resolveManagedSeller(null);
    }

    @SuppressWarnings("unused")
    private Seller createSellerFromRegistration(String registrationId) {
        ShopRegistration registration = shopRegistrationRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SELLER_NOT_FOUND",
                        "No seller registration was found for this dashboard."));

        Seller seller = new Seller();
        seller.setRegistrationId(registration.getRegistrationId());
        seller.setSellerCode("SELLER-" + registration.getShopUniqueId());
        seller.setShopName(defaultString(registration.getShopName(), "Mahakal Seller Hub"));
        seller.setOwnerName(defaultString(registration.getOwnerFullName(), "Seller Admin"));
        seller.setEmail(defaultString(registration.getShopEmail(), registration.getEmail()));
        seller.setPhoneNumber(defaultString(registration.getShopPhoneNumber(), registration.getPhoneNumber()));
        seller.setStatus(defaultString(registration.getStatus(), "ACTIVE"));
        seller.setGstNumber(defaultString(registration.getGstNumber(), "09ABCDE1234F1Z5"));
        seller.setShopAddress(buildAddress(registration));
        seller.setShopLogo("SC");
        seller.setShopBanner(defaultString(registration.getShopCategory(), "Seller storefront"));
        seller.setReturnPolicy("7-day returns for damaged and unopened devotional products.");
        seller.setBankAccountMasked(maskAccountNumber(registration.getAccountNumber()));
        seller.setCreatedAt(LocalDateTime.now());
        seller.setUpdatedAt(LocalDateTime.now());
        return sellerRepository.save(seller);
    }

    @SuppressWarnings("unused")
    private Seller createDemoSeller() {
        Seller seller = new Seller();
        seller.setSellerCode("DEMO-SELLER");
        seller.setShopName("Mahakal Traders");
        seller.setOwnerName("Seller Admin");
        seller.setEmail("seller@mahakal.example");
        seller.setPhoneNumber("+91 95406 94079");
        seller.setStatus("ACTIVE");
        seller.setGstNumber("09ABCDE1234F1Z5");
        seller.setShopAddress("Varanasi, Uttar Pradesh, India");
        seller.setShopLogo("MK");
        seller.setShopBanner("Puja Essentials premium storefront");
        seller.setReturnPolicy("7-day returns for damaged and unopened devotional products.");
        seller.setBankAccountMasked("•••• 2088");
        seller.setCreatedAt(LocalDateTime.now());
        seller.setUpdatedAt(LocalDateTime.now());
        return sellerRepository.save(seller);
    }

    @SuppressWarnings("unused")
    private void ensureDemoData(Seller seller) {
        if (seller == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        Product festivalKit = saveProduct(seller.getId(), "Puja Kits", "Festival Puja Kit", "FPK-102", "Curated puja hamper for festivals.", 1299, 15, 148, "ENABLED", "1.2 kg", "28 x 18 x 8 cm", 4.8, 92, now.minusDays(12));
        Product brassLamp = saveProduct(seller.getId(), "Decor", "Brass Aarti Lamp", "BAL-220", "Premium brass lamp for home temple rituals.", 1899, 10, 62, "FEATURED", "1.8 kg", "24 x 12 x 10 cm", 4.9, 68, now.minusDays(10));
        Product dhoop = saveProduct(seller.getId(), "Dhoop", "Saffron Dhoop Sticks", "SDS-558", "Temple-grade saffron dhoop sticks.", 499, 5, 12, "LOW_STOCK", "300 g", "16 x 5 x 4 cm", 4.2, 55, now.minusDays(8));
        Product diya = saveProduct(seller.getId(), "Decor", "Brass Diya Combo", "BDC-441", "Combo pack of handcrafted brass diyas.", 1499, 12, 94, "ENABLED", "900 g", "20 x 14 x 6 cm", 4.7, 47, now.minusDays(6));
        Product kalash = saveProduct(seller.getId(), "Accessories", "Copper Kalash Set", "CKS-203", "Traditional kalash set for rituals and gifting.", 999, 8, 4, "CRITICAL", "700 g", "18 x 12 x 12 cm", 4.1, 26, now.minusDays(4));

        inventoryItemRepository.saveAll(List.of(
                inventory(seller.getId(), festivalKit.getId(), 148, 14, 20, "Restocked 180 on Mar 10", now.minusHours(8)),
                inventory(seller.getId(), brassLamp.getId(), 62, 9, 15, "New batch received from Jaipur artisan cluster", now.minusHours(6)),
                inventory(seller.getId(), dhoop.getId(), 12, 8, 20, "Stock adjustment after campaign uplift", now.minusHours(2)),
                inventory(seller.getId(), diya.getId(), 94, 11, 18, "Bulk stock update by ops team", now.minusHours(4)),
                inventory(seller.getId(), kalash.getId(), 4, 6, 12, "Critical stock, supplier follow-up pending", now.minusMinutes(55))));

        CustomerProfile anjali = customer("Anjali Sharma", "anjali.sharma@example.com", "+91 98765 42110", 14, 24280, now.minusDays(1), seller.getId());
        CustomerProfile trust = customer("Temple Trust Jaipur", "procurement@templetrust.org", "+91 98110 73122", 5, 128400, now.minusDays(2), seller.getId());
        CustomerProfile mohit = customer("Mohit Verma", "mohit.verma@example.com", "+91 99300 11325", 3, 5890, now.minusDays(3), seller.getId());
        customerProfileRepository.saveAll(List.of(anjali, trust, mohit));

        List<CustomerProfile> savedCustomers = customerProfileRepository.findTop10BySellerIdOrderByTotalPurchasesDesc(seller.getId());
        Map<String, Long> customerIds = savedCustomers.stream().collect(Collectors.toMap(CustomerProfile::getName, CustomerProfile::getId));

        SellerOrder orderOne = order(seller.getId(), customerIds.get("Anjali Sharma"), "ORD-20911", "PROCESSING", "UPI", 2599, "Delhivery", "TRK-1001", festivalKit.getName(), 2, now.minusMinutes(90));
        SellerOrder orderTwo = order(seller.getId(), customerIds.get("Temple Trust Jaipur"), "ORD-20908", "PACKED", "Card", 1499, "Blue Dart", "TRK-1002", brassLamp.getName(), 1, now.minusHours(5));
        SellerOrder orderThree = order(seller.getId(), customerIds.get("Mohit Verma"), "ORD-20896", "PENDING", "COD", 2199, "DTDC", "TRK-1003", diya.getName(), 3, now.minusDays(1));
        SellerOrder orderFour = order(seller.getId(), customerIds.get("Anjali Sharma"), "ORD-20874", "DELIVERED", "Net Banking", 1299, "India Post", "TRK-1004", festivalKit.getName(), 1, now.minusDays(2));
        sellerOrderRepository.saveAll(List.of(orderOne, orderTwo, orderThree, orderFour));

        List<SellerOrder> savedOrders = sellerOrderRepository.findTop10BySellerIdOrderByOrderDateDesc(seller.getId());
        Map<String, Long> orderIds = savedOrders.stream().collect(Collectors.toMap(SellerOrder::getOrderCode, SellerOrder::getId));

        paymentRecordRepository.saveAll(List.of(
                payment(seller.getId(), orderIds.get("ORD-20911"), 2599, 312, 2287, "PENDING", now.minusMinutes(70)),
                payment(seller.getId(), orderIds.get("ORD-20908"), 1499, 180, 1319, "COMPLETED", now.minusHours(4)),
                payment(seller.getId(), orderIds.get("ORD-20896"), 2199, 264, 1935, "UNDER_REVIEW", now.minusDays(1)),
                payment(seller.getId(), orderIds.get("ORD-20874"), 1299, 155, 1144, "COMPLETED", now.minusDays(2))));

        reviewEntryRepository.saveAll(List.of(
                review(seller.getId(), festivalKit.getId(), "Kajal Mehta", 4.8, "Beautiful packaging and complete set.", "Thank you for the detailed review.", false, now.minusHours(7)),
                review(seller.getId(), dhoop.getId(), "Harsh Batra", 3.9, "Fragrance is strong but box arrived bent.", "We are improving packaging for long-distance shipments.", false, now.minusHours(12)),
                review(seller.getId(), brassLamp.getId(), "Renu Kapoor", 5.0, "Quality feels premium and durable.", "Appreciate the trust. We are glad it matched expectations.", false, now.minusDays(1))));

        couponCampaignRepository.saveAll(List.of(
                coupon(seller.getId(), "MAHADEV15", "Shravan Festival Launch", "Percentage", "15%", now.minusDays(1), now.plusDays(15), 1000, "ACTIVE"),
                coupon(seller.getId(), "FESTIVE250", "Festival Flat Discount", "Flat Discount", "Rs 250", now.plusDays(5), now.plusDays(20), 500, "SCHEDULED"),
                coupon(seller.getId(), "BOGO-DIYA", "Buy 1 Get 1 Diya", "Buy 1 Get 1", "Second diya free", now.minusDays(3), now.plusDays(2), 300, "HIGH_USAGE")));

        shippingSettingRepository.save(shippingSetting(seller.getId(), now));

        sellerNotificationRepository.saveAll(List.of(
                notification(seller.getId(), "ORDER", "New order received", "Order #ORD-20911 placed for Festival Puja Kit.", "success", false, now.minusMinutes(2)),
                notification(seller.getId(), "LOW_STOCK", "Low stock alert", "Copper Kalash Set is down to 4 units.", "warning", false, now.minusMinutes(14)),
                notification(seller.getId(), "REVIEW", "Customer review posted", "Brass Aarti Lamp received a 5-star review.", "info", true, now.minusMinutes(41)),
                notification(seller.getId(), "PAYMENT", "Payment processed", "Payout batch March-15-01 credited to registered bank account.", "success", true, now.minusHours(1)),
                notification(seller.getId(), "RETURN", "Order returned", "Return initiated for order #ORD-20896.", "critical", false, now.minusHours(2))));

        supportTicketRepository.saveAll(List.of(
                supportTicket(seller.getId(), "SUP-1192", "Payout reconciliation mismatch", "High", "Open", "Please verify settlement report for payout batch March-15-01.", "Finance Desk", now.minusHours(3)),
                supportTicket(seller.getId(), "SUP-1184", "Bulk CSV upload validation", "Medium", "In Progress", "CSV template rejected product variant rows.", "Catalog Ops", now.minusDays(1)),
                supportTicket(seller.getId(), "SUP-1170", "Courier return dispute", "Low", "Resolved", "Courier marked shipment returned without delivery attempt.", "Logistics", now.minusDays(2))));
    }

    private Product saveProduct(Long sellerId, String category, String name, String sku, String description,
            int price, int discount, int stock, String status, String weight, String dimensions,
            double rating, int reviewCount, LocalDateTime createdAt) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setCategoryName(category);
        product.setName(name);
        product.setSku(sku + "-" + sellerId);
        product.setDescription(description);
        product.setPrice(BigDecimal.valueOf(price));
        product.setDiscountPercent(BigDecimal.valueOf(discount));
        product.setStockQuantity(stock);
        product.setStatus(status);
        product.setWeight(weight);
        product.setDimensions(dimensions);
        product.setRatingAverage(BigDecimal.valueOf(rating).setScale(2, RoundingMode.HALF_UP));
        product.setReviewCount(reviewCount);
        product.setCreatedAt(createdAt);
        product.setUpdatedAt(createdAt);
        return productRepository.save(product);
    }

    private InventoryItem inventory(Long sellerId, Long productId, int available, int reserved, int threshold, String history, LocalDateTime updatedAt) {
        InventoryItem item = new InventoryItem();
        item.setSellerId(sellerId);
        item.setProductId(productId);
        item.setAvailableStock(available);
        item.setReservedStock(reserved);
        item.setLowStockThreshold(threshold);
        item.setStockHistory(history);
        item.setUpdatedAt(updatedAt);
        return item;
    }

    private CustomerProfile customer(String name, String email, String phone, int totalOrders, int totalPurchases,
            LocalDateTime lastOrderAt, Long sellerId) {
        CustomerProfile customer = new CustomerProfile();
        customer.setSellerId(sellerId);
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
        customer.setTotalOrders(totalOrders);
        customer.setTotalPurchases(BigDecimal.valueOf(totalPurchases));
        customer.setLastOrderAt(lastOrderAt);
        return customer;
    }

    private SellerOrder order(Long sellerId, Long customerId, String code, String status, String paymentMethod,
            int totalAmount, String shippingPartner, String trackingNumber, String productName, int quantity,
            LocalDateTime orderDate) {
        SellerOrder order = new SellerOrder();
        order.setSellerId(sellerId);
        order.setCustomerId(customerId);
        order.setOrderCode(code);
        order.setOrderStatus(status);
        order.setPaymentMethod(paymentMethod);
        order.setTotalAmount(BigDecimal.valueOf(totalAmount));
        order.setShippingPartner(shippingPartner);
        order.setTrackingNumber(trackingNumber);
        order.setPrimaryProductName(productName);
        order.setTotalQuantity(quantity);
        order.setOrderDate(orderDate);
        return order;
    }

    private PaymentRecord payment(Long sellerId, Long orderId, int orderAmount, int commissionAmount,
            int netAmount, String payoutStatus, LocalDateTime paymentDate) {
        PaymentRecord payment = new PaymentRecord();
        payment.setSellerId(sellerId);
        payment.setOrderId(orderId);
        payment.setOrderAmount(BigDecimal.valueOf(orderAmount));
        payment.setCommissionAmount(BigDecimal.valueOf(commissionAmount));
        payment.setNetAmount(BigDecimal.valueOf(netAmount));
        payment.setPayoutStatus(payoutStatus);
        payment.setPaymentDate(paymentDate);
        return payment;
    }

    private ReviewEntry review(Long sellerId, Long productId, String customerName, double rating,
            String reviewText, String replyText, boolean abusive, LocalDateTime createdAt) {
        ReviewEntry review = new ReviewEntry();
        review.setSellerId(sellerId);
        review.setProductId(productId);
        review.setCustomerName(customerName);
        review.setRating(BigDecimal.valueOf(rating).setScale(2, RoundingMode.HALF_UP));
        review.setReviewText(reviewText);
        review.setReplyText(replyText);
        review.setAbusive(abusive);
        review.setCreatedAt(createdAt);
        return review;
    }

    private CouponCampaign coupon(Long sellerId, String code, String campaignName, String discountType,
            String discountValue, LocalDateTime startDate, LocalDateTime endDate, int usageLimit, String status) {
        CouponCampaign coupon = new CouponCampaign();
        coupon.setSellerId(sellerId);
        coupon.setCode(code + "-" + sellerId);
        coupon.setCampaignName(campaignName);
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);
        coupon.setStartDate(startDate);
        coupon.setEndDate(endDate);
        coupon.setUsageLimit(usageLimit);
        coupon.setStatus(status);
        return coupon;
    }

    private ShippingSetting shippingSetting(Long sellerId, LocalDateTime updatedAt) {
        ShippingSetting shipping = new ShippingSetting();
        shipping.setSellerId(sellerId);
        shipping.setShippingPartners("Delhivery,Blue Dart,DTDC,India Post");
        shipping.setShippingCharges("Rs 65 standard / Rs 110 express");
        shipping.setDeliveryRegions("North India,West India,South Metro,Tier-2 Express");
        shipping.setFreeShippingThreshold("Rs 799");
        shipping.setEstimatedDeliveryTimes("2-5 business days");
        shipping.setUpdatedAt(updatedAt);
        return shipping;
    }

    private SellerNotification notification(Long sellerId, String type, String title, String detail, String tone,
            boolean read, LocalDateTime createdAt) {
        SellerNotification notification = new SellerNotification();
        notification.setSellerId(sellerId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setDetail(detail);
        notification.setTone(tone);
        notification.setReadStatus(read);
        notification.setCreatedAt(createdAt);
        return notification;
    }

    private SupportTicket supportTicket(Long sellerId, String code, String subject, String priority,
            String status, String message, String assignedTo, LocalDateTime updatedAt) {
        SupportTicket ticket = new SupportTicket();
        ticket.setSellerId(sellerId);
        ticket.setTicketCode(code + "-" + sellerId);
        ticket.setSubject(subject);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setMessage(message);
        ticket.setAssignedTo(assignedTo);
        ticket.setUpdatedAt(updatedAt);
        return ticket;
    }

    private List<SellerWorkspaceResponse.NavigationItem> buildNavigation() {
        return List.of(
                nav("dashboard", "Dashboard", "grid", null),
                nav("products", "Products", "cube", null),
                nav("add-product", "Add Product", "plus", null),
                nav("inventory", "Inventory", "layers", "18"),
                nav("orders", "Orders", "cart", "64"),
                nav("customers", "Customers", "users", null),
                nav("reviews", "Reviews", "star", null),
                nav("promotions", "Promotions", "ticket", null),
                nav("analytics", "Analytics", "chart", null),
                nav("payments", "Payments", "wallet", null),
                nav("shipping", "Shipping", "truck", null),
                nav("messages", "Messages", "chat", "2"),
                nav("settings", "Settings", "settings", null),
                nav("support", "Support", "support", null));
    }

    private List<SellerWorkspaceResponse.MetricCard> buildMetrics(BigDecimal grossRevenue, long orderCount,
            long pendingOrders, long productCount) {
        return List.of(
                metric("Total Revenue", formatCompactRupees(grossRevenue), "+14.8% vs last month", "revenue"),
                metric("Total Orders", String.valueOf(orderCount), "+112 today", "orders"),
                metric("Pending Orders", String.valueOf(pendingOrders), "Operational queue needs attention", "warning"),
                metric("Products Count", String.valueOf(productCount), "Catalog ready for bulk upload", "success"));
    }

    private List<SellerWorkspaceResponse.AlertItem> buildLowStockAlerts(List<Product> products, List<InventoryItem> inventoryItems) {
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, product -> product));
        return inventoryItems.stream()
                .filter(item -> item.getAvailableStock() <= item.getLowStockThreshold())
                .sorted(Comparator.comparingInt(InventoryItem::getAvailableStock))
                .limit(5)
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    String tone = item.getAvailableStock() <= 5 ? "danger" : "warning";
                    return SellerWorkspaceResponse.AlertItem.builder()
                            .label(item.getAvailableStock() <= 5 ? "Critical" : "Low Stock")
                            .value(item.getAvailableStock() + " units")
                            .detail(product.getName() + " • SKU " + product.getSku())
                            .tone(tone)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private SellerWorkspaceResponse.TableData buildRecentOrders(List<SellerOrder> orders, List<CustomerProfile> customers) {
        return table("Recent Orders", "Operational queue for the last 24 hours.",
                List.of(column("orderId", "Order ID", null), column("customer", "Customer Name", null),
                        column("product", "Product", null), column("quantity", "Quantity", null),
                        column("price", "Price", "currency"), column("date", "Order Date", "date"),
                        column("payment", "Payment Method", null), column("status", "Order Status", "badge")),
                orders.stream().sorted(Comparator.comparing(SellerOrder::getOrderDate).reversed()).limit(5)
                        .map(order -> mapOf(
                                "orderId", "#" + order.getOrderCode(),
                                "customer", findCustomerName(customers, order.getCustomerId()),
                                "product", order.getPrimaryProductName(),
                                "quantity", order.getTotalQuantity(),
                                "price", order.getTotalAmount(),
                                "date", order.getOrderDate(),
                                "payment", order.getPaymentMethod(),
                                "status", badge(orderStatusLabel(order.getOrderStatus()), badgeTone(order.getOrderStatus()))))
                        .collect(Collectors.toList()));
    }

    private SellerWorkspaceResponse.TableData buildTopProducts(List<Product> products) {
        return table("Top Selling Products", "Best performers by revenue contribution.",
                List.of(column("product", "Product", "imageText"), column("sku", "SKU", null),
                        column("category", "Category", null), column("stock", "Stock", null),
                        column("price", "Price", "currency"), column("status", "Status", "badge")),
                products.stream().sorted(Comparator.comparing(Product::getReviewCount).reversed()).limit(5)
                        .map(product -> mapOf(
                                "product", imageText(productImage(product), product.getName(), product.getReviewCount() + " reviews"),
                                "sku", product.getSku(),
                                "category", product.getCategoryName(),
                                "stock", product.getStockQuantity(),
                                "price", product.getPrice(),
                                "status", badge(productStatusLabel(product.getStatus()), badgeTone(product.getStatus()))))
                        .collect(Collectors.toList()));
    }

    private SellerWorkspaceResponse.TableData buildProductTable(List<Product> products) {
        return table("Product Management", "Add, edit, disable, and bulk manage catalog listings.",
                List.of(column("product", "Product Image", "imageText"), column("sku", "SKU", null),
                        column("category", "Category", null), column("price", "Price", "currency"),
                        column("discount", "Discount", null), column("stock", "Stock", null),
                        column("status", "Status", "badge"), column("actions", "Actions", "list")),
                products.stream().sorted(Comparator.comparing(Product::getCreatedAt).reversed()).limit(10)
                        .map(product -> mapOf(
                                "product", imageText(productImage(product), product.getName(), product.getWeight() + " • " + product.getDimensions()),
                                "sku", product.getSku(),
                                "category", product.getCategoryName(),
                                "price", product.getPrice(),
                                "discount", product.getDiscountPercent().stripTrailingZeros().toPlainString() + "%",
                                "stock", product.getStockQuantity(),
                                "status", badge(productStatusLabel(product.getStatus()), badgeTone(product.getStatus())),
                                "actions", List.of("Edit", "Delete", isEnabled(product) ? "Disable" : "Enable")))
                        .collect(Collectors.toList()));
    }

    private SellerWorkspaceResponse.TableData buildInventoryTable(List<Product> products, List<InventoryItem> inventoryItems) {
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, product -> product));
        return table("Inventory Management", "Stock levels, bulk updates, and movement history.",
                List.of(column("product", "Product", null), column("warehouse", "Warehouse", null),
                        column("available", "Available", null), column("reserved", "Reserved", null),
                        column("history", "Stock History", null), column("status", "Status", "badge")),
                inventoryItems.stream().sorted(Comparator.comparingInt(InventoryItem::getAvailableStock)).limit(10)
                        .map(item -> {
                            Product product = productMap.get(item.getProductId());
                            String status = item.getAvailableStock() <= 5 ? "Critical" : item.getAvailableStock() <= item.getLowStockThreshold() ? "Low Stock" : "Healthy";
                            String tone = item.getAvailableStock() <= 5 ? "danger" : item.getAvailableStock() <= item.getLowStockThreshold() ? "warning" : "success";
                            return mapOf(
                                    "product", product.getName(),
                                    "warehouse", deriveWarehouse(product.getCategoryName()),
                                    "available", item.getAvailableStock(),
                                    "reserved", item.getReservedStock(),
                                    "history", item.getStockHistory(),
                                    "status", badge(status, tone));
                        })
                        .collect(Collectors.toList()));
    }

    private SellerWorkspaceResponse.TableData buildOrdersTable(List<SellerOrder> orders, List<CustomerProfile> customers) {
        return table("Order Management", "Accept orders, print invoices, track shipments, and process returns.",
                List.of(column("orderId", "Order ID", null), column("customer", "Customer Name", null),
                        column("product", "Product", null), column("quantity", "Quantity", null),
                        column("price", "Price", "currency"), column("date", "Order Date", "date"),
                        column("payment", "Payment Method", null), column("status", "Order Status", "badge")),
                orders.stream().sorted(Comparator.comparing(SellerOrder::getOrderDate).reversed()).limit(10)
                        .map(order -> mapOf(
                                "orderId", "#" + order.getOrderCode(),
                                "customer", findCustomerName(customers, order.getCustomerId()),
                                "product", order.getPrimaryProductName(),
                                "quantity", order.getTotalQuantity(),
                                "price", order.getTotalAmount(),
                                "date", order.getOrderDate(),
                                "payment", order.getPaymentMethod(),
                                "status", badge(orderStatusLabel(order.getOrderStatus()), badgeTone(order.getOrderStatus()))))
                        .collect(Collectors.toList()));
    }

    private SellerWorkspaceResponse.TableData buildCustomerTable(List<CustomerProfile> customers) {
        return table("Customer Management", "Profile view, purchase history, and messaging insights.",
                List.of(column("customer", "Customer Name", null), column("email", "Email", null),
                        column("phone", "Phone", null), column("history", "Order History", null),
                        column("purchases", "Total Purchases", "currency")),
                customers.stream().limit(10)
                        .map(customer -> mapOf(
                                "customer", customer.getName(),
                                "email", customer.getEmail(),
                                "phone", customer.getPhoneNumber(),
                                "history", customer.getTotalOrders() + " orders in lifecycle",
                                "purchases", customer.getTotalPurchases()))
                        .collect(Collectors.toList()));
    }

    private SellerWorkspaceResponse.TableData buildReviewTable(List<Product> products, List<ReviewEntry> reviews) {
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, product -> product));
        return table("Reviews & Ratings", "Reply to customers and report abusive content.",
                List.of(column("product", "Product", null), column("customer", "Customer", null),
                        column("rating", "Rating", null), column("review", "Review", null),
                        column("action", "Actions", "list")),
                reviews.stream().limit(10)
                        .map(review -> mapOf(
                                "product", productMap.get(review.getProductId()).getName(),
                                "customer", review.getCustomerName(),
                                "rating", review.getRating().stripTrailingZeros().toPlainString() + " / 5",
                                "review", review.getReviewText(),
                                "action", List.of("Reply", "Report")))
                        .collect(Collectors.toList()));
    }

    private SellerWorkspaceResponse.TableData buildPromotionTable(List<CouponCampaign> coupons) {
        return table("Promotions & Discounts", "Coupons, campaigns, BOGO offers, and seasonal activations.",
                List.of(column("code", "Coupon Code", null), column("type", "Discount Type", null),
                        column("value", "Discount Value", null), column("start", "Start Date", "date"),
                        column("end", "End Date", "date"), column("limit", "Usage Limit", null),
                        column("status", "Status", "badge")),
                coupons.stream().limit(10)
                        .map(coupon -> mapOf(
                                "code", coupon.getCode(),
                                "type", coupon.getDiscountType(),
                                "value", coupon.getDiscountValue(),
                                "start", coupon.getStartDate(),
                                "end", coupon.getEndDate(),
                                "limit", coupon.getUsageLimit() + " redemptions",
                                "status", badge(couponStatusLabel(coupon.getStatus()), badgeTone(coupon.getStatus()))))
                        .collect(Collectors.toList()));
    }

    private SellerWorkspaceResponse.TableData buildPaymentTable(List<SellerOrder> orders, List<PaymentRecord> payments) {
        Map<Long, String> orderCodes = orders.stream().collect(Collectors.toMap(SellerOrder::getId, SellerOrder::getOrderCode));
        return table("Payments & Revenue", "Track commissions, net earnings, and payout statuses.",
                List.of(column("orderId", "Order ID", null), column("amount", "Order Amount", "currency"),
                        column("commission", "Commission", "currency"), column("net", "Net Amount", "currency"),
                        column("status", "Payout Status", "badge"), column("date", "Date", "date")),
                payments.stream().sorted(Comparator.comparing(PaymentRecord::getPaymentDate).reversed()).limit(10)
                        .map(payment -> mapOf(
                                "orderId", "#" + orderCodes.getOrDefault(payment.getOrderId(), "N/A"),
                                "amount", payment.getOrderAmount(),
                                "commission", payment.getCommissionAmount(),
                                "net", payment.getNetAmount(),
                                "status", badge(couponStatusLabel(payment.getPayoutStatus()), badgeTone(payment.getPayoutStatus())),
                                "date", payment.getPaymentDate()))
                        .collect(Collectors.toList()));
    }

    private List<SellerWorkspaceResponse.AlertItem> buildShippingAlerts(ShippingSetting shippingSetting, long pendingOrders) {
        List<SellerWorkspaceResponse.AlertItem> alerts = new ArrayList<>();
        alerts.add(alert("Courier Delay", Math.max(pendingOrders, 3) + " orders", "Courier pickup queue needs review before 6 PM cutoff.", "warning"));
        if (shippingSetting != null) {
            alerts.add(alert("Free Shipping", shippingSetting.getFreeShippingThreshold(), "Current threshold is configured for conversion lift.", "info"));
            alerts.add(alert("Delivery SLA", shippingSetting.getEstimatedDeliveryTimes(), "Shipping partners cover primary delivery regions.", "danger"));
        }
        return alerts;
    }

    private SellerWorkspaceResponse.TableData buildSupportTable(List<SupportTicket> supportTickets) {
        return table("Support Tickets", "Operational issues, payout queries, and admin chat threads.",
                List.of(column("ticket", "Ticket ID", null), column("topic", "Topic", null),
                        column("priority", "Priority", "badge"), column("status", "Status", "badge"),
                        column("owner", "Assigned To", null), column("updated", "Updated", "date")),
                supportTickets.stream().limit(10)
                        .map(ticket -> mapOf(
                                "ticket", ticket.getTicketCode(),
                                "topic", ticket.getSubject(),
                                "priority", badge(ticket.getPriority(), badgeTone(ticket.getPriority())),
                                "status", badge(ticket.getStatus(), badgeTone(ticket.getStatus())),
                                "owner", ticket.getAssignedTo(),
                                "updated", ticket.getUpdatedAt()))
                        .collect(Collectors.toList()));
    }

    private List<SellerWorkspaceResponse.ChartPoint> buildRevenueTrend(List<SellerOrder> orders) {
        Map<String, BigDecimal> revenueByDay = new LinkedHashMap<>();
        orders.stream().sorted(Comparator.comparing(SellerOrder::getOrderDate)).forEach(order -> {
            String label = order.getOrderDate().getDayOfWeek().name().substring(0, 3);
            revenueByDay.merge(label, order.getTotalAmount(), BigDecimal::add);
        });
        return chartFromBigDecimalMap(revenueByDay);
    }

    private List<SellerWorkspaceResponse.ChartPoint> buildCategoryPerformance(List<Product> products) {
        Map<String, Integer> categoryScores = new LinkedHashMap<>();
        products.forEach(product -> categoryScores.merge(product.getCategoryName(), product.getReviewCount(), Integer::sum));
        return categoryScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> chartPoint(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<SellerWorkspaceResponse.ChartPoint> buildSalesPerDay(List<SellerOrder> orders) {
        Map<String, Integer> orderCounts = new LinkedHashMap<>();
        orders.stream().sorted(Comparator.comparing(SellerOrder::getOrderDate)).forEach(order -> {
            String label = String.valueOf(order.getOrderDate().getDayOfMonth());
            orderCounts.merge(label, order.getTotalQuantity(), Integer::sum);
        });
        return orderCounts.entrySet().stream()
                .map(entry -> chartPoint(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<SellerWorkspaceResponse.ChartPoint> buildCustomerGrowth(List<CustomerProfile> customers) {
        List<SellerWorkspaceResponse.ChartPoint> growth = new ArrayList<>();
        int cumulative = 0;
        int month = 1;
        for (CustomerProfile customer : customers) {
            cumulative += customer.getTotalOrders();
            growth.add(chartPoint("M" + month++, cumulative));
        }
        if (growth.isEmpty()) {
            return List.of(chartPoint("M1", 0));
        }
        return growth;
    }

    private List<SellerWorkspaceResponse.NotificationItem> buildNotifications(List<SellerNotification> notifications) {
        return notifications.stream().limit(8)
                .map(notification -> SellerWorkspaceResponse.NotificationItem.builder()
                        .id("notif-" + notification.getId())
                        .title(notification.getTitle())
                        .detail(notification.getDetail())
                        .time(notification.getCreatedAt().format(TIME_FORMATTER))
                        .tone(notification.getTone())
                        .build())
                .collect(Collectors.toList());
    }

    private SellerWorkspaceResponse.ShippingSettings buildShippingSettings(ShippingSetting shippingSetting) {
        if (shippingSetting == null) {
            return SellerWorkspaceResponse.ShippingSettings.builder()
                    .partners(List.of())
                    .deliveryRegions(List.of())
                    .shippingCharge("Not configured")
                    .freeShippingThreshold("Not configured")
                    .estimatedDelivery("Not configured")
                    .build();
        }

        return SellerWorkspaceResponse.ShippingSettings.builder()
                .partners(splitCsv(shippingSetting.getShippingPartners()))
                .deliveryRegions(splitCsv(shippingSetting.getDeliveryRegions()))
                .shippingCharge(shippingSetting.getShippingCharges())
                .freeShippingThreshold(shippingSetting.getFreeShippingThreshold())
                .estimatedDelivery(shippingSetting.getEstimatedDeliveryTimes())
                .build();
    }

    private SellerWorkspaceResponse.ShopSettingsSnapshot buildShopSettings(Seller seller) {
        return SellerWorkspaceResponse.ShopSettingsSnapshot.builder()
                .shopName(seller.getShopName())
                .logo(defaultString(seller.getShopLogo(), "SC"))
                .banner(defaultString(seller.getShopBanner(), "Seller storefront"))
                .address(defaultString(seller.getShopAddress(), "India"))
                .returnPolicy(defaultString(seller.getReturnPolicy(), "Return policy unavailable"))
                .gstNumber(defaultString(seller.getGstNumber(), "GST pending"))
                .bankAccount(defaultString(seller.getBankAccountMasked(), "•••• 0000"))
                .build();
    }

    private List<SellerWorkspaceResponse.MessageItem> buildMessages(List<CustomerProfile> customers, List<ReviewEntry> reviews) {
        List<SellerWorkspaceResponse.MessageItem> messages = new ArrayList<>();
        if (!customers.isEmpty()) {
            CustomerProfile first = customers.get(0);
            messages.add(message(first.getName(), "Bulk order inquiry", "Need pricing support for temple procurement order.", true));
        }
        if (customers.size() > 1) {
            CustomerProfile second = customers.get(1);
            messages.add(message(second.getName(), "Delivery delay follow-up", "Customer wants updated dispatch estimate.", true));
        }
        if (!reviews.isEmpty()) {
            ReviewEntry review = reviews.get(0);
            messages.add(message(review.getCustomerName(), "Review follow-up", review.getReviewText(), false));
        }
        return messages;
    }

    private SellerWorkspaceResponse.NavigationItem nav(String key, String label, String icon, String badge) {
        return SellerWorkspaceResponse.NavigationItem.builder().key(key).label(label).icon(icon).badge(badge).build();
    }

    private SellerWorkspaceResponse.MetricCard metric(String label, String value, String delta, String tone) {
        return SellerWorkspaceResponse.MetricCard.builder().label(label).value(value).delta(delta).tone(tone).build();
    }

    private SellerWorkspaceResponse.AlertItem alert(String label, String value, String detail, String tone) {
        return SellerWorkspaceResponse.AlertItem.builder().label(label).value(value).detail(detail).tone(tone).build();
    }

    private SellerWorkspaceResponse.TableData table(String title, String subtitle,
            List<SellerWorkspaceResponse.TableColumn> columns, List<Map<String, Object>> rows) {
        return SellerWorkspaceResponse.TableData.builder().title(title).subtitle(subtitle).columns(columns).rows(rows).build();
    }

    private SellerWorkspaceResponse.TableColumn column(String key, String label, String type) {
        return SellerWorkspaceResponse.TableColumn.builder().key(key).label(label).type(type).build();
    }

    private SellerWorkspaceResponse.ChartPoint chartPoint(String label, Number value) {
        return SellerWorkspaceResponse.ChartPoint.builder().label(label).value(value).build();
    }

    private SellerWorkspaceResponse.MessageItem message(String customer, String subject, String excerpt, boolean unread) {
        return SellerWorkspaceResponse.MessageItem.builder().customer(customer).subject(subject).excerpt(excerpt).unread(unread).build();
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            map.put((String) values[index], values[index + 1]);
        }
        return map;
    }

    private Map<String, Object> badge(String label, String tone) {
        return mapOf("label", label, "tone", tone);
    }

    private Map<String, Object> imageText(String image, String title, String subtitle) {
        return mapOf("image", image, "title", title, "subtitle", subtitle);
    }

    private List<SellerWorkspaceResponse.ChartPoint> chartFromBigDecimalMap(Map<String, BigDecimal> values) {
        return values.entrySet().stream()
                .map(entry -> chartPoint(entry.getKey(), entry.getValue().setScale(0, RoundingMode.HALF_UP).intValue()))
                .collect(Collectors.toList());
    }

    private String findCustomerName(List<CustomerProfile> customers, Long customerId) {
        if (customerId == null) {
            return "Guest Customer";
        }
        return customers.stream()
                .filter(customer -> customerId.equals(customer.getId()))
                .map(CustomerProfile::getName)
                .findFirst()
                .orElse("Customer");
    }

    private String deriveWarehouse(String categoryName) {
        if ("Decor".equalsIgnoreCase(categoryName)) {
            return "Jaipur Hub";
        }
        if ("Dhoop".equalsIgnoreCase(categoryName)) {
            return "Lucknow DC";
        }
        return "Noida FC";
    }

    private List<String> splitCsv(String value) {
        return List.of(value.split(","));
    }

    private String buildAddress(ShopRegistration registration) {
        return List.of(registration.getAddressLine1(), registration.getCity(), registration.getState(), registration.getPincode())
                .stream()
                .filter(this::hasText)
                .collect(Collectors.joining(", "));
    }

    private String maskAccountNumber(String accountNumber) {
        if (!hasText(accountNumber) || accountNumber.length() < 4) {
            return "•••• 2088";
        }
        return "•••• " + accountNumber.substring(accountNumber.length() - 4);
    }

    private String formatCompactRupees(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(100000)) >= 0) {
            BigDecimal lakhs = amount.divide(BigDecimal.valueOf(100000), 2, RoundingMode.HALF_UP);
            return "Rs " + lakhs.stripTrailingZeros().toPlainString() + "L";
        }
        NumberFormat format = NumberFormat.getIntegerInstance(new Locale("en", "IN"));
        return "Rs " + format.format(amount.setScale(0, RoundingMode.HALF_UP));
    }

    private String orderStatusLabel(String status) {
        return toDisplay(status);
    }

    private String productStatusLabel(String status) {
        if ("ENABLED".equalsIgnoreCase(status)) {
            return "Enabled";
        }
        if ("FEATURED".equalsIgnoreCase(status)) {
            return "Featured";
        }
        if ("LOW_STOCK".equalsIgnoreCase(status)) {
            return "Low Stock";
        }
        if ("CRITICAL".equalsIgnoreCase(status)) {
            return "Critical";
        }
        return toDisplay(status);
    }

    private String couponStatusLabel(String status) {
        return toDisplay(status);
    }

    private String badgeTone(String status) {
        String normalized = defaultString(status, "").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "DELIVERED", "COMPLETED", "ENABLED", "FEATURED", "ACTIVE", "RESOLVED", "HEALTHY" -> "success";
            case "PENDING", "PACKED", "LOW_STOCK", "HIGH_USAGE", "OPEN" -> "warning";
            case "RETURNED", "REFUNDED", "DISABLED", "CRITICAL", "CANCELLED", "HIGH" -> "danger";
            default -> "info";
        };
    }

    private String toDisplay(String raw) {
        String normalized = defaultString(raw, "N/A").replace('_', ' ').toLowerCase(Locale.ROOT);
        String[] parts = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private String productImage(Product product) {
        return switch (product.getCategoryName()) {
            case "Decor" -> "https://images.unsplash.com/photo-1515377905703-c4788e51af15?auto=format&fit=crop&w=120&q=80";
            case "Dhoop" -> "https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?auto=format&fit=crop&w=120&q=80";
            case "Accessories" -> "https://images.unsplash.com/photo-1609599006353-e629aaabfeae?auto=format&fit=crop&w=120&q=80";
            default -> "https://images.unsplash.com/photo-1631646109661-7d0bdc54f5f2?auto=format&fit=crop&w=120&q=80";
        };
    }

    private boolean isEnabled(Product product) {
        return "ENABLED".equalsIgnoreCase(product.getStatus()) || "FEATURED".equalsIgnoreCase(product.getStatus());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultString(String primary, String fallback) {
        return hasText(primary) ? primary : fallback;
    }
}
