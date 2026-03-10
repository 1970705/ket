package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.UserWordProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UseCase for getting user's overall learning progress
 * Returns all word progress for the user
 */
class GetUserProgressUseCase
    @Inject
    constructor(
        private val progressRepository: ProgressRepository,
    ) {
        /**
         * Get all word progress for a user
         * @param userId User ID
         * @return Flow emitting list of user's word progress
         */
        operator fun invoke(userId: String): Flow<List<UserWordProgress>> =
            flow {
                emit(progressRepository.getAllWordProgress(userId))
            }
    }
