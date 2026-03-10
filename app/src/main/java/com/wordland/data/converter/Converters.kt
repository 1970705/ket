package com.wordland.data.converter

import androidx.room.TypeConverter
import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.LevelStatus

/**
 * Type converters for Room database
 * Handles complex type conversions for database storage
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        return value?.split(",")?.mapNotNull { it.toLongOrNull() }
    }

    @TypeConverter
    fun fromLevelStatus(value: LevelStatus?): String {
        return value?.name ?: "LOCKED"
    }

    @TypeConverter
    fun toLevelStatus(value: String?): LevelStatus {
        return try {
            LevelStatus.valueOf(value ?: "LOCKED")
        } catch (e: IllegalArgumentException) {
            LevelStatus.LOCKED
        }
    }

    @TypeConverter
    fun fromLearningStatus(value: LearningStatus?): String {
        return value?.name ?: "NEW"
    }

    @TypeConverter
    fun toLearningStatus(value: String?): LearningStatus {
        return try {
            LearningStatus.valueOf(value ?: "NEW")
        } catch (e: IllegalArgumentException) {
            LearningStatus.NEW
        }
    }
}
