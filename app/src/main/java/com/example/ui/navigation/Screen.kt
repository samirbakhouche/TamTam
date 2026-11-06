package com.example.ui.navigation

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    object Home : Screen()
    data class ChatDetail(
        val conversationId: Long,
        val title: String,
        val isOnline: Boolean,
        val avatarUrl: String?
    ) : Screen()
    object Settings : Screen()
}

enum class HomeTab {
    CHATS,
    GROUPS,
    STORIES,
    CALLS,
    CONTACTS
}
