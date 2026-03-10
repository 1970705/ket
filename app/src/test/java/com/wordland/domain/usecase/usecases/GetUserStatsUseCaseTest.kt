package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import com.wordland.domain.model.Result
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetUserStatsUseCase
 * Tests getting user statistics including progress and mastery
 */
class GetUserStatsUseCaseTest {
    private lateinit var getUserStatsUseCase: GetUserStatsUseCase
    private lateinit var progressRepository: ProgressRepository
    private lateinit var islandRepository: IslandMasteryRepository
    private lateinit var wordRepository: WordRepository

    private val testUserId = "test_user"

    @Before
    fun setup() {
        progressRepository = mockk(relaxed = true)
        islandRepository = mockk(relaxed = true)
        wordRepository = mockk(relaxed = true)

        getUserStatsUseCase =
            GetUserStatsUseCase(
                progressRepository,
                islandRepository,
                wordRepository,
            )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns user stats when data exists`() =
        runTest {
            coEvery { wordRepository.getWordCount() } returns 100
            coEvery { progressRepository.getMasteredWordCount(testUserId) } returns 25
            coEvery { islandRepository.getMasteredIslandCount(testUserId) } returns 2
            coEvery { progressRepository.getAllLevelProgress(testUserId) } returns
                listOf(
                    LevelProgress(
                        levelId = "level1",
                        userId = testUserId,
                        islandId = "island1",
                        status = LevelStatus.COMPLETED,
                        stars = 3,
                        score = 200,
                        totalTime = 3000L,
                        difficulty = "normal",
                    ),
                    LevelProgress(
                        levelId = "level2",
                        userId = testUserId,
                        islandId = "island1",
                        status = LevelStatus.IN_PROGRESS,
                        stars = 1,
                        score = 100,
                        totalTime = 1000L,
                        difficulty = "normal",
                    ),
                )

            val result = getUserStatsUseCase(testUserId)

            assertTrue(result is Result.Success)
            val stats = (result as Result.Success).data
            assertEquals(2, stats.totalLevels)
            assertEquals(1, stats.completedLevels)
            assertEquals(2, stats.masteredIslands)
            assertEquals(100, stats.totalWords)
            assertEquals(25, stats.masteredWords)
            assertEquals(0.5f, stats.completionRate, 0.01f)
        }

    @Test
    fun `invoke calculates completion rate correctly`() =
        runTest {
            coEvery { wordRepository.getWordCount() } returns 100
            coEvery { progressRepository.getMasteredWordCount(testUserId) } returns 10
            coEvery { islandRepository.getMasteredIslandCount(testUserId) } returns 0
            coEvery { progressRepository.getAllLevelProgress(testUserId) } returns
                listOf(
                    LevelProgress(
                        levelId = "level1",
                        userId = testUserId,
                        islandId = "island1",
                        status = LevelStatus.COMPLETED,
                        stars = 3,
                        score = 200,
                        totalTime = 3000L,
                        difficulty = "normal",
                    ),
                    LevelProgress(
                        levelId = "level2",
                        userId = testUserId,
                        islandId = "island1",
                        status = LevelStatus.COMPLETED,
                        stars = 2,
                        score = 150,
                        totalTime = 4000L,
                        difficulty = "normal",
                    ),
                    LevelProgress(
                        levelId = "level3",
                        userId = testUserId,
                        islandId = "island1",
                        status = LevelStatus.LOCKED,
                        stars = 0,
                        score = 0,
                        totalTime = 0L,
                        difficulty = "normal",
                    ),
                )

            val result = getUserStatsUseCase(testUserId)

            assertTrue(result is Result.Success)
            val stats = (result as Result.Success).data
            assertEquals(3, stats.totalLevels)
            assertEquals(2, stats.completedLevels)
            assertEquals(0.67f, stats.completionRate, 0.01f)
        }

    @Test
    fun `invoke returns zero completion rate when no levels`() =
        runTest {
            coEvery { wordRepository.getWordCount() } returns 50
            coEvery { progressRepository.getMasteredWordCount(testUserId) } returns 0
            coEvery { islandRepository.getMasteredIslandCount(testUserId) } returns 0
            coEvery { progressRepository.getAllLevelProgress(testUserId) } returns emptyList()

            val result = getUserStatsUseCase(testUserId)

            assertTrue(result is Result.Success)
            val stats = (result as Result.Success).data
            assertEquals(0, stats.totalLevels)
            assertEquals(0, stats.completedLevels)
            assertEquals(0f, stats.completionRate, 0.01f)
        }

    @Test
    fun `invoke returns Error when exception occurs`() =
        runTest {
            coEvery { wordRepository.getWordCount() } throws RuntimeException("Database error")

            val result = getUserStatsUseCase(testUserId)

            assertTrue(result is Result.Error)
        }

    @Test
    fun `invoke handles empty level progress list`() =
        runTest {
            coEvery { wordRepository.getWordCount() } returns 0
            coEvery { progressRepository.getMasteredWordCount(testUserId) } returns 0
            coEvery { islandRepository.getMasteredIslandCount(testUserId) } returns 0
            coEvery { progressRepository.getAllLevelProgress(testUserId) } returns emptyList()

            val result = getUserStatsUseCase(testUserId)

            assertTrue(result is Result.Success)
            val stats = (result as Result.Success).data
            assertEquals(0, stats.totalLevels)
            assertEquals(0, stats.masteredWords)
        }

    @Test
    fun `invoke counts only completed levels`() =
        runTest {
            coEvery { wordRepository.getWordCount() } returns 100
            coEvery { progressRepository.getMasteredWordCount(testUserId) } returns 15
            coEvery { islandRepository.getMasteredIslandCount(testUserId) } returns 1
            coEvery { progressRepository.getAllLevelProgress(testUserId) } returns
                listOf(
                    LevelProgress(
                        levelId = "level1",
                        userId = testUserId,
                        islandId = "island1",
                        status = LevelStatus.COMPLETED,
                        stars = 3,
                        score = 200,
                        totalTime = 3000L,
                        difficulty = "normal",
                    ),
                    LevelProgress(
                        levelId = "level2",
                        userId = testUserId,
                        islandId = "island1",
                        status = LevelStatus.IN_PROGRESS,
                        stars = 1,
                        score = 50,
                        totalTime = 1000L,
                        difficulty = "normal",
                    ),
                    LevelProgress(
                        levelId = "level3",
                        userId = testUserId,
                        islandId = "island1",
                        status = LevelStatus.UNLOCKED,
                        stars = 0,
                        score = 0,
                        totalTime = 0L,
                        difficulty = "normal",
                    ),
                )

            val result = getUserStatsUseCase(testUserId)

            assertTrue(result is Result.Success)
            val stats = (result as Result.Success).data
            assertEquals(3, stats.totalLevels)
            assertEquals(1, stats.completedLevels)
        }
}
