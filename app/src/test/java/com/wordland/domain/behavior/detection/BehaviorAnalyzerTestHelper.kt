package com.wordland.domain.behavior.detection

import com.wordland.data.repository.TrackingRepository
import com.wordland.domain.behavior.BehaviorAnalyzer
import com.wordland.domain.model.BehaviorTracking
import io.mockk.*
import org.junit.After
import org.junit.Before

/**
 * Base test class for BehaviorAnalyzer tests
 *
 * Provides shared setup, test data factory, and common configuration
 * for all BehaviorAnalyzer test subclasses.
 */
abstract class BehaviorAnalyzerTestHelper {
    protected lateinit var analyzer: BehaviorAnalyzer
    protected lateinit var trackingRepository: TrackingRepository

    @Before
    fun setup() {
        trackingRepository = mockk(relaxed = true)
        analyzer = BehaviorAnalyzer(trackingRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Test Data Factory ==========

    /**
     * Creates a BehaviorTracking with default or specified values
     */
    protected fun createTracking(
        action: String = "answer",
        isCorrect: Boolean? = true,
        responseTime: Long? = 5000,
    ) = BehaviorTracking(
        userId = "user1",
        wordId = "word1",
        sceneId = "level1",
        action = action,
        isCorrect = isCorrect,
        responseTime = responseTime,
        difficulty = 1,
        hintUsed = false,
        isNewWord = false,
        timestamp = System.currentTimeMillis(),
    )
}
