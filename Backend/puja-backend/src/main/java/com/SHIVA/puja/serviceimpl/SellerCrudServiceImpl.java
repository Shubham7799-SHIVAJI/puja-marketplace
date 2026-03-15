package com.SHIVA.puja.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.SHIVA.puja.entity.CouponCampaign;
import com.SHIVA.puja.entity.CustomerProfile;
import com.SHIVA.puja.entity.InventoryItem;
import com.SHIVA.puja.entity.OrderStatusHistory;
import com.SHIVA.puja.entity.Product;
import com.SHIVA.puja.entity.ProductImage;
import com.SHIVA.puja.entity.ProductVariant;
import com.SHIVA.puja.entity.ReviewEntry;
import com.SHIVA.puja.entity.Seller;
import com.SHIVA.puja.entity.SellerOrder;
import com.SHIVA.puja.entity.SupportTicket;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.CouponCampaignRepository;
import com.SHIVA.puja.repository.CustomerProfileRepository;
import com.SHIVA.puja.repository.InventoryItemRepository;
import com.SHIVA.puja.repository.OrderStatusHistoryRepository;
import com.SHIVA.puja.repository.ProductImageRepository;
import com.SHIVA.puja.repository.ProductRepository;
import com.SHIVA.puja.repository.ProductVariantRepository;
import com.SHIVA.puja.repository.ReviewEntryRepository;
import com.SHIVA.puja.repository.SellerOrderRepository;
import com.SHIVA.puja.repository.SupportTicketRepository;
import com.SHIVA.puja.security.SellerAccessService;
import com.SHIVA.puja.service.SellerCrudService;

@Service
@Transactional
public class SellerCrudServiceImpl implements SellerCrudService {

    private static final Set<String> ORDER_STATUSES = Set.of("PENDING", "CONFIRMED", "PACKED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED");
    private static final Map<String, Set<String>> ORDER_STATUS_TRANSITIONS = new HashMap<>();

    static {
        ORDER_STATUS_TRANSITIONS.put("PENDING", Set.of("CONFIRMED", "CANCELLED"));
        ORDER_STATUS_TRANSITIONS.put("CONFIRMED", Set.of("PACKED", "CANCELLED"));
        ORDER_STATUS_TRANSITIONS.put("PACKED", Set.of("SHIPPED", "CANCELLED"));
        ORDER_STATUS_TRANSITIONS.put("SHIPPED", Set.of("DELIVERED", "RETURNED"));
        ORDER_STATUS_TRANSITIONS.put("DELIVERED", Set.of("RETURNED"));
        ORDER_STATUS_TRANSITIONS.put("CANCELLED", Set.of());
        ORDER_STATUS_TRANSITIONS.put("RETURNED", Set.of());
    }

