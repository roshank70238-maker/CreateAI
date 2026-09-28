package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "user_default",
    val email: String = "creator@createai.studio",
    val name: String = "Creative Studio",
    val avatarUrl: String = "",
    val plan: String = "Free Plan",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val model: String = "gemini-3.5-flash",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-3.5-flash",
    val isError: Boolean = false
)

@Entity(tableName = "image_generations")
data class ImageGenerationEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val negativePrompt: String = "",
    val aspectRatio: String = "1:1",
    val imageSize: String = "1024x1024",
    val numImages: Int = 1,
    val imageUrlsJson: String, // JSON array string or comma separated
    val providerName: String = "Gemini Imagen / Custom Provider",
    val status: String = "COMPLETED", // PENDING, COMPLETED, FAILED
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_generations")
data class VideoGenerationEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val sourceImageUrl: String? = null,
    val durationSeconds: Int = 5,
    val aspectRatio: String = "16:9",
    val videoUrl: String? = null,
    val previewThumbnailUrl: String? = null,
    val providerName: String = "Veo / Luma Provider",
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, FAILED, UNCONFIGURED
    val progressPercent: Int = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "usage_records")
data class UsageRecordEntity(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val chatCount: Int = 0,
    val imageCount: Int = 0,
    val videoCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
