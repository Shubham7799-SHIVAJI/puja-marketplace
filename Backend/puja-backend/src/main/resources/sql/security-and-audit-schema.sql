CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_email VARCHAR(180) NULL,
    actor_role VARCHAR(40) NULL,
    action VARCHAR(16) NOT NULL,
    resource_path VARCHAR(255) NOT NULL,
    status_code INT NOT NULL,
    client_ip VARCHAR(64) NULL,
    created_at DATETIME NOT NULL,
    details VARCHAR(255) NULL,
    INDEX idx_audit_logs_created_at (created_at),
    INDEX idx_audit_logs_actor_email (actor_email)
);

CREATE TABLE IF NOT EXISTS admin_activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    action_type VARCHAR(80) NOT NULL,
    target_entity VARCHAR(80) NOT NULL,
    target_id BIGINT NOT NULL,
    ip_address VARCHAR(64) NULL,
    details VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_admin_activity_logs_created_at (created_at),
    INDEX idx_admin_activity_logs_admin_id (admin_id)
);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS admin_two_factor_enabled BIT NOT NULL DEFAULT b'1',
    ADD COLUMN IF NOT EXISTS admin_allowed_ips VARCHAR(512) NULL;

ALTER TABLE sellers
    ADD COLUMN IF NOT EXISTS commission_rate DECIMAL(5,2) NOT NULL DEFAULT 12.00;
