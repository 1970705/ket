package com.wordland.domain.usecase.usecases.hint

import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.behavior.BehaviorAnalyzer
import com.wordland.domain.hint.HintGenerator
import com.wordland.domain.hint.HintManager
import com.wordland.domain.model.Word
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

/**
 * Base test class for Hint System integration tests
 *
 * Provides shared setup, test data, and common configuration
 * for all hint system integration test subclasses.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class HintSystemIntegrationTestHelper {
    protected lateinit var useHintUseCase: com.wordland.domain.usecase.usecases.UseHintUseCaseEnhanced
    protected lateinit var trackingRepository: TrackingRepository
    protected lateinit var wordRepository: WordRepository
    protected lateinit var hintGenerator: HintGenerator
    protected lateinit var hintManager: HintManager
    protected lateinit var behaviorAnalyzer: BehaviorAnalyzer

    protected val testDispatcher = StandardTestDispatcher()

    // ========== Test Data ==========

    protected val testWord =
        Word(
            id = "test_word_001",
            word = "apple",
            translation = "苹果",
            pronunciation = "/ˈæpl/",
            audioPath = null,
            partOfSpeech = "noun",
            difficulty = 3,
            frequency = 80,
            theme = "food",
            islandId = "look_island",
            levelId = "look_island_level_01",
            order = 1,
            ketLevel = true,
            petLevel = false,
            exampleSentences = null,
            relatedWords = null,
            root = null,
            prefix = null,
            suffix = null,
        )

    protected val shortWord =
        testWord.copy(
            id = "test_word_002",
            word = "cat",
            translation = "猫",
        )

    protected val longWord =
        testWord.copy(
            id = "test_word_003",
            word = "international",
            translation = "国际",
        )

    // ========== Setup and Teardown ==========

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Setup mocks with relaxed behavior for tracking
        trackingRepository = mockk(relaxed = true)
        wordRepository = mockk()

        // Create real instances of hint components (not mocks)
        hintGenerator = HintGenerator()
        hintManager = HintManager()
        behaviorAnalyzer = BehaviorAnalyzer(trackingRepository)

        // Configure for testing - disable cooldown
        hintManager.maxHintsPerWord = 3
        hintManager.hintCooldownMs = 0L // Disable cooldown for integration tests

        // Create UseCase with real hint components
        useHintUseCase =
            com.wordland.domain.usecase.usecases.UseHintUseCaseEnhanced(
                trackingRepository = trackingRepository,
                wordRepository = wordRepository,
                hintGenerator = hintGenerator,
                hintManager = hintManager,
                behaviorAnalyzer = behaviorAnalyzer,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
}
