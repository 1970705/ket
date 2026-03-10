package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Behavior tracking for learning analytics
 * Tracks user actions for guessing detection and cross-scene validation
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
@Entity(tableName = "behavior_tracking")
data class BehaviorTracking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String, // "user_001"
    // Context
    val wordId: String, // Which word was tested
    val sceneId: String?, // Optional: Which scene (level_id)
    // Action
    val action: String, // "answer", "hint_used", "cross_scene_answer"
    // Response data
    val isCorrect: Boolean?, // null if not applicable (e.g., hint_used)
    val responseTime: Long?, // Response time in ms
    // Metadata
    val difficulty: Int?, // Word difficulty (1-5) if applicable
    val hintUsed: Boolean?, // Whether hint was used before answer
    val isNewWord: Boolean?, // Whether this was a new word for user
    // Timestamp
    val timestamp: Long = System.currentTimeMillis(),
)
