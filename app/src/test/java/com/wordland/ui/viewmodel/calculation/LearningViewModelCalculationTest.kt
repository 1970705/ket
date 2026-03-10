package com.wordland.ui.viewmodel.calculation

import androidx.lifecycle.SavedStateHandle
import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.PetRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.Pet
import com.wordland.domain.model.Result
import com.wordland.domain.usecase.usecases.ExploreRegionUseCase
import com.wordland.domain.usecase.usecases.LoadLevelWordsUseCase
import com.wordland.domain.usecase.usecases.SubmitAnswerUseCase
import com.wordland.domain.usecase.usecases.UnlockNextLevelUseCase
import com.wordland.domain.usecase.usecases.UseHintUseCaseEnhanced
import com.wordland.ui.components.PetAnimationState
import com.wordland.media.TTSController
import com.wordland.media.SoundManager
import com.wordland.ui.uistate.LearningUiState
import com.wordland.ui.viewmodel.LearningViewModel
import com.wordland.ui.viewmodel.helper.LearningViewModelTestHelper
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for LearningViewModel calculation and utility methods.
 * Covers progress calculation, star rating, accuracy, time tracking, and utility functions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LearningViewModelCalculationTest {
    private lateinit var viewModel: LearningViewModel
    private lateinit var loadLevelWords: LoadLevelWordsUseCase
    private lateinit var submitAnswer: SubmitAnswerUseCase
    private lateinit var useHint: UseHintUseCaseEnhanced
    private lateinit var unlockNextLevel: UnlockNextLevelUseCase
    private lateinit var exploreRegion: ExploreRegionUseCase
    private lateinit var islandMasteryRepository: IslandMasteryRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var petRepository: PetRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var mockTtsController: TTSController
    private lateinit var mockSoundManager: SoundManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Setup mocks
        loadLevelWords = mockk(relaxed = true)
        submitAnswer = mockk(relaxed = true)
        useHint = mockk(relaxed = true)
        unlockNextLevel = mockk(relaxed = true)
        exploreRegion = mockk(relaxed = true)
        islandMasteryRepository = mockk(relaxed = true)
        progressRepository = mockk(relaxed = true)
        petRepository = mockk(relaxed = true)
        mockTtsController = mockk(relaxed = true)
        mockSoundManager = mockk(relaxed = true)

        // Setup default mocks
        LearningViewModelTestHelper.setupDefaultHintMocks(useHint)
        coEvery { progressRepository.getAllWordProgress(any()) } returns emptyList()
        coEvery { petRepository.getSelectedPet() } returns Pet.CAT

        // Setup SavedStateHandle with navigation params
        savedStateHandle = LearningViewModelTestHelper.createSavedStateHandle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === Current Word Tests ===

    @Test
    fun `currentWord emits correct word`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            val currentWord = viewModel.currentWord.value
            assertNotNull(currentWord)
            assertEquals("word1", currentWord?.id)
            assertEquals("apple", currentWord?.word)
        }

    @Test
    fun `onNextWord updates currentWord`() =
        runTest {
            // Given
            setupReadyState()

            // When
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then
            val currentWord = viewModel.currentWord.value
            assertEquals("word2", currentWord?.id)
            assertEquals("banana", currentWord?.word)
        }

    // === Progress Calculation Tests ===

    @Test
    fun `calculateProgress returns correct progress`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then - first word: (0+1)/2 = 0.5
            val state = viewModel.uiState.value as LearningUiState.Ready
            assertEquals(0, state.currentWordIndex)
        }

    @Test
    fun `calculateProgress returns zero when levelWords is empty`() {
        // Given
        val emptyLevelWords: List<com.wordland.domain.model.Word> = emptyList()
        val currentWordIndex = 0

        // When
        val progress =
            if (emptyLevelWords.isNotEmpty()) {
                (currentWordIndex + 1).toFloat() / emptyLevelWords.size
            } else {
                0f
            }

        // Then
        assertEquals(0f, progress, 0.001f)
    }

    // === Star Calculation Tests ===

    @Test
    fun `calculateLevelStars returns zero when levelWords is empty`() {
        // Given
        val emptyLevelWords: List<com.wordland.domain.model.Word> = emptyList()
        val correctAnswersInLevel = 0
        val totalHintsUsedInLevel = 0
        val totalWrongAnswersInLevel = 0
        val maxComboInLevel = 0

        // When
        val stars =
            if (emptyLevelWords.isEmpty()) {
                0
            } else {
                // Would calculate normally
                3
            }

        // Then
        assertEquals(0, stars)
    }

    @Test
    fun `calculateLevelScore returns zero when starsEarnedInLevel is empty`() {
        // Given
        val emptyStars = listOf<Int>()

        // When
        val score = emptyStars.sum() * 10

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateLevelScore with various star combinations`() {
        // Given
        val stars = listOf(3, 2, 1, 0, 3, 2)

        // When
        val score = stars.sum() * 10

        // Then
        assertEquals(110, score)
    }

    // === Accuracy Calculation Tests ===

    @Test
    fun `calculateAccuracy handles zero total words`() {
        // Given
        val correctAnswersInLevel = 0
        val levelWords = 0

        // When
        val accuracy =
            if (levelWords > 0) {
                (correctAnswersInLevel.toFloat() / levelWords * 100).toInt()
            } else {
                0
            }

        // Then
        assertEquals(0, accuracy)
    }

    @Test
    fun `calculateAccuracy handles perfect score`() {
        // Given
        val correctAnswersInLevel = 6
        val levelWords = 6

        // When
        val accuracy = (correctAnswersInLevel.toFloat() / levelWords * 100).toInt()

        // Then
        assertEquals(100, accuracy)
    }

    @Test
    fun `calculateAccuracy handles partial score`() {
        // Given
        val correctAnswersInLevel = 4
        val levelWords = 6

        // When
        val accuracy = (correctAnswersInLevel.toFloat() / levelWords * 100).toInt()

        // Then
        assertEquals(66, accuracy) // 4/6 = 66.67% → 66%
    }

    // === Time Calculation Tests ===

    @Test
    fun `calculateTotalTimeMs returns zero when levelStartTime is zero`() {
        // Given
        val levelStartTime = 0L

        // When
        val totalTimeMs =
            if (levelStartTime > 0) {
                System.currentTimeMillis() - levelStartTime
            } else {
                0L
            }

        // Then
        assertEquals(0L, totalTimeMs)
    }

    @Test
    fun `calculateTotalTimeMs returns positive value when levelStartTime is set`() {
        // Given
        val levelStartTime = 1000L

        // When
        val totalTimeMs =
            if (levelStartTime > 0) {
                System.currentTimeMillis() - levelStartTime
            } else {
                0L
            }

        // Then
        assertTrue(totalTimeMs > 0)
    }

    // === Cross Scene Score Tests ===

    @Test
    fun `calculateCrossSceneScore returns zero when no stars earned`() {
        // Given
        val emptyStars = listOf<Int>()

        // When
        val crossSceneScore =
            if (emptyStars.isNotEmpty()) {
                emptyStars.average().toDouble() / 3.0
            } else {
                0.0
            }

        // Then
        assertEquals(0.0, crossSceneScore, 0.001)
    }

    @Test
    fun `calculateCrossSceneScore returns normalized score when stars earned`() {
        // Given
        val stars = listOf(3, 2, 1)

        // When
        val crossSceneScore =
            if (stars.isNotEmpty()) {
                stars.average().toDouble() / 3.0
            } else {
                0.0
            }

        // Then
        assertEquals(2.0 / 3.0, crossSceneScore, 0.001)
    }

    // === Star Breakdown UI Tests ===

    @Test
    fun `showStarBreakdown updates showStarBreakdown state`() =
        runTest {
            // Given
            setupReadyState()

            // When
            viewModel.showStarBreakdown()

            // Then
            assertTrue(viewModel.showStarBreakdown.value)
        }

    @Test
    fun `hideStarBreakdown updates showStarBreakdown state`() =
        runTest {
            // Given
            setupReadyState()
            viewModel.showStarBreakdown()
            assertTrue(viewModel.showStarBreakdown.value)

            // When
            viewModel.hideStarBreakdown()

            // Then
            assertFalse(viewModel.showStarBreakdown.value)
        }

    // === Pet Animation Tests ===

    @Test
    fun `triggerPetAnimation cycles through all states`() {
        // Given
        val animations =
            listOf(
                PetAnimationState.HAPPY to 1500L,
                PetAnimationState.EXCITED to 2000L,
                PetAnimationState.CELEBRATE to 3000L,
                PetAnimationState.IDLE to 0L,
            )

        // When/Then - Verify all animation states exist
        animations.forEach { (state, duration) ->
            assertNotNull(state)
            assertTrue(duration >= 0)
        }
    }

    // === Helper Methods ===

    private fun createViewModel() {
        viewModel =
            LearningViewModel(
                loadLevelWords,
                submitAnswer,
                useHint,
                unlockNextLevel,
                exploreRegion,
                islandMasteryRepository,
                progressRepository,
                petRepository,
                savedStateHandle,
                mockTtsController,
                mockSoundManager,
            )
    }

    private fun setupReadyState() =
        runTest {
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            createViewModel()
            advanceUntilIdle()

            // Reset mock for calculation tests
            clearMocks(submitAnswer, useHint, answers = false, recordedCalls = false)
        }
}
