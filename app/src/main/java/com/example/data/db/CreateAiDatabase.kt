package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        ChatMessageEntity::class,
        ImageGenerationEntity::class,
        VideoGenerationEntity::class,
        UsageRecordEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CreateAiDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun imageGenerationDao(): ImageGenerationDao
    abstract fun videoGenerationDao(): VideoGenerationDao
    abstract fun usageDao(): UsageDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: CreateAiDatabase? = null

        fun getInstance(context: Context): CreateAiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CreateAiDatabase::class.java,
                    "createai_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
