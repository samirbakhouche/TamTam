package com.example.di

import android.content.Context
import com.example.data.local.ConversationDao
import com.example.data.local.MessageDao
import com.example.data.local.TamTamDatabase
import com.example.data.local.UserDao
import com.example.data.remote.NetworkClient
import com.example.data.remote.TamTamApiService
import com.example.data.repository.TamTamRepository
import com.example.domain.repository.ConversationRepository
import com.example.domain.repository.UserRepository

interface AppContainer {
    val database: TamTamDatabase
    val userDao: UserDao
    val conversationDao: ConversationDao
    val messageDao: MessageDao
    val networkClient: NetworkClient
    val apiService: TamTamApiService
    val userRepository: UserRepository
    val conversationRepository: ConversationRepository
    val tamTamRepository: TamTamRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: TamTamDatabase by lazy {
        DatabaseModule.provideDatabase(context)
    }

    override val userDao: UserDao by lazy {
        DatabaseModule.provideUserDao(database)
    }

    override val conversationDao: ConversationDao by lazy {
        DatabaseModule.provideConversationDao(database)
    }

    override val messageDao: MessageDao by lazy {
        DatabaseModule.provideMessageDao(database)
    }

    override val networkClient: NetworkClient by lazy {
        NetworkModule.provideNetworkClient()
    }

    override val apiService: TamTamApiService by lazy {
        NetworkModule.provideApiService(networkClient)
    }

    override val userRepository: UserRepository by lazy {
        RepositoryModule.provideUserRepository(userDao, apiService)
    }

    override val conversationRepository: ConversationRepository by lazy {
        RepositoryModule.provideConversationRepository(conversationDao, messageDao, apiService)
    }

    override val tamTamRepository: TamTamRepository by lazy {
        RepositoryModule.provideTamTamRepository(
            conversationDao = conversationDao,
            messageDao = messageDao,
            userDao = userDao,
            networkClient = networkClient,
            userRepository = userRepository,
            conversationRepository = conversationRepository
        )
    }
}
