-- ========================================================================
-- TAMTAM MIGRATION: 002_create_users_table.sql
-- Description: Core Users table with authentication, security, and presence fields
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ========================================================================

-- UP MIGRATION
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` CHAR(36) NOT NULL,
    `username` VARCHAR(40) NOT NULL,
    `phone` VARCHAR(25) NULL,
    `email` VARCHAR(191) NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `is_phone_verified` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_email_verified` BOOLEAN NOT NULL DEFAULT FALSE,
    `role` ENUM('user', 'moderator', 'admin') NOT NULL DEFAULT 'user',
    `status` ENUM('active', 'suspended', 'deactivated') NOT NULL DEFAULT 'active',
    `is_online` BOOLEAN NOT NULL DEFAULT FALSE,
    `last_seen_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,

    -- Constraints
    CONSTRAINT `uk_users_uuid` UNIQUE (`uuid`),
    CONSTRAINT `uk_users_username` UNIQUE (`username`),
    CONSTRAINT `uk_users_phone` UNIQUE (`phone`),
    CONSTRAINT `uk_users_email` UNIQUE (`email`),

    -- Query optimization indexes
    INDEX `idx_users_status_last_seen` (`status`, `last_seen_at`),
    INDEX `idx_users_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DOWN MIGRATION (ROLLBACK)
-- DROP TABLE IF EXISTS `users`;
