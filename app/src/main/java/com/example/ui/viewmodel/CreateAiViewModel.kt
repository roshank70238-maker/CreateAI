package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.providers.ImageGenerationParams
import com.example.data.providers.VideoGenerationParams
import com.example.data.repository.CreateAiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenDestination {
    DASHBOARD,
    CHAT,
    IMAGE,
    VIDEO,
    HISTORY,
    PROFILE,
    ADMIN_SETTINGS
}

data class UiState(
    val currentScreen: ScreenDestination = ScreenDestination.DASHBOARD,
    val isDarkTheme: Boolean = true,
    val user: UserEntity? = null,
    val conversations: List<ConversationEntity> = emptyList(),
    val activeConversationId: String? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val isChatGenerating: Boolean = false,
    val imageGenerations: List<ImageGenerationEntity> = emptyList(),
    val isImageGenerating: Boolean = false,
    val videoGenerations: List<VideoGenerationEntity> = emptyList(),
    val isVideoGenerating: Boolean = false,
    val videoGenProgress: Int = 0,
    val todayUsage: UsageRecordEntity? = null,
    val settingsMap: Map<String, String> = emptyMap(),
    val snackbarMessage: String? = null,
    val isLoggedIn: Boolean = true,
    val showAuthDialog: Boolean = false,
    val maintenanceMode: Boolean = false
)

