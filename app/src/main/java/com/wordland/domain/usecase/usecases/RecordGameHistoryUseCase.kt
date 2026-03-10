package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.GameHistoryRepository
import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.GameHistory
import com.wordland.domain.model.statistics.GameMode
import java.util.UUID
import javax.inject.Inject

/**
 * UseCase for recording completed game history
 *
 * This UseCase handles the complete workflow of recording a game:
 * 1. Creates and saves a GameHistory record
 * 2. Updates the global statistics
 * 3. Updates the level statistics (if applicable)
 *
 * This ensures all statistics remain synchronized after each game.
 */
class RecordGameHistoryUseCase
    @Inject
    constructor(
        private val gameHistoryRepository: GameHistoryRepository,
        private val statisticsRepository: StatisticsRepository,
    ) {
        /**
         * Record a completed game (full version with all parameters)
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @param islandId Island identifier
         * @param gameMode The game mode played
         * @param startTime Game start timestamp
         * @param endTime Game end timestamp
         * @param score Final score
         * @param stars Number of stars earned (0-3)
         * @param correctAnswers Number of correct answers
         * @param totalQuestions Total number of questions
         * @param maxCombo Maximum combo achieved
         * @param hintsUsed Number of hints used
         * @param wrongAnswers Number of wrong answers
         * @param avgResponseTime Average response time
         * @param fastestAnswer Fastest answer time
         * @param slowestAnswer Slowest answer time
         * @param difficulty Difficulty setting
         * @return Result containing the created game history ID
         */
        suspend operator fun invoke(
            userId: String,
            levelId: String,
            islandId: String,
            gameMode: GameMode,
            startTime: Long,
            endTime: Long,
            score: Int,
            stars: Int,
            correctAnswers: Int,
            totalQuestions: Int,
            maxCombo: Int,
            hintsUsed: Int = 0,
            wrongAnswers: Int = 0,
            avgResponseTime: Long = 0,
            fastestAnswer: Long? = null,
            slowestAnswer: Long? = null,
            difficulty: String = "normal",
        ): Result<String> {
            return try {
                // Calculate duration
                val duration = endTime - startTime

                // Calculate accuracy
                val accuracy =
                    if (totalQuestions > 0) {
                        correctAnswers.toFloat() / totalQuestions.toFloat()
                    } else {
                        0f
                    }

                // Create game history record
                val gameHistory =
                    GameHistory(
                        gameId = UUID.randomUUID().toString(),
                        userId = userId,
                        levelId = levelId,
                        islandId = islandId,
                        gameMode = gameMode,
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
                        createdAt = System.currentTimeMillis(),
                    )

                // Insert game history
                val insertResult = gameHistoryRepository.insertGameHistory(gameHistory)
                if (insertResult is Result.Error) {
                    return insertResult
                }

                // Update global statistics
                val gamePerfect = stars >= 3
                val globalStatsResult =
                    statisticsRepository.updateGlobalStatistics(
                        userId = userId,
                        gameScore = score,
                        gamePerfect = gamePerfect,
                        studyTime = duration,
                    )

                if (globalStatsResult is Result.Error) {
                    return globalStatsResult
                }

                // Update level statistics
                val levelStatsResult =
                    statisticsRepository.updateLevelStatistics(
                        userId = userId,
                        levelId = levelId,
                        gameHistory = gameHistory,
                    )

                if (levelStatsResult is Result.Error) {
                    return levelStatsResult
                }

                Result.Success(gameHistory.gameId)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        /**
         * Record a completed game (simplified version for level-based games)
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @param islandId Island identifier
         * @param gameMode The game mode played
         * @param duration Game duration in milliseconds
         * @param score Final score
         * @param stars Number of stars earned (0-3)
         * @param correctAnswers Number of correct answers
         * @param totalQuestions Total number of questions
         * @param maxCombo Maximum combo achieved
         * @param hintsUsed Number of hints used
         * @param wrongAnswers Number of wrong answers
         * @param avgResponseTime Average response time
         * @param fastestAnswer Fastest answer time
         * @param slowestAnswer Slowest answer time
         * @param difficulty Difficulty setting
         * @return Result containing the created game history ID
         */
        suspend fun recordLevelGame(
            userId: String,
            levelId: String,
            islandId: String,
            gameMode: GameMode,
            duration: Long,
            score: Int,
            stars: Int,
            correctAnswers: Int,
            totalQuestions: Int,
            maxCombo: Int,
            hintsUsed: Int = 0,
            wrongAnswers: Int = 0,
            avgResponseTime: Long = 0,
            fastestAnswer: Long? = null,
            slowestAnswer: Long? = null,
            difficulty: String = "normal",
        ): Result<String> {
            val now = System.currentTimeMillis()
            return invoke(
                userId = userId,
                levelId = levelId,
                islandId = islandId,
                gameMode = gameMode,
                startTime = now - duration,
                endTime = now,
                score = score,
                stars = stars,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                maxCombo = maxCombo,
                hintsUsed = hintsUsed,
                wrongAnswers = wrongAnswers,
                avgResponseTime = avgResponseTime,
                fastestAnswer = fastestAnswer,
                slowestAnswer = slowestAnswer,
                difficulty = difficulty,
            )
        }
    }
