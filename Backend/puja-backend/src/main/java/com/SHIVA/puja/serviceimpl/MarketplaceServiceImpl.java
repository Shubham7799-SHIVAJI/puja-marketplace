package com.SHIVA.puja.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.SHIVA.puja.dto.OrderResponse;
import com.SHIVA.puja.dto.PageResponse;
import com.SHIVA.puja.dto.ProductResponse;
import com.SHIVA.puja.dto.ReviewResponse;
import com.SHIVA.puja.dto.WishlistItemResponse;
import com.SHIVA.puja.entity.Category;
import com.SHIVA.puja.entity.CouponCampaign;
import com.SHIVA.puja.entity.CustomerAddress;
import com.SHIVA.puja.entity.CustomerNotification;
import com.SHIVA.puja.entity.CustomerOrderLink;
import com.SHIVA.puja.entity.CustomerProfile;
import com.SHIVA.puja.entity.InventoryItem;
import com.SHIVA.puja.entity.OrderItem;
import com.SHIVA.puja.entity.OrderPayment;
import com.SHIVA.puja.entity.OrderShippingDetail;
import com.SHIVA.puja.entity.PaymentRecord;
import com.SHIVA.puja.entity.Product;
import com.SHIVA.puja.entity.ProductImage;
import com.SHIVA.puja.entity.ProductVariant;
import com.SHIVA.puja.entity.ReviewEntry;
import com.SHIVA.puja.entity.Seller;
import com.SHIVA.puja.entity.SellerOrder;
import com.SHIVA.puja.entity.ShippingSetting;
import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.entity.WishlistItem;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.CategoryRepository;
import com.SHIVA.puja.repository.CouponCampaignRepository;
import com.SHIVA.puja.repository.CustomerAddressRepository;
import com.SHIVA.puja.repository.CustomerNotificationRepository;
import com.SHIVA.puja.repository.CustomerOrderLinkRepository;
import com.SHIVA.puja.repository.CustomerProfileRepository;
import com.SHIVA.puja.repository.InventoryItemRepository;
import com.SHIVA.puja.repository.OrderItemRepository;
import com.SHIVA.puja.repository.OrderPaymentRepository;
import com.SHIVA.puja.repository.OrderShippingDetailRepository;
import com.SHIVA.puja.repository.PaymentRecordRepository;
import com.SHIVA.puja.repository.ProductImageRepository;
import com.SHIVA.puja.repository.ProductRepository;
import com.SHIVA.puja.repository.ProductVariantRepository;
import com.SHIVA.puja.repository.ReviewEntryRepository;
import com.SHIVA.puja.repository.SellerOrderRepository;
import com.SHIVA.puja.repository.SellerRepository;
import com.SHIVA.puja.repository.ShippingSettingRepository;
import com.SHIVA.puja.repository.UserRepository;
import com.SHIVA.puja.repository.WishlistItemRepository;
import com.SHIVA.puja.service.MarketplaceService;

@Service
@Transactional
public class MarketplaceServiceImpl implements MarketplaceService {

    private static final Pattern MONEY_PATTERN = Pattern.compile("(\\d+(?:\\.\\d{1,2})?)");
    private static final Set<String> HIDDEN_PRODUCT_STATUSES = Set.of("BLOCKED", "REJECTED", "DRAFT", "PENDING", "PENDING_APPROVAL", "INACTIVE");
    private static final BigDecimal DEFAULT_COMMISSION_RATE = BigDecimal.valueOf(0.12);

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewEntryRepository reviewEntryRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderShippingDetailRepository orderShippingDetailRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final CustomerOrderLinkRepository customerOrderLinkRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ShippingSettingRepository shippingSettingRepository;
    private final CouponCampaignRepository couponCampaignRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final CustomerNotificationRepository customerNotificationRepository;

