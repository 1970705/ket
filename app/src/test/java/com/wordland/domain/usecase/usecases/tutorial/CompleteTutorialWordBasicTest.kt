package com.wordland.domain.usecase.usecases.tutorial

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.PetType
import com.wordland.domain.repository.OnboardingRepository
import com.wordland.domain.usecase.usecases.CompleteTutorialWordUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Basic functionality tests for CompleteTutorialWordUseCase.
 * Tests word count incrementing, star accumulation, and phase transitions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompleteTutorialWordBasicTest {
    private lateinit var useCase: CompleteTutorialWordUseCase
    private lateinit var repository: OnboardingRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        useCase = CompleteTutorialWordUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === Basic Completion Tests ===

    @Test
    fun `invoke increments word count from 0 to 1`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 0,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            val result = useCase(3)

            // Then
            assertEquals(1, result.completedTutorialWords)
            assertEquals(1, savedStateSlot.captured.completedTutorialWords)
        }

    @Test
    fun `invoke increments word count from 4 to 5`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 4,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(3)

            // Then
            assertEquals(5, result.completedTutorialWords)
        }

    @Test
    fun `invoke adds stars to total`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 0,
                    totalStars = 5,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(3)

            // Then
            assertEquals(8, result.totalStars) // 5 + 3 = 8
        }

    @Test
    fun `invoke returns updated state`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 2,
                    totalStars = 6,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(2)

            // Then
            assertEquals(3, result.completedTutorialWords)
            assertEquals(8, result.totalStars)
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
        }

    // === Phase Transition Tests ===

    @Test
    fun `invoke transitions to FIRST_CHEST after 5 words`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 4,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When - complete 5th word
            val result = useCase(3)

            // Then
            assertEquals(OnboardingPhase.FIRST_CHEST, result.currentPhase)
            assertEquals(5, result.completedTutorialWords)
        }

    @Test
    fun `invoke stays in TUTORIAL phase before 5 words`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 2,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(3)

            // Then
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
            assertEquals(3, result.completedTutorialWords)
        }

    @Test
    fun `invoke stays in TUTORIAL phase at exactly 4 words`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 3,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(2)

            // Then
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
            assertEquals(4, result.completedTutorialWords)
        }

    @Test
    fun `invoke transitions at exactly 5 words completed`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 4, // One more to reach 5
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(1)

            // Then
            assertEquals(OnboardingPhase.FIRST_CHEST, result.currentPhase)
            assertEquals(5, result.completedTutorialWords)
        }

    // === State Preservation Tests ===

    @Test
    fun `invoke preserves selected pet`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 1,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase(2)

            // Then
            assertEquals(PetType.DOLPHIN, savedStateSlot.captured.selectedPet)
        }

    @Test
    fun `invoke preserves userId`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 2,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase(3)

            // Then
            assertEquals("user_001", savedStateSlot.captured.userId)
        }

    @Test
    fun `invoke updates updatedAt timestamp`() =
        runTest {
            // Given
            val initialTime = 1000L
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 0,
                    updatedAt = initialTime,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase(3)

            // Then
            assertTrue(savedStateSlot.captured.updatedAt > initialTime)
        }

    // === Constant Tests ===

    @Test
    fun `REQUIRED_WORDS_FOR_CHEST is 5`() {
        assertEquals(5, CompleteTutorialWordUseCase.REQUIRED_WORDS_FOR_CHEST)
    }
}
