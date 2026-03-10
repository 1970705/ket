package com.wordland.domain.repository

import com.wordland.domain.model.OnboardingState
import kotlinx.coroutines.flow.Flow

/**
 * Onboarding Repository 接口
 *
 * 定义 Onboarding 数据操作的抽象接口
 */
interface OnboardingRepository {
    /**
     * 获取 Onboarding 状态流
     *
     * @return Flow<OnboardingState?> 可能为空的 Onboarding 状态流
     */
    fun getOnboardingStateFlow(): Flow<OnboardingState?>

    /**
     * 获取 Onboarding 状态（同步）
     *
     * @return OnboardingState? 可能为空的 Onboarding 状态
     */
    suspend fun getOnboardingState(): OnboardingState?

    /**
     * 保存 Onboarding 状态
     *
     * @param state 要保存的状态
     */
    suspend fun saveOnboardingState(state: OnboardingState)

    /**
     * 更新 Onboarding 状态
     *
     * @param state 要更新的状态
     */
    suspend fun updateOnboardingState(state: OnboardingState)

    /**
     * 删除 Onboarding 状态
     *
     * @param userId 用户ID
     */
    suspend fun deleteOnboardingState(userId: String)

    /**
     * 检查用户是否已完成 Onboarding
     *
     * @param userId 用户ID
     * @return Boolean 是否已完成
     */
    suspend fun isOnboardingCompleted(userId: String): Boolean
}
