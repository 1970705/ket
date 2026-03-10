package com.wordland.data.entity

import androidx.room.Entity
import androidx.room.Index
import com.wordland.domain.model.statistics.LevelStatistics

/**
 * Room entity for aggregated level statistics
 * Tracks per-level performance metrics across all game sessions
 *
 * Table: level_statistics
 * Primary Key: (userId, levelId)
 * Indexes: userId, levelId, islandId (via levelId)
 */
@Entity(
    tableName = "level_statistics",
    primaryKeys = ["userId", "levelId"],
    indices = [
        Index(value = ["userId"], name = "index_level_statistics_userId"),
        Index(value = ["levelId"], name = "index_level_statistics_levelId"),
    ],
)
data class LevelStatisticsEntity(
    val userId: String,
    val levelId: String,
    // Game count
    val totalGames: Int = 0,
    val completedGames: Int = 0,
    val perfectGames: Int = 0, // 3-star games
    // Score stats
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val averageScore: Float = 0f,
    val totalScore: Int = 0,
    // Time stats
    val bestTime: Long? = null, // Best completion time (ms)
    val worstTime: Long? = null, // Worst completion time (ms)
    val averageTime: Long = 0, // Average completion time (ms)
    val totalTime: Long = 0, // Total game time (ms)
    // Performance stats
    val totalCorrect: Int = 0,
    val totalQuestions: Int = 0,
    val overallAccuracy: Float = 0f,
    // Combo record
    val bestCombo: Int = 0,
    // First and recent play
    val firstPlayedAt: Long? = null,
    val lastPlayedAt: Long? = null,
    // Metadata
    val lastUpdatedAt: Long,
)

/**
 * Convert LevelStatisticsEntity to domain model
 */
fun LevelStatisticsEntity.toDomainModel(): LevelStatistics =
    LevelStatistics(
        userId = userId,
        levelId = levelId,
        totalGames = totalGames,
        completedGames = completedGames,
        perfectGames = perfectGames,
        highestScore = highestScore,
        lowestScore = lowestScore,
        averageScore = averageScore,
        totalScore = totalScore,
        bestTime = bestTime,
        worstTime = worstTime,
        averageTime = averageTime,
        totalTime = totalTime,
        totalCorrect = totalCorrect,
        totalQuestions = totalQuestions,
        overallAccuracy = overallAccuracy,
        bestCombo = bestCombo,
        firstPlayedAt = firstPlayedAt,
        lastPlayedAt = lastPlayedAt,
        lastUpdatedAt = lastUpdatedAt,
    )

/**
 * Convert domain model to entity
 */
fun LevelStatistics.toEntity(): LevelStatisticsEntity =
    LevelStatisticsEntity(
        userId = userId,
        levelId = levelId,
        totalGames = totalGames,
        completedGames = completedGames,
        perfectGames = perfectGames,
        highestScore = highestScore,
        lowestScore = lowestScore,
        averageScore = averageScore,
        totalScore = totalScore,
        bestTime = bestTime,
        worstTime = worstTime,
        averageTime = averageTime,
        totalTime = totalTime,
        totalCorrect = totalCorrect,
        totalQuestions = totalQuestions,
        overallAccuracy = overallAccuracy,
        bestCombo = bestCombo,
        firstPlayedAt = firstPlayedAt,
        lastPlayedAt = lastPlayedAt,
        lastUpdatedAt = lastUpdatedAt,
    )
