package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.repository.OnboardingRepository

/**
 * 完成 Onboarding UseCase
 *
 * 在用户点击"太酷了！"按钮后调用，标记 onboarding 流程完成
 *
 * @property repository OnboardingRepository
 */
class CompleteOnboardingUseCase(
    private val repository: OnboardingRepository,
) {
    /**
     * 标记 onboarding 为完成状态
     *
     * @return OnboardingState 更新后的状态
     * @throws IllegalStateException 如果 Onboarding 未启动
     */
    suspend operator fun invoke(): OnboardingState {
        val state =
            repository.getOnboardingState()
                ?: throw IllegalStateException("Onboarding not started")

        val updated =
            state.copy(
                currentPhase = OnboardingPhase.COMPLETED,
                updatedAt = System.currentTimeMillis(),
            )
        repository.saveOnboardingState(updated)
        return updated
    }
}
