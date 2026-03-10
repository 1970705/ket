package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.PetType
import com.wordland.domain.repository.OnboardingRepository

/**
 * 选择宠物 UseCase
 *
 * 负责处理用户宠物选择，并推进到教学关卡阶段
 *
 * @property repository OnboardingRepository
 */
class SelectPetUseCase(
    private val repository: OnboardingRepository,
) {
    /**
     * 执行宠物选择
     *
     * @param pet 选择的宠物类型
     * @return OnboardingState 更新后的状态
     * @throws IllegalStateException 如果 Onboarding 未启动
     */
    suspend operator fun invoke(pet: PetType): OnboardingState {
        val state =
            repository.getOnboardingState()
                ?: throw IllegalStateException("Onboarding not started")

        val updated =
            state.copy(
                selectedPet = pet,
                currentPhase = OnboardingPhase.TUTORIAL,
                updatedAt = System.currentTimeMillis(),
            )
        repository.saveOnboardingState(updated)
        return updated
    }

    /**
     * 验证宠物类型是否有效
     *
     * @param pet 宠物类型
     * @return Boolean 是否有效
     */
    fun isValidPet(pet: PetType): Boolean {
        return pet in PetType.entries
    }
}
