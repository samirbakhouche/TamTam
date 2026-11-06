package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: T?,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: Long,
    @Json(name = "uuid") val uuid: String = "",
    @Json(name = "username") val username: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "is_online") val isOnline: Boolean? = false,
    @Json(name = "last_seen_at") val lastSeenAt: String? = null,
    @Json(name = "role") val role: String? = "user",
    @Json(name = "status") val status: String? = "active"
)

@JsonClass(generateAdapter = true)
data class AuthData(
    @Json(name = "user") val user: UserDto,
    @Json(name = "token") val token: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "password") val password: String = "123456"
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "username") val username: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "email") val email: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "password") val password: String = "123456"
)

@JsonClass(generateAdapter = true)
data class ConversationDto(
    @Json(name = "id") val id: Long,
    @Json(name = "uuid") val uuid: String = "",
    @Json(name = "type") val type: String = "direct",
    @Json(name = "title") val title: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "last_message") val lastMessage: String = "",
    @Json(name = "last_message_at") val lastMessageAt: String = "",
    @Json(name = "unread_count") val unreadCount: Int = 0,
    @Json(name = "is_pinned") val isPinned: Boolean? = false,
    @Json(name = "is_muted") val isMuted: Boolean? = false,
    @Json(name = "is_archived") val isArchived: Boolean? = false,
    @Json(name = "is_online") val isOnline: Boolean = false,
    @Json(name = "recipient_id") val recipientId: Long? = null
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    @Json(name = "id") val id: Long,
    @Json(name = "conversation_id") val conversationId: Long,
    @Json(name = "sender_id") val senderId: Long,
    @Json(name = "type") val type: String,
    @Json(name = "content") val content: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "delivery_status") val deliveryStatus: String
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "content") val content: String,
    @Json(name = "type") val type: String = "text",
    @Json(name = "sender_id") val senderId: Long = 1
)

@JsonClass(generateAdapter = true)
data class CreateDirectChatRequest(
    @Json(name = "recipient_id") val recipientId: Long
)

@JsonClass(generateAdapter = true)
data class PresenceRequest(
    @Json(name = "is_online") val isOnline: Boolean
)
