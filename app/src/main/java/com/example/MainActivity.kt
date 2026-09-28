package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.CreateAiDatabase
import com.example.data.repository.CreateAiRepository
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.CreateAiTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    private val viewModel: CreateAiViewModel by viewModels {
        val database = CreateAiDatabase.getInstance(applicationContext)
        val repository = CreateAiRepository(database)
        CreateAiViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearSnackbar()
                }
            }

            // Handle back button for secondary screens
            if (uiState.currentScreen != ScreenDestination.DASHBOARD) {
                BackHandler {
                    viewModel.selectScreen(ScreenDestination.DASHBOARD)
                }
            }

            CreateAiTheme(darkTheme = uiState.isDarkTheme) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth >= 600.dp

                    Scaffold(
                        topBar = {
                            CreateAiTopAppBar(
                                currentScreen = uiState.currentScreen,
                                isDarkTheme = uiState.isDarkTheme,
                                maintenanceMode = uiState.maintenanceMode,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onOpenProfile = { viewModel.selectScreen(ScreenDestination.PROFILE) },
                                onOpenAdmin = { viewModel.selectScreen(ScreenDestination.ADMIN_SETTINGS) }
                            )
                        },
                        bottomBar = {
                            if (!isWideScreen) {
                                CreateAiBottomNav(
                                    currentScreen = uiState.currentScreen,
                                    onNavigate = { viewModel.selectScreen(it) }
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isWideScreen) {
                                CreateAiNavRail(
                                    currentScreen = uiState.currentScreen,
                                    onNavigate = { viewModel.selectScreen(it) }
                                )
                            }

                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                when (uiState.currentScreen) {
                                    ScreenDestination.DASHBOARD -> {
                                        DashboardScreen(
                                            uiState = uiState,
                                            onNavigate = { viewModel.selectScreen(it) }
                                        )
                                    }
                                    ScreenDestination.CHAT -> {
                                        ChatScreen(
                                            uiState = uiState,
                                            onSendMessage = { viewModel.sendChatMessage(it) },
                                            onNewConversation = { viewModel.createNewConversation() },
                                            onSelectConversation = { viewModel.selectConversation(it) },
                                            onClearConversation = { viewModel.clearActiveConversation() },
                                            onDeleteConversation = { viewModel.deleteConversation(it) }
                                        )
                                    }
                                    ScreenDestination.IMAGE -> {
                                        ImageGeneratorScreen(
                                            uiState = uiState,
                                            onGenerate = { p, ar, sz, cnt, np ->
                                                viewModel.generateImages(p, ar, sz, cnt, np)
                                            },
                                            onDeleteGeneration = { viewModel.deleteImageGeneration(it) }
                                        )
                                    }
                                    ScreenDestination.VIDEO -> {
                                        VideoGeneratorScreen(
                                            uiState = uiState,
                                            onGenerate = { p, img, dur, ar ->
                                                viewModel.generateVideo(p, img, dur, ar)
                                            },
                                            onDeleteVideo = { viewModel.deleteVideoGeneration(it) }
                                        )
                                    }
                                    ScreenDestination.HISTORY -> {
                                        HistoryScreen(
                                            uiState = uiState,
                                            onNavigate = { viewModel.selectScreen(it) },
                                            onOpenChat = { viewModel.selectConversation(it) },
                                            onDeleteImage = { viewModel.deleteImageGeneration(it) },
                                            onDeleteVideo = { viewModel.deleteVideoGeneration(it) }
                                        )
                                    }
                                    ScreenDestination.PROFILE -> {
                                        ProfileScreen(
                                            uiState = uiState,
                                            onOpenAuthModal = { viewModel.showAuthModal(true) },
                                            onLogout = { viewModel.logout() }
                                        )
                                    }
                                    ScreenDestination.ADMIN_SETTINGS -> {
                                        AdminSettingsScreen(
                                            uiState = uiState,
                                            onSaveSetting = { k, v -> viewModel.saveSetting(k, v) },
                                            onToggleMaintenance = { viewModel.toggleMaintenanceMode() }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.showAuthDialog) {
                        AuthModalDialog(
                            onDismiss = { viewModel.showAuthModal(false) },
                            onLogin = { email, name -> viewModel.login(email, name) }
                        )
                    }
                }
            }
        }
    }
}
