package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUser(id: String = "user_default"): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET updatedAt = :timestamp WHERE id = :id")
    suspend fun touchConversation(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
}

@Dao
interface ImageGenerationDao {
    @Query("SELECT * FROM image_generations ORDER BY createdAt DESC")
    fun getAllImageGenerations(): Flow<List<ImageGenerationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageGeneration(generation: ImageGenerationEntity)

    @Query("DELETE FROM image_generations WHERE id = :id")
    suspend fun deleteImageGeneration(id: String)

    @Query("DELETE FROM image_generations")
    suspend fun deleteAllImageGenerations()
}

@Dao
interface VideoGenerationDao {
    @Query("SELECT * FROM video_generations ORDER BY createdAt DESC")
    fun getAllVideoGenerations(): Flow<List<VideoGenerationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoGeneration(generation: VideoGenerationEntity)

    @Update
    suspend fun updateVideoGeneration(generation: VideoGenerationEntity)

    @Query("DELETE FROM video_generations WHERE id = :id")
    suspend fun deleteVideoGeneration(id: String)

    @Query("DELETE FROM video_generations")
    suspend fun deleteAllVideoGenerations()
}

@Dao
interface UsageDao {
    @Query("SELECT * FROM usage_records WHERE dateString = :dateString LIMIT 1")
    suspend fun getUsageForDate(dateString: String): UsageRecordEntity?

    @Query("SELECT * FROM usage_records WHERE dateString = :dateString LIMIT 1")
    fun observeUsageForDate(dateString: String): Flow<UsageRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(record: UsageRecordEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingEntity>>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