    public MarketplaceServiceImpl(ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductVariantRepository productVariantRepository,
            SellerRepository sellerRepository,
            CategoryRepository categoryRepository,
            ReviewEntryRepository reviewEntryRepository,
            WishlistItemRepository wishlistItemRepository,
            UserRepository userRepository,
            CustomerAddressRepository customerAddressRepository,
            CustomerProfileRepository customerProfileRepository,
            SellerOrderRepository sellerOrderRepository,
            OrderItemRepository orderItemRepository,
            OrderShippingDetailRepository orderShippingDetailRepository,
            OrderPaymentRepository orderPaymentRepository,
            CustomerOrderLinkRepository customerOrderLinkRepository,
            PaymentRecordRepository paymentRecordRepository,
            ShippingSettingRepository shippingSettingRepository,
            CouponCampaignRepository couponCampaignRepository,
            InventoryItemRepository inventoryItemRepository,
            CustomerNotificationRepository customerNotificationRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.sellerRepository = sellerRepository;
        this.categoryRepository = categoryRepository;
        this.reviewEntryRepository = reviewEntryRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderShippingDetailRepository = orderShippingDetailRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.customerOrderLinkRepository = customerOrderLinkRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.shippingSettingRepository = shippingSettingRepository;
        this.couponCampaignRepository = couponCampaignRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.customerNotificationRepository = customerNotificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "marketplaceCatalog", key = "(#query ?: '') + ':' + (#category ?: '') + ':' + (#brand ?: '') + ':' + (#minPrice ?: -1) + ':' + (#maxPrice ?: -1) + ':' + (#minRating ?: -1) + ':' + (#inStockOnly ?: false) + ':' + #sortBy + ':' + #page + ':' + #size")
    public PageResponse<MarketplaceProductCardResponse> listCatalog(String query, String category, String brand, Double minPrice,
            Double maxPrice, Double minRating, Boolean inStockOnly, String sortBy, int page, int size) {
        Map<Long, Seller> sellersById = sellerRepository.findAll().stream()
                .collect(Collectors.toMap(Seller::getId, Function.identity()));
        Set<Long> wishlistedProductIds = resolveOptionalUserId()
                .map(userId -> wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                        .map(WishlistItem::getProductId)
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);

        List<MarketplaceProductCardResponse> filtered = productRepository.findAll().stream()
                .filter(this::isVisibleForCatalog)
                .filter(product -> matchesCatalogQuery(product, sellersById.get(product.getSellerId()), query))
                .filter(product -> matchesText(product.getCategoryName(), category))
                .filter(product -> matchesBrand(sellersById.get(product.getSellerId()), brand))
                .filter(product -> minPrice == null || finalPrice(product).compareTo(BigDecimal.valueOf(minPrice)) >= 0)
                .filter(product -> maxPrice == null || finalPrice(product).compareTo(BigDecimal.valueOf(maxPrice)) <= 0)
                .filter(product -> minRating == null || defaultDecimal(product.getRatingAverage()).compareTo(BigDecimal.valueOf(minRating)) >= 0)
                .filter(product -> !Boolean.TRUE.equals(inStockOnly) || defaultInteger(product.getStockQuantity()) > 0)
                .map(product -> toProductCard(product, sellersById.get(product.getSellerId()), wishlistedProductIds.contains(product.getId())))
                .sorted(productComparator(sortBy))
                .toList();

        return paginate(filtered, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listSuggestions(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        Map<Long, Seller> sellersById = sellerRepository.findAll().stream()
                .collect(Collectors.toMap(Seller::getId, Function.identity()));

        LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        for (Product product : productRepository.findAll()) {
            if (!isVisibleForCatalog(product)) {
                continue;
            }

            addSuggestion(suggestions, product.getName(), normalized);
            addSuggestion(suggestions, product.getCategoryName(), normalized);
            Seller seller = sellersById.get(product.getSellerId());
            if (seller != null) {
                addSuggestion(suggestions, seller.getShopName(), normalized);
            }
            if (suggestions.size() >= 8) {
                break;
            }
        }
        return suggestions.stream().limit(8).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryOptionResponse> listCategories() {
        Map<String, Long> counts = productRepository.findAll().stream()
                .filter(this::isVisibleForCatalog)
                .collect(Collectors.groupingBy(Product::getCategoryName, Collectors.counting()));

        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(category -> CategoryOptionResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .productCount(counts.getOrDefault(category.getName(), 0L))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "marketplaceHighlights", key = "'home'")
    public MarketplaceHighlightsResponse getHighlights() {
        Map<Long, Seller> sellersById = sellerRepository.findAll().stream()
                .collect(Collectors.toMap(Seller::getId, Function.identity()));
        List<Product> visible = productRepository.findAll().stream()
                .filter(this::isVisibleForCatalog)
                .toList();

        return MarketplaceHighlightsResponse.builder()
                .categories(listCategories())
                .featuredProducts(visible.stream()
                        .sorted(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(8)
                        .map(product -> toProductCard(product, sellersById.get(product.getSellerId()), false))
                        .toList())
                .trendingProducts(visible.stream()
                        .sorted(Comparator.comparing(Product::getReviewCount, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(Product::getRatingAverage, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(8)
                        .map(product -> toProductCard(product, sellersById.get(product.getSellerId()), false))
                        .toList())
                .budgetProducts(visible.stream()
                        .sorted(Comparator.comparing(this::finalPrice))
                        .limit(8)
                        .map(product -> toProductCard(product, sellersById.get(product.getSellerId()), false))
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MarketplaceProductDetailResponse getProductDetail(Long productId) {
        Product product = requireVisibleProduct(productId);
        Seller seller = sellerRepository.findById(product.getSellerId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SELLER_NOT_FOUND", "Seller not found."));
        ShippingSetting shippingSetting = shippingSettingRepository.findBySellerId(product.getSellerId()).orElse(null);
        boolean wishlisted = resolveOptionalUserId().map(userId -> wishlistItemRepository.existsByUserIdAndProductId(userId, productId)).orElse(false);

        return MarketplaceProductDetailResponse.builder()
                .product(toProductCard(product, seller, wishlisted))
                .sellerDescription(defaultString(seller.getShopAddress(), "Trusted marketplace seller"))
                .returnPolicy(defaultString(seller.getReturnPolicy(), "7-day seller-managed return window."))
                .shippingPartners(shippingSetting == null ? "Partner assigned at dispatch" : shippingSetting.getShippingPartners())
                .deliveryRegions(shippingSetting == null ? "Pan-India" : shippingSetting.getDeliveryRegions())
                .estimatedDelivery(shippingSetting == null ? "3-7 business days" : shippingSetting.getEstimatedDeliveryTimes())
                .images(productImageRepository.findByProductIdOrderBySortOrderAsc(productId).stream().map(this::toImageItem).toList())
                .variants(productVariantRepository.findByProductIdOrderByIdAsc(productId).stream().map(this::toVariantItem).toList())
                .reviews(reviewEntryRepository.findByProductIdOrderByCreatedAtDesc(productId).stream().map(review -> toReviewResponse(review, product)).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketplaceProductCardResponse> compareProducts(List<Long> productIds) {
        Map<Long, Seller> sellersById = sellerRepository.findAll().stream()
                .collect(Collectors.toMap(Seller::getId, Function.identity()));
        Set<Long> requested = new LinkedHashSet<>(productIds);
        return productRepository.findAllById(requested).stream()
                .filter(this::isVisibleForCatalog)
                .sorted(Comparator.comparing(product -> productIds.indexOf(product.getId())))
                .map(product -> toProductCard(product, sellersById.get(product.getSellerId()), false))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> listWishlist() {
        Long userId = currentUser().getId();
        Map<Long, Product> productsById = productRepository.findAllById(wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(WishlistItem::getProductId)
                .toList()).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Seller> sellersById = sellerRepository.findAll().stream().collect(Collectors.toMap(Seller::getId, Function.identity()));

        return wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(item -> {
                    Product product = productsById.get(item.getProductId());
                    if (product == null || !isVisibleForCatalog(product)) {
                        return null;
                    }
                    return WishlistItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProductId())
                            .createdAt(item.getCreatedAt())
                            .product(toProductCard(product, sellersById.get(product.getSellerId()), true))
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public WishlistItemResponse addToWishlist(Long productId) {
        User user = currentUser();
        Product product = requireVisibleProduct(productId);
        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseGet(() -> {
                    WishlistItem wishlistItem = new WishlistItem();
                    wishlistItem.setUserId(user.getId());
                    wishlistItem.setProductId(productId);
                    wishlistItem.setCreatedAt(LocalDateTime.now());
                    return wishlistItemRepository.save(wishlistItem);
                });
        Seller seller = sellerRepository.findById(product.getSellerId()).orElse(null);
        return WishlistItemResponse.builder()
                .id(item.getId())
                .productId(productId)
                .createdAt(item.getCreatedAt())
                .product(toProductCard(product, seller, true))
                .build();
    }

    @Override
    public void removeFromWishlist(Long productId) {
        User user = currentUser();
        wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId).ifPresent(wishlistItemRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> listAddresses() {
        return customerAddressRepository.findByUserIdOrderByDefaultAddressDescUpdatedAtDesc(currentUser().getId()).stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Override
    public CustomerAddressResponse createAddress(CustomerAddressRequest request) {
        User user = currentUser();
        CustomerAddress address = new CustomerAddress();
        address.setUserId(user.getId());
        applyAddress(address, request);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        normalizeDefaultAddress(user.getId(), address.getId(), Boolean.TRUE.equals(request.getDefaultAddress()));
        CustomerAddress saved = customerAddressRepository.save(address);
        if (Boolean.TRUE.equals(saved.getDefaultAddress())) {
            normalizeDefaultAddress(user.getId(), saved.getId(), true);
        }
        return toAddressResponse(saved);
    }

    @Override
    public CustomerAddressResponse updateAddress(Long addressId, CustomerAddressRequest request) {
        User user = currentUser();
        CustomerAddress address = requireAddress(user.getId(), addressId);
        applyAddress(address, request);
        address.setUpdatedAt(LocalDateTime.now());
        CustomerAddress saved = customerAddressRepository.save(address);
        if (Boolean.TRUE.equals(saved.getDefaultAddress())) {
            normalizeDefaultAddress(user.getId(), saved.getId(), true);
        }
        return toAddressResponse(saved);
    }

    @Override
    public void deleteAddress(Long addressId) {
        User user = currentUser();
        customerAddressRepository.delete(requireAddress(user.getId(), addressId));
    }

    @Override
    public CheckoutResponse checkout(CheckoutRequest request) {
        User user = currentUser();
        CustomerAddress address = requireAddress(user.getId(), request.getAddressId());
        LocalDateTime now = LocalDateTime.now();

        Map<Long, Product> productsById = productRepository.findAllById(request.getItems().stream().map(CheckoutRequest.Item::getProductId).toList()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, ProductVariant> variantsById = productVariantRepository.findAllById(request.getItems().stream()
                .map(CheckoutRequest.Item::getVariantId)
                .filter(Objects::nonNull)
                .toList()).stream().collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        Map<Long, Seller> sellersById = sellerRepository.findAll().stream().collect(Collectors.toMap(Seller::getId, Function.identity()));

        Map<Long, List<CheckoutLine>> groupedLines = new HashMap<>();
        for (CheckoutRequest.Item item : request.getItems()) {
            Product product = requireVisibleProduct(item.getProductId());
            if (defaultInteger(product.getStockQuantity()) < item.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "Requested quantity is not available for " + product.getName() + '.');
            }
            ProductVariant variant = item.getVariantId() == null ? null : variantsById.get(item.getVariantId());
            if (item.getVariantId() != null && (variant == null || !Objects.equals(variant.getProductId(), product.getId()))) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VARIANT", "Selected variant is invalid for " + product.getName() + '.');
            }
            groupedLines.computeIfAbsent(product.getSellerId(), ignored -> new ArrayList<>())
                    .add(new CheckoutLine(product, variant, item.getQuantity()));
        }

        List<CheckoutResponse.PlacedOrder> placedOrders = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        String paymentStatus = isOnlinePayment(request.getPaymentMethod()) ? "PAID" : "PENDING_COD";

        for (Map.Entry<Long, List<CheckoutLine>> entry : groupedLines.entrySet()) {
            Long sellerId = entry.getKey();
            Seller seller = sellersById.get(sellerId);
            if (seller == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "SELLER_NOT_FOUND", "Seller not found for checkout item.");
            }

            List<CheckoutLine> lines = entry.getValue();
            BigDecimal subtotal = lines.stream().map(CheckoutLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal discountAmount = applyCouponDiscount(sellerId, request.getCouponCode(), subtotal);
            BigDecimal shippingCharge = resolveShippingCharge(sellerId, subtotal.subtract(discountAmount));
            BigDecimal sellerTotal = subtotal.subtract(discountAmount).add(shippingCharge);

            CustomerProfile profile = customerProfileRepository.findBySellerIdAndEmailIgnoreCase(sellerId, user.getEmail())
                    .orElseGet(() -> createCustomerProfile(user, sellerId));

            SellerOrder order = new SellerOrder();
            order.setSellerId(sellerId);
            order.setCustomerId(profile.getId());
            order.setOrderCode(generateOrderCode(seller.getSellerCode()));
            order.setOrderStatus("PENDING");
            order.setPaymentMethod(normalizeText(request.getPaymentMethod()));
            order.setTotalAmount(scaleMoney(sellerTotal));
            order.setShippingPartner(resolveShippingPartner(sellerId));
            order.setTrackingNumber(generateTrackingNumber(seller.getSellerCode()));
            order.setPrimaryProductName(lines.get(0).product().getName());
            order.setTotalQuantity(lines.stream().mapToInt(CheckoutLine::quantity).sum());
            order.setOrderDate(now);
            SellerOrder savedOrder = sellerOrderRepository.save(order);

            List<OrderItem> items = lines.stream().map(line -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getId());
                orderItem.setProductId(line.product().getId());
                orderItem.setProductName(line.variant() == null ? line.product().getName()
                        : line.product().getName() + " - " + line.variant().getVariantValue());
                orderItem.setQuantity(line.quantity());
                orderItem.setUnitPrice(scaleMoney(line.unitPrice()));
                orderItem.setTotalPrice(scaleMoney(line.lineTotal()));
                return orderItem;
            }).toList();
            orderItemRepository.saveAll(items);

            OrderShippingDetail shippingDetail = new OrderShippingDetail();
            shippingDetail.setOrderId(savedOrder.getId());
            shippingDetail.setUserId(user.getId());
            shippingDetail.setRecipientName(address.getFullName());
            shippingDetail.setPhoneNumber(address.getPhoneNumber());
            shippingDetail.setAddressLine1(address.getAddressLine1());
            shippingDetail.setAddressLine2(address.getAddressLine2());
            shippingDetail.setCity(address.getCity());
            shippingDetail.setState(address.getState());
            shippingDetail.setPincode(address.getPincode());
            shippingDetail.setCountry(address.getCountry());
            shippingDetail.setLandmark(address.getLandmark());
            shippingDetail.setDeliveryInstructions(address.getDeliveryInstructions());
            shippingDetail.setDeliveryCharge(scaleMoney(shippingCharge));
            shippingDetail.setEstimatedDeliveryAt(now.plusDays(5));
            shippingDetail.setCreatedAt(now);
            shippingDetail.setUpdatedAt(now);
            orderShippingDetailRepository.save(shippingDetail);

            OrderPayment payment = new OrderPayment();
            payment.setOrderId(savedOrder.getId());
            payment.setUserId(user.getId());
            payment.setPaymentMethod(normalizeText(request.getPaymentMethod()));
            payment.setPaymentStatus(paymentStatus);
            payment.setGatewayProvider(resolveGatewayProvider(request.getPaymentMethod()));
            payment.setGatewayReference(isOnlinePayment(request.getPaymentMethod()) ? generateGatewayReference(savedOrder.getOrderCode()) : null);
            payment.setRefundStatus("NOT_REQUESTED");
            payment.setRefundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            payment.setTransactionLog(paymentStatus.equals("PAID") ? "Payment authorized successfully" : "COD payment to be collected on delivery");
            payment.setPaidAt(isOnlinePayment(request.getPaymentMethod()) ? now : null);
            payment.setCreatedAt(now);
            payment.setUpdatedAt(now);
            orderPaymentRepository.save(payment);

            PaymentRecord payout = new PaymentRecord();
            payout.setSellerId(sellerId);
            payout.setOrderId(savedOrder.getId());
            payout.setOrderAmount(scaleMoney(sellerTotal));
            payout.setCommissionAmount(scaleMoney(sellerTotal.multiply(DEFAULT_COMMISSION_RATE)));
            payout.setNetAmount(scaleMoney(sellerTotal.subtract(payout.getCommissionAmount())));
            payout.setPayoutStatus("PENDING");
            payout.setPaymentDate(now);
            paymentRecordRepository.save(payout);

            CustomerOrderLink link = new CustomerOrderLink();
            link.setUserId(user.getId());
            link.setOrderId(savedOrder.getId());
            link.setSellerId(sellerId);
            link.setCreatedAt(now);
            customerOrderLinkRepository.save(link);

            for (CheckoutLine line : lines) {
                Product product = line.product();
                product.setStockQuantity(defaultInteger(product.getStockQuantity()) - line.quantity());
                product.setUpdatedAt(now);
                productRepository.save(product);
                inventoryItemRepository.findBySellerIdAndProductId(sellerId, product.getId()).ifPresent(inventory -> {
                    inventory.setAvailableStock(Math.max(0, defaultInteger(inventory.getAvailableStock()) - line.quantity()));
                    inventory.setUpdatedAt(now);
                    inventoryItemRepository.save(inventory);
                });
            }

            profile.setTotalOrders(defaultInteger(profile.getTotalOrders()) + 1);
            profile.setTotalPurchases(scaleMoney(defaultDecimal(profile.getTotalPurchases()).add(sellerTotal)));
            profile.setLastOrderAt(now);
            customerProfileRepository.save(profile);

            createNotification(user.getId(), "ORDER", "Order placed", savedOrder.getOrderCode() + " has been placed with " + seller.getShopName() + '.');
            if (isOnlinePayment(request.getPaymentMethod())) {
                createNotification(user.getId(), "PAYMENT", "Payment confirmed", "Payment received for order " + savedOrder.getOrderCode() + '.');
            }

            placedOrders.add(CheckoutResponse.PlacedOrder.builder()
                    .orderId(savedOrder.getId())
                    .orderCode(savedOrder.getOrderCode())
                    .sellerName(seller.getShopName())
                    .status(savedOrder.getOrderStatus())
                    .totalAmount(savedOrder.getTotalAmount())
                    .trackingNumber(savedOrder.getTrackingNumber())
                    .build());
            grandTotal = grandTotal.add(sellerTotal);
        }

        return CheckoutResponse.builder()
                .placedAt(now)
                .totalAmount(scaleMoney(grandTotal))
                .paymentMethod(normalizeText(request.getPaymentMethod()))
                .paymentStatus(paymentStatus)
                .orders(placedOrders)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerOrderResponse> listOrders() {
        Long userId = currentUser().getId();
        return customerOrderLinkRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CustomerOrderLink::getOrderId)
                .map(this::getOrder)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerOrderResponse getOrder(Long orderId) {
        User user = currentUser();
        customerOrderLinkRepository.findByUserIdAndOrderId(user.getId(), orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", "Order does not belong to the current customer."));

        SellerOrder order = sellerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found."));
        Seller seller = sellerRepository.findById(order.getSellerId()).orElse(null);
        OrderShippingDetail shipping = orderShippingDetailRepository.findByOrderId(orderId).orElse(null);
        OrderPayment payment = orderPaymentRepository.findByOrderId(orderId).orElse(null);

        return CustomerOrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .sellerName(seller == null ? "Marketplace seller" : seller.getShopName())
                .sellerCode(seller == null ? null : seller.getSellerCode())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(payment == null ? "PENDING" : payment.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryCharge(shipping == null ? BigDecimal.ZERO : shipping.getDeliveryCharge())
                .shippingPartner(order.getShippingPartner())
                .trackingNumber(order.getTrackingNumber())
                .orderDate(order.getOrderDate())
                .estimatedDeliveryAt(shipping == null ? null : shipping.getEstimatedDeliveryAt())
                .shippingAddress(shipping == null ? null : CustomerOrderResponse.ShippingAddress.builder()
                        .recipientName(shipping.getRecipientName())
                        .phoneNumber(shipping.getPhoneNumber())
                        .addressLine1(shipping.getAddressLine1())
                        .addressLine2(shipping.getAddressLine2())
                        .city(shipping.getCity())
                        .state(shipping.getState())
                        .pincode(shipping.getPincode())
                        .country(shipping.getCountry())
                        .landmark(shipping.getLandmark())
                        .deliveryInstructions(shipping.getDeliveryInstructions())
                        .build())
                .items(orderItemRepository.findByOrderId(orderId).stream()
                        .map(item -> CustomerOrderResponse.Item.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .build())
                        .toList())
                .build();
    }

    @Override
    public ReviewResponse createReview(CustomerReviewCreateRequest request) {
        User user = currentUser();
        Product product = requireVisibleProduct(request.getProductId());
        Set<Long> customerOrderIds = customerOrderLinkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(CustomerOrderLink::getOrderId)
                .collect(Collectors.toSet());
        boolean purchased = !customerOrderIds.isEmpty() && orderItemRepository.findByOrderIdIn(customerOrderIds).stream()
                .anyMatch(item -> Objects.equals(item.getProductId(), product.getId()));
        if (!purchased) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "REVIEW_NOT_ALLOWED", "Review is allowed only after purchase.");
        }

        ReviewEntry review = new ReviewEntry();
        review.setSellerId(product.getSellerId());
        review.setProductId(product.getId());
        review.setCustomerName(defaultString(user.getFullName(), user.getEmail()));
        review.setRating(scaleMoney(request.getRating()));
        review.setReviewText(trimToNull(request.getReviewText()));
        review.setReplyText(null);
        review.setAbusive(false);
        review.setCreatedAt(LocalDateTime.now());
        ReviewEntry saved = reviewEntryRepository.save(review);
        refreshProductRating(product);
        return toReviewResponse(saved, product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerNotificationResponse> listNotifications() {
        return customerNotificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(currentUser().getId()).stream()
                .map(notification -> CustomerNotificationResponse.builder()
                        .id(notification.getId())
                        .type(notification.getNotificationType())
                        .title(notification.getTitle())
                        .detail(notification.getDetail())
                        .read(notification.getReadStatus())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .toList();
    }

    private boolean isVisibleForCatalog(Product product) {
        String status = normalizeText(product.getStatus());
        return status != null && !HIDDEN_PRODUCT_STATUSES.contains(status);
    }

    private Product requireVisibleProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found."));
        if (!isVisibleForCatalog(product)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product is not available in the marketplace.");
        }
        return product;
    }

    private boolean matchesCatalogQuery(Product product, Seller seller, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return contains(product.getName(), normalized)
                || contains(product.getDescription(), normalized)
                || contains(product.getCategoryName(), normalized)
                || contains(product.getSku(), normalized)
                || contains(seller == null ? null : seller.getShopName(), normalized);
    }

    private boolean matchesText(String source, String expected) {
        return expected == null || expected.isBlank()
                || (source != null && source.trim().equalsIgnoreCase(expected.trim()));
    }

    private boolean matchesBrand(Seller seller, String brand) {
        return brand == null || brand.isBlank() || contains(seller == null ? null : seller.getShopName(), brand.trim().toLowerCase(Locale.ROOT));
    }

    private Comparator<MarketplaceProductCardResponse> productComparator(String sortBy) {
        String normalized = normalizeText(sortBy);
        if ("PRICE_LOW".equals(normalized)) {
            return Comparator.comparing(MarketplaceProductCardResponse::getFinalPrice, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("PRICE_HIGH".equals(normalized)) {
            return Comparator.comparing(MarketplaceProductCardResponse::getFinalPrice, Comparator.nullsLast(Comparator.reverseOrder()));
        }
        if ("RATING".equals(normalized)) {
            return Comparator.comparing(MarketplaceProductCardResponse::getRatingAverage, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(MarketplaceProductCardResponse::getReviewCount, Comparator.nullsLast(Comparator.reverseOrder()));
        }
        if ("NEWEST".equals(normalized)) {
            return Comparator.comparing(MarketplaceProductCardResponse::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        }
        return Comparator.comparing(MarketplaceProductCardResponse::getReviewCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(MarketplaceProductCardResponse::getRatingAverage, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private MarketplaceProductCardResponse toProductCard(Product product, Seller seller, boolean wishlisted) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        ProductImage primaryImage = images.stream().filter(image -> Boolean.TRUE.equals(image.getPrimaryImage())).findFirst().orElse(images.isEmpty() ? null : images.get(0));
        return MarketplaceProductCardResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sellerName(seller == null ? "Marketplace seller" : seller.getShopName())
                .sellerCode(seller == null ? null : seller.getSellerCode())
                .categoryName(product.getCategoryName())
                .description(product.getDescription())
                .price(scaleMoney(defaultDecimal(product.getPrice())))
                .finalPrice(finalPrice(product))
                .discountPercent(scaleMoney(defaultDecimal(product.getDiscountPercent())))
                .ratingAverage(scaleMoney(defaultDecimal(product.getRatingAverage())))
                .reviewCount(defaultInteger(product.getReviewCount()))
                .stockQuantity(defaultInteger(product.getStockQuantity()))
                .status(product.getStatus())
                .imageUrl(primaryImage == null ? null : primaryImage.getImageUrl())
                .deliveryLabel(defaultInteger(product.getStockQuantity()) > 0 ? "Delivery in 3-7 days" : "Currently unavailable")
                .wishlisted(wishlisted)
                .build();
    }

    private ProductResponse.ImageItem toImageItem(ProductImage image) {
        return ProductResponse.ImageItem.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .sortOrder(image.getSortOrder())
                .primaryImage(image.getPrimaryImage())
                .build();
    }

    private ProductResponse.VariantItem toVariantItem(ProductVariant variant) {
        return ProductResponse.VariantItem.builder()
                .id(variant.getId())
                .variantName(variant.getVariantName())
                .variantValue(variant.getVariantValue())
                .skuSuffix(variant.getSkuSuffix())
                .additionalPrice(scaleMoney(defaultDecimal(variant.getAdditionalPrice())))
                .stockQuantity(defaultInteger(variant.getStockQuantity()))
                .build();
    }

    private CustomerAddressResponse toAddressResponse(CustomerAddress address) {
        return CustomerAddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .landmark(address.getLandmark())
                .deliveryInstructions(address.getDeliveryInstructions())
                .defaultAddress(address.getDefaultAddress())
                .build();
    }

    private void applyAddress(CustomerAddress address, CustomerAddressRequest request) {
        address.setFullName(trimRequired(request.getFullName(), "Recipient name is required."));
        address.setPhoneNumber(trimRequired(request.getPhoneNumber(), "Phone number is required."));
        address.setAddressLine1(trimRequired(request.getAddressLine1(), "Address line 1 is required."));
        address.setAddressLine2(trimToNull(request.getAddressLine2()));
        address.setCity(trimRequired(request.getCity(), "City is required."));
        address.setState(trimRequired(request.getState(), "State is required."));
        address.setPincode(trimRequired(request.getPincode(), "Pincode is required."));
        address.setCountry(trimRequired(request.getCountry(), "Country is required."));
        address.setLandmark(trimToNull(request.getLandmark()));
        address.setDeliveryInstructions(trimToNull(request.getDeliveryInstructions()));
        address.setDefaultAddress(Boolean.TRUE.equals(request.getDefaultAddress()));
    }

    private void normalizeDefaultAddress(Long userId, Long currentAddressId, boolean makeDefault) {
        if (!makeDefault) {
            return;
        }
        customerAddressRepository.findByUserIdOrderByDefaultAddressDescUpdatedAtDesc(userId).stream()
                .filter(address -> !Objects.equals(address.getId(), currentAddressId) && Boolean.TRUE.equals(address.getDefaultAddress()))
                .forEach(address -> {
                    address.setDefaultAddress(false);
                    address.setUpdatedAt(LocalDateTime.now());
                    customerAddressRepository.save(address);
                });
    }

    private CustomerAddress requireAddress(Long userId, Long addressId) {
        CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Address not found."));
        if (!Objects.equals(address.getUserId(), userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADDRESS_ACCESS_DENIED", "Address does not belong to the current user.");
        }
        return address;
    }

    private BigDecimal resolveShippingCharge(Long sellerId, BigDecimal orderSubtotal) {
        ShippingSetting setting = shippingSettingRepository.findBySellerId(sellerId).orElse(null);
        if (setting == null) {
            return BigDecimal.valueOf(79).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal threshold = parseAmount(setting.getFreeShippingThreshold(), BigDecimal.valueOf(999999));
        if (orderSubtotal.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return parseAmount(setting.getShippingCharges(), BigDecimal.valueOf(79));
    }

    private String resolveShippingPartner(Long sellerId) {
        return shippingSettingRepository.findBySellerId(sellerId)
                .map(ShippingSetting::getShippingPartners)
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.split(",")[0].trim())
                .orElse("Marketplace Logistics");
    }

    private BigDecimal applyCouponDiscount(Long sellerId, String couponCode, BigDecimal subtotal) {
        if (couponCode == null || couponCode.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        Optional<CouponCampaign> couponOptional = couponCampaignRepository.findBySellerId(sellerId).stream()
                .filter(coupon -> couponCode.equalsIgnoreCase(coupon.getCode()))
                .filter(coupon -> "ACTIVE".equalsIgnoreCase(coupon.getStatus()))
                .filter(coupon -> !coupon.getStartDate().isAfter(LocalDateTime.now()) && !coupon.getEndDate().isBefore(LocalDateTime.now()))
                .findFirst();
        if (couponOptional.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        CouponCampaign coupon = couponOptional.get();
        BigDecimal rawValue = parseAmount(coupon.getDiscountValue(), BigDecimal.ZERO);
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType()) || coupon.getDiscountValue().contains("%")) {
            return scaleMoney(subtotal.multiply(rawValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
        }
        return scaleMoney(rawValue.min(subtotal));
    }

    private CustomerProfile createCustomerProfile(User user, Long sellerId) {
        CustomerProfile profile = new CustomerProfile();
        profile.setSellerId(sellerId);
        profile.setName(defaultString(user.getFullName(), user.getEmail()));
        profile.setEmail(user.getEmail());
        profile.setPhoneNumber(user.getPhoneNumber());
        profile.setTotalOrders(0);
        profile.setTotalPurchases(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        profile.setLastOrderAt(null);
        return customerProfileRepository.save(profile);
    }

    private ReviewResponse toReviewResponse(ReviewEntry review, Product product) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .productName(product == null ? null : product.getName())
                .customerName(review.getCustomerName())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .replyText(review.getReplyText())
                .abusive(review.getAbusive())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private void refreshProductRating(Product product) {
        List<ReviewEntry> reviews = reviewEntryRepository.findByProductIdOrderByCreatedAtDesc(product.getId()).stream()
                .filter(review -> !Boolean.TRUE.equals(review.getAbusive()))
                .toList();
        if (reviews.isEmpty()) {
            product.setRatingAverage(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            product.setReviewCount(0);
        } else {
            BigDecimal total = reviews.stream().map(ReviewEntry::getRating).reduce(BigDecimal.ZERO, BigDecimal::add);
            product.setRatingAverage(total.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP));
            product.setReviewCount(reviews.size());
        }
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private void addSuggestion(Collection<String> suggestions, String candidate, String query) {
        if (candidate != null && candidate.toLowerCase(Locale.ROOT).contains(query)) {
            suggestions.add(candidate.trim());
        }
    }

    private PageResponse<MarketplaceProductCardResponse> paginate(List<MarketplaceProductCardResponse> items, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 48);
        int start = Math.min(safePage * safeSize, items.size());
        int end = Math.min(start + safeSize, items.size());
        return PageResponse.<MarketplaceProductCardResponse>builder()
                .items(items.subList(start, end))
                .page(safePage)
                .size(safeSize)
                .totalItems(items.size())
                .totalPages(items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / safeSize))
                .build();
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Authentication is required for this action.");
        }
        return userRepository.findTopByEmailOrderByIdDesc(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Authenticated user not found."));
    }

    private Optional<Long> resolveOptionalUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        return userRepository.findTopByEmailOrderByIdDesc(authentication.getName()).map(User::getId);
    }

    private void createNotification(Long userId, String type, String title, String detail) {
        CustomerNotification notification = new CustomerNotification();
        notification.setUserId(userId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setDetail(detail);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        customerNotificationRepository.save(notification);
    }

    private BigDecimal finalPrice(Product product) {
        BigDecimal discount = defaultDecimal(product.getDiscountPercent());
        BigDecimal multiplier = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return scaleMoney(defaultDecimal(product.getPrice()).multiply(multiplier));
    }

    private BigDecimal parseAmount(String value, BigDecimal fallback) {
        if (value == null || value.isBlank()) {
            return scaleMoney(fallback);
        }
        Matcher matcher = MONEY_PATTERN.matcher(value.replace(",", ""));
        if (!matcher.find()) {
            return scaleMoney(fallback);
        }
        return scaleMoney(new BigDecimal(matcher.group(1)));
    }

    private boolean isOnlinePayment(String paymentMethod) {
        String normalized = normalizeText(paymentMethod);
        return "RAZORPAY".equals(normalized) || "STRIPE".equals(normalized) || "ONLINE".equals(normalized) || "CARD".equals(normalized) || "UPI".equals(normalized);
    }

    private String resolveGatewayProvider(String paymentMethod) {
        String normalized = normalizeText(paymentMethod);
        if ("RAZORPAY".equals(normalized) || "STRIPE".equals(normalized)) {
            return normalized;
        }
        return isOnlinePayment(paymentMethod) ? "ONLINE_GATEWAY" : null;
    }

    private String generateOrderCode(String sellerCode) {
        return sellerCode + "-" + System.currentTimeMillis();
    }

    private String generateTrackingNumber(String sellerCode) {
        return "TRK-" + sellerCode + '-' + System.nanoTime();
    }

    private String generateGatewayReference(String orderCode) {
        return "PAY-" + orderCode;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimRequired(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
        }
        return trimmed;
    }

    private boolean contains(String source, String normalizedQuery) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return defaultDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private record CheckoutLine(Product product, ProductVariant variant, int quantity) {
        BigDecimal unitPrice() {
            BigDecimal variantPrice = variant == null ? BigDecimal.ZERO : defaultBigDecimal(variant.getAdditionalPrice());
            return scale(defaultBigDecimal(product.getPrice()).multiply(BigDecimal.ONE.subtract(defaultBigDecimal(product.getDiscountPercent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))).add(variantPrice));
        }

        BigDecimal lineTotal() {
            return scale(unitPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        private BigDecimal defaultBigDecimal(BigDecimal value) {
            return value == null ? BigDecimal.ZERO : value;
        }

        private BigDecimal scale(BigDecimal value) {
            return value.setScale(2, RoundingMode.HALF_UP);
        }
    }
}