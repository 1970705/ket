package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.LevelProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UseCase for getting levels with their progress
 */
class GetLevelsUseCase
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
    ) {
        /**
         * Get levels for an island with user progress
         * @param islandId The island ID
         * @param userId The user ID
         * @return Flow of levels with progress information
         */
        operator fun invoke(
            islandId: String,
            userId: String,
        ): Flow<List<LevelProgress>> =
            flow {
                emit(progressRepository.getLevelsByIsland(userId, islandId))
            }
    }
