package com.example.data.repository

import com.example.data.local.ConversationDao
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageDao
import com.example.data.local.MessageEntity
import com.example.data.mapper.toEntity
import com.example.data.remote.CreateDirectChatRequest
import com.example.data.remote.SendMessageRequest
import com.example.data.remote.TamTamApiService
import com.example.domain.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val apiService: TamTamApiService
) : ConversationRepository {

    override fun getConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    override fun getActiveConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getActiveConversations()
    }

    override fun getArchivedConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getArchivedConversations()
    }

    override fun getConversationById(convId: Long): Flow<ConversationEntity?> {
        return conversationDao.getConversationById(convId)
    }

    override fun getMessagesForConversation(convId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(convId)
    }

    override suspend fun refreshConversations(): Result<List<ConversationEntity>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getConversations()
            if (response.isSuccessful && response.body()?.data != null) {
                val entities = response.body()!!.data!!.map { it.toEntity() }
                conversationDao.insertConversations(entities)
                Result.success(entities)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch conversations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDirectConversation(recipientId: Long): Result<ConversationEntity> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createDirectConversation(CreateDirectChatRequest(recipientId))
            if (response.isSuccessful && response.body()?.data != null) {
                val entity = response.body()!!.data!!.toEntity()
                conversationDao.insertConversation(entity)
                Result.success(entity)
            } else {
                // Fallback local creation if offline
                val localId = System.currentTimeMillis()
                val fallbackEntity = ConversationEntity(
                    id = localId,
                    uuid = "conv_$localId",
                    type = "direct",
                    title = "New Chat",
                    recipientId = recipientId
                )
                conversationDao.insertConversation(fallbackEntity)
                Result.success(fallbackEntity)
            }
        } catch (e: Exception) {
            val localId = System.currentTimeMillis()
            val fallbackEntity = ConversationEntity(
                id = localId,
                uuid = "conv_$localId",
                type = "direct",
                title = "New Chat",
                recipientId = recipientId
            )
            conversationDao.insertConversation(fallbackEntity)
            Result.success(fallbackEntity)
        }
    }

    override suspend fun sendMessage(conversationId: Long, content: String): Result<MessageEntity> = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msgId = System.currentTimeMillis()
        val localMessage = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderId = 1L,
            type = "text",
            content = content,
            createdAt = now,
            deliveryStatus = "sent",
            isOutgoing = true
        )

        // Optimistic local persistence
        messageDao.insertMessage(localMessage)
        conversationDao.updateLastMessage(conversationId, content, now)

        try {
            val response = apiService.sendMessage(
                conversationId,
                SendMessageRequest(content = content, senderId = 1L)
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val remoteDto = response.body()!!.data!!
                messageDao.updateStatus(localMessage.id, remoteDto.deliveryStatus)
                Result.success(localMessage.copy(deliveryStatus = remoteDto.deliveryStatus))
            } else {
                Result.success(localMessage)
            }
        } catch (e: Exception) {
            // Remains persisted locally in Room
            Result.success(localMessage)
        }
    }

    override suspend fun setPinned(convId: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setPinned(convId, isPinned)
    }

    override suspend fun setArchived(convId: Long, isArchived: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setArchived(convId, isArchived)
    }

    override suspend fun setMuted(convId: Long, isMuted: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setMuted(convId, isMuted)
    }

    override suspend fun markAsRead(convId: Long) = withContext(Dispatchers.IO) {
        conversationDao.updateUnreadCount(convId, 0)
    }
}
