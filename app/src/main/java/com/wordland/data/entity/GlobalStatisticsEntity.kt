package com.wordland.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wordland.domain.model.statistics.GlobalStatistics

/**
 * Room entity for global learning statistics
 * Tracks overall user progress and performance across all content
 *
 * Table: global_statistics
 * Primary Key: userId
 */
@Entity(
    tableName = "global_statistics",
    indices = [
        Index(value = ["userId"], name = "index_global_statistics_userId"),
    ],
)
data class GlobalStatisticsEntity(
    @PrimaryKey
    val userId: String,
    // Game stats
    val totalGames: Int = 0,
    val totalScore: Int = 0,
    val totalPerfectGames: Int = 0, // Total 3-star games
    // Time stats
    val totalStudyTime: Long = 0, // Total study time (ms)
    val currentStreak: Int = 0, // Current consecutive study days
    val longestStreak: Int = 0, // Longest consecutive study days
    val lastStudyDate: Long? = null, // Last study date timestamp
    // Progress stats
    val totalLevelsCompleted: Int = 0, // Completed levels count
    val totalLevelsPerfected: Int = 0, // Perfected (3-star all) levels count
    val totalWordsMastered: Int = 0, // Mastered words count
    // Learning curve
    val totalCorrectAnswers: Int = 0,
    val totalPracticeSessions: Int = 0, // Total practice sessions
    // Metadata
    val firstUsedAt: Long,
    val lastUpdatedAt: Long,
)

/**
 * Convert GlobalStatisticsEntity to domain model
 */
fun GlobalStatisticsEntity.toDomainModel(): GlobalStatistics =
    GlobalStatistics(
        userId = userId,
        totalGames = totalGames,
        totalScore = totalScore,
        totalPerfectGames = totalPerfectGames,
        totalStudyTime = totalStudyTime,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastStudyDate = lastStudyDate,
        totalLevelsCompleted = totalLevelsCompleted,
        totalLevelsPerfected = totalLevelsPerfected,
        totalWordsMastered = totalWordsMastered,
        totalCorrectAnswers = totalCorrectAnswers,
        totalPracticeSessions = totalPracticeSessions,
        firstUsedAt = firstUsedAt,
        lastUpdatedAt = lastUpdatedAt,
    )

/**
 * Convert domain model to entity
 */
fun GlobalStatistics.toEntity(): GlobalStatisticsEntity =
    GlobalStatisticsEntity(
        userId = userId,
        totalGames = totalGames,
        totalScore = totalScore,
        totalPerfectGames = totalPerfectGames,
        totalStudyTime = totalStudyTime,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastStudyDate = lastStudyDate,
        totalLevelsCompleted = totalLevelsCompleted,
        totalLevelsPerfected = totalLevelsPerfected,
        totalWordsMastered = totalWordsMastered,
        totalCorrectAnswers = totalCorrectAnswers,
        totalPracticeSessions = totalPracticeSessions,
        firstUsedAt = firstUsedAt,
        lastUpdatedAt = lastUpdatedAt,
    )
