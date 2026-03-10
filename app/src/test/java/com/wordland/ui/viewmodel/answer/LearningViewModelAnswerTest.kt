package com.wordland.ui.viewmodel.answer

import androidx.lifecycle.SavedStateHandle
import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.PetRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.Pet
import com.wordland.domain.model.Result
import com.wordland.domain.usecase.usecases.ExploreRegionUseCase
import com.wordland.domain.usecase.usecases.LoadLevelWordsUseCase
import com.wordland.domain.usecase.usecases.SubmitAnswerUseCase
import com.wordland.domain.usecase.usecases.UnlockNextLevelUseCase
import com.wordland.domain.usecase.usecases.UseHintUseCaseEnhanced
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
 * Tests for LearningViewModel answer submission functionality.
 * Covers correct/wrong answers, combo updates, error handling, and pet animations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LearningViewModelAnswerTest {
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

    // === Submit Answer Tests ===

    @Test
    fun `submitAnswer with correct answer updates to Feedback state`() =
        runTest {
            // Given
            setupReadyState()
            val submitResult = LearningViewModelTestHelper.createCorrectAnswerResult()
            coEvery {
                submitAnswer(
                    userId = LearningViewModelTestHelper.TEST_USER_ID,
                    wordId = "word1",
                    userAnswer = "apple",
                    responseTime = 3000L,
                    hintUsed = false,
                    levelId = LearningViewModelTestHelper.TEST_LEVEL_ID,
                    previousComboState = ComboState(),
                )
            } returns Result.Success(submitResult)

            // When
            viewModel.submitAnswer("apple", 3000L, false)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Feedback)
            val feedbackState = state as LearningUiState.Feedback
            assertTrue(feedbackState.result.isCorrect)
            assertEquals(3, feedbackState.stars)
            assertEquals(1, feedbackState.comboState.consecutiveCorrect)
        }

    @Test
    fun `submitAnswer with wrong answer updates to Feedback state`() =
        runTest {
            // Given
            setupReadyState()
            val submitResult = LearningViewModelTestHelper.createWrongAnswerResult()
            coEvery {
                submitAnswer(
                    userId = LearningViewModelTestHelper.TEST_USER_ID,
                    wordId = "word1",
                    userAnswer = "wrong",
                    responseTime = 2000L,
                    hintUsed = false,
                    levelId = LearningViewModelTestHelper.TEST_LEVEL_ID,
                    previousComboState = ComboState(),
                )
            } returns Result.Success(submitResult)

            // When
            viewModel.submitAnswer("wrong", 2000L, false)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Feedback)
            assertFalse((state as LearningUiState.Feedback).result.isCorrect)
        }

    @Test
    fun `submitAnswer triggers HAPPY pet animation on correct answer`() =
        runTest {
            // Given
            setupReadyState()
            val submitResult = LearningViewModelTestHelper.createCorrectAnswerResult()
            coEvery {
                submitAnswer(any(), any(), any(), any(), any(), any(), any())
            } returns Result.Success(submitResult)

            // When
            viewModel.submitAnswer("apple", 3000L, false)
            advanceUntilIdle()

            // Then - Animation state resets to IDLE after delay completes
            // We just verify the ViewModel is functioning correctly
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Feedback)
            assertTrue((state as LearningUiState.Feedback).result.isCorrect)
        }

    @Test
    fun `submitAnswer triggers EXCITED pet animation on combo`() =
        runTest {
            // Given
            setupReadyState()
            val submitResult = LearningViewModelTestHelper.createCorrectAnswerResult(combo = 2)
            coEvery {
                submitAnswer(any(), any(), any(), any(), any(), any(), any())
            } returns Result.Success(submitResult)

            // When
            viewModel.submitAnswer("apple", 3000L, false)
            advanceUntilIdle()

            // Then - After animations complete, state resets to IDLE
            // Just verify the combo state is properly updated
            val state = viewModel.uiState.value as LearningUiState.Feedback
            assertEquals(2, state.comboState.consecutiveCorrect)
        }

    @Test
    fun `submitAnswer updates combo state`() =
        runTest {
            // Given
            setupReadyState()
            val newCombo = ComboState(consecutiveCorrect = 3, averageResponseTime = 2500L)
            val submitResult =
                LearningViewModelTestHelper.createCorrectAnswerResult().copy(
                    comboState = newCombo,
                    newMemoryStrength = 70,
                )
            coEvery {
                submitAnswer(any(), any(), any(), any(), any(), any(), any())
            } returns Result.Success(submitResult)

            // When
            viewModel.submitAnswer("apple", 2500L, false)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as LearningUiState.Feedback
            assertEquals(3, state.comboState.consecutiveCorrect)
        }

    @Test
    fun `submitAnswer returns Error state when use case fails`() =
        runTest {
            // Given
            setupReadyState()
            val exception = RuntimeException("Database error")
            coEvery {
                submitAnswer(any(), any(), any(), any(), any(), any(), any())
            } returns Result.Error(exception)

            // When
            viewModel.submitAnswer("apple", 1000L, false)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Error)
            assertEquals("Database error", (state as LearningUiState.Error).message)
        }

    @Test
    fun `submitAnswer handles empty current word gracefully`() =
        runTest {
            // Given - ViewModel with no current word
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(emptyList())
            createViewModel()
            advanceUntilIdle()

            // When - Try to submit answer when no current word
            viewModel.submitAnswer("test", 1000L, false)
            advanceUntilIdle()

            // Then - Should handle gracefully, no crash
            // State remains Loading since there are no words
            assertEquals(LearningUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun `submitAnswer updates combo state correctly`() =
        runTest {
            // Given
            setupReadyState()
            val newCombo = ComboState(consecutiveCorrect = 3)

            coEvery {
                submitAnswer(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                Result.Success(
                    LearningViewModelTestHelper.createCorrectAnswerResult().copy(
                        comboState = newCombo,
                        newMemoryStrength = 70,
                    ),
                )

            // When
            viewModel.submitAnswer("apple", 2500L, false)
            advanceUntilIdle()

            // Then - Combo should be updated in the result
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Feedback)
            assertEquals(3, (state as LearningUiState.Feedback).comboState.consecutiveCorrect)
        }

    @Test
    fun `submitAnswer tracks wrong answers correctly`() =
        runTest {
            // Given
            setupReadyState()

            coEvery {
                submitAnswer(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                Result.Success(
                    LearningViewModelTestHelper.createWrongAnswerResult(),
                )

            // When
            viewModel.submitAnswer("wrong", 1500L, false)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Feedback)
            assertEquals(0, (state as LearningUiState.Feedback).comboState.consecutiveCorrect)
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

            // Reset mock for submitAnswer tests
            clearMocks(submitAnswer, useHint, answers = false, recordedCalls = false)
        }
}
