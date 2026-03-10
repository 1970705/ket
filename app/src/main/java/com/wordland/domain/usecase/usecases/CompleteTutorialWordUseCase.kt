package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.repository.OnboardingRepository

/**
 * 完成教学关卡单词 UseCase
 *
 * 负责记录用户完成单词的进度，并在完成5个单词后推进到开宝箱阶段
 *
 * @property repository OnboardingRepository
 */
class CompleteTutorialWordUseCase(
    private val repository: OnboardingRepository,
) {
    companion object {
        const val REQUIRED_WORDS_FOR_CHEST = 5
    }

    /**
     * 完成单词并更新状态
     *
     * @param stars 获得的星星数（0-3）
     * @return OnboardingState 更新后的状态
     * @throws IllegalStateException 如果 Onboarding 未启动
     */
    suspend operator fun invoke(stars: Int): OnboardingState {
        val state =
            repository.getOnboardingState()
                ?: throw IllegalStateException("Onboarding not started")

        // 确保星星数在有效范围内
        val validStars = stars.coerceIn(0, 3)

        val newCount = state.completedTutorialWords + 1
        val newPhase =
            if (newCount >= REQUIRED_WORDS_FOR_CHEST) {
                OnboardingPhase.FIRST_CHEST
            } else {
                state.currentPhase
            }

        val updated =
            state.copy(
                completedTutorialWords = newCount,
                currentPhase = newPhase,
                totalStars = state.totalStars + validStars,
                updatedAt = System.currentTimeMillis(),
            )
        repository.saveOnboardingState(updated)
        return updated
    }

    /**
     * 检查是否已完成所有教学单词
     *
     * @return Boolean 是否已完成
     */
    suspend fun isTutorialComplete(): Boolean {
        val state = repository.getOnboardingState() ?: return false
        return state.completedTutorialWords >= REQUIRED_WORDS_FOR_CHEST
    }

    /**
     * 获取还需完成的单词数
     *
     * @return Int 剩余单词数
     */
    suspend fun getRemainingWords(): Int {
        val state = repository.getOnboardingState() ?: return REQUIRED_WORDS_FOR_CHEST
        return (REQUIRED_WORDS_FOR_CHEST - state.completedTutorialWords).coerceAtLeast(0)
    }
}
