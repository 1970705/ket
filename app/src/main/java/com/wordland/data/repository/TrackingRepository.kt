package com.wordland.data.repository

import com.wordland.data.dao.TrackingDao
import com.wordland.domain.algorithm.GuessingDetector
import com.wordland.domain.algorithm.GuessingDetector.ResponsePattern
import com.wordland.domain.model.BehaviorTracking

/**
 * Repository interface for behavior tracking operations
 */
interface TrackingRepository {
    suspend fun getRecentTracking(
        userId: String,
        limit: Int,
    ): List<BehaviorTracking>

    suspend fun getTrackingByWord(
        userId: String,
        wordId: String,
        limit: Int,
    ): List<BehaviorTracking>

    suspend fun getCrossSceneAttempts(
        userId: String,
        sceneId: String,
    ): List<BehaviorTracking>

    suspend fun getTrackingByTimeRange(
        userId: String,
        startTime: Long,
        endTime: Long,
    ): List<BehaviorTracking>

    suspend fun insertTracking(tracking: BehaviorTracking)

    suspend fun recordAnswer(
        userId: String,
        wordId: String,
        sceneId: String?,
        isCorrect: Boolean,
        responseTime: Long,
        difficulty: Int?,
        hintUsed: Boolean,
        isNewWord: Boolean,
    )

    suspend fun deleteOldTracking(beforeTime: Long): Int

    // === Statistics ===

    suspend fun getCorrectCrossSceneCount(userId: String): Int

    suspend fun getTotalCrossSceneCount(userId: String): Int

    suspend fun getAverageResponseTime(userId: String): Double?

    suspend fun getAverageCorrectResponseTime(userId: String): Double?

    suspend fun getRecentPatterns(
        userId: String,
        limit: Int,
    ): List<GuessingDetector.ResponsePattern>
}

/**
 * Implementation of TrackingRepository using Room database
 */
class TrackingRepositoryImpl(
    private val trackingDao: TrackingDao,
) : TrackingRepository {
    override suspend fun getRecentTracking(
        userId: String,
        limit: Int,
    ): List<BehaviorTracking> {
        return trackingDao.getRecentTracking(userId, limit)
    }

    override suspend fun getTrackingByWord(
        userId: String,
        wordId: String,
        limit: Int,
    ): List<BehaviorTracking> {
        return trackingDao.getTrackingByWord(userId, wordId, limit)
    }

    override suspend fun getCrossSceneAttempts(
        userId: String,
        sceneId: String,
    ): List<BehaviorTracking> {
        return trackingDao.getCrossSceneAttempts(userId, sceneId)
    }

    override suspend fun getTrackingByTimeRange(
        userId: String,
        startTime: Long,
        endTime: Long,
    ): List<BehaviorTracking> {
        return trackingDao.getTrackingByTimeRange(userId, startTime, endTime)
    }

    override suspend fun insertTracking(tracking: BehaviorTracking) {
        trackingDao.insertTracking(tracking)
    }

    override suspend fun recordAnswer(
        userId: String,
        wordId: String,
        sceneId: String?,
        isCorrect: Boolean,
        responseTime: Long,
        difficulty: Int?,
        hintUsed: Boolean,
        isNewWord: Boolean,
    ) {
        val tracking =
            BehaviorTracking(
                userId = userId,
                wordId = wordId,
                sceneId = sceneId,
                action = "answer",
                isCorrect = isCorrect,
                responseTime = responseTime,
                difficulty = difficulty,
                hintUsed = hintUsed,
                isNewWord = isNewWord,
                timestamp = System.currentTimeMillis(),
            )
        trackingDao.insertTracking(tracking)
    }

    override suspend fun deleteOldTracking(beforeTime: Long): Int {
        return trackingDao.deleteOldTracking(beforeTime)
    }

    override suspend fun getCorrectCrossSceneCount(userId: String): Int {
        return trackingDao.getCorrectCrossSceneCount(userId)
    }

    override suspend fun getTotalCrossSceneCount(userId: String): Int {
        return trackingDao.getTotalCrossSceneCount(userId)
    }

    override suspend fun getAverageResponseTime(userId: String): Double? {
        return trackingDao.getAverageResponseTime(userId)
    }

    override suspend fun getAverageCorrectResponseTime(userId: String): Double? {
        return trackingDao.getAverageCorrectResponseTime(userId)
    }

    override suspend fun getRecentPatterns(
        userId: String,
        limit: Int,
    ): List<ResponsePattern> {
        return trackingDao.getRecentTracking(userId, limit)
            .mapNotNull { tracking ->
                // Manually create ResponsePattern from BehaviorTracking
                if (tracking.responseTime == null) return@mapNotNull null
                ResponsePattern(
                    timestamp = tracking.timestamp,
                    responseTime = tracking.responseTime!!,
                    isCorrect = tracking.isCorrect ?: false,
                    hintUsed = tracking.hintUsed ?: false,
                )
            }
    }
}
