package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.TamTamViewModel
import com.example.ui.TamTamViewModelFactory
import com.example.ui.navigation.Screen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.TamTamTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TamTamViewModel by viewModels {
        TamTamViewModelFactory((application as TamTamApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val conversations by viewModel.conversations.collectAsState()
            val activeMessages by viewModel.activeMessages.collectAsState()

            val layoutDirection = if (uiState.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                TamTamTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        when (val screen = uiState.currentScreen) {
                            Screen.Splash -> {
                                SplashScreen(
                                    onSplashFinished = {
                                        if (uiState.isLoggedIn) {
                                            viewModel.navigateTo(Screen.Home)
                                        } else {
                                            viewModel.navigateTo(Screen.Auth)
                                        }
                                    }
                                )
                            }

                            Screen.Auth -> {
                                AuthScreen(
                                    onLoginSuccess = { identifier ->
                                        viewModel.login(identifier)
                                    },
                                    onRegisterSuccess = { username, phone, email, name ->
                                        viewModel.register(username, phone, email, name)
                                    }
                                )
                            }

                            Screen.Home -> {
                                HomeScreen(
                                    conversations = conversations,
                                    selectedTab = uiState.selectedTab,
                                    searchQuery = uiState.searchQuery,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onTabSelected = { viewModel.selectTab(it) },
                                    onConversationClick = { conv ->
                                        viewModel.navigateTo(
                                            Screen.ChatDetail(
                                                conversationId = conv.id,
                                                title = conv.title,
                                                isOnline = conv.isOnline,
                                                avatarUrl = conv.avatarUrl
                                            )
                                        )
                                    },
                                    onSettingsClick = {
                                        viewModel.navigateTo(Screen.Settings)
                                    },
                                    onLanguageToggle = {
                                        viewModel.toggleLanguage()
                                    }
                                )
                            }

                            is Screen.ChatDetail -> {
                                ChatDetailScreen(
                                    conversationId = screen.conversationId,
                                    title = screen.title,
                                    isOnline = screen.isOnline,
                                    avatarUrl = screen.avatarUrl,
                                    messages = activeMessages,
                                    onSendMessage = { text ->
                                        viewModel.sendMessage(text)
                                    },
                                    onBack = {
                                        viewModel.navigateBack()
                                    }
                                )
                            }

                            Screen.Settings -> {
                                SettingsScreen(
                                    currentUsername = uiState.currentUsername,
                                    currentDisplayName = uiState.currentDisplayName,
                                    serverUrl = uiState.serverUrl,
                                    isArabic = uiState.isArabic,
                                    onServerUrlChange = { newUrl ->
                                        viewModel.updateServerUrl(newUrl)
                                    },
                                    onLanguageToggle = {
                                        viewModel.toggleLanguage()
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                    },
                                    onBack = {
                                        viewModel.navigateBack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
