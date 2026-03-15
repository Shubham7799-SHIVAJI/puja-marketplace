export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface CategoryOption {
  id: number;
  name: string;
  slug: string;
  productCount: number;
}

export interface MarketplaceProductCard {
  id: number;
  name: string;
  sellerName: string;
  sellerCode: string | null;
  categoryName: string;
  description: string | null;
  price: number;
  finalPrice: number;
  discountPercent: number;
  ratingAverage: number;
  reviewCount: number;
  stockQuantity: number;
  status: string;
  imageUrl: string | null;
  deliveryLabel: string;
  wishlisted: boolean;
}

export interface MarketplaceHighlights {
  categories: CategoryOption[];
  featuredProducts: MarketplaceProductCard[];
  trendingProducts: MarketplaceProductCard[];
  budgetProducts: MarketplaceProductCard[];
}

export interface ProductImageItem {
  id: number;
  imageUrl: string;
  sortOrder: number;
  primaryImage: boolean;
}

export interface ProductVariantItem {
  id: number;
  variantName: string;
  variantValue: string;
  skuSuffix: string | null;
  additionalPrice: number;
  stockQuantity: number;
}

export interface ReviewItem {
  id: number;
  productId: number;
  productName: string | null;
  customerName: string;
  rating: number;
  reviewText: string | null;
  replyText: string | null;
  abusive: boolean;
  createdAt: string;
}

export interface MarketplaceProductDetail {
  product: MarketplaceProductCard;
  sellerDescription: string;
  returnPolicy: string;
  shippingPartners: string;
  deliveryRegions: string;
  estimatedDelivery: string;
  images: ProductImageItem[];
  variants: ProductVariantItem[];
  reviews: ReviewItem[];
}

export interface WishlistItem {
  id: number;
  productId: number;
  createdAt: string;
  product: MarketplaceProductCard;
}

export interface CustomerAddress {
  id: number;
  fullName: string;
  phoneNumber: string;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  state: string;
  pincode: string;
  country: string;
  landmark: string | null;
  deliveryInstructions: string | null;
  defaultAddress: boolean;
}

export interface CustomerAddressPayload {
  fullName: string;
  phoneNumber: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  pincode: string;
  country: string;
  landmark: string;
  deliveryInstructions: string;
  defaultAddress: boolean;
}

export interface CheckoutPayload {
  addressId: number;
  paymentMethod: string;
  couponCode?: string;
  items: Array<{
    productId: number;
    variantId?: number | null;
    quantity: number;
  }>;
}

export interface CheckoutResponse {
  placedAt: string;
  totalAmount: number;
  paymentMethod: string;
  paymentStatus: string;
  orders: Array<{
    orderId: number;
    orderCode: string;
    sellerName: string;
    status: string;
    totalAmount: number;
    trackingNumber: string;
  }>;
}

export interface CustomerOrder {
  orderId: number;
  orderCode: string;
  sellerName: string;
  sellerCode: string | null;
  orderStatus: string;
  paymentMethod: string;
  paymentStatus: string;
  totalAmount: number;
  deliveryCharge: number;
  shippingPartner: string | null;
  trackingNumber: string | null;
  orderDate: string;
  estimatedDeliveryAt: string | null;
  shippingAddress: {
    recipientName: string;
    phoneNumber: string;
    addressLine1: string;
    addressLine2: string | null;
    city: string;
    state: string;
    pincode: string;
    country: string;
    landmark: string | null;
    deliveryInstructions: string | null;
  } | null;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
  }>;
}

export interface CustomerNotification {
  id: number;
  type: string;
  title: string;
  detail: string;
  read: boolean;
  createdAt: string;
}

export interface CustomerReviewPayload {
  productId: number;
  rating: number;
  reviewText: string;
}