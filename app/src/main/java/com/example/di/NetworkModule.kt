package com.example.di

import com.example.data.remote.NetworkClient
import com.example.data.remote.TamTamApiService

object NetworkModule {
    @Volatile
    private var networkClientInstance: NetworkClient? = null

    fun provideNetworkClient(): NetworkClient {
        return networkClientInstance ?: synchronized(this) {
            networkClientInstance ?: NetworkClient().also { networkClientInstance = it }
        }
    }

    fun provideApiService(networkClient: NetworkClient): TamTamApiService {
        return networkClient.apiService
    }
}
