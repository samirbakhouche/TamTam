package com.example.domain.repository

import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(userId: Long): Flow<UserEntity?>
    fun getAllUsers(): Flow<List<UserEntity>>
    fun searchUsers(query: String): Flow<List<UserEntity>>
    suspend fun fetchAndCacheUsers(): Result<List<UserEntity>>
    suspend fun fetchUserProfile(userId: Long): Result<UserEntity>
    suspend fun updatePresence(isOnline: Boolean): Result<Unit>
    suspend fun saveUser(user: UserEntity)
}
