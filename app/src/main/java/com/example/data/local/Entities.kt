package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["uuid"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: Long,
    val uuid: String,
    val username: String,
    val phone: String? = null,
    val email: String? = null,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
    val role: String = "user",
    val status: String = "active"
)

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["isArchived", "isPinned"])
    ]
)
data class ConversationEntity(
    @PrimaryKey val id: Long,
    val uuid: String = "",
    val type: String = "direct", // "direct", "group", "channel"
    val title: String,
    val avatarUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageAt: String = "",
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isOnline: Boolean = false,
    val recipientId: Long? = null
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId", "createdAt"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: Long,
    val conversationId: Long,
    val senderId: Long,
    val type: String = "text", // "text", "image", "voice", "video"
    val content: String,
    val createdAt: String,
    val deliveryStatus: String = "delivered", // "sent", "delivered", "read"
    val isOutgoing: Boolean = false
)
