package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.TamTamRepository
import com.example.ui.navigation.HomeTab
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val currentScreen: Screen = Screen.Splash,
    val selectedTab: HomeTab = HomeTab.CHATS,
    val isLoggedIn: Boolean = true,
    val currentUserId: Long = 1L,
    val currentUsername: String = "samir_dev",
    val currentDisplayName: String = "Samir Bakhouche",
    val searchQuery: String = "",
    val serverUrl: String = "https://ais-dev-ywbbdoogqm3oxemnpid6sj-757345323820.europe-west2.run.app/",
    val serverConnected: Boolean = true,
    val isArabic: Boolean = true
)

class TamTamViewModel(
    val repository: TamTamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<MessageEntity>> = _activeMessages.asStateFlow()

    private var activeConversationId: Long? = null

    init {
        // Collect real-time WebSocket events from network client
        viewModelScope.launch {
            repository.networkClient.incomingEvents.collect {
                // When new real-time message or presence arrives, refresh or notify
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
        if (screen is Screen.ChatDetail) {
            activeConversationId = screen.conversationId
            viewModelScope.launch {
                repository.getMessagesFlow(screen.conversationId).collect { msgList ->
                    _activeMessages.value = msgList
                }
            }
        }
    }

    fun navigateBack() {
        when (_uiState.value.currentScreen) {
            is Screen.ChatDetail, Screen.Settings -> {
                _uiState.value = _uiState.value.copy(currentScreen = Screen.Home)
            }
            Screen.Auth -> {
                _uiState.value = _uiState.value.copy(currentScreen = Screen.Splash)
            }
            else -> {}
        }
    }

    fun selectTab(tab: HomeTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleLanguage() {
        _uiState.value = _uiState.value.copy(isArabic = !_uiState.value.isArabic)
    }

    fun updateServerUrl(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url)
        repository.networkClient.updateBaseUrl(url)
    }

    fun sendMessage(text: String) {
        val convId = activeConversationId ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(convId, text.trim())
        }
    }

    fun login(identifier: String) {
        viewModelScope.launch {
            repository.login(identifier)
            _uiState.value = _uiState.value.copy(
                isLoggedIn = true,
                currentScreen = Screen.Home,
                currentUsername = identifier,
                currentDisplayName = if (identifier.contains("samir")) "Samir Bakhouche" else identifier
            )
        }
    }

    fun register(username: String, phone: String, email: String, name: String) {
        viewModelScope.launch {
            repository.register(username, phone, email, name)
            _uiState.value = _uiState.value.copy(
                isLoggedIn = true,
                currentScreen = Screen.Home,
                currentUsername = username,
                currentDisplayName = name
            )
        }
    }

    fun logout() {
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            currentScreen = Screen.Auth
        )
    }
}

class TamTamViewModelFactory(private val repository: TamTamRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TamTamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TamTamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
