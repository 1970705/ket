package com.wordland.data.repository.progress

import com.wordland.data.dao.ProgressDao
import com.wordland.data.repository.ProgressRepositoryImpl
import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.UserWordProgress
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProgressRepositoryImpl - Word Progress
 * Tests word progress CRUD operations and practice result updates
 */
class ProgressRepositoryImplWordTest {
    private lateinit var repository: ProgressRepositoryImpl
    private lateinit var progressDao: ProgressDao

    private val testUserId = "test_user"
    private val testWordId = "test_word"

    @Before
    fun setup() {
        progressDao = mockk(relaxed = true)
        repository = ProgressRepositoryImpl(progressDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getWordProgress returns null when not found`() =
        runTest {
            coEvery { progressDao.getWordProgress(any(), any()) } returns null

            val result = repository.getWordProgress(testUserId, testWordId)

            assertNull(result)
            coVerify { progressDao.getWordProgress(testUserId, testWordId) }
        }

    @Test
    fun `getWordProgress returns progress when exists`() =
        runTest {
            val expectedProgress =
                UserWordProgress(
                    wordId = testWordId,
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 10,
                    correctAttempts = 7,
                    incorrectAttempts = 3,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            coEvery { progressDao.getWordProgress(testUserId, testWordId) } returns expectedProgress

            val result = repository.getWordProgress(testUserId, testWordId)

            assertNotNull(result)
            assertEquals(expectedProgress.wordId, result!!.wordId)
            assertEquals(expectedProgress.memoryStrength, result.memoryStrength)
        }

    @Test
    fun `getWordProgressFlow returns flow from DAO`() =
        runTest {
            val expectedProgress =
                UserWordProgress(
                    wordId = testWordId,
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 10,
                    correctAttempts = 7,
                    incorrectAttempts = 3,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            coEvery { progressDao.getWordProgressFlow(testUserId, testWordId) } returns flowOf(expectedProgress)

            val result = repository.getWordProgressFlow(testUserId, testWordId).first()

            assertNotNull(result)
            assertEquals(expectedProgress.wordId, result!!.wordId)
        }

    @Test
    fun `getAllWordProgress returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    UserWordProgress(
                        wordId = "word1",
                        userId = testUserId,
                        status = LearningStatus.LEARNING,
                        memoryStrength = 50,
                        totalAttempts = 10,
                        correctAttempts = 7,
                        incorrectAttempts = 3,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            coEvery { progressDao.getAllWordProgress(testUserId) } returns expectedList

            val result = repository.getAllWordProgress(testUserId)

            assertEquals(1, result.size)
            assertEquals("word1", result[0].wordId)
        }

    @Test
    fun `getWordsDueForReview returns list from DAO`() =
        runTest {
            val currentTime = System.currentTimeMillis()
            val expectedList =
                listOf(
                    UserWordProgress(
                        wordId = "word1",
                        userId = testUserId,
                        status = LearningStatus.NEED_REVIEW,
                        memoryStrength = 50,
                        totalAttempts = 10,
                        correctAttempts = 7,
                        incorrectAttempts = 3,
                        createdAt = currentTime - 1000000,
                        updatedAt = currentTime - 100000,
                        nextReviewTime = currentTime - 1000,
                    ),
                )
            coEvery { progressDao.getWordsDueForReview(testUserId, currentTime, 10) } returns expectedList

            val result = repository.getWordsDueForReview(testUserId, currentTime, 10)

            assertEquals(1, result.size)
            assertEquals("word1", result[0].wordId)
        }

    @Test
    fun `getLearningWords returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    UserWordProgress(
                        wordId = "word1",
                        userId = testUserId,
                        status = LearningStatus.LEARNING,
                        memoryStrength = 30,
                        totalAttempts = 5,
                        correctAttempts = 3,
                        incorrectAttempts = 2,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            coEvery { progressDao.getLearningWords(testUserId, 10) } returns expectedList

            val result = repository.getLearningWords(testUserId, 10)

            assertEquals(1, result.size)
            assertEquals(LearningStatus.LEARNING, result[0].status)
        }

    @Test
    fun `getMasteredWords returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    UserWordProgress(
                        wordId = "word1",
                        userId = testUserId,
                        status = LearningStatus.MASTERED,
                        memoryStrength = 95,
                        totalAttempts = 20,
                        correctAttempts = 19,
                        incorrectAttempts = 1,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            coEvery { progressDao.getMasteredWords(testUserId) } returns expectedList

            val result = repository.getMasteredWords(testUserId)

            assertEquals(1, result.size)
            assertEquals(LearningStatus.MASTERED, result[0].status)
        }

    @Test
    fun `insertWordProgress calls DAO`() =
        runTest {
            val progress =
                UserWordProgress(
                    wordId = testWordId,
                    userId = testUserId,
                    status = LearningStatus.NEW,
                    memoryStrength = 0,
                    totalAttempts = 0,
                    correctAttempts = 0,
                    incorrectAttempts = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            coEvery { progressDao.insertWordProgress(any()) } just Runs

            repository.insertWordProgress(progress)

            coVerify { progressDao.insertWordProgress(progress) }
        }

    @Test
    fun `updateWordProgress calls DAO`() =
        runTest {
            val progress =
                UserWordProgress(
                    wordId = testWordId,
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 10,
                    correctAttempts = 7,
                    incorrectAttempts = 3,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            coEvery { progressDao.updateWordProgress(any()) } just Runs

            repository.updateWordProgress(progress)

            coVerify { progressDao.updateWordProgress(progress) }
        }

    @Test
    fun `updatePracticeResult creates new progress if not exists`() =
        runTest {
            coEvery { progressDao.getWordProgress(testUserId, testWordId) } returns null
            coEvery { progressDao.insertWordProgress(any()) } just Runs
            coEvery { progressDao.updatePracticeResult(any(), any(), any(), any(), any(), any(), any()) } just Runs

            repository.updatePracticeResult(
                userId = testUserId,
                wordId = testWordId,
                isCorrect = true,
                responseTime = 2000L,
                newMemoryStrength = 60,
            )

            coVerify(atLeast = 1) { progressDao.insertWordProgress(any()) }
            coVerify(atLeast = 1) { progressDao.updatePracticeResult(any(), any(), any(), any(), any(), any(), any()) }
        }

    @Test
    fun `updatePracticeResult updates existing progress`() =
        runTest {
            val existingProgress =
                UserWordProgress(
                    wordId = testWordId,
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 40,
                    totalAttempts = 5,
                    correctAttempts = 3,
                    incorrectAttempts = 2,
                    createdAt = System.currentTimeMillis() - 10000,
                    updatedAt = System.currentTimeMillis() - 5000,
                )
            // Always return the existing progress when queried
            coEvery { progressDao.getWordProgress(testUserId, testWordId) } returns existingProgress
            coEvery { progressDao.updatePracticeResult(any(), any(), any(), any(), any(), any(), any()) } just Runs

            repository.updatePracticeResult(
                userId = testUserId,
                wordId = testWordId,
                isCorrect = true,
                responseTime = 2000L,
                newMemoryStrength = 55,
            )

            coVerify(atLeast = 1) { progressDao.updatePracticeResult(any(), any(), any(), any(), any(), any(), any()) }
        }
}
