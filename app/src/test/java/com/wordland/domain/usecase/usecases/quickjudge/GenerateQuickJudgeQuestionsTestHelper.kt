package com.wordland.domain.usecase.usecases.quickjudge

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.GenerateQuickJudgeQuestionsUseCase
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

/**
 * Base test class for GenerateQuickJudgeQuestionsUseCase tests
 *
 * Provides shared setup, test data, and common configuration
 * for all Quick Judge question generation test subclasses.
 */
abstract class GenerateQuickJudgeQuestionsTestHelper {
    protected lateinit var wordRepository: WordRepository
    protected lateinit var useCase: GenerateQuickJudgeQuestionsUseCase

    // Test data - Look Island Level 1 words
    protected val testWords =
        listOf(
            Word(
                id = "look_001",
                word = "look",
                translation = "看",
                pronunciation = "/lʊk/",
                audioPath = null,
                partOfSpeech = "verb",
                difficulty = 1,
                frequency = 100,
                theme = "observation",
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
                id = "look_002",
                word = "see",
                translation = "看见",
                pronunciation = "/siː/",
                audioPath = null,
                partOfSpeech = "verb",
                difficulty = 1,
                frequency = 95,
                theme = "observation",
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
                id = "look_003",
                word = "watch",
                translation = "观看",
                pronunciation = "/wɒtʃ/",
                audioPath = null,
                partOfSpeech = "verb",
                difficulty = 1,
                frequency = 90,
                theme = "observation",
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
                id = "look_004",
                word = "eye",
                translation = "眼睛",
                pronunciation = "/aɪ/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 85,
                theme = "observation",
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
                id = "look_005",
                word = "glass",
                translation = "玻璃",
                pronunciation = "/ɡlɑːs/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 80,
                theme = "observation",
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
                id = "look_006",
                word = "find",
                translation = "发现",
                pronunciation = "/faɪnd/",
                audioPath = null,
                partOfSpeech = "verb",
                difficulty = 1,
                frequency = 75,
                theme = "observation",
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
        wordRepository = mockk()
        useCase = GenerateQuickJudgeQuestionsUseCase(wordRepository)
    }

    @After
    fun tearDown() {
        // No cleanup needed with mockk
    }
}
