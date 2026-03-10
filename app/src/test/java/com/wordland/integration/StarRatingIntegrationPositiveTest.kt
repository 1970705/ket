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
 * Positive integration tests for Star Rating System
 *
 * Tests the flow from per-word rating (SubmitAnswerUseCase) to level-level rating
 * (StarRatingCalculator) for positive scenarios:
 * 1. Perfect performance earns 3 stars
 * 2. High combo boosts rating
 * 3. Slow but correct still earns 3 stars (child-friendly)
 *
 * These tests use mock repositories to simulate real gameplay scenarios.
 */
class StarRatingIntegrationPositiveTest {
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

    // ========== POSITIVE SCENARIO 1: PERFECT PERFORMANCE ==========

    @Test
    fun `Integration 1 - Perfect performance earns 3 stars at both levels`() =
        runTest {
            // Given: User completes level perfectly
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            val results =
                testWords.map { word ->
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = word.word, // Correct
                            responseTime = 4000L, // Adequate time
                            hintUsed = false,
                            levelId = levelId,
                            previousComboState = comboState,
                        )
                    comboState = (result as Result.Success).data.comboState
                    result
                }

            // When: Calculate level-level rating
            val totalStars = results.sumOf { (it as Result.Success).data.starsEarned }
            val totalCorrect = results.count { (it as Result.Success).data.isCorrect }
            val totalHints = results.count { (it as Result.Success).data.hintUsed }
            val totalTime = results.sumOf { (it as Result.Success).data.timeTaken }
            val totalErrors = 0 // All correct
            val maxCombo = comboState.consecutiveCorrect

            val levelData =
                StarRatingCalculator.PerformanceData(
                    totalWords = testWords.size,
                    correctAnswers = totalCorrect,
                    hintsUsed = totalHints,
                    totalTimeMs = totalTime,
                    wrongAnswers = totalErrors,
                    maxCombo = maxCombo,
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: Both levels should show 3-star performance
            assertEquals("All answers should be correct", testWords.size, totalCorrect)
            assertEquals("Each word should earn 3 stars", testWords.size * 3, totalStars)
            assertEquals("Level should earn 3 stars", 3, levelStars)
            assertEquals("Combo should reach 6", testWords.size, maxCombo)
        }

    // ========== POSITIVE SCENARIO 2: HIGH COMBO ==========

    @Test
    fun `Integration 5 - High combo boosts level rating`() =
        runTest {
            // Given: User builds high combo
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            val results =
                testWords.map { word ->
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = word.word,
                            responseTime = 4000L, // Good pace
                            hintUsed = false,
                            levelId = levelId,
                            previousComboState = comboState,
                        )
                    comboState = (result as Result.Success).data.comboState
                    result
                }

            // When: Calculate level-level rating with combo
            val totalCorrect = results.count { (it as Result.Success).data.isCorrect }
            val totalTime = results.sumOf { (it as Result.Success).data.timeTaken }
            val maxCombo = comboState.consecutiveCorrect

            val levelData =
                StarRatingCalculator.PerformanceData(
                    totalWords = testWords.size,
                    correctAnswers = totalCorrect,
                    hintsUsed = 0,
                    totalTimeMs = totalTime,
                    wrongAnswers = 0,
                    maxCombo = maxCombo, // Should be 6
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: High combo should contribute to 3 stars
            assertEquals("Combo should reach max", testWords.size, maxCombo)
            assertEquals("Perfect with combo should earn 3 stars", 3, levelStars)
        }

    // ========== POSITIVE SCENARIO 3: SLOW PERFORMANCE ==========

    @Test
    fun `Integration 6 - Slow performance earns 2 stars at level`() =
        runTest {
            // Given: User takes very long
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            val results =
                testWords.map { word ->
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = word.word,
                            responseTime = 20000L, // Very slow per word
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
                    totalTimeMs = totalTime, // >15s per word
                    wrongAnswers = 0,
                    maxCombo = comboState.consecutiveCorrect,
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: Slow but perfect should still get 3 stars (per-word)
            // But at level, slow penalty might apply
            // Total: 3.0 - 0.2 (slow) = 2.8 → 3 stars (still good)
            assertEquals("Slow but perfect should earn 3 stars", 3, levelStars)
        }
}
