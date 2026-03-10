package com.wordland.domain.usecase.usecases

import androidx.compose.ui.unit.dp
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.BubbleColor
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
 * Unit tests for GetWordPairsUseCase success scenarios
 *
 * Tests word pair retrieval, bubble creation, and proper
 * bubble configuration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetWordPairsUseCaseSuccessTest {
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

    @Test
    fun `invoke returns Success with correct bubble count for levelId`() =
        runTest {
            // Given
            val config =
                MatchGameConfig(
                    wordPairs = 5, // Must be between 5-50
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
            assertEquals(10, bubbles.size) // 5 pairs = 10 bubbles
        }

    @Test
    fun `invoke returns Success with correct bubble count for islandId`() =
        runTest {
            // Given
            val config =
                MatchGameConfig(
                    wordPairs = 5,
                    bubbleSize = 80.dp,
                    columns = 6,
                    enableTimer = true,
                    enableSound = true,
                    enableAnimation = true,
                    islandId = "look_island",
                    levelId = null,
                )
            coEvery { wordRepository.getWordsByIsland("look_island") } returns testWords

            // When
            val result = getWordPairsUseCase(config)

            // Then
            assertTrue(result is Result.Success)
            val bubbles = (result as Result.Success).data
            assertEquals(10, bubbles.size) // 5 pairs = 10 bubbles
        }

    @Test
    fun `invoke returns Success with random words when no levelId or islandId`() =
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
                    levelId = null,
                )
            coEvery { wordRepository.getRandomKETWords(Int.MAX_VALUE) } returns testWords

            // When
            val result = getWordPairsUseCase(config)

            // Then
            assertTrue(result is Result.Success)
            val bubbles = (result as Result.Success).data
            assertEquals(10, bubbles.size) // 5 pairs = 10 bubbles
        }

    @Test
    fun `invoke creates bubbles with correct pairId structure`() =
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
            assertTrue(bubbles.size >= 2)

            // Check pairId structure
            val bubbleEn = bubbles.find { it.word == "apple" }
            val bubbleZh = bubbles.find { it.word == "苹果" }

            assertNotNull(bubbleEn)
            assertNotNull(bubbleZh)
            assertEquals(bubbleEn?.pairId, bubbleZh?.pairId)
            assertTrue(bubbleEn?.pairId?.startsWith("pair_") == true)
        }

    @Test
    fun `invoke creates bubbles with English and Chinese content`() =
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

            // Each pair should have 2 bubbles (English and Chinese)
            // Get all unique pairIds
            val uniquePairIds = bubbles.map { it.pairId }.distinct()
            assertEquals(5, uniquePairIds.size) // 5 pairs

            // For each pair, verify there are 2 bubbles
            uniquePairIds.forEach { pairId ->
                val pairBubbles = bubbles.filter { it.pairId == pairId }
                assertEquals(2, pairBubbles.size)
                val words = pairBubbles.map { it.word }
                // One should be English, one should be Chinese
                assertTrue(words.any { it.all { char -> char.code in 97..122 || char.code in 65..90 } }) // Has English
                assertTrue(words.any { it.any { char -> char.code > 127 } }) // Has non-ASCII (Chinese)
            }
        }

    @Test
    fun `invoke assigns unique IDs to each bubble`() =
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

            val ids = bubbles.map { it.id }
            assertEquals(ids.size, ids.distinct().size)
        }

    @Test
    fun `invoke creates bubbles with valid colors`() =
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
                assertNotNull(bubble.color)
                assertTrue(BubbleColor.entries.contains(bubble.color))
            }
        }
}
