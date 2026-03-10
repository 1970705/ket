package com.wordland.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wordland.domain.model.statistics.GameHistory
import com.wordland.domain.model.statistics.GameMode

/**
 * Room entity for game history records
 * Tracks each completed game session with detailed statistics
 *
 * Table: game_history
 * Indexes: userId, levelId, gameMode, startTime (DESC), composite (userId, levelId, startTime)
 */
@Entity(
    tableName = "game_history",
    indices = [
        Index(value = ["userId"], name = "index_game_history_userId"),
        Index(value = ["levelId"], name = "index_game_history_levelId"),
        Index(value = ["gameMode"], name = "index_game_history_gameMode"),
        Index(value = ["startTime"], name = "index_game_history_startTime"),
        Index(
            value = ["userId", "levelId", "startTime"],
            name = "index_game_history_user_level_time",
        ),
    ],
)
data class GameHistoryEntity(
    @PrimaryKey
    val gameId: String,
    // Basic info
    val userId: String,
    val levelId: String,
    val islandId: String,
    val gameMode: String, // GameMode enum name
    // Time data
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    // Game data
    val score: Int,
    val stars: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    // Detailed stats
    val maxCombo: Int,
    val hintsUsed: Int,
    val wrongAnswers: Int,
    // Performance data
    val avgResponseTime: Long,
    val fastestAnswer: Long?,
    val slowestAnswer: Long?,
    // Difficulty setting
    val difficulty: String,
    // Metadata
    val createdAt: Long,
)

/**
 * Convert GameHistoryEntity to domain model
 */
fun GameHistoryEntity.toDomainModel(): GameHistory =
    GameHistory(
        gameId = gameId,
        userId = userId,
        levelId = levelId,
        islandId = islandId,
        gameMode = GameMode.valueOf(gameMode),
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        score = score,
        stars = stars,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        accuracy = accuracy,
        maxCombo = maxCombo,
        hintsUsed = hintsUsed,
        wrongAnswers = wrongAnswers,
        avgResponseTime = avgResponseTime,
        fastestAnswer = fastestAnswer,
        slowestAnswer = slowestAnswer,
        difficulty = difficulty,
        createdAt = createdAt,
    )

/**
 * Convert domain model to entity
 */
fun GameHistory.toEntity(): GameHistoryEntity =
    GameHistoryEntity(
        gameId = gameId,
        userId = userId,
        levelId = levelId,
        islandId = islandId,
        gameMode = gameMode.name,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        score = score,
        stars = stars,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        accuracy = accuracy,
        maxCombo = maxCombo,
        hintsUsed = hintsUsed,
        wrongAnswers = wrongAnswers,
        avgResponseTime = avgResponseTime,
        fastestAnswer = fastestAnswer,
        slowestAnswer = slowestAnswer,
        difficulty = difficulty,
        createdAt = createdAt,
    )
