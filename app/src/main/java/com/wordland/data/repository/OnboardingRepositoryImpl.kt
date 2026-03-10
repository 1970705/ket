package com.wordland.data.repository

import com.wordland.data.dao.OnboardingStateDao
import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Onboarding Repository 实现
 *
 * @property dao OnboardingStateDao
 * @property userId 用户ID（默认为 "user_001"）
 */
class OnboardingRepositoryImpl(
    private val dao: OnboardingStateDao,
    private val userId: String = "user_001",
) : OnboardingRepository {
    override fun getOnboardingStateFlow(): Flow<OnboardingState?> {
        return dao.getByUserId(userId)
    }

    override suspend fun getOnboardingState(): OnboardingState? {
        return dao.getByUserIdSync(userId)
    }

    override suspend fun saveOnboardingState(state: OnboardingState) {
        dao.insert(state)
    }

    override suspend fun updateOnboardingState(state: OnboardingState) {
        dao.update(state)
    }

    override suspend fun deleteOnboardingState(userId: String) {
        dao.delete(userId)
    }

    override suspend fun isOnboardingCompleted(userId: String): Boolean {
        val state = dao.getByUserIdSync(userId)
        return state?.currentPhase == OnboardingPhase.COMPLETED
    }

    /**
     * 创建初始 Onboarding 状态
     */
    suspend fun createInitialState(): OnboardingState {
        val state =
            OnboardingState(
                userId = userId,
                currentPhase = OnboardingPhase.WELCOME,
                selectedPet = null,
                completedTutorialWords = 0,
                lastOpenedChest = 0L,
                totalStars = 0,
            )
        saveOnboardingState(state)
        return state
    }
}
