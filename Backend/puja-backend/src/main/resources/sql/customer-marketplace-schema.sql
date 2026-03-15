CREATE TABLE IF NOT EXISTS customer_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address_line_1 VARCHAR(200) NOT NULL,
    address_line_2 VARCHAR(200) NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(120) NOT NULL,
    pincode VARCHAR(12) NOT NULL,
    country VARCHAR(120) NOT NULL,
    landmark VARCHAR(200) NULL,
    delivery_instructions VARCHAR(255) NULL,
    default_address BIT NOT NULL DEFAULT b'0',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_customer_addresses_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS wishlist_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_wishlist_user_product (user_id, product_id),
    INDEX idx_wishlist_user_id (user_id),
    INDEX idx_wishlist_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS customer_order_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_customer_order_link (user_id, order_id),
    INDEX idx_customer_order_links_user_id (user_id),
    INDEX idx_customer_order_links_order_id (order_id)
);

CREATE TABLE IF NOT EXISTS order_shipping_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    recipient_name VARCHAR(160) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address_line_1 VARCHAR(200) NOT NULL,
    address_line_2 VARCHAR(200) NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(120) NOT NULL,
    pincode VARCHAR(12) NOT NULL,
    country VARCHAR(120) NOT NULL,
    landmark VARCHAR(200) NULL,
    delivery_instructions VARCHAR(255) NULL,
    delivery_charge DECIMAL(12,2) NOT NULL,
    estimated_delivery_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_order_shipping_details_order_id (order_id),
    INDEX idx_order_shipping_details_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS order_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    payment_status VARCHAR(40) NOT NULL,
    gateway_provider VARCHAR(40) NULL,
    gateway_reference VARCHAR(120) NULL,
    refund_status VARCHAR(40) NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    transaction_log VARCHAR(255) NULL,
    paid_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_order_payments_order_id (order_id),
    INDEX idx_order_payments_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS customer_notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    read_status BIT NOT NULL DEFAULT b'0',
    created_at DATETIME NOT NULL,
    INDEX idx_customer_notifications_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_refresh_tokens_hash (token_hash),
    INDEX idx_refresh_tokens_user_id (user_id)
);