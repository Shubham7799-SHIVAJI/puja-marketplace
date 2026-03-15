ALTER TABLE users
    MODIFY phone_number VARCHAR(15) NULL;

SET @has_phone_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'otp_requests'
      AND COLUMN_NAME = 'phone_number'
);

SET @has_email_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'otp_requests'
      AND COLUMN_NAME = 'email'
);

SET @rename_sql := IF(
    @has_phone_col > 0 AND @has_email_col = 0,
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

SET @has_phone_number_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'otp_requests'
      AND COLUMN_NAME = 'phone_number'
);

SET @add_phone_number_col_sql := IF(
    @has_phone_number_col = 0,
    'ALTER TABLE otp_requests ADD COLUMN phone_number VARCHAR(15) NULL AFTER email',
    'SELECT 1'
);
PREPARE stmt FROM @add_phone_number_col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @otp_email_nullable := (
    SELECT IS_NULLABLE
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'otp_requests'
      AND COLUMN_NAME = 'email'
    LIMIT 1
);

SET @make_otp_email_nullable_sql := IF(
    @otp_email_nullable = 'NO',
    'ALTER TABLE otp_requests MODIFY email VARCHAR(255) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @make_otp_email_nullable_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_phone_index := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'otp_requests'
      AND INDEX_NAME = 'idx_otp_requests_phone_number'
);

SET @create_phone_index_sql := IF(
    @has_phone_index = 0,
    'CREATE INDEX idx_otp_requests_phone_number ON otp_requests (phone_number)',
    'SELECT 1'
);
PREPARE stmt FROM @create_phone_index_sql;
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

SET @duplicate_user_email_count := (
    SELECT COUNT(*)
    FROM (
        SELECT email
        FROM users
        WHERE email IS NOT NULL
          AND TRIM(email) <> ''
        GROUP BY email
        HAVING COUNT(*) > 1
    ) duplicate_emails
);

SET @add_users_email_uk_sql := IF(
    @has_users_email_uk = 0 AND @duplicate_user_email_count = 0,
    'CREATE UNIQUE INDEX uk_users_email ON users (email)',
    'SELECT 1'
);
PREPARE stmt FROM @add_users_email_uk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;