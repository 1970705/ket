package com.wordland.data.repository

import com.wordland.data.dao.ProgressDao
import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import com.wordland.domain.model.UserWordProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user progress operations
 */
interface ProgressRepository {
    // === Word Progress ===

    suspend fun getWordProgress(
        userId: String,
        wordId: String,
    ): UserWordProgress?

    fun getWordProgressFlow(
        userId: String,
        wordId: String,
    ): Flow<UserWordProgress?>

    suspend fun getAllWordProgress(userId: String): List<UserWordProgress>

    suspend fun getWordsDueForReview(
        userId: String,
        currentTime: Long,
        limit: Int,
    ): List<UserWordProgress>

    suspend fun getLearningWords(
        userId: String,
        limit: Int,
    ): List<UserWordProgress>

    suspend fun getMasteredWords(userId: String): List<UserWordProgress>

    suspend fun insertWordProgress(progress: UserWordProgress)

    suspend fun updateWordProgress(progress: UserWordProgress)

    suspend fun updatePracticeResult(
        userId: String,
        wordId: String,
        isCorrect: Boolean,
        responseTime: Long,
        newMemoryStrength: Int? = null,
    )

    // === Level Progress ===

    suspend fun getLevelProgress(
        userId: String,
        levelId: String,
    ): LevelProgress?

    fun getLevelProgressFlow(
        userId: String,
        levelId: String,
    ): Flow<LevelProgress?>

    suspend fun getAllLevelProgress(userId: String): List<LevelProgress>

    suspend fun getUnlockedLevels(userId: String): List<LevelProgress>

    suspend fun getLevelsByIsland(
        userId: String,
        islandId: String,
    ): List<LevelProgress>

    suspend fun insertLevelProgress(progress: LevelProgress)

    suspend fun updateLevelProgress(progress: LevelProgress)

    suspend fun recordLevelAttempt(
        userId: String,
        levelId: String,
        status: LevelStatus,
        stars: Int,
        score: Int,
        timeSpent: Long,
    )

    // === Statistics ===

    suspend fun getMasteredWordCount(userId: String): Int

    suspend fun getLearningWordCount(userId: String): Int

    suspend fun getAverageMemoryStrength(userId: String): Double?

    suspend fun getTotalPracticeCount(userId: String): Int
}

/**
 * Implementation of ProgressRepository using Room database
 */
