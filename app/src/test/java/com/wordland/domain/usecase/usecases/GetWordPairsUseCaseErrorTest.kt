package com.wordland.domain.usecase.usecases

import androidx.compose.ui.unit.dp
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.MatchGameConfig
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetWordPairsUseCase error and edge case scenarios
 *
 * Tests error handling, edge cases, and bubble state initialization.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetWordPairsUseCaseErrorTest {
    private lateinit var getWordPairsUseCase: GetWordPairsUseCase
    private lateinit var wordRepository: WordRepository

    private val testDispatcher = Dispatchers.Unconfined

    // Test words
    private val testWords =
        listOf(
            Word(
                id = "word_001",
                word = "apple",
                translation = "苹果",
                pronunciation = "/ˈæpl/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 100,
                theme = "fruit",
                islandId = "look_island",
                levelId = "look_level_01",
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
                id = "word_002",
                word = "banana",
                translation = "香蕉",
                pronunciation = "/bəˈnɑːnə/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 90,
                theme = "fruit",
                islandId = "look_island",
                levelId = "look_level_01",
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
                id = "word_003",
                word = "orange",
                translation = "橙子",
                pronunciation = "/ˈɒrɪndʒ/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 80,
                theme = "fruit",
                islandId = "look_island",
                levelId = "look_level_01",
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
                id = "word_004",
                word = "grape",
                translation = "葡萄",
                pronunciation = "/ɡreɪp/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 70,
                theme = "fruit",
                islandId = "look_island",
                levelId = "look_level_01",
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
                id = "word_005",
                word = "pear",
                translation = "梨",
                pronunciation = "/peər/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 60,
                theme = "fruit",
                islandId = "look_island",
                levelId = "look_level_01",
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
                id = "word_006",
                word = "peach",
                translation = "桃子",
                pronunciation = "/piːtʃ/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 50,
                theme = "fruit",
                islandId = "look_island",
                levelId = "look_level_01",
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
        Dispatchers.setMain(testDispatcher)
        wordRepository = mockk()
        getWordPairsUseCase = GetWordPairsUseCase(wordRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Error Cases ===

    @Test
    fun `invoke returns Error when not enough words available`() =
        runTest {
            // Given
            val config =
                MatchGameConfig(
                    wordPairs = 10, // Request 10 pairs (need 10 words)
                    bubbleSize = 80.dp,
                    columns = 6,
                    enableTimer = true,
                    enableSound = true,
                    enableAnimation = true,
                    islandId = null,
                    levelId = "look_level_01",
                )
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords // Only 3 words

            // When
            val result = getWordPairsUseCase(config)

            // Then
            assertTrue(result is Result.Error)
            val error = (result as Result.Error).exception
            assertTrue(error is IllegalStateException)
            assertTrue(error.message?.contains("Not enough words") == true)
        }

    @Test
    fun `invoke returns Error when repository throws exception`() =
        runTest {
            // Given
            val config =
                MatchGameConfig(
                    wordPairs = 5,
                    bubbleSize = 80.dp,
                    columns = 4,
                    enableTimer = true,
                    enableSound = true,
                    enableAnimation = true,
                    islandId = null,
                    levelId = "look_level_01",
                )
            coEvery { wordRepository.getWordsByLevel("look_level_01") } throws RuntimeException("Database error")

            // When
            val result = getWordPairsUseCase(config)

            // Then
            assertTrue(result is Result.Error)
        }

    // === Edge Cases ===

    @Test
    fun `invoke works with exact word count requested`() =
        runTest {
            // Given - exactly 3 words for 3 pairs
            val config =
                MatchGameConfig(
                    wordPairs = 5,
                    bubbleSize = 80.dp,
                    columns = 6,
                    enableTimer = true,
                    enableSound = true,
                    enableAnimation = true,
                    islandId = null,
                    levelId = "look_level_01",
                )
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = getWordPairsUseCase(config)

            // Then
            assertTrue(result is Result.Success)
            val bubbles = (result as Result.Success).data
            assertEquals(10, bubbles.size) // 5 pairs = 10 bubbles
        }

    @Test
    fun `invoke prioritizes levelId over islandId`() =
        runTest {
            // Given - both levelId and islandId provided
            val config =
                MatchGameConfig(
                    wordPairs = 5,
                    bubbleSize = 80.dp,
                    columns = 4,
                    enableTimer = true,
                    enableSound = true,
                    enableAnimation = true,
                    islandId = "look_island",
                    levelId = "look_level_01",
                )
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords
            coEvery { wordRepository.getWordsByIsland("look_island") } returns emptyList()

            // When
            val result = getWordPairsUseCase(config)

            // Then
            assertTrue(result is Result.Success)
            // Verify that levelId was used (words were retrieved)
            coVerify(exactly = 1) { wordRepository.getWordsByLevel("look_level_01") }
            coVerify(exactly = 0) { wordRepository.getWordsByIsland(any()) }
        }

    @Test
    fun `invoke initializes bubbles with default states`() =
        runTest {
            // Given
            val config =
                MatchGameConfig(
                    wordPairs = 5,
                    bubbleSize = 80.dp,
                    columns = 4,
                    enableTimer = true,
                    enableSound = true,
                    enableAnimation = true,
                    islandId = null,
                    levelId = "look_level_01",
                )
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = getWordPairsUseCase(config)

            // Then
            assertTrue(result is Result.Success)
            val bubbles = (result as Result.Success).data

            bubbles.forEach { bubble ->
                assertFalse(bubble.isSelected)
                assertFalse(bubble.isMatched)
                assertNotNull(bubble.id)
                assertNotNull(bubble.word)
                assertNotNull(bubble.pairId)
            }
        }
}
