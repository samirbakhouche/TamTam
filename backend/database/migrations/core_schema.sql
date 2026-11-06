-- ========================================================================
-- TAMTAM MESSAGING PLATFORM - CORE SCHEMA SQL MIGRATION
-- Core Tables: users, profiles, conversations, conversation_members, messages
-- Engine: InnoDB | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ========================================================================

CREATE DATABASE IF NOT EXISTS `tamtam_db` 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `tamtam_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------------
-- 1. USERS TABLE
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
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

    CONSTRAINT `uk_users_uuid` UNIQUE (`uuid`),
    CONSTRAINT `uk_users_username` UNIQUE (`username`),
    CONSTRAINT `uk_users_phone` UNIQUE (`phone`),
    CONSTRAINT `uk_users_email` UNIQUE (`email`),

    INDEX `idx_users_status_last_seen` (`status`, `last_seen_at`),
    INDEX `idx_users_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 2. PROFILES TABLE (1:1 with users)
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `profiles`;
CREATE TABLE `profiles` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `display_name` VARCHAR(100) NOT NULL,
    `bio` VARCHAR(255) NULL DEFAULT NULL,
    `avatar_url` VARCHAR(500) NULL DEFAULT NULL,
    `cover_url` VARCHAR(500) NULL DEFAULT NULL,
    `date_of_birth` DATE NULL DEFAULT NULL,
    `gender` ENUM('male', 'female', 'other', 'unspecified') NOT NULL DEFAULT 'unspecified',
    `country_code` CHAR(2) NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT `uk_profiles_user_id` UNIQUE (`user_id`),
    CONSTRAINT `fk_profiles_user` 
        FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,

    INDEX `idx_profiles_display_name` (`display_name`),
    INDEX `idx_profiles_country` (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 3. CONVERSATIONS TABLE
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` CHAR(36) NOT NULL,
    `type` ENUM('direct', 'group', 'channel') NOT NULL DEFAULT 'direct',
    `title` VARCHAR(150) NULL DEFAULT NULL,
    `avatar_url` VARCHAR(500) NULL DEFAULT NULL,
    `created_by` BIGINT UNSIGNED NULL DEFAULT NULL,
    `last_message_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `last_message_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT `uk_conversations_uuid` UNIQUE (`uuid`),
    CONSTRAINT `fk_conversations_creator` 
        FOREIGN KEY (`created_by`) 
        REFERENCES `users` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,

    INDEX `idx_conv_type_timeline` (`type`, `last_message_at` DESC),
    INDEX `idx_conv_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 4. CONVERSATION_MEMBERS TABLE (Pivot for Multi-user & Direct Inboxes)
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `conversation_members`;
CREATE TABLE `conversation_members` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `role` ENUM('member', 'admin', 'owner') NOT NULL DEFAULT 'member',
    `unread_count` INT UNSIGNED NOT NULL DEFAULT 0,
    `last_read_message_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `is_pinned` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_muted` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_archived` BOOLEAN NOT NULL DEFAULT FALSE,
    `joined_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `left_at` TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT `uk_conv_user` UNIQUE (`conversation_id`, `user_id`),
    CONSTRAINT `fk_cm_conversation` 
        FOREIGN KEY (`conversation_id`) 
        REFERENCES `conversations` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_cm_user` 
        FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,

    INDEX `idx_cm_user_inbox` (`user_id`, `is_archived`, `is_pinned`),
    INDEX `idx_cm_unread` (`user_id`, `unread_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 5. MESSAGES TABLE
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` CHAR(36) NOT NULL,
    `conversation_id` BIGINT UNSIGNED NOT NULL,
    `sender_id` BIGINT UNSIGNED NOT NULL,
    `reply_to_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `forwarded_from_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `type` ENUM('text', 'image', 'video', 'audio', 'voice', 'document', 'location', 'system') NOT NULL DEFAULT 'text',
    `content` TEXT NULL DEFAULT NULL,
    `is_edited` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_pinned` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT `uk_messages_uuid` UNIQUE (`uuid`),
    CONSTRAINT `fk_messages_conversation` 
        FOREIGN KEY (`conversation_id`) 
        REFERENCES `conversations` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_messages_sender` 
        FOREIGN KEY (`sender_id`) 
        REFERENCES `users` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_messages_reply_to` 
        FOREIGN KEY (`reply_to_id`) 
        REFERENCES `messages` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_messages_forwarded_from` 
        FOREIGN KEY (`forwarded_from_id`) 
        REFERENCES `messages` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,

    INDEX `idx_messages_conv_timeline` (`conversation_id`, `created_at` DESC),
    INDEX `idx_messages_sender_id` (`sender_id`),
    INDEX `idx_messages_deleted_at` (`deleted_at`),
    FULLTEXT INDEX `ft_messages_content` (`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Circular reference: update conversations with fk to messages
ALTER TABLE `conversations`
    ADD CONSTRAINT `fk_conversations_last_message`
    FOREIGN KEY (`last_message_id`)
    REFERENCES `messages` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- ------------------------------------------------------------------------
-- 6. AUTOMATED TRIGGER: Maintain conversation last_message pointer
-- ------------------------------------------------------------------------
DELIMITER $$
DROP TRIGGER IF EXISTS `trg_after_message_insert`$$
CREATE TRIGGER `trg_after_message_insert`
AFTER INSERT ON `messages`
FOR EACH ROW
BEGIN
    UPDATE `conversations`
    SET `last_message_id` = NEW.id,
        `last_message_at` = NEW.created_at
    WHERE `id` = NEW.conversation_id;

    -- Increment unread counter for other conversation members
    UPDATE `conversation_members`
    SET `unread_count` = `unread_count` + 1
    WHERE `conversation_id` = NEW.conversation_id
      AND `user_id` != NEW.sender_id;
END$$
DELIMITER ;

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================================
-- SEED DATA FOR DEMO & INTEGRATION TESTING
-- ========================================================================
INSERT INTO `users` (`id`, `uuid`, `username`, `phone`, `email`, `password_hash`, `is_online`)
VALUES 
(1, '8e3bfa70-a81d-4eb2-8c11-fb45fbe2ef41', 'samir_dev', '+213550112233', 'samir@tamtam.dz', '$2a$12$K8y4Zf0Kx1Q4u9i8Vw6aU.ZlD5ZtO8Wc3N9Q1xK0L7mP4e2rT6y8u', TRUE),
(2, '9f4c0b81-b92e-5fc3-9d22-0c560cf3fa52', 'amira_tech', '+213550445566', 'amira@tamtam.dz', '$2a$12$K8y4Zf0Kx1Q4u9i8Vw6aU.ZlD5ZtO8Wc3N9Q1xK0L7mP4e2rT6y8u', TRUE);

INSERT INTO `profiles` (`user_id`, `display_name`, `bio`, `avatar_url`, `country_code`)
VALUES 
(1, 'Samir Bakhouche', 'Senior Android & Backend Engineer', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200', 'DZ'),
(2, 'Amira Mansour', 'Product Designer & UI Specialist', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200', 'DZ');

INSERT INTO `conversations` (`id`, `uuid`, `type`, `created_by`)
VALUES 
(1, 'conv-direct-1-2', 'direct', 1);

INSERT INTO `conversation_members` (`conversation_id`, `user_id`, `role`, `unread_count`)
VALUES 
(1, 1, 'member', 0),
(1, 2, 'member', 1);

INSERT INTO `messages` (`id`, `uuid`, `conversation_id`, `sender_id`, `type`, `content`)
VALUES 
(1, 'msg-1001', 1, 1, 'text', 'السلام عليكم يا أميرة، تم تجهيز وتأكيد جداول قاعدة بيانات MySQL بنجاح!'),
(2, 'msg-1002', 1, 2, 'text', 'وعليكم السلام يا سمير! ممتاز جداً، بنية الجداول متماسكة ومجهزة للمراسلة الفورية.');
