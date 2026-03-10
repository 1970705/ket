package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.LevelStatistics
import com.wordland.domain.model.statistics.PerformanceTier
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for getting level-specific statistics
 *
 * Provides access to detailed statistics for individual levels,
 * including performance metrics, completion counts, and practice recommendations.
 */
class GetLevelStatisticsUseCase
    @Inject
    constructor(
        private val statisticsRepository: StatisticsRepository,
    ) {
        /**
         * Get statistics for a specific level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return Result containing level statistics
         */
        suspend operator fun invoke(
            userId: String,
            levelId: String,
        ): Result<LevelStatistics?> {
            return statisticsRepository.getLevelStatisticsSync(userId, levelId)
        }

        /**
         * Get level statistics as Flow for reactive updates
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return Flow of level statistics
         */
        fun getFlow(
            userId: String,
            levelId: String,
        ): Flow<LevelStatistics?> {
            return statisticsRepository.getLevelStatistics(userId, levelId)
        }

        /**
         * Get statistics for all levels in an island
         *
         * @param userId User identifier
         * @param islandId Island ID prefix (e.g., "look_island")
         * @return Flow of level statistics for the island
         */
        fun getByIsland(
            userId: String,
            islandId: String,
        ): Flow<List<LevelStatistics>> {
            return statisticsRepository.getLevelStatisticsByIsland(userId, islandId)
        }

        /**
         * Get count of completed levels
         *
         * @param userId User identifier
         * @return Flow of completed level count
         */
        fun getCompletedCount(userId: String): Flow<Int> {
            return statisticsRepository.getCompletedLevelsCount(userId)
        }

        /**
         * Get count of perfected levels (3 stars achieved)
         *
         * @param userId User identifier
         * @return Flow of perfected level count
         */
        fun getPerfectedCount(userId: String): Flow<Int> {
            return statisticsRepository.getPerfectedLevelsCount(userId)
        }

        /**
         * Get best performing levels
         *
         * @param userId User identifier
         * @param limit Maximum number of levels to return
         * @return Flow of level statistics ordered by accuracy
         */
        fun getBestPerforming(
            userId: String,
            limit: Int = 5,
        ): Flow<List<LevelStatistics>> {
            return statisticsRepository.getBestPerformingLevels(userId, limit)
        }

        /**
         * Get levels that need practice
         *
         * @param userId User identifier
         * @param limit Maximum number of levels to return
         * @return Flow of level statistics ordered by accuracy (ascending)
         */
        fun getLevelsNeedingPractice(
            userId: String,
            limit: Int = 5,
        ): Flow<List<LevelStatistics>> {
            return statisticsRepository.getLevelsNeedingPractice(userId, limit)
        }

        /**
         * Check if a level is mastered
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return true if level is mastered (high accuracy + multiple completions)
         */
        suspend fun isMastered(
            userId: String,
            levelId: String,
        ): Boolean {
            return when (val result = invoke(userId, levelId)) {
                is Result.Success -> result.data?.isMastered ?: false
                is Result.Error -> false
                is Result.Loading -> false
            }
        }

        /**
         * Get performance tier for a level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return PerformanceTier enum value
         */
        suspend fun getPerformanceTier(
            userId: String,
            levelId: String,
        ): PerformanceTier? {
            return when (val result = invoke(userId, levelId)) {
                is Result.Success -> result.data?.performanceTier
                is Result.Error -> null
                is Result.Loading -> null
            }
        }

        /**
         * Get average score for a level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return Average score, or null if no games played
         */
        suspend fun getAverageScore(
            userId: String,
            levelId: String,
        ): Float? {
            return when (val result = invoke(userId, levelId)) {
                is Result.Success -> {
                    val stats = result.data
                    if (stats != null && stats.totalGames > 0) {
                        stats.totalScore.toFloat() / stats.totalGames.toFloat()
                    } else {
                        null
                    }
                }
                is Result.Error -> null
                is Result.Loading -> null
            }
        }

        /**
         * Get best time for a level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return Best completion time in ms, or null if not completed
         */
        suspend fun getBestTime(
            userId: String,
            levelId: String,
        ): Long? {
            return when (val result = invoke(userId, levelId)) {
                is Result.Success -> result.data?.bestTime
                is Result.Error -> null
                is Result.Loading -> null
            }
        }

        /**
         * Get completion rate for a level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return Completion rate (0.0 to 1.0), or null if no games played
         */
        suspend fun getCompletionRate(
            userId: String,
            levelId: String,
        ): Float? {
            return when (val result = invoke(userId, levelId)) {
                is Result.Success -> result.data?.completionRate
                is Result.Error -> null
                is Result.Loading -> null
            }
        }

        /**
         * Get recommendation for a level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return Recommendation message
         */
        suspend fun getRecommendation(
            userId: String,
            levelId: String,
        ): LevelRecommendation {
            return when (val result = invoke(userId, levelId)) {
                is Result.Success -> {
                    val stats = result.data
                    when {
                        stats == null || stats.totalGames == 0 -> LevelRecommendation.START_PLAYING
                        stats.isMastered -> LevelRecommendation.MASTERED
                        stats.performanceTier == PerformanceTier.EXPERT ->
                            LevelRecommendation.KEEP_PRACTICING
                        stats.performanceTier == PerformanceTier.ADVANCED ->
                            LevelRecommendation.GOOD_PROGRESS
                        stats.overallAccuracy < 0.5f -> LevelRecommendation.NEEDS_REVIEW
                        stats.totalGames < 3 -> LevelRecommendation.PLAY_MORE
                        else -> LevelRecommendation.IMPROVING
                    }
                }
                is Result.Error -> LevelRecommendation.START_PLAYING
                is Result.Loading -> LevelRecommendation.START_PLAYING
            }
        }
    }

/**
 * Level practice recommendations
 */
enum class LevelRecommendation {
    /** User hasn't played this level yet */
    START_PLAYING,

    /** User has played a few times, need more data */
    PLAY_MORE,

    /** User is making good progress */
    IMPROVING,

    /** User has good accuracy, keep practicing */
    GOOD_PROGRESS,

    /** User has excellent performance, maintain it */
    KEEP_PRACTICING,

    /** User has low accuracy, needs review */
    NEEDS_REVIEW,

    /** User has mastered the level */
    MASTERED,
}
