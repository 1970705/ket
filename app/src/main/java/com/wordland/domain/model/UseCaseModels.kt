package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * UseCase返回数据模型
 * 这些模型专门用于UseCase层向ViewModel层传递数据
 *
 * All classes are immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */

/**
 * 岛屿及进度信息
 */
@Immutable
data class IslandWithProgress(
    val islandId: String,
    val islandName: String,
    val masteryPercentage: Double,
    val isUnlocked: Boolean,
    val totalWords: Int,
    val masteredWords: Int,
)

/**
 * 关卡及进度信息
 */
@Immutable
data class LevelWithProgress(
    val levelId: String,
    val levelName: String,
    val islandId: String,
    val stars: Int,
    val isCompleted: Boolean,
    val isUnlocked: Boolean,
    val totalWords: Int,
    val masteredWords: Int,
)

/**
 * 用户统计数据
 */
@Immutable
data class UserStats(
    val totalLevels: Int,
    val completedLevels: Int,
    val masteredIslands: Int,
    val totalWords: Int,
    val masteredWords: Int,
    val completionRate: Float,
)

/**
 * 提交答案结果
 */
@Immutable
data class SubmitAnswerResult(
    val word: Word,
    val isCorrect: Boolean,
    val newMemoryStrength: Int,
    val isGuessing: Boolean,
    val timeTaken: Long,
    val hintUsed: Boolean,
    val starsEarned: Int,
    val message: String,
    /**
     * Combo state after this answer submission
     * Contains current combo count, multiplier, and stats
     */
    val comboState: ComboState = ComboState(),
) {
    fun toLearnWordResult(): LearnWordResult {
        return LearnWordResult(
            word = word,
            isCorrect = isCorrect,
            message = message,
            newMemoryStrength = newMemoryStrength,
            timeTaken = timeTaken,
            hintUsed = hintUsed,
        )
    }
}
