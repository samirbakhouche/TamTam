-- ========================================================================
-- TAMTAM MIGRATION: 003_create_profiles_table.sql
-- Description: User Profiles table storing display metadata, bio, avatar, and demographics
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ========================================================================

-- UP MIGRATION
CREATE TABLE IF NOT EXISTS `profiles` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `display_name` VARCHAR(100) NOT NULL,
    `bio` VARCHAR(255) NULL DEFAULT NULL,
    `avatar_url` VARCHAR(500) NULL DEFAULT NULL,
    `cover_url` VARCHAR(500) NULL DEFAULT NULL,
    `date_of_birth` DATE NULL DEFAULT NULL,
    `gender` ENUM('male', 'female', 'other', 'unspecified') NOT NULL DEFAULT 'unspecified',
    `country_code` CHAR(2) NULL DEFAULT NULL, -- ISO 3166-1 alpha-2 (e.g. 'DZ', 'SA', 'EG')
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 1-to-1 Relationship Constraint
    CONSTRAINT `uk_profiles_user_id` UNIQUE (`user_id`),
    CONSTRAINT `fk_profiles_user_id` 
        FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,

    -- Search and filtering indexes
    INDEX `idx_profiles_display_name` (`display_name`),
    INDEX `idx_profiles_country` (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DOWN MIGRATION (ROLLBACK)
-- DROP TABLE IF EXISTS `profiles`;
