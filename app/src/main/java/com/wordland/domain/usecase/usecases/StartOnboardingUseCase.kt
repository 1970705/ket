package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.repository.OnboardingRepository

/**
 * 启动 Onboarding 流程 UseCase
 *
 * 负责初始化或恢复用户的 Onboarding 状态
 *
 * @property repository OnboardingRepository
 */
class StartOnboardingUseCase(
    private val repository: OnboardingRepository,
) {
    /**
     * 执行启动 Onboarding
     *
     * @return OnboardingState 当前 Onboarding 状态
     */
    suspend operator fun invoke(): OnboardingState {
        val existing = repository.getOnboardingState()

        return if (existing == null) {
            // 首次启动，创建初始状态
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                )
            repository.saveOnboardingState(initialState)
            initialState
        } else {
            // 恢复现有状态
            existing
        }
    }

    /**
     * 检查是否已完成 Onboarding
     *
     * @return Boolean 是否已完成
     */
    suspend fun isCompleted(): Boolean {
        val state = repository.getOnboardingState()
        return state?.currentPhase == OnboardingPhase.COMPLETED
    }
}
