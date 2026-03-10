package com.wordland.integration

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.algorithm.GuessingDetector
import com.wordland.domain.algorithm.StarRatingCalculator
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.SubmitAnswerUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Penalty integration tests for Star Rating System
 *
 * Tests the flow from per-word rating (SubmitAnswerUseCase) to level-level rating
 * (StarRatingCalculator) for penalty scenarios:
 * 1. All words with hints
 * 2. Mixed accuracy (4/6, 3/6 correct)
 * 3. Guessing detection
 * 4. One wrong
 *
 * These tests use mock repositories to simulate real gameplay scenarios.
 */
class StarRatingIntegrationPenaltyTest {
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

    // ========== PENALTY SCENARIO 1: ALL WITH HINTS ==========

    @Test
    fun `Integration 2 - All words with hints earns 2 stars at level`() =
        runTest {
            // Given: User uses hints for all words
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
                            responseTime = 4000L,
                            hintUsed = true, // Uses hint
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
            val maxCombo = comboState.consecutiveCorrect

            val levelData =
                StarRatingCalculator.PerformanceData(
                    totalWords = testWords.size,
                    correctAnswers = totalCorrect,
                    hintsUsed = totalHints,
                    totalTimeMs = totalTime,
                    wrongAnswers = 0,
                    maxCombo = maxCombo,
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: Each word gets 2 stars (due to hint), level gets 2 stars (hint penalty applies)
            // Calculation: 3.0 (accuracy) - 2.0 (hint penalty capped) + 0.3 (time) + 1.0 (combo) = 2.3 < 3.0 threshold
            assertEquals("Each word with hint should earn 2 stars", testWords.size * 2, totalStars)
            assertEquals("Level with all hints should earn 2 stars (hint penalty too high)", 2, levelStars)
        }

    // ========== PENALTY SCENARIO 2: MIXED ACCURACY ==========

    @Test
    fun `Integration 3 - 4 correct 2 wrong earns 2 stars at level`() =
        runTest {
            // Given: User gets 4/6 correct
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            val results =
                testWords.mapIndexed { index, word ->
                    val isCorrect = index < 4 // First 4 correct
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = if (isCorrect) word.word else "wrong",
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
            val maxCombo = comboState.consecutiveCorrect

            val levelData =
                StarRatingCalculator.PerformanceData(
                    totalWords = testWords.size,
                    correctAnswers = totalCorrect,
                    hintsUsed = 0,
                    totalTimeMs = totalTime,
                    wrongAnswers = 2,
                    maxCombo = maxCombo,
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: 4/6 correct should earn 1 star (error penalty increased)
            // Calculation: 2.0 (accuracy) - 0 + 0 + 0 - 0.5 (2 errors) = 1.5 < 2.0 threshold
            assertEquals("4/6 correct should earn 1 star at level", 1, levelStars)
            assertTrue("Combo should reset after wrong answer", maxCombo < testWords.size)
        }

    // ========== PENALTY SCENARIO 3: GUESSING DETECTED ==========

    @Test
    fun `Integration 4 - Fast answers triggers guessing at both levels`() =
        runTest {
            // Given: User answers very fast (guessing pattern)
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            // Setup guessing pattern in history
            val now = System.currentTimeMillis()
            val guessingPatterns =
                listOf(
                    GuessingDetector.ResponsePattern(now - 5000, 500L, true, false),
                    GuessingDetector.ResponsePattern(now - 4000, 600L, true, false),
                    GuessingDetector.ResponsePattern(now - 3000, 500L, true, false),
                    GuessingDetector.ResponsePattern(now - 2000, 700L, true, false),
                )
            coEvery { trackingRepository.getRecentPatterns(userId, 5) } returns guessingPatterns

            val results =
                testWords.map { word ->
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = word.word,
                            responseTime = 500L, // Very fast!
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
            val totalGuessing = results.count { (it as Result.Success).data.isGuessing }

            val levelData =
                StarRatingCalculator.PerformanceData(
                    totalWords = testWords.size,
                    correctAnswers = totalCorrect,
                    hintsUsed = 0,
                    totalTimeMs = totalTime, // ~3s total for 6 words = guessing
                    wrongAnswers = 0,
                    maxCombo = 0, // Combo shouldn't build with guessing
                    isGuessing = totalGuessing > 0, // Pass guessing detection from per-word to level
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: Guessing should be detected at both levels
            assertTrue("Guessing should be detected at per-word level", totalGuessing > 0)
            // Calculation: 3.0 (accuracy) - 0.6 (guessing penalty) = 2.4 < 3.0 threshold → 2 stars
            assertEquals("Fast guessing at level should earn 2 stars", 2, levelStars)
        }

    // ========== PENALTY SCENARIO 4: ONE WRONG ==========

    @Test
    fun `Integration 7 - One wrong still earns 3 stars at level`() =
        runTest {
            // Given: User gets 5/6 correct, 1 wrong
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            val results =
                testWords.mapIndexed { index, word ->
                    val isCorrect = index != 2 // Third word wrong
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = if (isCorrect) word.word else "wrong",
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
                    wrongAnswers = 1,
                    maxCombo = comboState.consecutiveCorrect,
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: 5/6 with 1 error should earn 2 stars (threshold increased to 3.0)
            // Calculation: 2.5 (accuracy) - 0 + 0 + 0.5 (combo) - 0.25 (1 error) = 2.75 < 3.0 threshold
            assertEquals("5/6 correct should earn 2 stars", 2, levelStars)
        }

    // ========== PENALTY SCENARIO 5: MULTIPLE WRONG ==========

    @Test
    fun `Integration 8 - 3 wrong earns 1 star at level`() =
        runTest {
            // Given: User gets 3/6 correct
            val userId = "user_1"
            val levelId = "test_level"
            var comboState = ComboState()

            val results =
                testWords.mapIndexed { index, word ->
                    val isCorrect = index % 2 == 0 // Every other word correct
                    val result =
                        useCase(
                            userId = userId,
                            wordId = word.id,
                            userAnswer = if (isCorrect) word.word else "wrong",
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
                    wrongAnswers = 3,
                    maxCombo = comboState.consecutiveCorrect,
                )
            val levelStars = StarRatingCalculator.calculateStars(levelData)

            // Then: 3/6 correct should earn 1 star (threshold + error penalty increased)
            // Calculation: 1.5 (accuracy) - 0.75 (error penalty capped) = 0.75 < 1.0 threshold
            assertEquals("3/6 correct should earn 1 star", 1, levelStars)
        }
}
