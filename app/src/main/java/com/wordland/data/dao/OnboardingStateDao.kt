package com.wordland.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wordland.domain.model.OnboardingState
import kotlinx.coroutines.flow.Flow

/**
 * Onboarding State DAO
 *
 * 提供 Onboarding 状态的数据库操作
 */
@Dao
interface OnboardingStateDao {
    /**
     * 获取指定用户的 Onboarding 状态（Flow）
     */
    @Query("SELECT * FROM onboarding_state WHERE userId = :userId")
    fun getByUserId(userId: String): Flow<OnboardingState?>

    /**
     * 获取指定用户的 Onboarding 状态（同步）
     */
    @Query("SELECT * FROM onboarding_state WHERE userId = :userId")
    suspend fun getByUserIdSync(userId: String): OnboardingState?

    /**
     * 获取所有 Onboarding 状态（用于调试）
     */
    @Query("SELECT * FROM onboarding_state")
    suspend fun getAll(): List<OnboardingState>

    /**
     * 插入或替换 Onboarding 状态
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: OnboardingState)

    /**
     * 更新 Onboarding 状态
     */
    @Update
    suspend fun update(state: OnboardingState)

    /**
     * 删除指定用户的 Onboarding 状态
     */
    @Query("DELETE FROM onboarding_state WHERE userId = :userId")
    suspend fun delete(userId: String)

    /**
     * 删除所有 Onboarding 状态
     */
    @Query("DELETE FROM onboarding_state")
    suspend fun deleteAll()

    /**
     * 检查指定用户是否存在 Onboarding 状态
     */
    @Query("SELECT COUNT(*) > 0 FROM onboarding_state WHERE userId = :userId")
    suspend fun exists(userId: String): Boolean
}
