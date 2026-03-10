package com.wordland.integration

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.algorithm.StarRatingCalculator
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.SubmitAnswerUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Edge case integration tests for Star Rating System
 *
 * Tests the flow from per-word rating (SubmitAnswerUseCase) to level-level rating
 * (StarRatingCalculator) for edge cases:
 * 1. All wrong earns 0 stars
 * 2. Mixed penalties (hints + errors + slow) still gets 1 star (child-friendly)
 *
 * These tests use mock repositories to simulate real gameplay scenarios.
 */
class StarRatingIntegrationEdgeCaseTest {
    private lateinit var wordRepository: WordRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var trackingRepository: TrackingRepository
    private lateinit var useCase: SubmitAnswerUseCase

    // Test words
    private val testWords =
        listOf(
            Word(
                id = "word_1",
                word = "cat",
                translation = "猫",
                pronunciation = "/kæt/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 90,
                theme = "animals",
                islandId = "test_island",
                levelId = "test_level",
                order = 1,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            ),
            Word(
                id = "word_2",
                word = "apple",
                translation = "苹果",
                pronunciation = "/ˈæpl/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 85,
                theme = "food",
                islandId = "test_island",
                levelId = "test_level",
                order = 2,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            ),
            Word(
                id = "word_3",
                word = "run",
                translation = "跑",
                pronunciation = "/rʌn/",
                audioPath = null,
                partOfSpeech = "verb",
                difficulty = 1,
                frequency = 80,
                theme = "action",
                islandId = "test_island",
                levelId = "test_level",
                order = 3,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            ),
            Word(
                id = "word_4",
                word = "blue",
                translation = "蓝色",
                pronunciation = "/bluː/",
                audioPath = null,
                partOfSpeech = "adjective",
                difficulty = 1,
                frequency = 75,
                theme = "color",
                islandId = "test_island",
                levelId = "test_level",
                order = 4,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            ),
            Word(
                id = "word_5",
                word = "book",
                translation = "书",
                pronunciation = "/bʊk/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 70,
                theme = "objects",
                islandId = "test_island",
                levelId = "test_level",
                order = 5,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            ),
            Word(
                id = "word_6",
                word = "jump",
                translation = "跳",
                pronunciation = "/dʒʌmp/",
                audioPath = null,
                partOfSpeech = "verb",
                difficulty = 1,
                frequency = 65,
                theme = "action",
                islandId = "test_island",
                levelId = "test_level",
                order = 6,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            ),
        )

    @Before
    fun setup() {
        wordRepository = mockk()
        progressRepository = mockk()
        trackingRepository = mockk()
        useCase = SubmitAnswerUseCase(wordRepository, progressRepository, trackingRepository)

        // Setup default mock behaviors
        testWords.forEach { word ->
            coEvery { wordRepository.getWordById(word.id) } returns word
        }
        coEvery { progressRepository.getWordProgress(any(), any()) } returns null
        coEvery { progressRepository.updatePracticeResult(any(), any(), any(), any(), any()) } just Runs
        coEvery { trackingRepository.recordAnswer(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
        coEvery { trackingRepository.getRecentPatterns(any(), any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== EDGE CASE 1: ALL WRONG ==========

    @Test
    fun `Integration 9 - All wrong earns 0 stars at both levels`() =
        runTest {
            // Given: User gets all wrong
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            val results =
                testWords.map { word ->
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = "wrong",
                            responseTime = 4000L,
                            hintUsed = false,
                            levelId = levelId,
                            previousComboState = comboState,
                        )
                    comboState = (result as Result.Success).data.comboState
                    result
                }

            // When: Calculate level-level rating
            val totalCorrect = results.count { (it as Result.Success).data.isCorrect }
            val totalTime = results.sumOf { (it as Result.Success).data.timeTaken }

            val levelData =
                StarRatingCalculator.PerformanceData(
                    totalWords = testWords.size,
                    correctAnswers = totalCorrect,
                    hintsUsed = 0,
                    totalTimeMs = totalTime,
                    wrongAnswers = testWords.size,
                    maxCombo = comboState.consecutiveCorrect,
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: 0 correct = 0 stars
            assertEquals("0 correct should earn 0 stars", 0, levelStars)
            assertEquals("Combo should be 0", 0, comboState.consecutiveCorrect)
        }

    // ========== EDGE CASE 2: MIXED PENALTIES ==========

    @Test
    fun `Integration Edge - Hints + errors + slow still gets 1 star`() =
        runTest {
            // Given: Multiple penalties but some correct
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            val results =
                testWords.mapIndexed { index, word ->
                    val isCorrect = index < 3 // First 3 correct
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = if (isCorrect) word.word else "wrong",
                            responseTime = 20000L, // Slow
                            hintUsed = isCorrect, // Use hints on correct ones
                            levelId = levelId,
                            previousComboState = comboState,
                        )
                    comboState = (result as Result.Success).data.comboState
                    result
                }

            // When: Calculate level-level rating
            val totalCorrect = results.count { (it as Result.Success).data.isCorrect }
            val totalHints = results.count { (it as Result.Success).data.hintUsed }
            val totalTime = results.sumOf { (it as Result.Success).data.timeTaken }

            val levelData =
                StarRatingCalculator.PerformanceData(
                    totalWords = testWords.size,
                    correctAnswers = totalCorrect,
                    hintsUsed = totalHints,
                    totalTimeMs = totalTime,
                    wrongAnswers = 3,
                    maxCombo = comboState.consecutiveCorrect,
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: Child-friendly minimum 1 star
            assertEquals("Worst case with 3 correct should earn 1 star", 1, levelStars)
        }
}
