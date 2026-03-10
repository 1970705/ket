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
 * Unit tests for OnboardingViewModel answer submission and word skipping
 * Tests updateAnswer, submitAnswer, and skipWord functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelAnswerTest {
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

    // === updateAnswer Tests ===

    @Test
    fun `updateAnswer in Tutorial state updates currentAnswer`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.updateAnswer("cat")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is OnboardingUiState.Tutorial)
            val tutorial = state as OnboardingUiState.Tutorial
            assertEquals("cat", tutorial.question.currentAnswer)
        }

    @Test
    fun `updateAnswer when not in Tutorial state does nothing`() =
        runTest {
            // Given - in Welcome state
            coEvery { startOnboardingUseCase() } returns welcomeState
            viewModel.startOnboarding()
            advanceUntilIdle()

            // When - should not crash
            viewModel.updateAnswer("test")
            advanceUntilIdle()

            // Then - state unchanged
            assertTrue(viewModel.uiState.value is OnboardingUiState.Welcome)
        }

    // === submitAnswer Tests ===

    @Test
    fun `submitAnswer with correct answer awards 3 stars and completes word`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            val updatedState = tutorialState.copy(completedTutorialWords = 1, totalStars = 3)
            coEvery { completeTutorialWordUseCase(3) } returns updatedState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.submitAnswer("cat")
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { completeTutorialWordUseCase(3) }
            assertEquals(1, viewModel.onboardingState.value?.completedTutorialWords)
        }

    @Test
    fun `submitAnswer with incorrect answer awards 1 star`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            val updatedState = tutorialState.copy(completedTutorialWords = 1, totalStars = 1)
            coEvery { completeTutorialWordUseCase(1) } returns updatedState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.submitAnswer("xyz")
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { completeTutorialWordUseCase(1) }
        }

    @Test
    fun `submitAnswer after 5 words opens chest`() =
        runTest {
            // Given - start in Tutorial at word 4 (4 words already completed)
            val stateAtWord4 = tutorialState.copy(completedTutorialWords = 4, totalStars = 12)
            coEvery { startOnboardingUseCase() } returns stateAtWord4
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            val atChestState =
                stateAtWord4.copy(
                    completedTutorialWords = 5,
                    totalStars = 15,
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                )
            coEvery { completeTutorialWordUseCase(3) } returns atChestState
            coEvery { openFirstChestUseCase() } returns
                ChestReward.PetEmoji(
                    petType = PetType.DOLPHIN,
                    emoji = "🐬",
                    description = "宠物表情",
                )

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When - submit answer for 5th word
            viewModel.submitAnswer("red")
            advanceUntilIdle()

            // Then - chest should open
            coVerify(exactly = 1) { openFirstChestUseCase() }
            assertTrue(viewModel.uiState.value is OnboardingUiState.OpeningChest)
        }

    @Test
    fun `submitAnswer before 5 words shows next word`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            val updatedState = tutorialState.copy(completedTutorialWords = 1, totalStars = 3)
            coEvery { completeTutorialWordUseCase(3) } returns updatedState

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.submitAnswer("cat")
            advanceUntilIdle()

            // Then - should show next word (index 1)
            val state = viewModel.uiState.value
            assertTrue(state is OnboardingUiState.Tutorial)
            val tutorial = state as OnboardingUiState.Tutorial
            assertEquals(1, tutorial.currentWordIndex)
        }

    @Test
    fun `submitAnswer when onboardingState is null does nothing`() =
        runTest {
            // Given - onboardingState is null (initial state)

            // When - should not crash
            viewModel.submitAnswer("test")
            advanceUntilIdle()

            // Then - useCase not called
            coVerify(exactly = 0) { completeTutorialWordUseCase(any()) }
        }

    @Test
    fun `submitAnswer on exception emits Error state`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            coEvery { completeTutorialWordUseCase(any()) } throws IllegalStateException("Submit failed")

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.submitAnswer("cat")
            advanceUntilIdle()

            // Then
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Error)
            val error = result as OnboardingUiState.Error
            assertEquals("Submit failed", error.message)
        }

    // === skipWord Tests ===

    @Test
    fun `skipWord completes word with 0 stars`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            val updatedState = tutorialState.copy(completedTutorialWords = 1, totalStars = 0)
            coEvery { completeTutorialWordUseCase(0) } returns updatedState

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.skipWord()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { completeTutorialWordUseCase(0) }
        }

    @Test
    fun `skipWord after 5 words opens chest`() =
        runTest {
            // Given - start in Tutorial at word 4 (4 words already completed)
            val stateAtWord4 = tutorialState.copy(completedTutorialWords = 4, totalStars = 10)
            coEvery { startOnboardingUseCase() } returns stateAtWord4
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            val atChestState =
                stateAtWord4.copy(
                    completedTutorialWords = 5,
                    totalStars = 10,
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                )
            coEvery { completeTutorialWordUseCase(0) } returns atChestState
            coEvery { openFirstChestUseCase() } returns
                ChestReward.PetEmoji(
                    petType = PetType.DOLPHIN,
                    emoji = "🐬",
                    description = "宠物表情",
                )

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When - skip 5th word
            viewModel.skipWord()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { openFirstChestUseCase() }
            assertTrue(viewModel.uiState.value is OnboardingUiState.OpeningChest)
        }

    @Test
    fun `skipWord before 5 words shows next word`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            val updatedState = tutorialState.copy(completedTutorialWords = 1)
            coEvery { completeTutorialWordUseCase(0) } returns updatedState

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.skipWord()
            advanceUntilIdle()

            // Then - should show next word
            val state = viewModel.uiState.value
            assertTrue(state is OnboardingUiState.Tutorial)
            val tutorial = state as OnboardingUiState.Tutorial
            assertEquals(1, tutorial.currentWordIndex)
        }

    @Test
    fun `skipWord on exception emits Error state`() =
        runTest {
            // Given
            coEvery { startOnboardingUseCase() } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords
            coEvery { completeTutorialWordUseCase(0) } throws IllegalStateException("Skip failed")

            viewModel.startOnboarding()
            advanceUntilIdle()

            // When
            viewModel.skipWord()
            advanceUntilIdle()

            // Then
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Error)
            val error = result as OnboardingUiState.Error
            assertEquals("Skip failed", error.message)
        }
}
