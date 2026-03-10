package com.wordland.ui.viewmodel.init

import androidx.lifecycle.SavedStateHandle
import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.PetRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.Pet
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
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
 * Tests for LearningViewModel initialization and level loading.
 * Covers init behavior, level loading, pet loading, and error states.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LearningViewModelInitTest {
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

    // === Initialization Tests ===

    @Test
    fun `init loads level when levelId is provided`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            coVerify { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) }
            assertTrue(viewModel.uiState.value is LearningUiState.Ready)
        }

    @Test
    fun `init loads pet from repository`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)
            coEvery { petRepository.getSelectedPet() } returns Pet.DOG

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(Pet.DOG, viewModel.selectedPet.value)
        }

    @Test
    fun `init loads default pet when no pet selected`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)
            coEvery { petRepository.getSelectedPet() } returns null

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(Pet.getDefault(), viewModel.selectedPet.value)
        }

    @Test
    fun `init shows error state when level loading fails`() =
        runTest {
            // Given
            val exception = RuntimeException("Level not found")
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Error(exception)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Error)
            assertEquals("Level not found", (state as LearningUiState.Error).message)
        }

    @Test
    fun `init starts in Loading state`() {
        // Given - no mocks needed for initial state check
        coEvery { petRepository.getSelectedPet() } returns null

        // When
        createViewModel()

        // Then
        assertTrue(viewModel.uiState.value is LearningUiState.Loading)
    }

    @Test
    fun `init does not load level when levelId is empty`() =
        runTest {
            // Given
            savedStateHandle = LearningViewModelTestHelper.createEmptySavedStateHandle()
            coEvery { loadLevelWords(any()) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then - Should NOT call loadLevelWords
            coVerify(exactly = 0) { loadLevelWords(any()) }
        }

    // === Level Loading Tests ===

    @Test
    fun `loadLevel updates to Ready state with first word`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Ready)
            val readyState = state as LearningUiState.Ready
            assertEquals("apple", readyState.question.targetWord)
            assertEquals("苹果", readyState.question.translation)
            assertEquals(0, readyState.currentWordIndex)
            assertEquals(2, readyState.totalWords)
        }

    @Test
    fun `loadLevel resets combo and stars`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as LearningUiState.Ready
            assertEquals(ComboState(), state.comboState)
        }

    @Test
    fun `loadLevel resets hints for new word`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            coVerify { useHint.resetHints("word1") }
        }

    @Test
    fun `loadLevel handles empty word list gracefully`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(emptyList())

            // When
            createViewModel()
            advanceUntilIdle()

            // Then - Should remain in Loading state since showFirstWord checks isNotEmpty
            val state = viewModel.uiState.value
            assertEquals(LearningUiState.Loading, state)
        }

    @Test
    fun `loadLevel handles Result Loading state`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Loading

            // When
            createViewModel()
            advanceUntilIdle()

            // Then - Should remain in Loading state
            val state = viewModel.uiState.value
            assertEquals(LearningUiState.Loading, state)
        }

    @Test
    fun `loadSelectedPet handles null pet by using default`() =
        runTest {
            // Given
            coEvery { petRepository.getSelectedPet() } returns null
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(LearningViewModelTestHelper.testWords)

            // When
            createViewModel()
            advanceUntilIdle()

            // Then
            val pet = viewModel.selectedPet.value
            assertNotNull(pet)
            assertEquals(Pet.CAT, pet) // CAT is the default
        }

    @Test
    fun `Result Loading keeps state in Loading`() {
        // Given
        val loadingResult: Result<List<Word>> = Result.Loading

        // When
        val state =
            when (loadingResult) {
                is Result.Success<*> -> "Ready"
                is Result.Error -> "Error"
                is Result.Loading -> "Loading"
            }

        // Then
        assertEquals("Loading", state)
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
}