    private final SellerAccessService sellerAccessService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final CouponCampaignRepository couponCampaignRepository;
    private final ReviewEntryRepository reviewEntryRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public SellerCrudServiceImpl(
            SellerAccessService sellerAccessService,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductVariantRepository productVariantRepository,
            InventoryItemRepository inventoryItemRepository,
            SellerOrderRepository sellerOrderRepository,
            CouponCampaignRepository couponCampaignRepository,
            ReviewEntryRepository reviewEntryRepository,
            SupportTicketRepository supportTicketRepository,
            CustomerProfileRepository customerProfileRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.sellerAccessService = sellerAccessService;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.couponCampaignRepository = couponCampaignRepository;
        this.reviewEntryRepository = reviewEntryRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "sellerProducts", key = "(#sellerCode ?: 'self') + ':' + #page + ':' + #size + ':' + (#query ?: '') + ':' + (#status ?: '') + ':' + (#category ?: '')")
    public PageResponse<ProductResponse> listProducts(String sellerCode, int page, int size, String query, String status, String category) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        List<Product> filtered = productRepository.findBySellerId(seller.getId()).stream()
                .filter(product -> matches(product.getName(), query)
                        || matches(product.getSku(), query)
                        || matches(product.getDescription(), query))
                .filter(product -> matchesExact(product.getStatus(), status))
                .filter(product -> matchesExact(product.getCategoryName(), category))
                .sorted(Comparator.comparing(Product::getUpdatedAt).reversed())
                .toList();
        return paginate(filtered, sanitizePage(page), sanitizeSize(size), this::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(String sellerCode, Long productId) {
        Product product = requireProduct(sellerCode, productId);
        return toProductResponse(product);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public ProductResponse createProduct(String sellerCode, ProductUpsertRequest request) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        ensureUniqueProductSku(request.getSku(), null);

        LocalDateTime now = LocalDateTime.now();
        Product product = new Product();
        product.setSellerId(seller.getId());
        applyProduct(product, request, now);
        Product savedProduct = productRepository.save(product);

        saveProductAssets(savedProduct.getId(), request);
        upsertInventoryForProduct(savedProduct, request.getStockQuantity(), now, defaultStockHistory("Created product"));
        return toProductResponse(savedProduct);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public ProductResponse updateProduct(String sellerCode, Long productId, ProductUpsertRequest request) {
        Product product = requireProduct(sellerCode, productId);
        ensureUniqueProductSku(request.getSku(), product.getId());

        applyProduct(product, request, LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        replaceProductAssets(savedProduct.getId(), request);
        upsertInventoryForProduct(savedProduct, request.getStockQuantity(), LocalDateTime.now(), defaultStockHistory("Updated from product editor"));
        return toProductResponse(savedProduct);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public void deleteProduct(String sellerCode, Long productId) {
        Product product = requireProduct(sellerCode, productId);
        inventoryItemRepository.findBySellerIdAndProductId(product.getSellerId(), product.getId())
                .ifPresent(inventoryItemRepository::delete);
        productImageRepository.deleteByProductId(product.getId());
        productVariantRepository.deleteByProductId(product.getId());
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> listInventory(String sellerCode, int page, int size, boolean lowStockOnly) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        Map<Long, Product> productsById = productRepository.findBySellerId(seller.getId()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<InventoryItem> filtered = inventoryItemRepository.findBySellerId(seller.getId()).stream()
                .filter(item -> !lowStockOnly || item.getAvailableStock() <= item.getLowStockThreshold())
                .sorted(Comparator.comparing(InventoryItem::getUpdatedAt).reversed())
                .toList();
        return paginate(filtered, sanitizePage(page), sanitizeSize(size), item -> toInventoryResponse(item, productsById.get(item.getProductId())));
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public InventoryResponse createInventory(String sellerCode, InventoryRequest request) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        Product product = requireProductForSeller(seller.getId(), request.getProductId());
        InventoryItem inventoryItem = inventoryItemRepository.findBySellerIdAndProductId(seller.getId(), request.getProductId())
                .orElse(new InventoryItem());
        inventoryItem.setSellerId(seller.getId());
        applyInventory(inventoryItem, request);
        InventoryItem saved = inventoryItemRepository.save(inventoryItem);
        syncProductStock(product, request.getAvailableStock());
        return toInventoryResponse(saved, product);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public InventoryResponse updateInventory(String sellerCode, Long inventoryId, InventoryRequest request) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        InventoryItem inventoryItem = requireInventory(seller.getId(), inventoryId);
        Product product = requireProductForSeller(seller.getId(), request.getProductId());
        applyInventory(inventoryItem, request);
        InventoryItem saved = inventoryItemRepository.save(inventoryItem);
        syncProductStock(product, request.getAvailableStock());
        return toInventoryResponse(saved, product);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public PageResponse<InventoryResponse> bulkUpdateInventory(String sellerCode, InventoryBulkUpdateRequest request) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        List<InventoryResponse> responses = new ArrayList<>();
        for (InventoryRequest item : request.getItems()) {
            InventoryItem inventory = inventoryItemRepository.findBySellerIdAndProductId(seller.getId(), item.getProductId())
                    .orElse(new InventoryItem());
            Product product = requireProductForSeller(seller.getId(), item.getProductId());
            inventory.setSellerId(seller.getId());
            applyInventory(inventory, item);
            InventoryItem saved = inventoryItemRepository.save(inventory);
            syncProductStock(product, item.getAvailableStock());
            responses.add(toInventoryResponse(saved, product));
        }
        return PageResponse.<InventoryResponse>builder()
                .items(responses)
                .page(0)
                .size(responses.size())
                .totalItems(responses.size())
                .totalPages(responses.isEmpty() ? 0 : 1)
                .build();
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public void deleteInventory(String sellerCode, Long inventoryId) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        InventoryItem inventoryItem = requireInventory(seller.getId(), inventoryId);
        Product product = requireProductForSeller(seller.getId(), inventoryItem.getProductId());
        product.setStockQuantity(0);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        inventoryItemRepository.delete(inventoryItem);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listOrders(String sellerCode, int page, int size, String status) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        Map<Long, CustomerProfile> customersById = customerProfileRepository.findAll().stream()
                .filter(customer -> Objects.equals(customer.getSellerId(), seller.getId()))
                .collect(Collectors.toMap(CustomerProfile::getId, Function.identity()));
        List<SellerOrder> filtered = sellerOrderRepository.findBySellerId(seller.getId()).stream()
                .filter(order -> matchesExact(order.getOrderStatus(), status))
                .sorted(Comparator.comparing(SellerOrder::getOrderDate).reversed())
                .toList();
        return paginate(filtered, sanitizePage(page), sanitizeSize(size), order -> toOrderResponse(order, customersById.get(order.getCustomerId())));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String sellerCode, Long orderId) {
        SellerOrder order = requireOrder(sellerCode, orderId);
        CustomerProfile customer = order.getCustomerId() == null ? null : customerProfileRepository.findById(order.getCustomerId()).orElse(null);
        return toOrderResponse(order, customer);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public OrderResponse createOrder(String sellerCode, OrderRequest request) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        ensureUniqueOrderCode(request.getOrderCode(), null, seller.getId());
        CustomerProfile customer = requireCustomerIfPresent(seller.getId(), request.getCustomerId());

        SellerOrder order = new SellerOrder();
        order.setSellerId(seller.getId());
        applyOrder(order, request);
        order.setOrderDate(LocalDateTime.now());
        SellerOrder saved = sellerOrderRepository.save(order);
        recordOrderStatusChange(saved.getId(), null, saved.getOrderStatus(), sellerAccessService.currentEmail(), "SELLER", "Seller created order");
        adjustStockFromOrder(seller.getId(), request.getPrimaryProductName(), request.getTotalQuantity());
        touchCustomerStats(customer, request.getTotalAmount(), saved.getOrderDate());
        return toOrderResponse(saved, customer);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public OrderResponse updateOrder(String sellerCode, Long orderId, OrderRequest request) {
        SellerOrder order = requireOrder(sellerCode, orderId);
        ensureUniqueOrderCode(request.getOrderCode(), order.getId(), order.getSellerId());
        CustomerProfile customer = requireCustomerIfPresent(order.getSellerId(), request.getCustomerId());
        String previousStatus = order.getOrderStatus();
        applyOrder(order, request);
        ensureValidOrderStatusTransition(previousStatus, order.getOrderStatus());
        SellerOrder saved = sellerOrderRepository.save(order);
        if (!Objects.equals(previousStatus, saved.getOrderStatus())) {
            recordOrderStatusChange(saved.getId(), previousStatus, saved.getOrderStatus(), sellerAccessService.currentEmail(), "SELLER", "Seller updated order status");
        }
        return toOrderResponse(saved, customer);
    }

    @Override
    public void deleteOrder(String sellerCode, Long orderId) {
        SellerOrder order = requireOrder(sellerCode, orderId);
        sellerOrderRepository.delete(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> listCoupons(String sellerCode, int page, int size, String status) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        List<CouponCampaign> filtered = couponCampaignRepository.findBySellerId(seller.getId()).stream()
                .filter(coupon -> matchesExact(coupon.getStatus(), status))
                .sorted(Comparator.comparing(CouponCampaign::getStartDate).reversed())
                .toList();
        return paginate(filtered, sanitizePage(page), sanitizeSize(size), this::toCouponResponse);
    }

    @Override
    public CouponResponse createCoupon(String sellerCode, CouponRequest request) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        validateCouponDates(request);
        ensureUniqueCouponCode(request.getCode(), null, seller.getId());
        CouponCampaign coupon = new CouponCampaign();
        coupon.setSellerId(seller.getId());
        applyCoupon(coupon, request);
        return toCouponResponse(couponCampaignRepository.save(coupon));
    }

    @Override
    public CouponResponse updateCoupon(String sellerCode, Long couponId, CouponRequest request) {
        CouponCampaign coupon = requireCoupon(sellerCode, couponId);
        validateCouponDates(request);
        ensureUniqueCouponCode(request.getCode(), coupon.getId(), coupon.getSellerId());
        applyCoupon(coupon, request);
        return toCouponResponse(couponCampaignRepository.save(coupon));
    }

    @Override
    public void deleteCoupon(String sellerCode, Long couponId) {
        CouponCampaign coupon = requireCoupon(sellerCode, couponId);
        couponCampaignRepository.delete(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listReviews(String sellerCode, int page, int size, Boolean abusive) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        Map<Long, Product> productsById = productRepository.findBySellerId(seller.getId()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<ReviewEntry> filtered = reviewEntryRepository.findBySellerId(seller.getId()).stream()
                .filter(review -> abusive == null || Objects.equals(review.getAbusive(), abusive))
                .sorted(Comparator.comparing(ReviewEntry::getCreatedAt).reversed())
                .toList();
        return paginate(filtered, sanitizePage(page), sanitizeSize(size), review -> toReviewResponse(review, productsById.get(review.getProductId())));
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public ReviewResponse createReview(String sellerCode, ReviewRequest request) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        Product product = requireProductForSeller(seller.getId(), request.getProductId());
        ReviewEntry review = new ReviewEntry();
        review.setSellerId(seller.getId());
        applyReview(review, request);
        review.setCreatedAt(LocalDateTime.now());
        ReviewEntry saved = reviewEntryRepository.save(review);
        refreshProductRating(product);
        return toReviewResponse(saved, product);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public ReviewResponse updateReview(String sellerCode, Long reviewId, ReviewRequest request) {
        ReviewEntry review = requireReview(sellerCode, reviewId);
        Product product = requireProductForSeller(review.getSellerId(), request.getProductId());
        applyReview(review, request);
        ReviewEntry saved = reviewEntryRepository.save(review);
        refreshProductRating(product);
        return toReviewResponse(saved, product);
    }

    @Override
    @CacheEvict(value = "sellerProducts", allEntries = true)
    public void deleteReview(String sellerCode, Long reviewId) {
        ReviewEntry review = requireReview(sellerCode, reviewId);
        Product product = review.getProductId() == null ? null : requireProductForSeller(review.getSellerId(), review.getProductId());
        reviewEntryRepository.delete(review);
        if (product != null) {
            refreshProductRating(product);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupportTicketResponse> listSupportTickets(String sellerCode, int page, int size, String status) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        List<SupportTicket> filtered = supportTicketRepository.findBySellerId(seller.getId()).stream()
                .filter(ticket -> matchesExact(ticket.getStatus(), status))
                .sorted(Comparator.comparing(SupportTicket::getUpdatedAt).reversed())
                .toList();
        return paginate(filtered, sanitizePage(page), sanitizeSize(size), this::toSupportTicketResponse);
    }

    @Override
    public SupportTicketResponse createSupportTicket(String sellerCode, SupportTicketRequest request) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        ensureUniqueTicketCode(request.getTicketCode(), null, seller.getId());
        SupportTicket ticket = new SupportTicket();
        ticket.setSellerId(seller.getId());
        applySupportTicket(ticket, request);
        ticket.setUpdatedAt(LocalDateTime.now());
        return toSupportTicketResponse(supportTicketRepository.save(ticket));
    }

    @Override
    public SupportTicketResponse updateSupportTicket(String sellerCode, Long ticketId, SupportTicketRequest request) {
        SupportTicket ticket = requireSupportTicket(sellerCode, ticketId);
        ensureUniqueTicketCode(request.getTicketCode(), ticket.getId(), ticket.getSellerId());
        applySupportTicket(ticket, request);
        ticket.setUpdatedAt(LocalDateTime.now());
        return toSupportTicketResponse(supportTicketRepository.save(ticket));
    }

    @Override
    public void deleteSupportTicket(String sellerCode, Long ticketId) {
        SupportTicket ticket = requireSupportTicket(sellerCode, ticketId);
        supportTicketRepository.delete(ticket);
    }

    private void applyProduct(Product product, ProductUpsertRequest request, LocalDateTime now) {
        product.setCategoryName(request.getCategoryName().trim());
        product.setName(request.getName().trim());
        product.setSku(request.getSku().trim().toUpperCase(Locale.ROOT));
        product.setDescription(trimToNull(request.getDescription()));
        product.setPrice(scaleMoney(request.getPrice()));
        product.setDiscountPercent(scaleMoney(request.getDiscountPercent()));
        product.setStockQuantity(request.getStockQuantity());
        product.setStatus(normalizeValue(request.getStatus()));
        product.setWeight(trimToNull(request.getWeight()));
        product.setDimensions(trimToNull(request.getDimensions()));
        product.setRatingAverage(defaultIfNull(product.getRatingAverage(), BigDecimal.ZERO));
        product.setReviewCount(defaultIfNull(product.getReviewCount(), 0));
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(now);
        }
        product.setUpdatedAt(now);
    }

    private void saveProductAssets(Long productId, ProductUpsertRequest request) {
        List<ProductUpsertRequest.ImagePayload> images = Optional.ofNullable(request.getImages()).orElse(List.of());
        for (int index = 0; index < images.size(); index++) {
            ProductUpsertRequest.ImagePayload imagePayload = images.get(index);
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imagePayload.getImageUrl().trim());
            image.setSortOrder(imagePayload.getSortOrder() == null ? index + 1 : imagePayload.getSortOrder());
            image.setPrimaryImage(Boolean.TRUE.equals(imagePayload.getPrimaryImage()));
            productImageRepository.save(image);
        }

        List<ProductUpsertRequest.VariantPayload> variants = Optional.ofNullable(request.getVariants()).orElse(List.of());
        for (ProductUpsertRequest.VariantPayload variantPayload : variants) {
            ProductVariant variant = new ProductVariant();
            variant.setProductId(productId);
            variant.setVariantName(variantPayload.getVariantName().trim());
            variant.setVariantValue(variantPayload.getVariantValue().trim());
            variant.setSkuSuffix(trimToNull(variantPayload.getSkuSuffix()));
            variant.setAdditionalPrice(scaleMoney(defaultIfNull(variantPayload.getAdditionalPrice(), BigDecimal.ZERO)));
            variant.setStockQuantity(defaultIfNull(variantPayload.getStockQuantity(), 0));
            productVariantRepository.save(variant);
        }
    }

    private void replaceProductAssets(Long productId, ProductUpsertRequest request) {
        productImageRepository.deleteByProductId(productId);
        productVariantRepository.deleteByProductId(productId);
        saveProductAssets(productId, request);
    }

    private void applyInventory(InventoryItem inventoryItem, InventoryRequest request) {
        inventoryItem.setProductId(request.getProductId());
        inventoryItem.setAvailableStock(request.getAvailableStock());
        inventoryItem.setReservedStock(request.getReservedStock());
        inventoryItem.setLowStockThreshold(request.getLowStockThreshold());
        inventoryItem.setStockHistory(trimToNull(request.getStockHistory()));
        inventoryItem.setUpdatedAt(LocalDateTime.now());
    }

    private void upsertInventoryForProduct(Product product, Integer availableStock, LocalDateTime now, String history) {
        InventoryItem inventory = inventoryItemRepository.findBySellerIdAndProductId(product.getSellerId(), product.getId())
                .orElseGet(InventoryItem::new);
        inventory.setSellerId(product.getSellerId());
        inventory.setProductId(product.getId());
        inventory.setAvailableStock(availableStock);
        inventory.setReservedStock(defaultIfNull(inventory.getReservedStock(), 0));
        inventory.setLowStockThreshold(defaultIfNull(inventory.getLowStockThreshold(), Math.max(5, availableStock / 5)));
        inventory.setStockHistory(history);
        inventory.setUpdatedAt(now);
        inventoryItemRepository.save(inventory);
    }

    private void syncProductStock(Product product, Integer availableStock) {
        product.setStockQuantity(availableStock);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private void applyOrder(SellerOrder order, OrderRequest request) {
        order.setCustomerId(request.getCustomerId());
        order.setOrderCode(request.getOrderCode().trim().toUpperCase(Locale.ROOT));
        String normalizedStatus = normalizeValue(request.getOrderStatus());
        if (!ORDER_STATUSES.contains(normalizedStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ORDER_STATUS", "Unsupported order status provided.");
        }
        order.setOrderStatus(normalizedStatus);
        order.setPaymentMethod(normalizeValue(request.getPaymentMethod()));
        order.setTotalAmount(scaleMoney(request.getTotalAmount()));
        order.setShippingPartner(trimToNull(request.getShippingPartner()));
        order.setTrackingNumber(trimToNull(request.getTrackingNumber()));
        order.setPrimaryProductName(request.getPrimaryProductName().trim());
        order.setTotalQuantity(request.getTotalQuantity());
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
    }

    private void ensureValidOrderStatusTransition(String previousStatus, String newStatus) {
        if (previousStatus == null || Objects.equals(previousStatus, newStatus)) {
            return;
        }

        Set<String> allowedNextStates = ORDER_STATUS_TRANSITIONS.getOrDefault(previousStatus, Set.of());
        if (!allowedNextStates.contains(newStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ORDER_STATUS_TRANSITION",
                    "Order status cannot move from " + previousStatus + " to " + newStatus + '.');
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

    private void applyCoupon(CouponCampaign coupon, CouponRequest request) {
        coupon.setCode(request.getCode().trim().toUpperCase(Locale.ROOT));
        coupon.setCampaignName(trimToNull(request.getCampaignName()));
        coupon.setDiscountType(normalizeValue(request.getDiscountType()));
        coupon.setDiscountValue(request.getDiscountValue().trim());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setStatus(normalizeValue(request.getStatus()));
    }

    private void applyReview(ReviewEntry review, ReviewRequest request) {
        review.setProductId(request.getProductId());
        review.setCustomerName(request.getCustomerName().trim());
        review.setRating(request.getRating().setScale(2, RoundingMode.HALF_UP));
        review.setReviewText(trimToNull(request.getReviewText()));
        review.setReplyText(trimToNull(request.getReplyText()));
        review.setAbusive(Boolean.TRUE.equals(request.getAbusive()));
    }

    private void applySupportTicket(SupportTicket ticket, SupportTicketRequest request) {
        ticket.setTicketCode(request.getTicketCode().trim().toUpperCase(Locale.ROOT));
        ticket.setSubject(request.getSubject().trim());
        ticket.setPriority(normalizeValue(request.getPriority()));
        ticket.setStatus(normalizeValue(request.getStatus()));
        ticket.setMessage(trimToNull(request.getMessage()));
        ticket.setAssignedTo(trimToNull(request.getAssignedTo()));
    }

    private void refreshProductRating(Product product) {
        List<ReviewEntry> productReviews = reviewEntryRepository.findBySellerId(product.getSellerId()).stream()
                .filter(review -> Objects.equals(review.getProductId(), product.getId()))
                .toList();
        BigDecimal total = productReviews.stream()
                .map(ReviewEntry::getRating)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        product.setReviewCount(productReviews.size());
        product.setRatingAverage(productReviews.isEmpty()
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : total.divide(BigDecimal.valueOf(productReviews.size()), 2, RoundingMode.HALF_UP));
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private void adjustStockFromOrder(Long sellerId, String productName, Integer quantity) {
        if (productName == null || productName.isBlank()) {
            return;
        }
        Optional<Product> matchingProduct = productRepository.findBySellerId(sellerId).stream()
                .filter(product -> product.getName() != null && product.getName().equalsIgnoreCase(productName.trim()))
                .findFirst();
        if (matchingProduct.isEmpty()) {
            return;
        }
        Product product = matchingProduct.get();
        InventoryItem inventoryItem = inventoryItemRepository.findBySellerIdAndProductId(sellerId, product.getId()).orElse(null);
        if (inventoryItem == null) {
            return;
        }
        int nextAvailable = Math.max(0, inventoryItem.getAvailableStock() - quantity);
        inventoryItem.setAvailableStock(nextAvailable);
        inventoryItem.setReservedStock(defaultIfNull(inventoryItem.getReservedStock(), 0) + quantity);
        inventoryItem.setUpdatedAt(LocalDateTime.now());
        inventoryItem.setStockHistory(defaultStockHistory("Reserved from order " + quantity));
        inventoryItemRepository.save(inventoryItem);
        syncProductStock(product, nextAvailable);
    }

    private void touchCustomerStats(CustomerProfile customer, BigDecimal amount, LocalDateTime orderDate) {
        if (customer == null) {
            return;
        }
        customer.setTotalOrders(defaultIfNull(customer.getTotalOrders(), 0) + 1);
        customer.setTotalPurchases(defaultIfNull(customer.getTotalPurchases(), BigDecimal.ZERO).add(scaleMoney(amount)));
        customer.setLastOrderAt(orderDate);
        customerProfileRepository.save(customer);
    }

    private Product requireProduct(String sellerCode, Long productId) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        return requireProductForSeller(seller.getId(), productId);
    }

    private Product requireProductForSeller(Long sellerId, Long productId) {
        return productRepository.findById(productId)
                .filter(product -> Objects.equals(product.getSellerId(), sellerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product was not found."));
    }

    private InventoryItem requireInventory(Long sellerId, Long inventoryId) {
        return inventoryItemRepository.findById(inventoryId)
                .filter(item -> Objects.equals(item.getSellerId(), sellerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "INVENTORY_NOT_FOUND", "Inventory record was not found."));
    }

    private SellerOrder requireOrder(String sellerCode, Long orderId) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        return sellerOrderRepository.findById(orderId)
                .filter(order -> Objects.equals(order.getSellerId(), seller.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order was not found."));
    }

    private CouponCampaign requireCoupon(String sellerCode, Long couponId) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        return couponCampaignRepository.findById(couponId)
                .filter(coupon -> Objects.equals(coupon.getSellerId(), seller.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COUPON_NOT_FOUND", "Coupon was not found."));
    }

    private ReviewEntry requireReview(String sellerCode, Long reviewId) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        return reviewEntryRepository.findById(reviewId)
                .filter(review -> Objects.equals(review.getSellerId(), seller.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "Review was not found."));
    }

    private SupportTicket requireSupportTicket(String sellerCode, Long ticketId) {
        Seller seller = sellerAccessService.resolveManagedSeller(sellerCode);
        return supportTicketRepository.findById(ticketId)
                .filter(ticket -> Objects.equals(ticket.getSellerId(), seller.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND", "Support ticket was not found."));
    }

    private CustomerProfile requireCustomerIfPresent(Long sellerId, Long customerId) {
        if (customerId == null) {
            return null;
        }
        return customerProfileRepository.findById(customerId)
                .filter(customer -> Objects.equals(customer.getSellerId(), sellerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer was not found."));
    }

    private void ensureUniqueProductSku(String sku, Long currentProductId) {
        String normalizedSku = sku.trim().toUpperCase(Locale.ROOT);
        boolean duplicate = productRepository.findAll().stream()
                .anyMatch(product -> product.getSku() != null
                        && product.getSku().equalsIgnoreCase(normalizedSku)
                        && !Objects.equals(product.getId(), currentProductId));
        if (duplicate) {
            throw new ApiException(HttpStatus.CONFLICT, "PRODUCT_SKU_EXISTS", "A product with this SKU already exists.");
        }
    }

    private void ensureUniqueOrderCode(String orderCode, Long currentOrderId, Long sellerId) {
        String normalizedCode = orderCode.trim().toUpperCase(Locale.ROOT);
        boolean duplicate = sellerOrderRepository.findBySellerId(sellerId).stream()
                .anyMatch(order -> order.getOrderCode() != null
                        && order.getOrderCode().equalsIgnoreCase(normalizedCode)
                        && !Objects.equals(order.getId(), currentOrderId));
        if (duplicate) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_CODE_EXISTS", "An order with this code already exists.");
        }
    }

    private void ensureUniqueCouponCode(String code, Long currentCouponId, Long sellerId) {
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        boolean duplicate = couponCampaignRepository.findBySellerId(sellerId).stream()
                .anyMatch(coupon -> coupon.getCode() != null
                        && coupon.getCode().equalsIgnoreCase(normalizedCode)
                        && !Objects.equals(coupon.getId(), currentCouponId));
        if (duplicate) {
            throw new ApiException(HttpStatus.CONFLICT, "COUPON_CODE_EXISTS", "A coupon with this code already exists.");
        }
    }

    private void ensureUniqueTicketCode(String ticketCode, Long currentTicketId, Long sellerId) {
        String normalizedCode = ticketCode.trim().toUpperCase(Locale.ROOT);
        boolean duplicate = supportTicketRepository.findBySellerId(sellerId).stream()
                .anyMatch(ticket -> ticket.getTicketCode() != null
                        && ticket.getTicketCode().equalsIgnoreCase(normalizedCode)
                        && !Objects.equals(ticket.getId(), currentTicketId));
        if (duplicate) {
            throw new ApiException(HttpStatus.CONFLICT, "TICKET_CODE_EXISTS", "A support ticket with this code already exists.");
        }
    }

    private void validateCouponDates(CouponRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_COUPON_DATES", "Coupon end date must be after start date.");
        }
    }

    private ProductResponse toProductResponse(Product product) {
        List<ProductResponse.ImageItem> images = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId()).stream()
                .map(image -> ProductResponse.ImageItem.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .sortOrder(image.getSortOrder())
                        .primaryImage(image.getPrimaryImage())
                        .build())
                .toList();
        List<ProductResponse.VariantItem> variants = productVariantRepository.findByProductIdOrderByIdAsc(product.getId()).stream()
                .map(variant -> ProductResponse.VariantItem.builder()
                        .id(variant.getId())
                        .variantName(variant.getVariantName())
                        .variantValue(variant.getVariantValue())
                        .skuSuffix(variant.getSkuSuffix())
                        .additionalPrice(variant.getAdditionalPrice())
                        .stockQuantity(variant.getStockQuantity())
                        .build())
                .toList();
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .categoryName(product.getCategoryName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPercent(product.getDiscountPercent())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .ratingAverage(product.getRatingAverage())
                .reviewCount(product.getReviewCount())
                .images(images)
                .variants(variants)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private InventoryResponse toInventoryResponse(InventoryItem inventoryItem, Product product) {
        return InventoryResponse.builder()
                .id(inventoryItem.getId())
                .productId(inventoryItem.getProductId())
                .productName(product == null ? null : product.getName())
                .availableStock(inventoryItem.getAvailableStock())
                .reservedStock(inventoryItem.getReservedStock())
                .lowStockThreshold(inventoryItem.getLowStockThreshold())
                .stockHistory(inventoryItem.getStockHistory())
                .updatedAt(inventoryItem.getUpdatedAt())
                .build();
    }

    private OrderResponse toOrderResponse(SellerOrder order, CustomerProfile customer) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerName(customer == null ? null : customer.getName())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .shippingPartner(order.getShippingPartner())
                .trackingNumber(order.getTrackingNumber())
                .primaryProductName(order.getPrimaryProductName())
                .totalQuantity(order.getTotalQuantity())
                .orderDate(order.getOrderDate())
                .build();
    }

    private CouponResponse toCouponResponse(CouponCampaign coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .campaignName(coupon.getCampaignName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .usageLimit(coupon.getUsageLimit())
                .status(coupon.getStatus())
                .build();
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

    private SupportTicketResponse toSupportTicketResponse(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .subject(ticket.getSubject())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .message(ticket.getMessage())
                .assignedTo(ticket.getAssignedTo())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private <T, R> PageResponse<R> paginate(List<T> items, int page, int size, Function<T, R> mapper) {
        int safeSize = sanitizeSize(size);
        int safePage = sanitizePage(page);
        long totalItems = items.size();
        int start = Math.min(safePage * safeSize, items.size());
        int end = Math.min(start + safeSize, items.size());
        List<R> mapped = items.subList(start, end).stream().map(mapper).toList();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / safeSize);
        return PageResponse.<R>builder()
                .items(mapped)
                .page(safePage)
                .size(safeSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .build();
    }

    private int sanitizePage(int page) {
        return Math.max(page, 0);
    }

    private int sanitizeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private boolean matches(String value, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesExact(String value, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return value != null && value.equalsIgnoreCase(filter.trim());
    }

    private String normalizeValue(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultStockHistory(String prefix) {
        return prefix + " at " + LocalDateTime.now();
    }

    private <T> T defaultIfNull(T value, T fallback) {
        return value == null ? fallback : value;
    }
}
