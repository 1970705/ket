package com.wordland.ui.viewmodel.onboarding

import com.wordland.domain.model.*
import com.wordland.domain.usecase.usecases.CompleteOnboardingUseCase
import com.wordland.domain.usecase.usecases.CompleteTutorialWordUseCase
import com.wordland.domain.usecase.usecases.GetTutorialWordsUseCase
import com.wordland.domain.usecase.usecases.OpenFirstChestUseCase
import com.wordland.domain.usecase.usecases.SelectPetUseCase
import com.wordland.domain.usecase.usecases.StartOnboardingUseCase
import com.wordland.ui.viewmodel.OnboardingViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for OnboardingViewModel chest dismissal and complete flow functionality.
 * Covers dismissChest, complete flow from Welcome to Tutorial to Chest to Completed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelChestTest {
    private lateinit var viewModel: OnboardingViewModel

    private val startOnboardingUseCase: StartOnboardingUseCase = mockk()
    private val selectPetUseCase: SelectPetUseCase = mockk()
    private val getTutorialWordsUseCase: GetTutorialWordsUseCase = mockk()
    private val completeTutorialWordUseCase: CompleteTutorialWordUseCase = mockk()
    private val openFirstChestUseCase: OpenFirstChestUseCase = mockk()
    private val completeOnboardingUseCase: CompleteOnboardingUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

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

    private val tutorialWords =
        listOf(
            TutorialWordConfig(word = "cat", translation = "猫"),
            TutorialWordConfig(word = "dog", translation = "狗"),
            TutorialWordConfig(word = "sun", translation = "太阳"),
            TutorialWordConfig(word = "eye", translation = "眼睛"),
            TutorialWordConfig(word = "red", translation = "红色"),
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

    // === dismissChest Tests ===

    @Test
    fun `dismissChest emits Completed state with pet info`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns chestState
            val completedStateFromUseCase = chestState.copy(currentPhase = OnboardingPhase.COMPLETED)
            coEvery { completeOnboardingUseCase() } returns completedStateFromUseCase

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.dismissChest()
            advanceUntilIdle() // Wait for coroutine to complete

            // Then
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Completed)
            val completed = result as OnboardingUiState.Completed
            assertEquals(PetType.DOLPHIN, completed.pet)
            assertEquals(5, completed.wordsLearned)
            assertEquals(15, completed.stars)
        }

    @Test
    fun `dismissChest when onboardingState is null uses defaults`() =
        runTest {
            // Given - onboardingState is null (initial state)
            // Note: When state is null, completeOnboardingUseCase throws exception
            // The ViewModel catches this and emits Error state

            // When
            viewModel.dismissChest()
            advanceUntilIdle() // Wait for coroutine to complete

            // Then - should be in Error state since onboarding wasn't started
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Error)
        }

    // === Complete Flow Tests ===

    @Test
    fun `complete flow from Welcome to Tutorial`() =
        runTest {
            // Given - start at Welcome
            coEvery { startOnboardingUseCase() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                )
            viewModel.startOnboarding()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is OnboardingUiState.Welcome)

            // When - select pet
            coEvery { selectPetUseCase(PetType.DOLPHIN) } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            viewModel.selectPetByType(PetType.DOLPHIN)
            advanceUntilIdle()

            // Then - should be in Tutorial
            assertTrue(viewModel.uiState.value is OnboardingUiState.Tutorial)
            assertEquals(PetType.DOLPHIN, viewModel.onboardingState.value?.selectedPet)
        }

    @Test
    fun `complete flow from Tutorial to Chest to Completed`() =
        runTest {
            // Given - start in Tutorial at word 4
            val stateAtWord4 = tutorialState.copy(completedTutorialWords = 4, totalStars = 12)
            coEvery { startOnboardingUseCase() } returns stateAtWord4
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            viewModel.startOnboarding()
            advanceUntilIdle()

            // When - complete 5th word
            val chestStateFinal =
                stateAtWord4.copy(
                    completedTutorialWords = 5,
                    totalStars = 15,
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                )
            val reward =
                ChestReward.CelebrationEffect(
                    effectName = "彩带",
                    description = "庆祝特效",
                )
            coEvery { completeTutorialWordUseCase(3) } returns chestStateFinal
            coEvery { openFirstChestUseCase() } returns reward
            val completedStateFromUseCase = chestStateFinal.copy(currentPhase = OnboardingPhase.COMPLETED)
            coEvery { completeOnboardingUseCase() } returns completedStateFromUseCase

            viewModel.submitAnswer("red")
            advanceUntilIdle()

            // Then - should be in OpeningChest
            assertTrue(viewModel.uiState.value is OnboardingUiState.OpeningChest)

            // When - dismiss chest
            viewModel.dismissChest()
            advanceUntilIdle() // Wait for coroutine to complete

            // Then - should be Completed
            assertTrue(viewModel.uiState.value is OnboardingUiState.Completed)
            assertEquals(5, viewModel.onboardingState.value?.completedTutorialWords)
        }
}
