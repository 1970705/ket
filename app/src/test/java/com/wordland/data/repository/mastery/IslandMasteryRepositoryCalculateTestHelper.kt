package com.wordland.data.repository.mastery

import com.wordland.data.dao.IslandMasteryDao
import com.wordland.data.dao.ProgressDao
import com.wordland.data.repository.IslandMasteryRepositoryImpl
import com.wordland.domain.model.IslandMastery
import io.mockk.*
import org.junit.After
import org.junit.Before

/**
 * Base test class for IslandMasteryRepository calculation tests
 *
 * Provides shared setup, test data, and helper methods
 * for all mastery calculation test subclasses.
 */
abstract class IslandMasteryRepositoryCalculateTestHelper {
    protected lateinit var repository: IslandMasteryRepositoryImpl
    protected lateinit var islandMasteryDao: IslandMasteryDao
    protected lateinit var progressDao: ProgressDao

    protected val userId = "user_001"
    protected val islandId = "look_island"

    @Before
    fun setup() {
        islandMasteryDao = mockk(relaxed = true)
        progressDao = mockk(relaxed = true)
        repository = IslandMasteryRepositoryImpl(islandMasteryDao, progressDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Test Data Builders ==========

    /**
     * Creates a default IslandMastery with zero progress
     */
    protected fun createDefaultMastery() =
        IslandMastery(
            islandId = islandId,
            userId = userId,
            totalWords = 30,
            masteredWords = 0,
            totalLevels = 5,
            completedLevels = 0,
            crossSceneScore = 0.0,
            masteryPercentage = 0.0,
            isNextIslandUnlocked = false,
        )

    /**
     * Creates an IslandMastery with specified progress
     */
    protected fun createMasteryWithProgress(
        masteredWords: Int = 0,
        completedLevels: Int = 0,
        crossSceneScore: Double = 0.0,
        masteryPercentage: Double = 0.0,
        isUnlocked: Boolean = false,
        masteredAt: Long? = null,
    ) = IslandMastery(
        islandId = islandId,
        userId = userId,
        totalWords = 30,
        masteredWords = masteredWords,
        totalLevels = 5,
        completedLevels = completedLevels,
        crossSceneScore = crossSceneScore,
        masteryPercentage = masteryPercentage,
        isNextIslandUnlocked = isUnlocked,
        masteredAt = masteredAt,
    )

    /**
     * Configures DAO mocks for a new record scenario
     */
    protected fun setupForNewRecord() {
        coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns null
        coEvery { islandMasteryDao.insertIslandMastery(any()) } just Runs
    }

    /**
     * Configures DAO mocks for an update scenario
     */
    protected fun setupForUpdate(existing: IslandMastery) {
        coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns existing
        coEvery {
            islandMasteryDao.updateMasteryProgress(
                userId = userId,
                islandId = islandId,
                masteredWords = any(),
                completedLevels = any(),
                crossSceneScore = any(),
                masteryPercentage = any(),
                isUnlocked = any(),
                masteredAt = any(),
                updatedAt = any(),
            )
        } just Runs
    }
}
