-- ========================================================================
-- TAMTAM MIGRATION: 005_create_messages_table.sql
-- Description: Core Messages table with nested replies, forwarding, fulltext search, and pagination indexes
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ========================================================================

-- UP MIGRATION: 1. Messages Table
CREATE TABLE IF NOT EXISTS `messages` (
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

    -- Constraints
    CONSTRAINT `uk_messages_uuid` UNIQUE (`uuid`),

    -- Foreign Key: Belongs to Conversation
    CONSTRAINT `fk_messages_conversation` 
        FOREIGN KEY (`conversation_id`) 
        REFERENCES `conversations` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,

    -- Foreign Key: Sent by User
    CONSTRAINT `fk_messages_sender` 
        FOREIGN KEY (`sender_id`) 
        REFERENCES `users` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,

    -- Self-Referencing Foreign Key: Reply to another message
    CONSTRAINT `fk_messages_reply_to` 
        FOREIGN KEY (`reply_to_id`) 
        REFERENCES `messages` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,

    -- Self-Referencing Foreign Key: Forwarded message
    CONSTRAINT `fk_messages_forwarded_from` 
        FOREIGN KEY (`forwarded_from_id`) 
        REFERENCES `messages` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,

    -- Composite Index for cursor-based chat history pagination (ORDER BY created_at DESC)
    INDEX `idx_messages_conv_timeline` (`conversation_id`, `created_at` DESC),
    
    -- Index for querying all messages from a specific sender
    INDEX `idx_messages_sender_id` (`sender_id`),

    -- Soft delete index
    INDEX `idx_messages_deleted_at` (`deleted_at`),

    -- Full-Text search index on message body
    FULLTEXT INDEX `ft_messages_content` (`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- UP MIGRATION: 2. Add circular foreign key to conversations for last_message_id
ALTER TABLE `conversations`
    ADD CONSTRAINT `fk_conversations_last_message`
    FOREIGN KEY (`last_message_id`)
    REFERENCES `messages` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- DOWN MIGRATION (ROLLBACK)
-- ALTER TABLE `conversations` DROP FOREIGN KEY `fk_conversations_last_message`;
-- DROP TABLE IF EXISTS `messages`;
