export const MARKETPLACE_TEXT = {
  home: {
    eyebrow: 'Marketplace storefront',
    title: 'Discover sacred products, trusted sellers, and festival-ready essentials.',
    subtitle:
      'Browse a multi-vendor catalog with instant search, wishlist, compare, and checkout flows built on the live marketplace backend.',
    searchPlaceholder: 'Search by product, category, or seller',
    featuredTitle: 'Featured arrivals',
    trendingTitle: 'Trending with devotees',
    budgetTitle: 'Budget-friendly picks',
    catalogTitle: 'Shop the live catalog',
    compareTitle: 'Compare tray',
    notificationsTitle: 'Customer notifications',
    emptyNotifications: 'No notifications yet. Order updates and payment confirmations will appear here.',
    signInRequired: 'Sign in as a customer to save wishlists and place orders.',
    emptyCatalog: 'No products match the current filters.',
  },
  product: {
    galleryTitle: 'Product gallery',
    reviewTitle: 'Customer reviews',
    sellerTitle: 'Seller and fulfillment',
    addToCart: 'Add to cart',
    toggleWishlist: 'Save for later',
    compare: 'Add to compare',
    quantity: 'Quantity',
    reviewEmpty: 'No reviews yet. Be the first verified buyer to review this product.',
  },
  wishlist: {
    title: 'Saved shortlist',
    subtitle: 'Track products you want to revisit before checkout.',
    empty: 'Your wishlist is empty right now.',
  },
  checkout: {
    title: 'Checkout',
    subtitle: 'Review cart items, choose a delivery address, and place the order.',
    addressTitle: 'Delivery addresses',
    addressFormTitle: 'Add a new address',
    orderSummary: 'Order summary',
    placeOrder: 'Place order',
    empty: 'Your cart is empty. Add products from the catalog before checkout.',
    successTitle: 'Order placed successfully',
  },
  orders: {
    title: 'Order history',
    subtitle: 'Track delivery progress, payment state, and seller fulfillment activity.',
    empty: 'No orders found yet.',
  },
  common: {
    wishlistLabel: 'Wishlist',
    ordersLabel: 'Orders',
    checkoutLabel: 'Checkout',
    backToCatalog: 'Back to marketplace',
    remove: 'Remove',
    loadFailed: 'Unable to load marketplace data right now.',
  },
};

export const MARKETPLACE_SORT_OPTIONS = [
  { label: 'Popular', value: 'popular' },
  { label: 'Top rated', value: 'rating' },
  { label: 'Price: low to high', value: 'price_low' },
  { label: 'Price: high to low', value: 'price_high' },
  { label: 'Newest', value: 'newest' },
];

export const MARKETPLACE_PAYMENT_METHODS = [
  { label: 'Cash on Delivery', value: 'COD' },
  { label: 'Razorpay', value: 'RAZORPAY' },
  { label: 'Stripe', value: 'STRIPE' },
];