CREATE TABLE IF NOT EXISTS sellers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registration_id VARCHAR(64) NULL,
    seller_code VARCHAR(64) NOT NULL UNIQUE,
    shop_name VARCHAR(180) NOT NULL,
    owner_name VARCHAR(120) NULL,
    email VARCHAR(160) NULL,
    phone_number VARCHAR(20) NULL,
    status VARCHAR(32) NOT NULL,
    gst_number VARCHAR(32) NULL,
    shop_address VARCHAR(255) NULL,
    shop_logo VARCHAR(255) NULL,
    shop_banner VARCHAR(255) NULL,
    return_policy TEXT NULL,
    bank_account_masked VARCHAR(64) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_sellers_registration_id (registration_id)
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(140) NOT NULL UNIQUE,
    active BIT NOT NULL DEFAULT b'1'
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    category_name VARCHAR(120) NOT NULL,
    name VARCHAR(180) NOT NULL,
    sku VARCHAR(80) NOT NULL UNIQUE,
    description TEXT NULL,
    price DECIMAL(12,2) NOT NULL,
    discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
    stock_quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    weight VARCHAR(60) NULL,
    dimensions VARCHAR(80) NULL,
    rating_average DECIMAL(3,2) NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_products_seller_id (seller_id),
    INDEX idx_products_category_name (category_name)
);

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    primary_image BIT NOT NULL DEFAULT b'0',
    INDEX idx_product_images_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS product_variants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    variant_name VARCHAR(80) NOT NULL,
    variant_value VARCHAR(120) NOT NULL,
    sku_suffix VARCHAR(40) NULL,
    additional_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    stock_quantity INT NOT NULL DEFAULT 0,
    INDEX idx_product_variants_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    available_stock INT NOT NULL,
    reserved_stock INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 10,
    stock_history VARCHAR(255) NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_inventory_seller_id (seller_id),
    INDEX idx_inventory_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(180) NULL,
    phone_number VARCHAR(20) NULL,
    total_orders INT NOT NULL DEFAULT 0,
    total_purchases DECIMAL(12,2) NOT NULL DEFAULT 0,
    last_order_at DATETIME NULL,
    INDEX idx_customers_seller_id (seller_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    customer_id BIGINT NULL,
    order_code VARCHAR(64) NOT NULL UNIQUE,
    order_status VARCHAR(40) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    shipping_partner VARCHAR(80) NULL,
    tracking_number VARCHAR(100) NULL,
    primary_product_name VARCHAR(180) NULL,
    total_quantity INT NOT NULL DEFAULT 1,
    order_date DATETIME NOT NULL,
    INDEX idx_orders_seller_id (seller_id),
    INDEX idx_orders_customer_id (customer_id),
    INDEX idx_orders_status (order_status)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    product_name VARCHAR(180) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    INDEX idx_order_items_order_id (order_id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    order_amount DECIMAL(12,2) NOT NULL,
    commission_amount DECIMAL(12,2) NOT NULL,
    net_amount DECIMAL(12,2) NOT NULL,
    payout_status VARCHAR(40) NOT NULL,
    payment_date DATETIME NOT NULL,
    INDEX idx_payments_seller_id (seller_id),
    INDEX idx_payments_order_id (order_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    customer_name VARCHAR(160) NOT NULL,
    rating DECIMAL(3,2) NOT NULL,
    review_text TEXT NULL,
    reply_text TEXT NULL,
    abusive BIT NOT NULL DEFAULT b'0',
    created_at DATETIME NOT NULL,
    INDEX idx_reviews_seller_id (seller_id),
    INDEX idx_reviews_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    campaign_name VARCHAR(160) NULL,
    discount_type VARCHAR(40) NOT NULL,
    discount_value VARCHAR(40) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    usage_limit INT NOT NULL,
    status VARCHAR(40) NOT NULL,
    INDEX idx_coupons_seller_id (seller_id)
);

CREATE TABLE IF NOT EXISTS shipping_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL UNIQUE,
    shipping_partners VARCHAR(255) NOT NULL,
    shipping_charges VARCHAR(120) NOT NULL,
    delivery_regions VARCHAR(255) NOT NULL,
    free_shipping_threshold VARCHAR(80) NOT NULL,
    estimated_delivery_times VARCHAR(120) NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    tone VARCHAR(32) NOT NULL,
    read_status BIT NOT NULL DEFAULT b'0',
    created_at DATETIME NOT NULL,
    INDEX idx_notifications_seller_id (seller_id)
);

CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    ticket_code VARCHAR(64) NOT NULL UNIQUE,
    subject VARCHAR(180) NOT NULL,
    priority VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    message TEXT NULL,
    assigned_to VARCHAR(120) NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_support_tickets_seller_id (seller_id)
);