class CreateAiViewModel(
    private val repository: CreateAiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            loadObservables()
        }
    }

    private fun loadObservables() {
        // Observe User
        viewModelScope.launch {
            repository.observeUser().collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }

        // Observe Conversations
        viewModelScope.launch {
            repository.observeConversations().collect { convos ->
                _uiState.update { state ->
                    val activeId = if (state.activeConversationId == null && convos.isNotEmpty()) {
                        convos.first().id
                    } else {
                        state.activeConversationId
                    }
                    state.copy(conversations = convos, activeConversationId = activeId)
                }
                val currentActive = _uiState.value.activeConversationId
                if (currentActive != null) {
                    observeConversationMessages(currentActive)
                }
            }
        }

        // Observe Image generations
        viewModelScope.launch {
            repository.observeImageGenerations().collect { images ->
                _uiState.update { it.copy(imageGenerations = images) }
            }
        }

        // Observe Video generations
        viewModelScope.launch {
            repository.observeVideoGenerations().collect { videos ->
                _uiState.update { it.copy(videoGenerations = videos) }
            }
        }

        // Observe Daily usage
        viewModelScope.launch {
            repository.observeTodayUsage().collect { usage ->
                _uiState.update { it.copy(todayUsage = usage) }
            }
        }

        // Observe Settings
        viewModelScope.launch {
            repository.observeSettings().collect { settingsList ->
                val map = settingsList.associate { it.key to it.value }
                val isMaint = map["maintenance_mode"] == "true"
                _uiState.update { it.copy(settingsMap = map, maintenanceMode = isMaint) }
            }
        }
    }

    private fun observeConversationMessages(conversationId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.observeMessages(conversationId).collect { msgList ->
                _uiState.update { it.copy(messages = msgList) }
            }
        }
    }

    fun selectScreen(destination: ScreenDestination) {
        _uiState.update { it.copy(currentScreen = destination) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    // -------------------------------------------------------------
    // CHAT ACTIONS
    // -------------------------------------------------------------

    fun selectConversation(id: String) {
        _uiState.update { it.copy(activeConversationId = id) }
        observeConversationMessages(id)
    }

    fun createNewConversation(title: String = "New Chat") {
        viewModelScope.launch {
            val convo = repository.createConversation(title)
            _uiState.update { it.copy(activeConversationId = convo.id) }
            observeConversationMessages(convo.id)
            showSnackbar("Created new conversation")
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_uiState.value.activeConversationId == id) {
                val next = _uiState.value.conversations.firstOrNull { it.id != id }
                _uiState.update { it.copy(activeConversationId = next?.id) }
                if (next != null) {
                    observeConversationMessages(next.id)
                } else {
                    _uiState.update { it.copy(messages = emptyList()) }
                }
            }
            showSnackbar("Conversation deleted")
        }
    }

    fun clearActiveConversation() {
        val activeId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.clearMessages(activeId)
            showSnackbar("Conversation cleared")
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        if (_uiState.value.maintenanceMode) {
            showSnackbar("Service is temporarily in maintenance mode.")
            return
        }

        viewModelScope.launch {
            var activeId = _uiState.value.activeConversationId
            if (activeId == null) {
                val newConvo = repository.createConversation(
                    title = text.take(30).trim() + if (text.length > 30) "..." else ""
                )
                activeId = newConvo.id
                _uiState.update { it.copy(activeConversationId = activeId) }
                observeConversationMessages(activeId)
            }

            _uiState.update { it.copy(isChatGenerating = true) }
            val model = _uiState.value.settingsMap["model_text"] ?: "gemini-3.5-flash"
            val result = repository.sendChatMessage(activeId, text.trim(), model)
            _uiState.update { it.copy(isChatGenerating = false) }

            result.onFailure { error ->
                showSnackbar(error.message ?: "Failed to generate reply")
            }
        }
    }

    // -------------------------------------------------------------
    // IMAGE ACTIONS
    // -------------------------------------------------------------

    fun generateImages(
        prompt: String,
        aspectRatio: String,
        imageSize: String,
        numImages: Int,
        negativePrompt: String = ""
    ) {
        if (prompt.isBlank()) {
            showSnackbar("Please enter a prompt to generate an image.")
            return
        }
        if (_uiState.value.maintenanceMode) {
            showSnackbar("Image generation is temporarily paused for maintenance.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isImageGenerating = true) }
            val params = ImageGenerationParams(
                prompt = prompt.trim(),
                negativePrompt = negativePrompt.trim(),
                aspectRatio = aspectRatio,
                imageSize = imageSize,
                numImages = numImages
            )
            val result = repository.requestImageGeneration(params)
            _uiState.update { it.copy(isImageGenerating = false) }

            result.onSuccess {
                showSnackbar("Image generation complete!")
            }.onFailure { error ->
                showSnackbar("Error: ${error.message}")
            }
        }
    }

    fun deleteImageGeneration(id: String) {
        viewModelScope.launch {
            repository.deleteImageGeneration(id)
            showSnackbar("Image record deleted")
        }
    }

    // -------------------------------------------------------------
    // VIDEO ACTIONS
    // -------------------------------------------------------------

    fun generateVideo(
        prompt: String,
        sourceImageUrl: String? = null,
        durationSeconds: Int = 5,
        aspectRatio: String = "16:9"
    ) {
        if (prompt.isBlank()) {
            showSnackbar("Please enter a video prompt description.")
            return
        }
        if (_uiState.value.maintenanceMode) {
            showSnackbar("Video generation is temporarily paused for maintenance.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVideoGenerating = true, videoGenProgress = 10) }

            val params = VideoGenerationParams(
                prompt = prompt.trim(),
                sourceImageUrl = sourceImageUrl?.takeIf { it.isNotBlank() },
                durationSeconds = durationSeconds,
                aspectRatio = aspectRatio
            )

            // Step progress representation
            for (p in listOf(25, 50, 75)) {
                delay(300)
                _uiState.update { it.copy(videoGenProgress = p) }
            }

            val result = repository.requestVideoGeneration(params)
            _uiState.update { it.copy(isVideoGenerating = false, videoGenProgress = 0) }

            result.onSuccess {
                showSnackbar("Video generation request submitted!")
            }.onFailure { error ->
                showSnackbar(error.message ?: "Video generation failed")
            }
        }
    }

    fun deleteVideoGeneration(id: String) {
        viewModelScope.launch {
            repository.deleteVideoGeneration(id)
            showSnackbar("Video record deleted")
        }
    }

    // -------------------------------------------------------------
    // SETTINGS & ADMIN ACTIONS
    // -------------------------------------------------------------

    fun saveSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
            showSnackbar("Saved setting: $key")
        }
    }

    fun toggleMaintenanceMode() {
        val current = _uiState.value.maintenanceMode
        val next = !current
        saveSetting("maintenance_mode", next.toString())
    }

    // -------------------------------------------------------------
    // AUTH & PROFILE
    // -------------------------------------------------------------

    fun showAuthModal(show: Boolean) {
        _uiState.update { it.copy(showAuthDialog = show) }
    }

    fun login(email: String, name: String) {
        viewModelScope.launch {
            val user = UserEntity(
                email = email.ifBlank { "creator@createai.studio" },
                name = name.ifBlank { "Creative Studio User" },
                plan = "Free Plan"
            )
            repository.saveUser(user)
            _uiState.update { it.copy(isLoggedIn = true, showAuthDialog = false) }
            showSnackbar("Welcome back, ${user.name}!")
        }
    }

    fun logout() {
        _uiState.update { it.copy(isLoggedIn = false) }
        showSnackbar("You have logged out")
    }
}

class CreateAiViewModelFactory(
    private val repository: CreateAiRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateAiViewModel(repository) as T
    }
}
