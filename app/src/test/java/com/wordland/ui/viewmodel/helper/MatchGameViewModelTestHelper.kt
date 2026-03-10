package com.wordland.ui.viewmodel.helper

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.BubbleColor
import com.wordland.domain.model.BubbleState
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.CheckMatchUseCase
import com.wordland.domain.usecase.usecases.GetWordPairsUseCase

/**
 * Test helper class providing shared test data and utilities for MatchGameViewModel tests.
 * Reduces code duplication across split test files.
 */
object MatchGameViewModelTestHelper {
    // Test data constants
    const val TEST_LEVEL_ID = "look_level_01"
    const val TEST_ISLAND_ID = "look_island"

    // Test words
    val testWords =
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
                islandId = TEST_ISLAND_ID,
                levelId = TEST_LEVEL_ID,
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
                islandId = TEST_ISLAND_ID,
                levelId = TEST_LEVEL_ID,
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
                word = "cat",
                translation = "猫",
                pronunciation = "/kæt/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 95,
                theme = "animal",
                islandId = TEST_ISLAND_ID,
                levelId = TEST_LEVEL_ID,
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
                word = "dog",
                translation = "狗",
                pronunciation = "/dɒɡ/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 92,
                theme = "animal",
                islandId = TEST_ISLAND_ID,
                levelId = TEST_LEVEL_ID,
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
                word = "red",
                translation = "红色",
                pronunciation = "/red/",
                audioPath = null,
                partOfSpeech = "adjective",
                difficulty = 1,
                frequency = 88,
                theme = "color",
                islandId = TEST_ISLAND_ID,
                levelId = TEST_LEVEL_ID,
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
                word = "blue",
                translation = "蓝色",
                pronunciation = "/bluː/",
                audioPath = null,
                partOfSpeech = "adjective",
                difficulty = 1,
                frequency = 85,
                theme = "color",
                islandId = TEST_ISLAND_ID,
                levelId = TEST_LEVEL_ID,
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

    // Test bubbles for all 6 word pairs (12 bubbles total)
    val testBubbles =
        listOf(
            BubbleState(
                id = "pair_word_001_en",
                word = "apple",
                pairId = "pair_word_001",
                color = BubbleColor.PINK,
            ),
            BubbleState(
                id = "pair_word_001_zh",
                word = "苹果",
                pairId = "pair_word_001",
                color = BubbleColor.GREEN,
            ),
            BubbleState(
                id = "pair_word_002_en",
                word = "banana",
                pairId = "pair_word_002",
                color = BubbleColor.BLUE,
            ),
            BubbleState(
                id = "pair_word_002_zh",
                word = "香蕉",
                pairId = "pair_word_002",
                color = BubbleColor.ORANGE,
            ),
            BubbleState(
                id = "pair_word_003_en",
                word = "cat",
                pairId = "pair_word_003",
                color = BubbleColor.PINK,
            ),
            BubbleState(
                id = "pair_word_003_zh",
                word = "猫",
                pairId = "pair_word_003",
                color = BubbleColor.GREEN,
            ),
            BubbleState(
                id = "pair_word_004_en",
                word = "dog",
                pairId = "pair_word_004",
                color = BubbleColor.BLUE,
            ),
            BubbleState(
                id = "pair_word_004_zh",
                word = "狗",
                pairId = "pair_word_004",
                color = BubbleColor.ORANGE,
            ),
            BubbleState(
                id = "pair_word_005_en",
                word = "red",
                pairId = "pair_word_005",
                color = BubbleColor.PINK,
            ),
            BubbleState(
                id = "pair_word_005_zh",
                word = "红色",
                pairId = "pair_word_005",
                color = BubbleColor.GREEN,
            ),
            BubbleState(
                id = "pair_word_006_en",
                word = "blue",
                pairId = "pair_word_006",
                color = BubbleColor.BLUE,
            ),
            BubbleState(
                id = "pair_word_006_zh",
                word = "蓝色",
                pairId = "pair_word_006",
                color = BubbleColor.ORANGE,
            ),
        )

    /**
     * Creates a single pair of bubbles for simplified testing.
     */
    fun createSinglePairBubbles() =
        listOf(
            BubbleState(
                id = "pair_word_001_en",
                word = "apple",
                pairId = "pair_word_001",
                color = BubbleColor.PINK,
            ),
            BubbleState(
                id = "pair_word_001_zh",
                word = "苹果",
                pairId = "pair_word_001",
                color = BubbleColor.GREEN,
            ),
        )

    /**
     * Creates use cases with default configuration.
     */
    fun createUseCases(wordRepository: WordRepository): Pair<GetWordPairsUseCase, CheckMatchUseCase> {
        val checkMatchUseCase = CheckMatchUseCase()
        val getWordPairsUseCase = GetWordPairsUseCase(wordRepository)
        return Pair(getWordPairsUseCase, checkMatchUseCase)
    }
}
