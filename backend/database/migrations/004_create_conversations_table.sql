-- ========================================================================
-- TAMTAM MIGRATION: 004_create_conversations_table.sql
-- Description: Conversations and Conversation Members (pivot) tables
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ========================================================================

-- UP MIGRATION: 1. Conversations Table
CREATE TABLE IF NOT EXISTS `conversations` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `uuid` CHAR(36) NOT NULL,
    `type` ENUM('direct', 'group', 'channel') NOT NULL DEFAULT 'direct',
    `title` VARCHAR(150) NULL DEFAULT NULL, -- Nullable for direct 1:1 chats
    `avatar_url` VARCHAR(500) NULL DEFAULT NULL,
    `created_by` BIGINT UNSIGNED NULL DEFAULT NULL,
    `last_message_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `last_message_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,

    -- Constraints
    CONSTRAINT `uk_conversations_uuid` UNIQUE (`uuid`),
    CONSTRAINT `fk_conversations_creator` 
        FOREIGN KEY (`created_by`) 
        REFERENCES `users` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,

    -- Inbox ordering and query indexes
    INDEX `idx_conv_type_last_msg` (`type`, `last_message_at` DESC),
    INDEX `idx_conv_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- UP MIGRATION: 2. Conversation Members Pivot Table
CREATE TABLE IF NOT EXISTS `conversation_members` (
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

    -- Unique Membership Constraint per (conversation, user)
    CONSTRAINT `uk_conv_user_membership` UNIQUE (`conversation_id`, `user_id`),

    -- Foreign Keys with Cascade Deletion
    CONSTRAINT `fk_conv_members_conversation` 
        FOREIGN KEY (`conversation_id`) 
        REFERENCES `conversations` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_conv_members_user` 
        FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,

    -- Optimization Indexes for Instant User Inbox Queries
    INDEX `idx_cm_user_inbox` (`user_id`, `is_archived`, `is_pinned`),
    INDEX `idx_cm_unread` (`user_id`, `unread_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DOWN MIGRATION (ROLLBACK)
-- DROP TABLE IF EXISTS `conversation_members`;
-- DROP TABLE IF EXISTS `conversations`;
