package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.behavior.BehaviorAnalyzer
import com.wordland.domain.hint.HintGenerator
import com.wordland.domain.hint.HintManager
import com.wordland.domain.model.Word
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UseHintUseCaseEnhanced
 * Tests the enhanced hint system with usage tracking and limitations
 */
class UseHintUseCaseEnhancedTest {
    private lateinit var useHintUseCase: UseHintUseCaseEnhanced
    private lateinit var trackingRepository: TrackingRepository
    private lateinit var wordRepository: WordRepository
    private lateinit var hintGenerator: HintGenerator
    private lateinit var hintManager: HintManager
    private lateinit var behaviorAnalyzer: BehaviorAnalyzer

    private val testUserId = "test_user"
    private val testWordId = "test_word"
    private val testLevelId = "test_level"

    private val testWord =
        Word(
            id = testWordId,
            word = "apple",
            translation = "苹果",
            pronunciation = "/ˈæpl/",
            audioPath = null,
            partOfSpeech = "noun",
            difficulty = 2,
            frequency = 80,
            theme = "fruit",
            islandId = "test_island",
            levelId = testLevelId,
            order = 1,
            ketLevel = true,
            petLevel = false,
            exampleSentences = null,
            relatedWords = null,
            root = null,
            prefix = null,
            suffix = null,
        )

