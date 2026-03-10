package com.wordland.data.dao

import androidx.room.*
import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import com.wordland.domain.model.UserWordProgress
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user progress tracking
 */
@Dao
interface ProgressDao {
    // === Word Progress ===

    @Query("SELECT * FROM user_word_progress WHERE userId = :userId AND wordId = :wordId")
    suspend fun getWordProgress(
        userId: String,
        wordId: String,
    ): UserWordProgress?

    @Query("SELECT * FROM user_word_progress WHERE userId = :userId AND wordId = :wordId")
    fun getWordProgressFlow(
        userId: String,
        wordId: String,
    ): Flow<UserWordProgress?>

    @Query("SELECT * FROM user_word_progress WHERE userId = :userId ORDER BY nextReviewTime ASC")
    suspend fun getAllWordProgress(userId: String): List<UserWordProgress>

    @Query(
        """
        SELECT * FROM user_word_progress
        WHERE userId = :userId
        AND nextReviewTime IS NOT NULL
        AND nextReviewTime <= :currentTime
        ORDER BY nextReviewTime ASC
        LIMIT :limit
    """,
    )
    suspend fun getWordsDueForReview(
        userId: String,
        currentTime: Long,
        limit: Int = 20,
    ): List<UserWordProgress>

    @Query(
        """
        SELECT * FROM user_word_progress
        WHERE userId = :userId
        AND status = 'LEARNING'
        ORDER BY lastReviewTime DESC
        LIMIT :limit
    """,
    )
    suspend fun getLearningWords(
        userId: String,
        limit: Int = 10,
    ): List<UserWordProgress>

    @Query(
        """
        SELECT * FROM user_word_progress
        WHERE userId = :userId
        AND status = 'MASTERED'
        ORDER BY masteryTime DESC
    """,
    )
    suspend fun getMasteredWords(userId: String): List<UserWordProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordProgress(progress: UserWordProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordProgressList(progressList: List<UserWordProgress>)

    @Update
    suspend fun updateWordProgress(progress: UserWordProgress)

    @Delete
    suspend fun deleteWordProgress(progress: UserWordProgress)

    @Query(
        """
        UPDATE user_word_progress
        SET memoryStrength = :strength,
            lastReviewTime = :practiceTime,
            nextReviewTime = :nextReview,
            totalAttempts = totalAttempts + 1,
            correctAttempts = correctAttempts + :isCorrect,
            updatedAt = :updatedAt
        WHERE userId = :userId AND wordId = :wordId
    """,
    )
    suspend fun updatePracticeResult(
        userId: String,
        wordId: String,
        strength: Int,
        practiceTime: Long,
        nextReview: Long,
        isCorrect: Boolean,
        updatedAt: Long,
    )

    // === Level Progress ===

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND levelId = :levelId")
    suspend fun getLevelProgress(
        userId: String,
        levelId: String,
    ): LevelProgress?

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND levelId = :levelId")
    fun getLevelProgressFlow(
        userId: String,
        levelId: String,
    ): Flow<LevelProgress?>

    @Query("SELECT * FROM level_progress WHERE userId = :userId ORDER BY levelId ASC")
    suspend fun getAllLevelProgress(userId: String): List<LevelProgress>

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND status = 'UNLOCKED' ORDER BY levelId ASC")
    suspend fun getUnlockedLevels(userId: String): List<LevelProgress>

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND islandId = :islandId ORDER BY levelId ASC")
    suspend fun getLevelsByIsland(
        userId: String,
        islandId: String,
    ): List<LevelProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevelProgress(progress: LevelProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevelProgressList(progressList: List<LevelProgress>)

    @Update
    suspend fun updateLevelProgress(progress: LevelProgress)

    @Query(
        """
        UPDATE level_progress
        SET status = :status,
            stars = :stars,
            score = :score,
            totalTime = totalTime + :timeSpent,
            lastPlayTime = :lastPlayTime,
            practiceCount = practiceCount + 1,
            updatedAt = :updatedAt
        WHERE userId = :userId AND levelId = :levelId
    """,
    )
    suspend fun updateLevelAttempt(
        userId: String,
        levelId: String,
        status: LevelStatus,
        stars: Int,
        score: Int,
        timeSpent: Long,
        lastPlayTime: Long,
        updatedAt: Long,
    )

    @Query("SELECT SUM(CASE WHEN status = 'MASTERED' THEN 1 ELSE 0 END) FROM level_progress WHERE userId = :userId")
    suspend fun getMasteredLevelCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM level_progress WHERE userId = :userId AND status = 'LEARNING'")
    suspend fun getLearningLevelCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM user_word_progress WHERE userId = :userId AND status = 'MASTERED'")
    suspend fun getMasteredWordCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM user_word_progress WHERE userId = :userId AND status = 'LEARNING'")
    suspend fun getLearningWordCount(userId: String): Int

    @Query("SELECT AVG(memoryStrength) FROM user_word_progress WHERE userId = :userId")
    suspend fun getAverageMemoryStrength(userId: String): Double?

    @Query("SELECT SUM(totalAttempts) FROM user_word_progress WHERE userId = :userId")
    suspend fun getTotalPracticeCount(userId: String): Int
}
