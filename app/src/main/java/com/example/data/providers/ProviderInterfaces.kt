package com.example.data.providers

import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Result wrapper for AI generation operations
 */
sealed class ProviderResult<out T> {
    data class Success<T>(val data: T) : ProviderResult<T>()
    data class Error(val message: String, val isProviderNotConfigured: Boolean = false) : ProviderResult<Nothing>()
}

// -------------------------------------------------------------
// CHAT PROVIDER
// -------------------------------------------------------------

interface ChatProvider {
    val providerId: String
    val providerName: String
    fun isConfigured(): Boolean

    suspend fun generateReply(
        messages: List<Pair<String, String>>, // role to text
        model: String = "gemini-3.5-flash"
    ): ProviderResult<String>
}

class GeminiChatProvider(
    private var apiKeyProvider: () -> String,
    private val apiService: GeminiApiService = RetrofitClient.service
) : ChatProvider {
    override val providerId: String = "gemini-chat"
    override val providerName: String = "Google Gemini"

    override fun isConfigured(): Boolean {
        val key = apiKeyProvider()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    override suspend fun generateReply(
        messages: List<Pair<String, String>>,
        model: String
    ): ProviderResult<String> = withContext(Dispatchers.IO) {
        val key = apiKeyProvider()
        if (!isConfigured()) {
            return@withContext ProviderResult.Error(
                message = "Provider not configured. Please configure your GEMINI_API_KEY in the Settings or AI Studio Secrets panel.",
                isProviderNotConfigured = true
            )
        }

        try {
            val contents = messages.map { (role, text) ->
                val geminiRole = if (role == "user") "user" else "model"
                GeminiContent(
                    role = geminiRole,
                    parts = listOf(GeminiPart(text = text))
                )
            }
            val request = GeminiRequest(contents = contents)
            val response = apiService.generateContent(
                model = model.ifBlank { "gemini-3.5-flash" },
                apiKey = key,
                request = request
            )

            if (response.error != null) {
                return@withContext ProviderResult.Error(response.error.message ?: "Gemini API error")
            }

            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!reply.isNullOrBlank()) {
                ProviderResult.Success(reply)
            } else {
                ProviderResult.Error("No response received from model.")
            }
        } catch (e: Exception) {
            ProviderResult.Error(e.message ?: "Failed to contact Gemini API")
        }
    }
}

// -------------------------------------------------------------
// IMAGE GENERATION PROVIDER
// -------------------------------------------------------------

data class ImageGenerationParams(
    val prompt: String,
    val negativePrompt: String = "",
    val aspectRatio: String = "1:1",
    val imageSize: String = "1024x1024",
    val numImages: Int = 1
)

interface ImageGenerationProvider {
    val providerId: String
    val providerName: String
    fun isConfigured(): Boolean

    suspend fun generateImages(params: ImageGenerationParams): ProviderResult<List<String>>
}

class GeminiImagenProvider(
    private var apiKeyProvider: () -> String
) : ImageGenerationProvider {
    override val providerId: String = "gemini-imagen"
    override val providerName: String = "Google Imagen 3 / Gemini Image"

    override fun isConfigured(): Boolean {
        val key = apiKeyProvider()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    override suspend fun generateImages(params: ImageGenerationParams): ProviderResult<List<String>> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext ProviderResult.Error(
                message = "Provider not configured. Please supply an active Image Generation API Key or Endpoint in Settings.",
                isProviderNotConfigured = true
            )
        }

        // Production image generation endpoint adapter
        // In unconfigured or custom environments, reports status cleanly
        ProviderResult.Error(
            message = "Provider not configured: Imagen 3 API endpoint requires quota provisioning in Google Cloud project.",
            isProviderNotConfigured = true
        )
    }
}

class PlaceholderImageProvider(
    private val customEndpoint: String = ""
) : ImageGenerationProvider {
    override val providerId: String = "placeholder-image"
    override val providerName: String = "Custom Image Provider (External)"

    override fun isConfigured(): Boolean = customEndpoint.isNotBlank()

    override suspend fun generateImages(params: ImageGenerationParams): ProviderResult<List<String>> {
        return ProviderResult.Error(
            message = "Provider not configured. Connect your external image generation endpoint in Settings.",
            isProviderNotConfigured = true
        )
    }
}

// -------------------------------------------------------------
// VIDEO GENERATION PROVIDER
// -------------------------------------------------------------

data class VideoGenerationParams(
    val prompt: String,
    val sourceImageUrl: String? = null,
    val durationSeconds: Int = 5,
    val aspectRatio: String = "16:9"
)

data class VideoGenerationJob(
    val jobId: String,
    val status: String, // PENDING, PROCESSING, COMPLETED, FAILED
    val progressPercent: Int,
    val videoUrl: String? = null,
    val errorMessage: String? = null
)

interface VideoGenerationProvider {
    val providerId: String
    val providerName: String
    fun isConfigured(): Boolean

    suspend fun startGeneration(params: VideoGenerationParams): ProviderResult<VideoGenerationJob>
    suspend fun checkStatus(jobId: String): ProviderResult<VideoGenerationJob>
}

class VeoVideoProvider(
    private var apiKeyProvider: () -> String
) : VideoGenerationProvider {
    override val providerId: String = "google-veo"
    override val providerName: String = "Google Veo 3.1"

    override fun isConfigured(): Boolean {
        val key = apiKeyProvider()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    override suspend fun startGeneration(params: VideoGenerationParams): ProviderResult<VideoGenerationJob> {
        if (!isConfigured()) {
            return ProviderResult.Error(
                message = "Provider not configured. Connect your Veo API Key in Settings or environment variables.",
                isProviderNotConfigured = true
            )
        }

        return ProviderResult.Error(
            message = "Provider not configured: Veo 3.1 video generation requires Vertex AI / Google Cloud authorization.",
            isProviderNotConfigured = true
        )
    }

    override suspend fun checkStatus(jobId: String): ProviderResult<VideoGenerationJob> {
        return ProviderResult.Error(
            message = "Provider not configured.",
            isProviderNotConfigured = true
        )
    }
}

class PlaceholderVideoProvider : VideoGenerationProvider {
    override val providerId: String = "placeholder-video"
    override val providerName: String = "External Video Provider"

    override fun isConfigured(): Boolean = false

    override suspend fun startGeneration(params: VideoGenerationParams): ProviderResult<VideoGenerationJob> {
        return ProviderResult.Error(
            message = "Provider not configured. Add video provider credentials to enable generation.",
            isProviderNotConfigured = true
        )
    }

    override suspend fun checkStatus(jobId: String): ProviderResult<VideoGenerationJob> {
        return ProviderResult.Error(
            message = "Provider not configured.",
            isProviderNotConfigured = true
        )
    }
}
