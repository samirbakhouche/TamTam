package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.mapper.toEntity
import com.example.data.remote.PresenceRequest
import com.example.data.remote.TamTamApiService
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val apiService: TamTamApiService
) : UserRepository {

    override fun getUserById(userId: Long): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }

    override fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    override fun searchUsers(query: String): Flow<List<UserEntity>> {
        return userDao.searchUsers(query)
    }

    override suspend fun fetchAndCacheUsers(): Result<List<UserEntity>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body()?.data != null) {
                val entities = response.body()!!.data!!.map { it.toEntity() }
                userDao.insertUsers(entities)
                Result.success(entities)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUserProfile(userId: Long): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserProfile(userId)
            if (response.isSuccessful && response.body()?.data != null) {
                val entity = response.body()!!.data!!.toEntity()
                userDao.insertUser(entity)
                Result.success(entity)
            } else {
                // Fallback to local cached user if offline
                val local = userDao.getUserDirect(userId)
                if (local != null) Result.success(local)
                else Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            val local = userDao.getUserDirect(userId)
            if (local != null) Result.success(local)
            else Result.failure(e)
        }
    }

    override suspend fun updatePresence(isOnline: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.updatePresence(PresenceRequest(isOnline))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }
}
