package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, id DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, id DESC")
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY id DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :convId LIMIT 1")
    fun getConversationById(convId: Long): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :convId LIMIT 1")
    suspend fun getConversationDirect(convId: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageAt = :lastMessageAt WHERE id = :convId")
    suspend fun updateLastMessage(convId: Long, lastMessage: String, lastMessageAt: String)

    @Query("UPDATE conversations SET unreadCount = :count WHERE id = :convId")
    suspend fun updateUnreadCount(convId: Long, count: Int)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :convId")
    suspend fun setPinned(convId: Long, isPinned: Boolean)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE id = :convId")
    suspend fun setArchived(convId: Long, isArchived: Boolean)

    @Query("UPDATE conversations SET isMuted = :isMuted WHERE id = :convId")
    suspend fun setMuted(convId: Long, isMuted: Boolean)

    @Query("DELETE FROM conversations WHERE id = :convId")
    suspend fun deleteConversationById(convId: Long)

    @Query("DELETE FROM conversations")
    suspend fun clearAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY id ASC")
    fun getMessagesForConversation(convId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("UPDATE messages SET deliveryStatus = :status WHERE id = :msgId")
    suspend fun updateStatus(msgId: Long, status: String)

    @Query("DELETE FROM messages WHERE conversationId = :convId")
    suspend fun deleteMessagesForConversation(convId: Long)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserDirect(userId: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY displayName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE displayName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET isOnline = :isOnline, lastSeenAt = :lastSeen WHERE id = :userId")
    suspend fun updateUserStatus(userId: Long, isOnline: Boolean, lastSeen: String?)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAll()
}
