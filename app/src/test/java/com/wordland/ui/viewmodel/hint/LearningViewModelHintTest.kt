package com.wordland.ui.viewmodel.hint

import androidx.lifecycle.SavedStateHandle
import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.PetRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.Pet
import com.wordland.domain.model.Result
import com.wordland.domain.usecase.usecases.ExploreRegionUseCase
import com.wordland.domain.usecase.usecases.HintLimitException
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
 * Tests for LearningViewModel hint functionality.
 * Covers hint usage, progressive hints, hint limits, and error handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LearningViewModelHintTest {
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

    // === Hint Tests ===

    @Test
    fun `useHint updates Ready state with hint information`() =
        runTest {
            // Given
            setupReadyState()
            val hintResult = LearningViewModelTestHelper.createHintResult()
            coEvery {
                useHint(
                    LearningViewModelTestHelper.TEST_USER_ID, "word1",
                    LearningViewModelTestHelper.TEST_LEVEL_ID,
                )
            } returns Result.Success(hintResult)

            // When
            viewModel.useHint()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Ready)
            val readyState = state as LearningUiState.Ready
            assertTrue(readyState.hintShown)
            assertEquals("首字母: A", readyState.hintText)
            assertEquals(1, readyState.hintLevel)
            assertEquals(2, readyState.hintsRemaining)
            assertTrue(readyState.hintPenaltyApplied)
        }

    @Test
    fun `useHint sets hintAvailable to false on error`() =
        runTest {
            // Given
            setupReadyState()
            coEvery {
                useHint(
                    LearningViewModelTestHelper.TEST_USER_ID, "word1",
                    LearningViewModelTestHelper.TEST_LEVEL_ID,
                )
            } returns
                Result.Error(
                    HintLimitException("已达到提示次数上限"),
                )

            // When
            viewModel.useHint()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Ready)
            val readyState = state as LearningUiState.Ready
            assertFalse(readyState.hintAvailable)
            assertEquals("已达到提示次数上限", readyState.hintText)
        }

    @Test
    fun `useHint handles progressive hints`() =
        runTest {
            // Given
            setupReadyState()
            val hintResult1 =
                LearningViewModelTestHelper.createHintResult(
                    level = 2,
                    text = "前半部分: ap___",
                )
            val hintResult2 =
                LearningViewModelTestHelper.createHintResult(
                    level = 3,
                    text = "完整单词（元音隐藏）: _ppl_",
                )
            coEvery {
                useHint(
                    LearningViewModelTestHelper.TEST_USER_ID, "word1",
                    LearningViewModelTestHelper.TEST_LEVEL_ID,
                )
            } returnsMany listOf(Result.Success(hintResult1), Result.Success(hintResult2))

            // When - Use first hint
            viewModel.useHint()
            advanceUntilIdle()

            // Then - Check level 2 hint state
            var state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Ready)
            var readyState = state as LearningUiState.Ready
            assertEquals(2, readyState.hintLevel)
            assertEquals(1, readyState.hintsRemaining)

            // When - Use second hint
            viewModel.useHint()
            advanceUntilIdle()

            // Then - Check level 3 hint state
            state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Ready)
            readyState = state as LearningUiState.Ready
            assertEquals(3, readyState.hintLevel)
            assertEquals(0, readyState.hintsRemaining)
        }

    @Test
    fun `useHint returns early when current word is null`() =
        runTest {
            // Given - ViewModel with no current word
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(emptyList())
            createViewModel()
            advanceUntilIdle()

            // When - Try to use hint when no current word
            viewModel.useHint()
            advanceUntilIdle()

            // Then - Should handle gracefully, no crash
            assertEquals(LearningUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun `useHint updates totalHintsUsedInLevel`() =
        runTest {
            // Given
            setupReadyState()

            // First hint (level 1)
            coEvery {
                useHint(any(), any(), any())
            } returns
                com.wordland.domain.model.Result.Success(
                    LearningViewModelTestHelper.createHintResult(),
                )

            // When
            viewModel.useHint()
            advanceUntilIdle()

            // Then - totalHintsUsedInLevel should be incremented in useHint function
            // We can't directly verify this private variable, but the penalty will be applied
            val state = viewModel.uiState.value as LearningUiState.Ready
            assertTrue(state.hintPenaltyApplied)
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

            // Reset mock for hint tests
            clearMocks(submitAnswer, useHint, answers = false, recordedCalls = false)
        }
}
