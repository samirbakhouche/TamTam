package com.example.data.mapper

import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserEntity
import com.example.data.remote.ConversationDto
import com.example.data.remote.MessageDto
import com.example.data.remote.UserDto

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        uuid = uuid.ifBlank { "user_$id" },
        username = username,
        phone = phone,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bio = bio,
        isOnline = isOnline ?: false,
        lastSeenAt = lastSeenAt,
        role = role ?: "user",
        status = status ?: "active"
    )
}

fun UserEntity.toDto(): UserDto {
    return UserDto(
        id = id,
        uuid = uuid,
        username = username,
        phone = phone,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bio = bio,
        isOnline = isOnline,
        lastSeenAt = lastSeenAt,
        role = role,
        status = status
    )
}

fun ConversationDto.toEntity(): ConversationEntity {
    return ConversationEntity(
        id = id,
        uuid = uuid.ifBlank { "conv_$id" },
        type = type,
        title = title,
        avatarUrl = avatarUrl,
        lastMessage = lastMessage,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        isPinned = isPinned ?: false,
        isMuted = isMuted ?: false,
        isArchived = isArchived ?: false,
        isOnline = isOnline,
        recipientId = recipientId
    )
}

fun ConversationEntity.toDto(): ConversationDto {
    return ConversationDto(
        id = id,
        uuid = uuid,
        type = type,
        title = title,
        avatarUrl = avatarUrl,
        lastMessage = lastMessage,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
        isArchived = isArchived,
        isOnline = isOnline,
        recipientId = recipientId
    )
}

fun MessageDto.toEntity(currentUserId: Long = 1L): MessageEntity {
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        type = type,
        content = content,
        createdAt = createdAt,
        deliveryStatus = deliveryStatus,
        isOutgoing = senderId == currentUserId
    )
}
