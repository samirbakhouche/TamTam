-- ========================================================================
-- TAMTAM MESSAGING PLATFORM - MYSQL RELATIONAL DATABASE SCHEMA (V1.0)
-- Production-Ready Schema with Foreign Keys, Composite Indexes, Soft Deletes
-- Compatible with MySQL 8.0+ and MariaDB 10.5+
-- ========================================================================

CREATE DATABASE IF NOT EXISTS `tamtam_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `tamtam_db`;

-- Disable foreign key checks during migration
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------------
-- 1. USERS TABLE
-- Core identity and credentials
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` VARCHAR(36) NOT NULL UNIQUE,
    `username` VARCHAR(40) NOT NULL UNIQUE,
    `phone` VARCHAR(25) UNIQUE,
    `email` VARCHAR(191) UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `is_phone_verified` BOOLEAN DEFAULT FALSE,
    `is_email_verified` BOOLEAN DEFAULT FALSE,
    `role` ENUM('user', 'moderator', 'admin') DEFAULT 'user',
    `status` ENUM('active', 'suspended', 'deactivated') DEFAULT 'active',
    `last_seen_at` TIMESTAMP NULL DEFAULT NULL,
    `is_online` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_users_username` (`username`),
    INDEX `idx_users_phone` (`phone`),
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 2. USER_PROFILES TABLE
-- Extended public and private profile metadata
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `user_profiles`;
CREATE TABLE `user_profiles` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL UNIQUE,
    `display_name` VARCHAR(100) NOT NULL,
    `bio` VARCHAR(255) DEFAULT NULL,
    `avatar_url` VARCHAR(500) DEFAULT NULL,
    `cover_url` VARCHAR(500) DEFAULT NULL,
    `date_of_birth` DATE DEFAULT NULL,
    `gender` ENUM('male', 'female', 'other', 'unspecified') DEFAULT 'unspecified',
    `country_code` VARCHAR(5) DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_profile_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 3. USER_DEVICES TABLE
-- Push notification tokens (FCM) & session devices
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `user_devices`;
CREATE TABLE `user_devices` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `device_uuid` VARCHAR(100) NOT NULL,
    `fcm_token` VARCHAR(500) DEFAULT NULL,
    `platform` ENUM('android', 'ios', 'web', 'desktop') DEFAULT 'android',
    `device_model` VARCHAR(100) DEFAULT NULL,
    `os_version` VARCHAR(50) DEFAULT NULL,
    `app_version` VARCHAR(50) DEFAULT NULL,
    `last_active_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_device` (`user_id`, `device_uuid`),
    INDEX `idx_user_devices_user` (`user_id`),
    CONSTRAINT `fk_devices_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 4. CONTACTS TABLE
