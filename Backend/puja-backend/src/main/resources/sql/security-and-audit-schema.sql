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
