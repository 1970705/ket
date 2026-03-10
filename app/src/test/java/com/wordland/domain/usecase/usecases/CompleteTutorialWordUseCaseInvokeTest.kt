package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.PetType
import com.wordland.domain.repository.OnboardingRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CompleteTutorialWordUseCase.invoke()
 * Tests word completion tracking, star accumulation, and phase transitions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompleteTutorialWordUseCaseInvokeTest {
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

    // === Star Coercion Tests ===

    @Test
    fun `invoke coerces negative stars to 0`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 0,
                    totalStars = 0,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            val result = useCase(-1)

            // Then
            assertEquals(0, result.totalStars) // Should be coerced to 0, then added
            // Actually -1 coerced to 0, 0 + 0 = 0
            assertEquals(0, savedStateSlot.captured.totalStars)
        }

    @Test
    fun `invoke coerces stars above 3 to 3`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 0,
                    totalStars = 0,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            val result = useCase(5)

            // Then
            assertEquals(3, result.totalStars) // Should be coerced to 3, then added
        }

    @Test
    fun `invoke accepts 0 stars`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 0,
                    totalStars = 10,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(0)

            // Then
            assertEquals(10, result.totalStars) // 10 + 0 = 10
        }

    @Test
    fun `invoke accepts 1 star`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 0,
                    totalStars = 5,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(1)

            // Then
            assertEquals(6, result.totalStars) // 5 + 1 = 6
        }

    @Test
    fun `invoke accepts 2 stars`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 0,
                    totalStars = 0,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(2)

            // Then
            assertEquals(2, result.totalStars) // 0 + 2 = 2
        }

    @Test
    fun `invoke accepts 3 stars`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 0,
                    totalStars = 0,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(3)

            // Then
            assertEquals(3, result.totalStars) // 0 + 3 = 3
        }

    // === Error Handling Tests ===

    @Test
    fun `invoke throws IllegalStateException when no state exists`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When & Then
            var exception: IllegalStateException? = null
            try {
                useCase(3)
            } catch (e: IllegalStateException) {
                exception = e
            }
            assertNotNull(exception)
            assertEquals("Onboarding not started", exception?.message)
        }

    @Test
    fun `invoke does not save when exception thrown`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            try {
                useCase(3)
            } catch (e: IllegalStateException) {
                // Expected
            }

            // Then
            coVerify(exactly = 0) { repository.saveOnboardingState(any()) }
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
}
