package com.wordland.data.repository

import com.wordland.data.dao.TrackingDao
import com.wordland.domain.model.BehaviorTracking
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TrackingRepositoryImpl
 * Tests the repository layer with mocked DAO
 */
class TrackingRepositoryImplTest {
    private lateinit var repository: TrackingRepositoryImpl
    private lateinit var trackingDao: TrackingDao

    private val testUserId = "test_user"
    private val testWordId = "test_word"
    private val testSceneId = "test_level"

    @Before
    fun setup() {
        trackingDao = mockk(relaxed = true)
        repository = TrackingRepositoryImpl(trackingDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // === Basic Query Tests ===

    @Test
    fun `getRecentTracking returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    BehaviorTracking(
                        id = 1,
                        userId = testUserId,
                        wordId = testWordId,
                        sceneId = testSceneId,
                        action = "answer",
                        isCorrect = true,
                        responseTime = 2000L,
                        difficulty = 1,
                        hintUsed = false,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            coEvery { trackingDao.getRecentTracking(testUserId, 10) } returns expectedList

            val result = repository.getRecentTracking(testUserId, 10)

            assertEquals(1, result.size)
            assertEquals(testWordId, result[0].wordId)
        }

    @Test
    fun `getRecentTracking returns empty list when no tracking`() =
        runTest {
            coEvery { trackingDao.getRecentTracking(any(), any()) } returns emptyList()

            val result = repository.getRecentTracking(testUserId, 10)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getTrackingByWord returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    BehaviorTracking(
                        id = 1,
                        userId = testUserId,
                        wordId = testWordId,
                        sceneId = testSceneId,
                        action = "answer",
                        isCorrect = true,
                        responseTime = 2000L,
                        difficulty = 1,
                        hintUsed = false,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis(),
                    ),
                    BehaviorTracking(
                        id = 2,
                        userId = testUserId,
                        wordId = testWordId,
                        sceneId = testSceneId,
                        action = "answer",
                        isCorrect = false,
                        responseTime = 3000L,
                        difficulty = 1,
                        hintUsed = true,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            coEvery { trackingDao.getTrackingByWord(testUserId, testWordId, 10) } returns expectedList

            val result = repository.getTrackingByWord(testUserId, testWordId, 10)

            assertEquals(2, result.size)
            assertTrue(result.all { it.wordId == testWordId })
        }

    @Test
    fun `getCrossSceneAttempts returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    BehaviorTracking(
                        id = 1,
                        userId = testUserId,
                        wordId = testWordId,
                        sceneId = "different_scene",
                        action = "answer",
                        isCorrect = true,
                        responseTime = 2000L,
                        difficulty = 1,
                        hintUsed = false,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            coEvery { trackingDao.getCrossSceneAttempts(testUserId, testSceneId) } returns expectedList

            val result = repository.getCrossSceneAttempts(testUserId, testSceneId)

            assertEquals(1, result.size)
        }

    @Test
    fun `getTrackingByTimeRange returns list from DAO`() =
        runTest {
            val startTime = System.currentTimeMillis() - 100000
            val endTime = System.currentTimeMillis()
            val expectedList =
                listOf(
                    BehaviorTracking(
                        id = 1,
                        userId = testUserId,
                        wordId = testWordId,
                        sceneId = testSceneId,
                        action = "answer",
                        isCorrect = true,
                        responseTime = 2000L,
                        difficulty = 1,
                        hintUsed = false,
                        isNewWord = false,
                        timestamp = startTime + 50000,
                    ),
                )
            coEvery { trackingDao.getTrackingByTimeRange(testUserId, startTime, endTime) } returns expectedList

            val result = repository.getTrackingByTimeRange(testUserId, startTime, endTime)

            assertEquals(1, result.size)
        }

    @Test
    fun `insertTracking calls DAO`() =
        runTest {
            val tracking =
                BehaviorTracking(
                    id = 0,
                    userId = testUserId,
                    wordId = testWordId,
                    sceneId = testSceneId,
                    action = "answer",
                    isCorrect = true,
                    responseTime = 2000L,
                    difficulty = 1,
                    hintUsed = false,
                    isNewWord = false,
                    timestamp = System.currentTimeMillis(),
                )
            coEvery { trackingDao.insertTracking(any()) } just Runs

            repository.insertTracking(tracking)

            coVerify { trackingDao.insertTracking(tracking) }
        }

    // === recordAnswer Tests ===

    @Test
    fun `recordAnswer creates tracking record`() =
        runTest {
            coEvery { trackingDao.insertTracking(any()) } just Runs

            repository.recordAnswer(
                userId = testUserId,
                wordId = testWordId,
                sceneId = testSceneId,
                isCorrect = true,
                responseTime = 2500L,
                difficulty = 2,
                hintUsed = false,
                isNewWord = false,
            )

            coVerify {
                trackingDao.insertTracking(
                    match {
                        it.userId == testUserId &&
                            it.wordId == testWordId &&
                            it.sceneId == testSceneId &&
                            it.action == "answer" &&
                            it.isCorrect == true &&
                            it.responseTime == 2500L &&
                            it.difficulty == 2 &&
                            it.hintUsed == false &&
                            it.isNewWord == false
                    },
                )
            }
        }

    @Test
    fun `recordAnswer with hint creates tracking record`() =
        runTest {
            coEvery { trackingDao.insertTracking(any()) } just Runs

            repository.recordAnswer(
                userId = testUserId,
                wordId = testWordId,
                sceneId = testSceneId,
                isCorrect = true,
                responseTime = 3500L,
                difficulty = 3,
                hintUsed = true,
                isNewWord = true,
            )

            coVerify {
                trackingDao.insertTracking(
                    match {
                        it.hintUsed == true &&
                            it.isNewWord == true &&
                            it.isCorrect == true
                    },
                )
            }
        }

    @Test
    fun `recordAnswer with null sceneId`() =
        runTest {
            coEvery { trackingDao.insertTracking(any()) } just Runs

            repository.recordAnswer(
                userId = testUserId,
                wordId = testWordId,
                sceneId = null,
                isCorrect = false,
                responseTime = 1500L,
                difficulty = 1,
                hintUsed = false,
                isNewWord = false,
            )

            coVerify {
                trackingDao.insertTracking(
                    match {
                        it.sceneId == null &&
                            it.isCorrect == false
                    },
                )
            }
        }

    @Test
    fun `deleteOldTracking returns deleted count`() =
        runTest {
            val beforeTime = System.currentTimeMillis() - 86400000 // 1 day ago
            coEvery { trackingDao.deleteOldTracking(beforeTime) } returns 50

            val result = repository.deleteOldTracking(beforeTime)

            assertEquals(50, result)
        }

    @Test
    fun `deleteOldTracking returns zero when no records deleted`() =
        runTest {
            val beforeTime = System.currentTimeMillis() - 86400000
            coEvery { trackingDao.deleteOldTracking(beforeTime) } returns 0

            val result = repository.deleteOldTracking(beforeTime)

            assertEquals(0, result)
        }

    // === Statistics Tests ===

    @Test
    fun `getCorrectCrossSceneCount returns count from DAO`() =
        runTest {
            coEvery { trackingDao.getCorrectCrossSceneCount(testUserId) } returns 25

            val result = repository.getCorrectCrossSceneCount(testUserId)

            assertEquals(25, result)
        }

    @Test
    fun `getTotalCrossSceneCount returns count from DAO`() =
        runTest {
            coEvery { trackingDao.getTotalCrossSceneCount(testUserId) } returns 40

            val result = repository.getTotalCrossSceneCount(testUserId)

            assertEquals(40, result)
        }

    @Test
    fun `getAverageResponseTime returns average from DAO`() =
        runTest {
            coEvery { trackingDao.getAverageResponseTime(testUserId) } returns 2500.5

            val result = repository.getAverageResponseTime(testUserId)

            assertEquals(2500.5, result)
        }

    @Test
    fun `getAverageResponseTime returns null when no data`() =
        runTest {
            coEvery { trackingDao.getAverageResponseTime(testUserId) } returns null

            val result = repository.getAverageResponseTime(testUserId)

            assertEquals(null, result)
        }

    @Test
    fun `getAverageCorrectResponseTime returns average from DAO`() =
        runTest {
            coEvery { trackingDao.getAverageCorrectResponseTime(testUserId) } returns 2000.0

            val result = repository.getAverageCorrectResponseTime(testUserId)

            assertEquals(2000.0, result)
        }

    // === getRecentPatterns Tests ===

    @Test
    fun `getRecentPatterns returns valid patterns`() =
        runTest {
            val trackingList =
                listOf(
                    BehaviorTracking(
                        id = 1,
                        userId = testUserId,
                        wordId = "word1",
                        sceneId = testSceneId,
                        action = "answer",
                        isCorrect = true,
                        responseTime = 2000L,
                        difficulty = 1,
                        hintUsed = false,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis() - 3000,
                    ),
                    BehaviorTracking(
                        id = 2,
                        userId = testUserId,
                        wordId = "word2",
                        sceneId = testSceneId,
                        action = "answer",
                        isCorrect = false,
                        responseTime = 1500L,
                        difficulty = 2,
                        hintUsed = true,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis() - 2000,
                    ),
                )
            coEvery { trackingDao.getRecentTracking(testUserId, 5) } returns trackingList

            val result = repository.getRecentPatterns(testUserId, 5)

            assertEquals(2, result.size)
            assertEquals(2000L, result[0].responseTime)
            assertEquals(true, result[0].isCorrect)
            assertEquals(false, result[0].hintUsed)
            assertEquals(1500L, result[1].responseTime)
            assertEquals(false, result[1].isCorrect)
            assertEquals(true, result[1].hintUsed)
        }

    @Test
    fun `getRecentPatterns filters out null responseTime`() =
        runTest {
            val trackingList =
                listOf(
                    BehaviorTracking(
                        id = 1,
                        userId = testUserId,
                        wordId = "word1",
                        sceneId = testSceneId,
                        action = "hint_used",
                        isCorrect = null,
                        responseTime = null,
                        difficulty = 1,
                        hintUsed = true,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis() - 1000,
                    ),
                    BehaviorTracking(
                        id = 2,
                        userId = testUserId,
                        wordId = "word2",
                        sceneId = testSceneId,
                        action = "answer",
                        isCorrect = true,
                        responseTime = 2000L,
                        difficulty = 1,
                        hintUsed = false,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            coEvery { trackingDao.getRecentTracking(testUserId, 5) } returns trackingList

            val result = repository.getRecentPatterns(testUserId, 5)

            assertEquals(1, result.size)
            assertEquals(2000L, result[0].responseTime)
        }

    @Test
    fun `getRecentPatterns returns empty list when no tracking`() =
        runTest {
            coEvery { trackingDao.getRecentTracking(any(), any()) } returns emptyList()

            val result = repository.getRecentPatterns(testUserId, 5)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getRecentPatterns respects limit parameter`() =
        runTest {
            val trackingList =
                (1..10).map { i ->
                    BehaviorTracking(
                        id = i.toLong(),
                        userId = testUserId,
                        wordId = "word$i",
                        sceneId = testSceneId,
                        action = "answer",
                        isCorrect = true,
                        responseTime = 2000L,
                        difficulty = 1,
                        hintUsed = false,
                        isNewWord = false,
                        timestamp = System.currentTimeMillis() - (i * 1000),
                    )
                }
            coEvery { trackingDao.getRecentTracking(testUserId, 3) } returns trackingList.take(3)

            val result = repository.getRecentPatterns(testUserId, 3)

            assertEquals(3, result.size)
        }
}
