package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.WorldMapRepository
import com.wordland.domain.model.ExplorationState
import kotlinx.coroutines.flow.Flow

/**
 * Use case for exploring a region on the world map
 * Handles region discovery and tracking
 */
class ExploreRegionUseCase(
    private val worldMapRepository: WorldMapRepository,
) {
    /**
     * Mark a region as explored
     * @return Result indicating success or if already explored
     */
    suspend operator fun invoke(
        userId: String,
        regionId: String,
    ): ExploreRegionResult {
        val currentState = worldMapRepository.getExplorationState(userId)
        val alreadyExplored = currentState?.hasExplored(regionId) == true

        // Mark as explored
        worldMapRepository.markRegionExplored(userId, regionId)

        // Set as current region
        worldMapRepository.setCurrentRegion(userId, regionId)

        return ExploreRegionResult(
            regionId = regionId,
            isNewDiscovery = !alreadyExplored,
            totalDiscoveries = (currentState?.totalDiscoveries ?: 0) + if (!alreadyExplored) 1 else 0,
        )
    }

    /**
     * Get exploration state
     */
    suspend fun getExplorationState(userId: String): ExplorationState? {
        return worldMapRepository.getExplorationState(userId)
    }

    /**
     * Observe exploration state changes
     */
    fun observeExplorationState(userId: String): Flow<ExplorationState?> {
        return worldMapRepository.getExplorationStateFlow(userId)
    }
}

/**
 * Result of exploring a region
 */
data class ExploreRegionResult(
    val regionId: String,
    val isNewDiscovery: Boolean,
    val totalDiscoveries: Int,
)