    @Before
    fun setup() {
        trackingRepository = mockk(relaxed = true)
        wordRepository = mockk(relaxed = true)
        hintGenerator = mockk(relaxed = true)
        hintManager = mockk(relaxed = true)
        behaviorAnalyzer = mockk(relaxed = true)

        useHintUseCase =
            UseHintUseCaseEnhanced(
                trackingRepository,
                wordRepository,
                hintGenerator,
                hintManager,
                behaviorAnalyzer,
            )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // === Success Cases ===

    @Test
    fun `invoke returns HintResult when hint is available`() =
        runTest {
            coEvery { wordRepository.getWordById(testWordId) } returns testWord
            coEvery { hintManager.canUseHint(testWordId) } returns Pair(true, null)
            coEvery { hintManager.useHint(testWordId) } returns 1
            coEvery { hintGenerator.generateAdaptiveHint("apple", 1) } returns "首字母: A"
            coEvery { hintManager.getHintStats(testWordId) } returns Triple(1, 2, 3)
            coEvery { trackingRepository.insertTracking(any()) } just Runs

            val result = useHintUseCase(testUserId, testWordId, testLevelId)

            assertTrue(result is com.wordland.domain.model.Result.Success)
            val hintResult = (result as com.wordland.domain.model.Result.Success).data
            assertEquals("首字母: A", hintResult.hintText)
            assertEquals(1, hintResult.hintLevel)
            assertEquals(2, hintResult.hintsRemaining)
            assertEquals("apple", hintResult.word)
            assertTrue(hintResult.shouldApplyPenalty)
        }

    @Test
    fun `invoke records hint usage in tracking repository`() =
        runTest {
            coEvery { wordRepository.getWordById(testWordId) } returns testWord
            coEvery { hintManager.canUseHint(testWordId) } returns Pair(true, null)
            coEvery { hintManager.useHint(testWordId) } returns 1
            coEvery { hintGenerator.generateAdaptiveHint(any(), any()) } returns "hint"
            coEvery { hintManager.getHintStats(testWordId) } returns Triple(1, 2, 3)

            useHintUseCase(testUserId, testWordId, testLevelId)

            coVerify {
                trackingRepository.insertTracking(
                    match {
                        it.userId == testUserId &&
                            it.wordId == testWordId &&
                            it.sceneId == testLevelId &&
                            it.action == "hint_used" &&
                            it.hintUsed == true
                    },
                )
            }
        }

    // === Error Cases ===

    @Test
    fun `invoke returns Error when hint limit reached`() =
        runTest {
            coEvery { hintManager.canUseHint(testWordId) } returns Pair(false, "已达到提示次数上限")

            val result = useHintUseCase(testUserId, testWordId, testLevelId)

            assertTrue(result is com.wordland.domain.model.Result.Error)
            val exception = (result as com.wordland.domain.model.Result.Error).exception
            assertTrue(exception is HintLimitException)
            assertEquals("已达到提示次数上限", exception.message)
        }

    @Test
    fun `invoke returns Error when word not found`() =
        runTest {
            coEvery { hintManager.canUseHint(testWordId) } returns Pair(true, null)
            coEvery { wordRepository.getWordById(testWordId) } returns null

            val result = useHintUseCase(testUserId, testWordId, testLevelId)

            assertTrue(result is com.wordland.domain.model.Result.Error)
        }

    @Test
    fun `invoke returns Error when exception occurs`() =
        runTest {
            coEvery { hintManager.canUseHint(testWordId) } throws RuntimeException("Test error")

            val result = useHintUseCase(testUserId, testWordId, testLevelId)

            assertTrue(result is com.wordland.domain.model.Result.Error)
        }

    // === Helper Methods ===

    @Test
    fun `canUseHint returns true when hint is available`() {
        coEvery { hintManager.canUseHint(testWordId) } returns Pair(true, null)

        val (canUse, reason) = useHintUseCase.canUseHint(testWordId)

        assertTrue(canUse)
        assertEquals(null, reason)
    }

    @Test
    fun `canUseHint returns false with reason when hint not available`() {
        coEvery { hintManager.canUseHint(testWordId) } returns Pair(false, "冷却中")

        val (canUse, reason) = useHintUseCase.canUseHint(testWordId)

        assertFalse(canUse)
        assertEquals("冷却中", reason)
    }

    @Test
    fun `getHintStats returns correct statistics`() {
        coEvery { hintManager.getHintStats(testWordId) } returns Triple(1, 2, 3)
        coEvery { hintManager.getCurrentHintLevel(testWordId) } returns 1
        coEvery { hintManager.getHintDependencyScore(testWordId) } returns 0.3f
        coEvery { hintManager.isOverusingHints(testWordId) } returns false

        val stats = useHintUseCase.getHintStats(testWordId)

        assertNotNull(stats)
        assertEquals(testWordId, stats!!.wordId)
        assertEquals(1, stats.currentLevel)
        assertEquals(1, stats.hintsUsed)
        assertEquals(2, stats.hintsRemaining)
        assertEquals(3, stats.totalHints)
        assertEquals(0.3f, stats.dependencyScore, 0.01f)
        assertFalse(stats.isOverusing)
    }

    @Test
    fun `getHintStats returns null when word not tracked`() {
        coEvery { hintManager.getHintStats(testWordId) } returns Triple(0, 0, 0)
        coEvery { hintManager.getCurrentHintLevel(testWordId) } returns 0
        coEvery { hintManager.getHintDependencyScore(testWordId) } returns 0f
        coEvery { hintManager.isOverusingHints(testWordId) } returns false

        val stats = useHintUseCase.getHintStats(testWordId)

        assertNotNull(stats)
        assertEquals(0, stats!!.currentLevel)
        assertEquals(0, stats.hintsUsed)
    }

    @Test
    fun `resetHints calls hintManager resetHints`() {
        useHintUseCase.resetHints(testWordId)

        coVerify { hintManager.resetHints(testWordId) }
    }

    @Test
    fun `configureForDifficulty calls hintManager setMaxHintsForDifficulty`() {
        useHintUseCase.configureForDifficulty(3)

        coVerify { hintManager.setMaxHintsForDifficulty(3) }
    }

    // === Edge Cases ===

    @Test
    fun `invoke handles empty word gracefully`() =
        runTest {
            val emptyWord = testWord.copy(word = "")
            coEvery { hintManager.canUseHint(testWordId) } returns Pair(true, null)
            coEvery { wordRepository.getWordById(testWordId) } returns emptyWord
            coEvery { hintManager.useHint(testWordId) } returns 1
            coEvery { hintGenerator.generateAdaptiveHint("", 1) } returns ""
            coEvery { hintManager.getHintStats(testWordId) } returns Triple(1, 2, 3)

            val result = useHintUseCase(testUserId, testWordId, testLevelId)

            assertTrue(result is com.wordland.domain.model.Result.Success)
            assertEquals("", (result as com.wordland.domain.model.Result.Success).data.hintText)
        }

    @Test
    fun `invoke handles special characters in word`() =
        runTest {
            val specialWord = testWord.copy(word = "café")
            coEvery { hintManager.canUseHint(testWordId) } returns Pair(true, null)
            coEvery { wordRepository.getWordById(testWordId) } returns specialWord
            coEvery { hintManager.useHint(testWordId) } returns 1
            coEvery { hintGenerator.generateAdaptiveHint("café", 1) } returns "首字母: c"
            coEvery { hintManager.getHintStats(testWordId) } returns Triple(1, 2, 3)

            val result = useHintUseCase(testUserId, testWordId, testLevelId)

            assertTrue(result is com.wordland.domain.model.Result.Success)
            assertEquals("首字母: c", (result as com.wordland.domain.model.Result.Success).data.hintText)
        }

    @Test
    fun `invoke tracks correct difficulty level`() =
        runTest {
            val hardWord = testWord.copy(difficulty = 5)
            coEvery { hintManager.canUseHint(testWordId) } returns Pair(true, null)
            coEvery { wordRepository.getWordById(testWordId) } returns hardWord
            coEvery { hintManager.useHint(testWordId) } returns 1
            coEvery { hintGenerator.generateAdaptiveHint(any(), any()) } returns "hint"
            coEvery { hintManager.getHintStats(testWordId) } returns Triple(1, 2, 3)

            useHintUseCase(testUserId, testWordId, testLevelId)

            coVerify {
                trackingRepository.insertTracking(
                    match {
                        it.difficulty == 5
                    },
                )
            }
        }
}
