package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.UserStats
import javax.inject.Inject

/**
 * UseCase for getting user statistics
 */
class GetUserStatsUseCase
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
        private val islandRepository: IslandMasteryRepository,
        private val wordRepository: WordRepository,
    ) {
        /**
         * Get user statistics
         * @param userId User ID
         * @return Result containing user stats
         */
        suspend operator fun invoke(userId: String): Result<UserStats> {
            return try {
                // Get all data in parallel
                val totalWords = wordRepository.getWordCount()

                val masteredWordCount = progressRepository.getMasteredWordCount(userId)

                val masteredIslandCount = islandRepository.getMasteredIslandCount(userId)

                val allLevels = progressRepository.getAllLevelProgress(userId)
                val totalLevels = allLevels.size
                val completedLevels = allLevels.count { it.status == com.wordland.domain.model.LevelStatus.COMPLETED }

                val completionRate =
                    if (totalLevels > 0) {
                        completedLevels.toFloat() / totalLevels
                    } else {
                        0f
                    }

                Result.Success(
                    UserStats(
                        totalLevels = totalLevels,
                        completedLevels = completedLevels,
                        masteredIslands = masteredIslandCount,
                        totalWords = totalWords,
                        masteredWords = masteredWordCount,
                        completionRate = completionRate,
                    ),
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
