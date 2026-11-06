package com.example.di

import com.example.data.local.ConversationDao
import com.example.data.local.MessageDao
import com.example.data.local.UserDao
import com.example.data.remote.NetworkClient
import com.example.data.remote.TamTamApiService
import com.example.data.repository.ConversationRepositoryImpl
import com.example.data.repository.TamTamRepository
import com.example.data.repository.UserRepositoryImpl
import com.example.domain.repository.ConversationRepository
import com.example.domain.repository.UserRepository

object RepositoryModule {
    fun provideUserRepository(
        userDao: UserDao,
        apiService: TamTamApiService
    ): UserRepository {
        return UserRepositoryImpl(userDao, apiService)
    }

    fun provideConversationRepository(
        conversationDao: ConversationDao,
        messageDao: MessageDao,
        apiService: TamTamApiService
    ): ConversationRepository {
        return ConversationRepositoryImpl(conversationDao, messageDao, apiService)
    }

    fun provideTamTamRepository(
        conversationDao: ConversationDao,
        messageDao: MessageDao,
        userDao: UserDao,
        networkClient: NetworkClient,
        userRepository: UserRepository,
        conversationRepository: ConversationRepository
    ): TamTamRepository {
        return TamTamRepository(
            conversationDao = conversationDao,
            messageDao = messageDao,
            userDao = userDao,
            networkClient = networkClient,
            userRepository = userRepository,
            conversationRepository = conversationRepository
        )
    }
}
