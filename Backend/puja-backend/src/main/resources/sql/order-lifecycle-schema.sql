CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    previous_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    actor_email VARCHAR(180),
    actor_role VARCHAR(40),
    reason VARCHAR(255),
    changed_at DATETIME NOT NULL,
    INDEX idx_order_status_history_order_id (order_id),
    INDEX idx_order_status_history_changed_at (changed_at)
);
