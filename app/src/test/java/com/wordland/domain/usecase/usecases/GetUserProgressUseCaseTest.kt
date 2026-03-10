package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.UserWordProgress
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetUserProgressUseCase
 * Tests getting user's overall learning progress
 */
class GetUserProgressUseCaseTest {
    private lateinit var getUserProgressUseCase: GetUserProgressUseCase
    private lateinit var progressRepository: ProgressRepository

    private val testUserId = "test_user"

    @Before
    fun setup() {
        progressRepository = mockk(relaxed = true)
        getUserProgressUseCase = GetUserProgressUseCase(progressRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns user progress list`() =
        runTest {
            val expectedProgress =
                listOf(
                    UserWordProgress(
                        wordId = "word1",
                        userId = testUserId,
                        status = LearningStatus.LEARNING,
                        memoryStrength = 50,
                        totalAttempts = 10,
                        correctAttempts = 7,
                        incorrectAttempts = 3,
                        createdAt = System.currentTimeMillis() - 100000,
                        updatedAt = System.currentTimeMillis() - 5000,
                    ),
                    UserWordProgress(
                        wordId = "word2",
                        userId = testUserId,
                        status = LearningStatus.MASTERED,
                        memoryStrength = 90,
                        totalAttempts = 20,
                        correctAttempts = 19,
                        incorrectAttempts = 1,
                        createdAt = System.currentTimeMillis() - 200000,
                        updatedAt = System.currentTimeMillis() - 10000,
                    ),
                )
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns expectedProgress

            val result = getUserProgressUseCase(testUserId).first()

            assertEquals(2, result.size)
            assertEquals("word1", result[0].wordId)
            assertEquals("word2", result[1].wordId)
        }

    @Test
    fun `invoke returns empty list when user has no progress`() =
        runTest {
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns emptyList()

            val result = getUserProgressUseCase(testUserId).first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun `invoke calls repository with correct userId`() =
        runTest {
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns emptyList()

            getUserProgressUseCase(testUserId).first()

            coVerify { progressRepository.getAllWordProgress(testUserId) }
        }
}
