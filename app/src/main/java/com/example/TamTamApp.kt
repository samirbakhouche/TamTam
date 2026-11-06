package com.example

import android.app.Application
import com.example.data.repository.TamTamRepository
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TamTamApp : Application() {
    lateinit var container: AppContainer
        private set

    val repository: TamTamRepository
        get() = container.tamTamRepository

    override fun onCreate() {
        super.onCreate()

        // Initialize Dependency Injection Container
        container = DefaultAppContainer(this)

        // Seed initial data asynchronously on first run
        CoroutineScope(Dispatchers.IO).launch {
            container.tamTamRepository.initializeSeedDataIfNeeded()
        }
    }
}
