package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.LevelStatus
import javax.inject.Inject

/**
 * UseCase for unlocking the next level in an island
 * Called when a level is completed
 */
class UnlockNextLevelUseCase
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
    ) {
        /**
         * Unlock the next level in the island
         * @param userId User ID
         * @param completedLevelId The level that was just completed
         */
        suspend operator fun invoke(
            userId: String,
            completedLevelId: String,
        ) {
            // Extract island ID from level ID (e.g., "look_island_level_01" -> "look_island")
            val islandId = completedLevelId.substringBeforeLast("_level_")

            // Get all levels for this island
            val allLevels = progressRepository.getLevelsByIsland(userId, islandId)

            // Find the completed level's index
            val completedLevelIndex = allLevels.indexOfFirst { it.levelId == completedLevelId }
            if (completedLevelIndex == -1) return

            // Unlock the next level if it exists
            val nextLevelIndex = completedLevelIndex + 1
            if (nextLevelIndex < allLevels.size) {
                val nextLevel = allLevels[nextLevelIndex]

                // Update status to UNLOCKED
                val updatedProgress =
                    nextLevel.copy(
                        status = LevelStatus.UNLOCKED,
                    )

                progressRepository.updateLevelProgress(updatedProgress)
            }
        }
    }