class ProgressRepositoryImpl(
    private val progressDao: ProgressDao,
) : ProgressRepository {
    override suspend fun getWordProgress(
        userId: String,
        wordId: String,
    ): UserWordProgress? {
        return progressDao.getWordProgress(userId, wordId)
    }

    override fun getWordProgressFlow(
        userId: String,
        wordId: String,
    ): Flow<UserWordProgress?> {
        return progressDao.getWordProgressFlow(userId, wordId)
    }

    override suspend fun getAllWordProgress(userId: String): List<UserWordProgress> {
        return progressDao.getAllWordProgress(userId)
    }

    override suspend fun getWordsDueForReview(
        userId: String,
        currentTime: Long,
        limit: Int,
    ): List<UserWordProgress> {
        return progressDao.getWordsDueForReview(userId, currentTime, limit)
    }

    override suspend fun getLearningWords(
        userId: String,
        limit: Int,
    ): List<UserWordProgress> {
        return progressDao.getLearningWords(userId, limit)
    }

    override suspend fun getMasteredWords(userId: String): List<UserWordProgress> {
        return progressDao.getMasteredWords(userId)
    }

    override suspend fun insertWordProgress(progress: UserWordProgress) {
        progressDao.insertWordProgress(progress)
    }

    override suspend fun updateWordProgress(progress: UserWordProgress) {
        progressDao.updateWordProgress(progress)
    }

    override suspend fun updatePracticeResult(
        userId: String,
        wordId: String,
        isCorrect: Boolean,
        responseTime: Long,
        newMemoryStrength: Int?,
    ) {
        // Get current progress or create new one
        var current = getWordProgress(userId, wordId)

        // If first time learning this word, create initial progress record
        if (current == null) {
            val now = System.currentTimeMillis()
            val newProgress =
                com.wordland.domain.model.UserWordProgress(
                    wordId = wordId,
                    userId = userId,
                    status = com.wordland.domain.model.LearningStatus.NEW,
                    memoryStrength = com.wordland.domain.constants.DomainConstants.MEMORY_STRENGTH_INITIAL,
                    totalAttempts = 0,
                    correctAttempts = 0,
                    incorrectAttempts = 0,
                    createdAt = now,
                    updatedAt = now,
                )
            insertWordProgress(newProgress)
            current = newProgress
        }

        // Use the provided newMemoryStrength, or calculate it using algorithm
        // This allows the UseCase to pre-calculate using pattern-based guessing detection
        val newStrength =
            if (newMemoryStrength != null) {
                // Use the pre-calculated value from UseCase (pattern-based guessing detection)
                newMemoryStrength
            } else {
                // Fallback: calculate using time-based guessing detection
                // This maintains backward compatibility but is less accurate
                val wordDifficulty = 3
                com.wordland.domain.algorithm.MemoryStrengthAlgorithm.calculateNewStrength(
                    currentStrength = current.memoryStrength,
                    isCorrect = isCorrect,
                    isGuessing =
                        com.wordland.domain.algorithm.MemoryStrengthAlgorithm.detectGuessing(
                            responseTime = responseTime,
                            wordDifficulty = wordDifficulty,
                        ),
                    wordDifficulty = wordDifficulty,
                )
            }

        // Calculate next review time
        val nextReview =
            com.wordland.domain.algorithm.MemoryStrengthAlgorithm.calculateNextReview(
                currentStrength = newStrength,
                lastReviewTime = System.currentTimeMillis(),
            )

        // Update learning status
        val newStatus =
            when {
                newStrength >= 100 -> LearningStatus.MASTERED
                current.status == LearningStatus.NEW && isCorrect -> LearningStatus.LEARNING
                else -> current.status
            }

        val now = System.currentTimeMillis()
        progressDao.updatePracticeResult(
            userId = userId,
            wordId = wordId,
            isCorrect = isCorrect,
            strength = newStrength,
            practiceTime = now,
            nextReview = nextReview,
            updatedAt = now,
        )

        // Debug logging to trace memory strength updates
        android.util.Log.d(
            "ProgressRepository",
            "updatePracticeResult: wordId=$wordId, " +
                "oldStrength=${current.memoryStrength}, newStrength=$newStrength, " +
                "isCorrect=$isCorrect, responseTime=$responseTime, " +
                "newMemoryStrengthParam=$newMemoryStrength",
        )
    }

    override suspend fun getLevelProgress(
        userId: String,
        levelId: String,
    ): LevelProgress? {
        return progressDao.getLevelProgress(userId, levelId)
    }

    override fun getLevelProgressFlow(
        userId: String,
        levelId: String,
    ): Flow<LevelProgress?> {
        return progressDao.getLevelProgressFlow(userId, levelId)
    }

    override suspend fun getAllLevelProgress(userId: String): List<LevelProgress> {
        return progressDao.getAllLevelProgress(userId)
    }

    override suspend fun getUnlockedLevels(userId: String): List<LevelProgress> {
        return progressDao.getUnlockedLevels(userId)
    }

    override suspend fun getLevelsByIsland(
        userId: String,
        islandId: String,
    ): List<LevelProgress> {
        return progressDao.getLevelsByIsland(userId, islandId)
    }

    override suspend fun insertLevelProgress(progress: LevelProgress) {
        progressDao.insertLevelProgress(progress)
    }

    override suspend fun updateLevelProgress(progress: LevelProgress) {
        progressDao.updateLevelProgress(progress)
    }

    override suspend fun recordLevelAttempt(
        userId: String,
        levelId: String,
        status: LevelStatus,
        stars: Int,
        score: Int,
        timeSpent: Long,
    ) {
        val now = System.currentTimeMillis()
        progressDao.updateLevelAttempt(
            userId = userId,
            levelId = levelId,
            status = status,
            stars = stars,
            score = score,
            timeSpent = timeSpent,
            lastPlayTime = now,
            updatedAt = now,
        )
    }

    override suspend fun getMasteredWordCount(userId: String): Int {
        return progressDao.getMasteredWordCount(userId)
    }

    override suspend fun getLearningWordCount(userId: String): Int {
        return progressDao.getLearningWordCount(userId)
    }

    override suspend fun getAverageMemoryStrength(userId: String): Double? {
        return progressDao.getAverageMemoryStrength(userId)
    }

    override suspend fun getTotalPracticeCount(userId: String): Int {
        return progressDao.getTotalPracticeCount(userId)
    }
}
