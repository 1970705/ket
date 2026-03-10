package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.domain.model.IslandWithProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UseCase for getting all islands with user progress
 */
class GetIslandsUseCase
    @Inject
    constructor(
        private val islandRepository: IslandMasteryRepository,
    ) {
        /**
         * Get all islands with mastery progress
         * @param userId The user ID
         * @return Flow of islands with progress information
         */
        operator fun invoke(userId: String): Flow<List<IslandWithProgress>> =
            flow {
                val masteryList = islandRepository.getAllIslandMastery(userId)
                emit(
                    masteryList.map { mastery ->
                        IslandWithProgress(
                            islandId = mastery.islandId,
                            islandName = extractIslandName(mastery.islandId),
                            masteryPercentage = mastery.masteryPercentage,
                            isUnlocked = mastery.unlockedAt != null,
                            totalWords = mastery.totalWords,
                            masteredWords = mastery.masteredWords,
                        )
                    },
                )
            }

        private fun extractIslandName(islandId: String): String {
            return when (islandId) {
                "look_island" -> "Look Island"
                "move_valley" -> "Move Valley"
                "make_lake" -> "Make Lake"
                "say_mountain" -> "Say Mountain"
                "think_forest" -> "Think Forest"
                "feel_garden" -> "Feel Garden"
                "go_volcano" -> "Go Volcano"
                else -> islandId.replace("_", " ").replaceFirstChar { it.uppercase() }
            }
        }
    }
