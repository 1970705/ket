package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wordland.domain.constants.DomainConstants
import kotlin.math.abs

/**
 * User's progress for a specific word
 * Tracks learning status, memory strength, and review scheduling
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
@Entity(tableName = "user_word_progress")
data class UserWordProgress(
    @PrimaryKey
    val wordId: String, // FK to Word.id
    val userId: String, // "user_001" (MVP: single user)
    // Learning Status
    val status: LearningStatus,
    val memoryStrength: Int = DomainConstants.MEMORY_STRENGTH_INITIAL, // 0-100
    // Review Tracking
    val lastReviewTime: Long? = null,
    val nextReviewTime: Long? = null,
    // Practice Statistics
    val totalAttempts: Int = 0,
    val correctAttempts: Int = 0,
    val incorrectAttempts: Int = 0,
    // Behavior Analysis (Anti-Guessing System)
    val averageResponseTime: Long = 0, // in milliseconds
    val isGuessingDetected: Boolean = false,
    // Cross-Scene Validation
    val sceneExposure: List<String>? = emptyList(), // ["level_01", "level_03"]
    val crossSceneCorrect: Int = 0, // correct attempts in cross-scene tests
    val crossSceneTotal: Int = 0, // total cross-scene attempts
    // Mastery Tracking
    val firstLearnTime: Long? = null,
    val masteryTime: Long? = null,
    // SM-2 Spaced Repetition Algorithm Fields
    val easeFactor: Float = 2.5f, // How easily the user remembers this word (1.3 - 2.7)
    val sm2Interval: Int = 0, // Days until next review (SM-2 algorithm)
    val sm2Repetitions: Int = 0, // Number of successful recalls (SM-2 algorithm)
    val lastQuality: Int? = null, // Last quality score (0-5) from SM-2 algorithm
    val forgettingRate: Float = 0.5f, // Estimated individual forgetting rate (0.0 - 1.0)
    val totalReviews: Int = 0, // Total review count for this word
    val successfulReviews: Int = 0, // Successful review count
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    /**
     * Check if this word is mastered
     * Criteria:
     * 1. Consecutively correct 3+ times
     * 2. Memory strength >= 60%
     * 3. Cross-scene correct rate >= 70%
     */
    fun isMastered(): Boolean {
        val recentCorrect = correctAttempts >= 3
        val strengthMet = memoryStrength >= DomainConstants.MASTERY_PERCENTAGE_COMPLETE
        val crossSceneMet =
            if (crossSceneTotal > 0) {
                (crossSceneCorrect.toDouble() / crossSceneTotal) >= DomainConstants.CROSS_SCENE_CORRECT_RATE_THRESHOLD
            } else {
                true
            }

        return recentCorrect && strengthMet && crossSceneMet
    }

    /**
     * Calculate priority score for review scheduling
     * Higher score = more urgent to review
     *
     * Factors:
     * - Overdue penalty (how many days past due)
     * - Difficulty bonus (harder words = higher priority)
     * - Ease factor penalty (lower EF = higher priority)
     */
    fun calculateReviewPriority(): Float {
        val now = System.currentTimeMillis()
        val nextReview = nextReviewTime ?: return 100f // Never reviewed = highest priority

        val daysUntilDue = ((nextReview - now) / (24 * 60 * 60 * 1000f)).toInt()

        // Urgent if overdue or due today
        val overduePenalty =
            when {
                daysUntilDue < 0 -> abs(daysUntilDue).toFloat() * 10f // Urgent!
                daysUntilDue < 1 -> 5f // Due today
                else -> 0f
            }

        // Harder words (lower EF) get higher priority
        val difficultyBonus = (2.7f - easeFactor) * 5f

        // Words with low success rate get higher priority
        val successRate =
            if (totalReviews > 0) {
                successfulReviews.toFloat() / totalReviews.toFloat()
            } else {
                0.5f
            }
        val successRatePenalty = (1f - successRate) * 10f

        return overduePenalty + difficultyBonus + successRatePenalty
    }

    /**
     * Get estimated days until review is due
     * Returns negative value if overdue
     */
    fun getDaysUntilDue(): Int {
        val now = System.currentTimeMillis()
        val nextReview = nextReviewTime ?: return 0
        return ((nextReview - now) / (24 * 60 * 60 * 1000f)).toInt()
    }

    /**
     * Calculate success rate for this word
     */
    fun getSuccessRate(): Float {
        return if (totalReviews > 0) {
            successfulReviews.toFloat() / totalReviews.toFloat()
        } else {
            0f
        }
    }
}

/**
 * Learning Status enum
 */
enum class LearningStatus {
    NEW, // New word, not yet learned
    LEARNING, // Currently learning
    MASTERED, // Mastered (can be reviewed)
    NEED_REVIEW, // Due for review
}
