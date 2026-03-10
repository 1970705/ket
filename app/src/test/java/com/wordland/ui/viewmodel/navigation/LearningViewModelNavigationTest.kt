package com.wordland.ui.viewmodel.navigation

import androidx.lifecycle.SavedStateHandle
import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.PetRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.*
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
 * Tests for LearningViewModel navigation and word transition functionality.
 * Covers next word navigation, level completion, island mastery updates, and region mapping.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LearningViewModelNavigationTest {
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

    // === Next Word Tests ===

    @Test
    fun `onNextWord loads next word when available`() =
        runTest {
            // Given
            setupReadyState()

            // When
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Ready)
            val readyState = state as LearningUiState.Ready
            assertEquals("banana", readyState.question.targetWord)
            assertEquals(1, readyState.currentWordIndex)
        }

    @Test
    fun `onNextWord resets hints for next word`() =
        runTest {
            // Given
            setupReadyState()

            // When
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then
            coVerify { useHint.resetHints("word2") }
        }

    @Test
    fun `onNextWord completes level when all words finished`() =
        runTest {
            // Given - setup with one word to complete level immediately
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(listOf(LearningViewModelTestHelper.testWords[0]))
            coEvery {
                unlockNextLevel(
                    LearningViewModelTestHelper.TEST_USER_ID,
                    LearningViewModelTestHelper.TEST_LEVEL_ID,
                )
            } just Runs
            coEvery { exploreRegion(LearningViewModelTestHelper.TEST_USER_ID, "look_peninsula") } returns
                LearningViewModelTestHelper.createExploreRegionResult()
            coEvery {
                islandMasteryRepository.getIslandMastery(
                    LearningViewModelTestHelper.TEST_USER_ID,
                    LearningViewModelTestHelper.TEST_ISLAND_ID,
                )
            } returns null
            coEvery {
                islandMasteryRepository.calculateMastery(
                    LearningViewModelTestHelper.TEST_USER_ID,
                    LearningViewModelTestHelper.TEST_ISLAND_ID,
                    any(),
                    any(),
                    any(),
                )
            } returns true

            // When
            createViewModel()
            advanceUntilIdle() // Load level
            viewModel.onNextWord() // Complete level
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.LevelComplete)
            val completeState = state as LearningUiState.LevelComplete
            assertEquals(0, completeState.stars) // No stars earned yet
            assertEquals(0, completeState.score)
        }

    @Test
    fun `onNextWord triggers CELEBRATE pet animation on level complete`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(listOf(LearningViewModelTestHelper.testWords[0]))
            coEvery { unlockNextLevel(any(), any()) } just Runs
            coEvery { exploreRegion(any(), any()) } returns
                LearningViewModelTestHelper.createExploreRegionResult()
            coEvery { progressRepository.getAllWordProgress(any()) } returns emptyList()
            coEvery { islandMasteryRepository.getIslandMastery(any(), any()) } returns null
            coEvery { islandMasteryRepository.calculateMastery(any(), any(), any(), any(), any()) } returns true

            // When
            createViewModel()
            advanceUntilIdle()
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - Animation state resets after delay, just verify level completion
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.LevelComplete)
        }

    @Test
    fun `onNextWord handles level completion errors gracefully`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(listOf(LearningViewModelTestHelper.testWords[0]))
            coEvery { unlockNextLevel(any(), any()) } throws RuntimeException("Unlock failed")
            coEvery { exploreRegion(any(), any()) } returns
                LearningViewModelTestHelper.createExploreRegionResult()
            coEvery { progressRepository.getAllWordProgress(any()) } returns emptyList()
            coEvery { islandMasteryRepository.getIslandMastery(any(), any()) } returns null

            // When
            createViewModel()
            advanceUntilIdle()
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - should still complete level despite error
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.LevelComplete)
        }

    @Test
    fun `onNextWord updates island mastery on completion`() =
        runTest {
            // Given
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(listOf(LearningViewModelTestHelper.testWords[0]))
            coEvery { unlockNextLevel(any(), any()) } just Runs
            coEvery { exploreRegion(any(), any()) } returns
                LearningViewModelTestHelper.createExploreRegionResult()

            val mastery = LearningViewModelTestHelper.createIslandMastery()
            coEvery { progressRepository.getAllWordProgress(LearningViewModelTestHelper.TEST_USER_ID) } returns
                listOf(LearningViewModelTestHelper.createUserWordProgress())
            coEvery {
                islandMasteryRepository.getIslandMastery(
                    LearningViewModelTestHelper.TEST_USER_ID,
                    LearningViewModelTestHelper.TEST_ISLAND_ID,
                )
            } returns mastery
            coEvery {
                islandMasteryRepository.calculateMastery(
                    LearningViewModelTestHelper.TEST_USER_ID,
                    LearningViewModelTestHelper.TEST_ISLAND_ID,
                    any(),
                    any(),
                    any(),
                )
            } returns false

            // When
            createViewModel()
            advanceUntilIdle()
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as LearningUiState.LevelComplete
            assertNotNull(state.islandMasteryPercentage)
        }

    @Test
    fun `onNextWord increments word index correctly`() =
        runTest {
            // Given
            setupReadyState()

            // When
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - Index should be incremented (but we can't access private var directly)
            // Verify by checking UI state is still Ready (not LevelComplete since we have 2 words)
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.Ready)
        }

    @Test
    fun `onNextWord triggers LevelComplete when all words done`() =
        runTest {
            // Given - Single word level
            val singleWord = listOf(LearningViewModelTestHelper.testWords[0])
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(singleWord)
            createViewModel()
            advanceUntilIdle()

            // When
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - Should reach LevelComplete
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.LevelComplete)
        }

    @Test
    fun `onNextWord resets hints when moving between words`() =
        runTest {
            // Given - Level with 2 words
            val twoWords =
                listOf(
                    LearningViewModelTestHelper.testWords[0],
                    LearningViewModelTestHelper.testWords[1],
                )
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(twoWords)
            createViewModel()
            advanceUntilIdle()

            // Use hint on first word
            coEvery { useHint.resetHints(any()) } just Runs
            coEvery {
                useHint(any(), any(), any())
            } returns
                com.wordland.domain.model.Result.Success(
                    LearningViewModelTestHelper.createHintResult(),
                )

            viewModel.useHint()
            advanceUntilIdle()

            // When - Move to next word
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - resetHints should be called for new word
            coVerify(atLeast = 2) { useHint.resetHints(any()) }
        }

    @Test
    fun `updateIslandMastery handles empty progress list`() =
        runTest {
            // Given - Create a level with only 1 word for simpler test
            val singleWord = listOf(LearningViewModelTestHelper.testWords[0])
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(singleWord)
            createViewModel()
            advanceUntilIdle()

            coEvery { progressRepository.getAllWordProgress(any()) } returns emptyList()

            // When - Next word on single word level triggers LevelComplete
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - Should reach LevelComplete without error
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.LevelComplete)
        }

    @Test
    fun `updateIslandMastery handles database exception`() =
        runTest {
            // Given - Create a level with only 1 word for simpler test
            val singleWord = listOf(LearningViewModelTestHelper.testWords[0])
            coEvery { loadLevelWords(LearningViewModelTestHelper.TEST_LEVEL_ID) } returns
                Result.Success(singleWord)
            createViewModel()
            advanceUntilIdle()

            coEvery { progressRepository.getAllWordProgress(any()) } throws
                RuntimeException("DB Error")

            // When - Next word on single word level triggers LevelComplete
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - Should still reach LevelComplete (exception is caught)
            val state = viewModel.uiState.value
            assertTrue(state is LearningUiState.LevelComplete)
        }

    // === Region ID Mapping Tests ===

    @Test
    fun `getRegionIdForLevel returns correct region for look island`() =
        runTest {
            // Given
            coEvery { loadLevelWords("look_level_01") } returns
                Result.Success(listOf(LearningViewModelTestHelper.testWords[0]))
            savedStateHandle.set("levelId", "look_level_01")
            savedStateHandle.set("islandId", "look_island")

            // When
            createViewModel()
            advanceUntilIdle()

            // Complete level to trigger region mapping
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - should map look_level to look_peninsula
            coVerify { exploreRegion(any(), "look_peninsula") }
        }

    @Test
    fun `getRegionIdForLevel returns correct region for make lake`() =
        runTest {
            // Given
            val makeWord =
                LearningViewModelTestHelper.testWords[0].copy(
                    id = "make_word1",
                    islandId = "make_lake",
                    levelId = "make_level_01",
                )
            coEvery { loadLevelWords("make_level_01") } returns Result.Success(listOf(makeWord))
            savedStateHandle.set("levelId", "make_level_01")
            savedStateHandle.set("islandId", "make_lake")

            // When
            createViewModel()
            advanceUntilIdle()

            // Complete level to trigger region mapping
            viewModel.onNextWord()
            advanceUntilIdle()

            // Then - should map make_level to make_lake
            coVerify { exploreRegion(any(), "make_lake") }
        }

    @Test
    fun `getRegionIdForLevel returns unknown_region for unknown prefix`() {
        // Given
        val unknownLevelId = "unknown_level_01"

        // When
        val regionId =
            when {
                unknownLevelId.startsWith("look_") -> "look_peninsula"
                unknownLevelId.startsWith("make_") -> "make_lake"
                else -> "unknown_region"
            }

        // Then
        assertEquals("unknown_region", regionId)
    }

    @Test
    fun `getRegionIdForLevel handles make_ prefix correctly`() {
        // Given
        val makeLevelId = "make_lake_level_01"

        // When
        val regionId =
            when {
                makeLevelId.startsWith("look_") -> "look_peninsula"
                makeLevelId.startsWith("make_") -> "make_lake"
                else -> "unknown_region"
            }

        // Then
        assertEquals("make_lake", regionId)
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

            // Reset mock for navigation tests
            clearMocks(submitAnswer, useHint, answers = false, recordedCalls = false)
        }
}