-- User address book and relationship synchronization
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `contacts`;
CREATE TABLE `contacts` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `contact_user_id` BIGINT UNSIGNED NOT NULL,
    `custom_name` VARCHAR(100) DEFAULT NULL,
    `is_favorite` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_contact` (`user_id`, `contact_user_id`),
    INDEX `idx_contacts_user` (`user_id`),
    CONSTRAINT `fk_contacts_owner` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_contacts_target` FOREIGN KEY (`contact_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 5. CONVERSATIONS TABLE
-- Unified chat threads (direct 1:1 or group)
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` VARCHAR(36) NOT NULL UNIQUE,
    `type` ENUM('direct', 'group', 'channel') DEFAULT 'direct',
    `title` VARCHAR(150) DEFAULT NULL,
    `avatar_url` VARCHAR(500) DEFAULT NULL,
    `last_message_id` BIGINT UNSIGNED DEFAULT NULL,
    `last_message_at` TIMESTAMP NULL DEFAULT NULL,
    `created_by` BIGINT UNSIGNED DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_conversations_last_msg` (`last_message_at`),
    CONSTRAINT `fk_conv_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 6. CONVERSATION_MEMBERS TABLE
-- Membership and per-user thread state
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `conversation_members`;
CREATE TABLE `conversation_members` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `role` ENUM('member', 'admin', 'owner') DEFAULT 'member',
    `is_muted` BOOLEAN DEFAULT FALSE,
    `is_pinned` BOOLEAN DEFAULT FALSE,
    `is_archived` BOOLEAN DEFAULT FALSE,
    `unread_count` INT UNSIGNED DEFAULT 0,
    `last_read_message_id` BIGINT UNSIGNED DEFAULT NULL,
    `joined_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `left_at` TIMESTAMP NULL DEFAULT NULL,
    UNIQUE KEY `uk_conv_member` (`conversation_id`, `user_id`),
    INDEX `idx_conv_member_user` (`user_id`),
    CONSTRAINT `fk_cm_conv` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cm_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 7. GROUPS TABLE
-- Extended group metadata, permissions, invite links
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `groups`;
CREATE TABLE `groups` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` BIGINT UNSIGNED NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `invite_code` VARCHAR(64) UNIQUE DEFAULT NULL,
    `is_private` BOOLEAN DEFAULT TRUE,
    `only_admins_send_messages` BOOLEAN DEFAULT FALSE,
    `max_members` INT UNSIGNED DEFAULT 200000,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_groups_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 8. GROUP_MEMBERS TABLE
-- Detailed member roles and administrative permission flags
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `group_members`;
CREATE TABLE `group_members` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `group_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `role` ENUM('member', 'admin', 'owner') DEFAULT 'member',
    `can_change_info` BOOLEAN DEFAULT FALSE,
    `can_post_messages` BOOLEAN DEFAULT TRUE,
    `can_delete_messages` BOOLEAN DEFAULT FALSE,
    `can_invite_users` BOOLEAN DEFAULT TRUE,
    `can_pin_messages` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_group_member` (`group_id`, `user_id`),
    CONSTRAINT `fk_gm_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_gm_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 9. MESSAGES TABLE
-- Text, audio, media, voice notes, system messages
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` VARCHAR(36) NOT NULL UNIQUE,
    `conversation_id` BIGINT UNSIGNED NOT NULL,
    `sender_id` BIGINT UNSIGNED NOT NULL,
    `reply_to_id` BIGINT UNSIGNED DEFAULT NULL,
    `forwarded_from_id` BIGINT UNSIGNED DEFAULT NULL,
    `type` ENUM('text', 'image', 'video', 'audio', 'voice', 'document', 'location', 'system') DEFAULT 'text',
    `content` TEXT DEFAULT NULL,
    `is_edited` BOOLEAN DEFAULT FALSE,
    `is_pinned` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    INDEX `idx_messages_conv_created` (`conversation_id`, `created_at`),
    INDEX `idx_messages_sender` (`sender_id`),
    FULLTEXT INDEX `ft_messages_content` (`content`),
    CONSTRAINT `fk_messages_conv` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_messages_reply` FOREIGN KEY (`reply_to_id`) REFERENCES `messages` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 10. MESSAGE_ATTACHMENTS TABLE
-- Media storage details (URL, mime type, dimensions, size, duration)
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `message_attachments`;
CREATE TABLE `message_attachments` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `message_id` BIGINT UNSIGNED NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_url` VARCHAR(500) NOT NULL,
    `file_size` BIGINT UNSIGNED NOT NULL,
    `mime_type` VARCHAR(100) NOT NULL,
    `thumbnail_url` VARCHAR(500) DEFAULT NULL,
    `width` INT UNSIGNED DEFAULT NULL,
    `height` INT UNSIGNED DEFAULT NULL,
    `duration_seconds` INT UNSIGNED DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_attachments_msg` (`message_id`),
    CONSTRAINT `fk_attach_msg` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 11. MESSAGE_READS TABLE
-- Message delivery status and read receipts (sent, delivered, read)
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `message_reads`;
CREATE TABLE `message_reads` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `message_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `status` ENUM('sent', 'delivered', 'read') DEFAULT 'delivered',
    `delivered_at` TIMESTAMP NULL DEFAULT NULL,
    `read_at` TIMESTAMP NULL DEFAULT NULL,
    UNIQUE KEY `uk_msg_user_read` (`message_id`, `user_id`),
    INDEX `idx_read_user` (`user_id`),
    CONSTRAINT `fk_reads_msg` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reads_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 12. MESSAGE_REACTIONS TABLE
-- Emoji reactions on messages
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `message_reactions`;
CREATE TABLE `message_reactions` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `message_id` BIGINT UNSIGNED NOT NULL,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `reaction_emoji` VARCHAR(32) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_msg_user_reaction` (`message_id`, `user_id`, `reaction_emoji`),
    CONSTRAINT `fk_react_msg` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_react_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 13. BLOCKED_USERS TABLE
-- User block lists
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `blocked_users`;
CREATE TABLE `blocked_users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `blocked_user_id` BIGINT UNSIGNED NOT NULL,
    `reason` VARCHAR(255) DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_block_pair` (`user_id`, `blocked_user_id`),
    CONSTRAINT `fk_block_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_block_target` FOREIGN KEY (`blocked_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 14. STORIES TABLE
-- Ephemeral 24-hour stories (images, videos, text statuses)
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `stories`;
CREATE TABLE `stories` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` VARCHAR(36) NOT NULL UNIQUE,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `media_type` ENUM('image', 'video', 'text') DEFAULT 'image',
    `media_url` VARCHAR(500) DEFAULT NULL,
    `caption` VARCHAR(500) DEFAULT NULL,
    `background_color` VARCHAR(20) DEFAULT NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `privacy` ENUM('everyone', 'contacts', 'close_friends') DEFAULT 'contacts',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_stories_user` (`user_id`),
    INDEX `idx_stories_expires` (`expires_at`),
    CONSTRAINT `fk_stories_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 15. STORY_VIEWS TABLE
-- Story viewers list and timestamps
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `story_views`;
CREATE TABLE `story_views` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `story_id` BIGINT UNSIGNED NOT NULL,
    `viewer_user_id` BIGINT UNSIGNED NOT NULL,
    `viewed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_story_viewer` (`story_id`, `viewer_user_id`),
    CONSTRAINT `fk_sv_story` FOREIGN KEY (`story_id`) REFERENCES `stories` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sv_viewer` FOREIGN KEY (`viewer_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 16. CALLS TABLE
-- WebRTC Voice & Video call logs and session signaling
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `calls`;
CREATE TABLE `calls` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` VARCHAR(36) NOT NULL UNIQUE,
    `caller_id` BIGINT UNSIGNED NOT NULL,
    `receiver_id` BIGINT UNSIGNED NOT NULL,
    `call_type` ENUM('voice', 'video') DEFAULT 'voice',
    `status` ENUM('initiated', 'ringing', 'accepted', 'rejected', 'missed', 'ended', 'busy') DEFAULT 'initiated',
    `started_at` TIMESTAMP NULL DEFAULT NULL,
    `ended_at` TIMESTAMP NULL DEFAULT NULL,
    `duration_seconds` INT UNSIGNED DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_calls_caller` (`caller_id`),
    INDEX `idx_calls_receiver` (`receiver_id`),
    CONSTRAINT `fk_calls_caller` FOREIGN KEY (`caller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_calls_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 17. NOTIFICATIONS TABLE
-- Activity stream and alert queue
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL,
    `actor_id` BIGINT UNSIGNED DEFAULT NULL,
    `type` ENUM('message', 'mention', 'call', 'group_invite', 'contact_joined', 'system') NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `body` TEXT NOT NULL,
    `payload_json` JSON DEFAULT NULL,
    `is_read` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_notif_user_read` (`user_id`, `is_read`),
    CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notif_actor` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 18. USER_SETTINGS TABLE
-- Preferences: Privacy, Notifications, Theme, Language
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT UNSIGNED NOT NULL UNIQUE,
    `theme` ENUM('system', 'light', 'dark') DEFAULT 'system',
    `language` ENUM('en', 'ar') DEFAULT 'en',
    `privacy_last_seen` ENUM('everyone', 'contacts', 'nobody') DEFAULT 'everyone',
    `privacy_profile_photo` ENUM('everyone', 'contacts', 'nobody') DEFAULT 'everyone',
    `privacy_status` ENUM('everyone', 'contacts', 'nobody') DEFAULT 'contacts',
    `read_receipts_enabled` BOOLEAN DEFAULT TRUE,
    `notifications_sound` BOOLEAN DEFAULT TRUE,
    `notifications_vibrate` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_settings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------
-- 19. REPORTS TABLE
-- User abuse reporting and moderation queue
-- ------------------------------------------------------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `reporter_id` BIGINT UNSIGNED NOT NULL,
    `reported_user_id` BIGINT UNSIGNED DEFAULT NULL,
    `reported_message_id` BIGINT UNSIGNED DEFAULT NULL,
    `reported_group_id` BIGINT UNSIGNED DEFAULT NULL,
    `reason` ENUM('spam', 'harassment', 'inappropriate_content', 'violence', 'scam', 'other') NOT NULL,
    `description` TEXT DEFAULT NULL,
    `status` ENUM('pending', 'reviewing', 'resolved', 'dismissed') DEFAULT 'pending',
    `moderator_notes` TEXT DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_reports_status` (`status`),
    CONSTRAINT `fk_reports_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reports_user` FOREIGN KEY (`reported_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_reports_msg` FOREIGN KEY (`reported_message_id`) REFERENCES `messages` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================================
-- END OF SCHEMA MIGRATION 001
-- ========================================================================
