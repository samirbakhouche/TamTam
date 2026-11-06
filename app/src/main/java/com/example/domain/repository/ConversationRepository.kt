package com.example.domain.repository

import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getConversations(): Flow<List<ConversationEntity>>
    fun getActiveConversations(): Flow<List<ConversationEntity>>
    fun getArchivedConversations(): Flow<List<ConversationEntity>>
    fun getConversationById(convId: Long): Flow<ConversationEntity?>
    fun getMessagesForConversation(convId: Long): Flow<List<MessageEntity>>
    suspend fun refreshConversations(): Result<List<ConversationEntity>>
    suspend fun createDirectConversation(recipientId: Long): Result<ConversationEntity>
    suspend fun sendMessage(conversationId: Long, content: String): Result<MessageEntity>
    suspend fun setPinned(convId: Long, isPinned: Boolean)
    suspend fun setArchived(convId: Long, isArchived: Boolean)
    suspend fun setMuted(convId: Long, isMuted: Boolean)
    suspend fun markAsRead(convId: Long)
}
