package com.wordland.data.repository.mastery

import com.wordland.data.dao.IslandMasteryDao
import com.wordland.data.dao.ProgressDao
import com.wordland.data.repository.IslandMasteryRepositoryImpl
import io.mockk.mockk
import io.mockk.unmockkAll

/**
 * Test helper for IslandMasteryRepository tests.
 * Provides common setup and teardown logic.
 */
object IslandMasteryRepositoryTestHelper {
    /**
     * Creates a test repository instance with mocked dependencies.
     */
    fun createRepository(
        islandMasteryDao: IslandMasteryDao = mockk(relaxed = true),
        progressDao: ProgressDao = mockk(relaxed = true),
    ): IslandMasteryRepositoryImpl {
        return IslandMasteryRepositoryImpl(islandMasteryDao, progressDao)
    }

    /**
     * Cleans up mocks after tests.
     */
    fun cleanup() {
        unmockkAll()
    }

    /**
     * Default test user ID.
     */
    const val TEST_USER_ID = "user_001"

    /**
     * Default test island ID.
     */
    const val TEST_ISLAND_ID = "look_island"

    /**
     * Total words for Look Island.
     */
    const val LOOK_ISLAND_TOTAL_WORDS = 30

    /**
     * Total levels for Look Island.
     */
    const val LOOK_ISLAND_TOTAL_LEVELS = 5

    /**
     * Mastery unlock threshold percentage.
     */
    const val MASTERY_UNLOCK_THRESHOLD = 60.0

    /**
     * Mastery word weight (70%).
     */
    const val MASTERY_WORD_WEIGHT = 0.7

    /**
     * Mastery cross-scene weight (30%).
     */
    const val MASTERY_CROSS_SCENE_WEIGHT = 0.3
}
