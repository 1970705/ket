package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.GlobalStatistics
import com.wordland.domain.model.statistics.UserLevel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for getting user's global statistics
 *
 * Provides both suspend and Flow-based access to global statistics,
 * including total games, scores, streaks, and mastery progress.
 */
class GetGlobalStatisticsUseCase
    @Inject
    constructor(
        private val statisticsRepository: StatisticsRepository,
    ) {
        /**
         * Get global statistics (suspend function for one-time fetch)
         *
         * @param userId User identifier
         * @return Result containing global statistics
         */
        suspend operator fun invoke(userId: String): Result<GlobalStatistics> {
            return statisticsRepository.getGlobalStatistics(userId)
        }

        /**
         * Get global statistics as Flow for reactive updates
         *
         * @param userId User identifier
         * @return Flow of global statistics (null if not exists)
         */
        fun getFlow(userId: String): Flow<GlobalStatistics?> {
            return statisticsRepository.getGlobalStatisticsFlow(userId)
        }

        /**
         * Get current streak for a user
         *
         * @param userId User identifier
         * @return Current streak days, or 0 if no data
         */
        suspend fun getCurrentStreak(userId: String): Int {
            return when (val result = invoke(userId)) {
                is Result.Success -> result.data.currentStreak
                is Result.Error -> 0
                is Result.Loading -> 0
            }
        }

        /**
         * Get longest streak for a user
         *
         * @param userId User identifier
         * @return Longest streak days, or 0 if no data
         */
        suspend fun getLongestStreak(userId: String): Int {
            return when (val result = invoke(userId)) {
                is Result.Success -> result.data.longestStreak
                is Result.Error -> 0
                is Result.Loading -> 0
            }
        }

        /**
         * Get total games played
         *
         * @param userId User identifier
         * @return Total games, or 0 if no data
         */
        suspend fun getTotalGames(userId: String): Int {
            return when (val result = invoke(userId)) {
                is Result.Success -> result.data.totalGames
                is Result.Error -> 0
                is Result.Loading -> 0
            }
        }

        /**
         * Calculate average score per game
         *
         * @param userId User identifier
         * @return Average score, or 0f if no games played
         */
        suspend fun getAverageScore(userId: String): Float {
            return when (val result = invoke(userId)) {
                is Result.Success -> {
                    val stats = result.data
                    if (stats.totalGames > 0) {
                        stats.totalScore.toFloat() / stats.totalGames.toFloat()
                    } else {
                        0f
                    }
                }
                is Result.Error -> 0f
                is Result.Loading -> 0f
            }
        }

        /**
         * Get total study time in hours
         *
         * @param userId User identifier
         * @return Study time in hours
         */
        suspend fun getStudyTimeHours(userId: String): Float {
            return when (val result = invoke(userId)) {
                is Result.Success -> {
                    val stats = result.data
                    stats.totalStudyTime / (1000f * 60f * 60f) // Convert ms to hours
                }
                is Result.Error -> 0f
                is Result.Loading -> 0f
            }
        }

        /**
         * Check if user is on a streak (studied today)
         *
         * @param userId User identifier
         * @return true if user studied today or yesterday
         */
        suspend fun isOnStreak(userId: String): Boolean {
            return when (val result = invoke(userId)) {
                is Result.Success -> {
                    val stats = result.data
                    stats.currentStreak > 0
                }
                is Result.Error -> false
                is Result.Loading -> false
            }
        }

        /**
         * Get user level based on total mastered words
         *
         * @param userId User identifier
         * @return UserLevel enum value
         */
        suspend fun getUserLevel(userId: String): UserLevel {
            return when (val result = invoke(userId)) {
                is Result.Success -> result.data.userLevel
                is Result.Error -> UserLevel.NEWCOMER
                is Result.Loading -> UserLevel.NEWCOMER
            }
        }

        /**
         * Get progress to next level (0.0 to 1.0)
         *
         * @param userId User identifier
         * @return Progress percentage, or null if at max level
         */
        suspend fun getProgressToNextLevel(userId: String): Float? {
            return when (val result = invoke(userId)) {
                is Result.Success -> {
                    val stats = result.data
                    val currentLevel = stats.userLevel
                    val nextLevel =
                        when (currentLevel) {
                            UserLevel.NEWCOMER -> 5
                            UserLevel.BEGINNER -> 20
                            UserLevel.INTERMEDIATE -> 50
                            UserLevel.ADVANCED -> 100
                            UserLevel.EXPERT -> null // Max level
                        }

                    if (nextLevel != null) {
                        val prevLevel =
                            when (currentLevel) {
                                UserLevel.NEWCOMER -> 0
                                UserLevel.BEGINNER -> 5
                                UserLevel.INTERMEDIATE -> 20
                                UserLevel.ADVANCED -> 50
                                UserLevel.EXPERT -> 100
                            }
                        val range = (nextLevel - prevLevel).coerceAtLeast(1)
                        val progress = stats.totalWordsMastered - prevLevel
                        progress.toFloat() / range.toFloat()
                    } else {
                        null // At max level
                    }
                }
                is Result.Error -> 0f
                is Result.Loading -> 0f
            }
        }
    }
