ALTER TABLE users
    MODIFY phone_number VARCHAR(15) NULL;

SET @has_phone_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'otp_requests'
      AND COLUMN_NAME = 'phone_number'
);

SET @rename_sql := IF(
    @has_phone_col > 0,
    'ALTER TABLE otp_requests CHANGE phone_number email VARCHAR(255) NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_old_index := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'otp_requests'
      AND INDEX_NAME = 'idx_otp_phone'
);

SET @drop_old_index_sql := IF(
    @has_old_index > 0,
    'DROP INDEX idx_otp_phone ON otp_requests',
    'SELECT 1'
);
PREPARE stmt FROM @drop_old_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_new_index := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'otp_requests'
      AND INDEX_NAME = 'idx_otp_email'
);

SET @create_new_index_sql := IF(
    @has_new_index = 0,
    'CREATE INDEX idx_otp_email ON otp_requests (email)',
    'SELECT 1'
);
PREPARE stmt FROM @create_new_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_password_hash_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'password_hash'
);

SET @add_password_hash_col_sql := IF(
    @has_password_hash_col = 0,
    'ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL AFTER email_verified',
    'SELECT 1'
);
PREPARE stmt FROM @add_password_hash_col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_users_email_uk := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND INDEX_NAME = 'uk_users_email'
);

SET @add_users_email_uk_sql := IF(
    @has_users_email_uk = 0,
    'CREATE UNIQUE INDEX uk_users_email ON users (email)',
    'SELECT 1'
);
PREPARE stmt FROM @add_users_email_uk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;