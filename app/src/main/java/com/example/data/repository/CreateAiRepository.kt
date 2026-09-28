package com.example.data.repository

import com.example.BuildConfig
import com.example.data.db.*
import com.example.data.providers.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateAiRepository(
    private val database: CreateAiDatabase
) {
    private val userDao = database.userDao()
    private val conversationDao = database.conversationDao()
    private val chatMessageDao = database.chatMessageDao()
    private val imageDao = database.imageGenerationDao()
    private val videoDao = database.videoGenerationDao()
    private val usageDao = database.usageDao()
    private val settingsDao = database.settingsDao()

    // Key providers
    private var customApiKey: String? = null

    private fun getEffectiveApiKey(): String {
        return customApiKey?.takeIf { it.isNotBlank() }
            ?: runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("")
    }

    var chatProvider: ChatProvider = GeminiChatProvider(::getEffectiveApiKey)
    var imageProvider: ImageGenerationProvider = GeminiImagenProvider(::getEffectiveApiKey)
    var videoProvider: VideoGenerationProvider = VeoVideoProvider(::getEffectiveApiKey)

    fun updateCustomApiKey(key: String) {
        customApiKey = key
    }

    // -------------------------------------------------------------
    // USAGE LIMITS & COST CONTROL
    // -------------------------------------------------------------

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    suspend fun getTodayUsage(): UsageRecordEntity {
        val today = getTodayDateString()
        val existing = usageDao.getUsageForDate(today)
        if (existing != null) return existing
        val newRecord = UsageRecordEntity(dateString = today)
        usageDao.insertUsage(newRecord)
        return newRecord
    }

    fun observeTodayUsage(): Flow<UsageRecordEntity?> {
        return usageDao.observeUsageForDate(getTodayDateString())
    }

    suspend fun getLimit(key: String, defaultVal: Int): Int {
        val str = settingsDao.getSetting(key)
        return str?.toIntOrNull() ?: defaultVal
    }

    suspend fun checkCanUse(type: String): Pair<Boolean, String> {
        val usage = getTodayUsage()
        return when (type) {
            "chat" -> {
                val limit = getLimit("daily_chat_limit", 50)
                if (usage.chatCount >= limit) {
                    false to "Daily chat limit reached ($limit/day on Free Plan). Increase limit in Admin Settings."
                } else true to ""
            }
            "image" -> {
                val limit = getLimit("daily_image_limit", 5)
                if (usage.imageCount >= limit) {
                    false to "Daily image limit reached ($limit/day on Free Plan). Increase limit in Admin Settings."
                } else true to ""
            }
            "video" -> {
                val limit = getLimit("daily_video_limit", 2)
                if (usage.videoCount >= limit) {
                    false to "Daily video limit reached ($limit/day on Free Plan). Increase limit in Admin Settings."
                } else true to ""
            }
            else -> true to ""
        }
    }

    suspend fun incrementUsage(type: String) {
        val usage = getTodayUsage()
        val updated = when (type) {
            "chat" -> usage.copy(chatCount = usage.chatCount + 1, lastUpdated = System.currentTimeMillis())
            "image" -> usage.copy(imageCount = usage.imageCount + 1, lastUpdated = System.currentTimeMillis())
            "video" -> usage.copy(videoCount = usage.videoCount + 1, lastUpdated = System.currentTimeMillis())
            else -> usage
        }
        usageDao.insertUsage(updated)
    }

    // -------------------------------------------------------------
    // USER
    // -------------------------------------------------------------

    fun observeUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun saveUser(user: UserEntity) = userDao.insertUser(user)

    // -------------------------------------------------------------
    // CHAT & CONVERSATIONS
    // -------------------------------------------------------------

    fun observeConversations(): Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    fun observeMessages(conversationId: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesForConversation(conversationId)

    suspend fun createConversation(title: String, model: String = "gemini-3.5-flash"): ConversationEntity {
        val convo = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "New Conversation" },
            model = model
        )
        conversationDao.insertConversation(convo)
        return convo
    }

    suspend fun sendChatMessage(
        conversationId: String,
        userMessageText: String,
        model: String = "gemini-3.5-flash"
    ): Result<String> {
        val (canUse, reason) = checkCanUse("chat")
        if (!canUse) {
            return Result.failure(Exception(reason))
        }

        // Save user message
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                conversationId = conversationId,
                role = "user",
                content = userMessageText,
                modelUsed = model
            )
        )
        conversationDao.touchConversation(conversationId)

        // Fetch conversation history
        val messages = chatMessageDao.getMessagesForConversation(conversationId).firstOrNull() ?: emptyList()
        val history = messages.map { it.role to it.content }

        // Call provider
        val result = chatProvider.generateReply(history, model)
        return when (result) {
            is ProviderResult.Success -> {
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = conversationId,
                        role = "assistant",
                        content = result.data,
                        modelUsed = model
                    )
                )
                conversationDao.touchConversation(conversationId)
                incrementUsage("chat")
                Result.success(result.data)
            }
            is ProviderResult.Error -> {
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = conversationId,
                        role = "assistant",
                        content = "⚠️ ${result.message}",
                        modelUsed = model,
                        isError = true
                    )
                )
                Result.failure(Exception(result.message))
            }
        }
    }

    suspend fun clearMessages(conversationId: String) {
        chatMessageDao.deleteMessagesForConversation(conversationId)
    }

    suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteConversation(conversationId)
    }

    // -------------------------------------------------------------
    // IMAGE GENERATIONS
    // -------------------------------------------------------------

    fun observeImageGenerations(): Flow<List<ImageGenerationEntity>> =
        imageDao.getAllImageGenerations()

    suspend fun requestImageGeneration(params: ImageGenerationParams): Result<List<String>> {
        val (canUse, reason) = checkCanUse("image")
        if (!canUse) {
            return Result.failure(Exception(reason))
        }

        val genId = UUID.randomUUID().toString()
        val result = imageProvider.generateImages(params)

        when (result) {
            is ProviderResult.Success -> {
                val entity = ImageGenerationEntity(
                    id = genId,
                    prompt = params.prompt,
                    negativePrompt = params.negativePrompt,
                    aspectRatio = params.aspectRatio,
                    imageSize = params.imageSize,
                    numImages = params.numImages,
                    imageUrlsJson = result.data.joinToString(","),
                    providerName = imageProvider.providerName,
                    status = "COMPLETED"
                )
                imageDao.insertImageGeneration(entity)
                incrementUsage("image")
                return Result.success(result.data)
            }
            is ProviderResult.Error -> {
                val entity = ImageGenerationEntity(
                    id = genId,
                    prompt = params.prompt,
                    negativePrompt = params.negativePrompt,
                    aspectRatio = params.aspectRatio,
                    imageSize = params.imageSize,
                    numImages = params.numImages,
                    imageUrlsJson = "",
                    providerName = imageProvider.providerName,
                    status = if (result.isProviderNotConfigured) "UNCONFIGURED" else "FAILED",
                    errorMessage = result.message
                )
                imageDao.insertImageGeneration(entity)
                return Result.failure(Exception(result.message))
            }
        }
    }

    suspend fun deleteImageGeneration(id: String) {
        imageDao.deleteImageGeneration(id)
    }

    // -------------------------------------------------------------
    // VIDEO GENERATIONS
    // -------------------------------------------------------------

    fun observeVideoGenerations(): Flow<List<VideoGenerationEntity>> =
        videoDao.getAllVideoGenerations()

    suspend fun requestVideoGeneration(params: VideoGenerationParams): Result<VideoGenerationEntity> {
        val (canUse, reason) = checkCanUse("video")
        if (!canUse) {
            return Result.failure(Exception(reason))
        }

        val genId = UUID.randomUUID().toString()
        val result = videoProvider.startGeneration(params)

        when (result) {
            is ProviderResult.Success -> {
                val entity = VideoGenerationEntity(
                    id = genId,
                    prompt = params.prompt,
                    sourceImageUrl = params.sourceImageUrl,
                    durationSeconds = params.durationSeconds,
                    aspectRatio = params.aspectRatio,
                    videoUrl = result.data.videoUrl,
                    providerName = videoProvider.providerName,
                    status = result.data.status,
                    progressPercent = result.data.progressPercent
                )
                videoDao.insertVideoGeneration(entity)
                incrementUsage("video")
                return Result.success(entity)
            }
            is ProviderResult.Error -> {
                val entity = VideoGenerationEntity(
                    id = genId,
                    prompt = params.prompt,
                    sourceImageUrl = params.sourceImageUrl,
                    durationSeconds = params.durationSeconds,
                    aspectRatio = params.aspectRatio,
                    providerName = videoProvider.providerName,
                    status = if (result.isProviderNotConfigured) "UNCONFIGURED" else "FAILED",
                    errorMessage = result.message
                )
                videoDao.insertVideoGeneration(entity)
                return Result.failure(Exception(result.message))
            }
        }
    }

    suspend fun deleteVideoGeneration(id: String) {
        videoDao.deleteVideoGeneration(id)
    }

    // -------------------------------------------------------------
    // SETTINGS
    // -------------------------------------------------------------

    fun observeSettings(): Flow<List<AppSettingEntity>> = settingsDao.getAllSettings()

    suspend fun getSetting(key: String, defaultValue: String = ""): String {
        return settingsDao.getSetting(key) ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        settingsDao.setSetting(AppSettingEntity(key, value))
        if (key == "gemini_api_key") {
            updateCustomApiKey(value)
        }
    }

    suspend fun initializeDefaultsIfNeeded() {
        val user = userDao.getUser().firstOrNull()
        if (user == null) {
            userDao.insertUser(UserEntity())
        }
        if (settingsDao.getSetting("daily_image_limit") == null) {
            settingsDao.setSetting(AppSettingEntity("daily_image_limit", "5"))
        }
        if (settingsDao.getSetting("daily_video_limit") == null) {
            settingsDao.setSetting(AppSettingEntity("daily_video_limit", "2"))
        }
        if (settingsDao.getSetting("daily_chat_limit") == null) {
            settingsDao.setSetting(AppSettingEntity("daily_chat_limit", "50"))
        }
        if (settingsDao.getSetting("model_text") == null) {
            settingsDao.setSetting(AppSettingEntity("model_text", "gemini-3.5-flash"))
        }
    }
}
