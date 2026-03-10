package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.WorldMapRepository
import com.wordland.domain.model.WorldMapState
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting the world map state
 * Returns the complete world map including regions, fog, and player position
 */
class GetWorldMapStateUseCase(
    private val worldMapRepository: WorldMapRepository,
) {
    /**
     * Get current world map state
     */
    suspend operator fun invoke(userId: String): WorldMapState {
        return worldMapRepository.getWorldMapState(userId)
    }

    /**
     * Observe world map state changes
     */
    fun observe(userId: String): Flow<WorldMapState> {
        return worldMapRepository.getWorldMapStateFlow(userId)
    }
}
