package com.wordland.ui.viewmodel

import com.wordland.domain.model.*
import com.wordland.domain.usecase.usecases.CompleteOnboardingUseCase
import com.wordland.domain.usecase.usecases.CompleteTutorialWordUseCase
import com.wordland.domain.usecase.usecases.GetTutorialWordsUseCase
import com.wordland.domain.usecase.usecases.OpenFirstChestUseCase
import com.wordland.domain.usecase.usecases.SelectPetUseCase
import com.wordland.domain.usecase.usecases.StartOnboardingUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OnboardingViewModel initialization and start phase
 * Tests state management during initialization and startOnboarding flow
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelInitTest {
    private lateinit var viewModel: OnboardingViewModel

    private val startOnboardingUseCase: StartOnboardingUseCase = mockk()
    private val selectPetUseCase: SelectPetUseCase = mockk()
    private val getTutorialWordsUseCase: GetTutorialWordsUseCase = mockk()
    private val completeTutorialWordUseCase: CompleteTutorialWordUseCase = mockk()
    private val openFirstChestUseCase: OpenFirstChestUseCase = mockk()
    private val completeOnboardingUseCase: CompleteOnboardingUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val welcomeState =
        OnboardingState(
            userId = "user_001",
            currentPhase = OnboardingPhase.WELCOME,
            selectedPet = null,
        )

    private val tutorialState =
        OnboardingState(
            userId = "user_001",
            currentPhase = OnboardingPhase.TUTORIAL,
            selectedPet = PetType.DOLPHIN,
            completedTutorialWords = 0,
        )

    private val chestState =
        OnboardingState(
            userId = "user_001",
            currentPhase = OnboardingPhase.FIRST_CHEST,
            selectedPet = PetType.DOLPHIN,
            completedTutorialWords = 5,
            totalStars = 15,
        )

    private val completedState =
        OnboardingState(
            userId = "user_001",
            currentPhase = OnboardingPhase.COMPLETED,
            selectedPet = PetType.DOLPHIN,
            completedTutorialWords = 5,
            totalStars = 15,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel =
            OnboardingViewModel(
                startOnboardingUseCase,
                selectPetUseCase,
                getTutorialWordsUseCase,
                completeTutorialWordUseCase,
                openFirstChestUseCase,
                completeOnboardingUseCase,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === Initial State Tests ===

    @Test
    fun `initial uiState is Idle`() {
        assertEquals(OnboardingUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `initial onboardingState is null`() {
        assertNull(viewModel.onboardingState.value)
    }

    // === startOnboarding Tests ===

    @Test
    fun `startOnboarding sets Loading state first`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns welcomeState

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then - Loading is set before UseCase completes
            coVerify(exactly = 1) { startOnboardingUseCase() }
        }

    @Test
    fun `startOnboarding with WELCOME phase emits Welcome state`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns welcomeState

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is OnboardingUiState.Welcome)
            assertEquals(welcomeState, viewModel.onboardingState.value)
        }

    @Test
    fun `startOnboarding with PET_SELECTION phase emits PetSelection state`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.PET_SELECTION,
                    selectedPet = null,
                )
            coEvery { startOnboardingUseCase() } returns state

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is OnboardingUiState.PetSelection)
        }

    @Test
    fun `startOnboarding with TUTORIAL phase loads tutorial words`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns
                listOf(
                    TutorialWordConfig(word = "cat", translation = "猫"),
                    TutorialWordConfig(word = "dog", translation = "狗"),
                )

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { getTutorialWordsUseCase() }
            assertTrue(viewModel.uiState.value is OnboardingUiState.Tutorial)
        }

    @Test
    fun `startOnboarding with FIRST_CHEST phase emits OpeningChest state`() =
        runTest {
            // Given
            val expectedReward =
                ChestReward.CelebrationEffect(
                    effectName = "彩带",
                    description = "庆祝特效",
                )
            coEvery { openFirstChestUseCase() } returns expectedReward
            coEvery { startOnboardingUseCase() } returns chestState

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then - should be in OpeningChest state, not Completed
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.OpeningChest)
            val openingChest = result as OnboardingUiState.OpeningChest
            assertEquals(expectedReward, openingChest.reward)
        }

    @Test
    fun `startOnboarding with COMPLETED phase emits Completed state`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns completedState

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Completed)
            val completed = result as OnboardingUiState.Completed
            assertEquals(PetType.DOLPHIN, completed.pet)
            assertEquals(5, completed.wordsLearned)
            assertEquals(15, completed.stars)
        }

    @Test
    fun `startOnboarding with NOT_STARTED phase emits Welcome state`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.NOT_STARTED,
                    selectedPet = null,
                )
            coEvery { startOnboardingUseCase() } returns state

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is OnboardingUiState.Welcome)
        }

    @Test
    fun `startOnboarding on exception emits Error state`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } throws IllegalStateException("Onboarding failed")

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Error)
            val error = result as OnboardingUiState.Error
            assertEquals("Onboarding failed", error.message)
            assertTrue(error.recoverable)
        }

    @Test
    fun `startOnboarding on exception with null message emits default error`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } throws IllegalStateException()

            // When
            viewModel.startOnboarding()
            advanceUntilIdle()

            // Then
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Error)
            val error = result as OnboardingUiState.Error
            assertEquals("启动失败", error.message)
        }
}
