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
 * Unit tests for SelectPetUseCase
 * Tests pet selection, state transitions, and validation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SelectPetUseCaseTest {
    private lateinit var useCase: SelectPetUseCase
    private lateinit var repository: OnboardingRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        useCase = SelectPetUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === Basic Pet Selection Tests ===

    @Test
    fun `invoke selects DOLPHIN and transitions to TUTORIAL`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            val result = useCase(PetType.DOLPHIN)

            // Then
            assertEquals(PetType.DOLPHIN, result.selectedPet)
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
            assertTrue(savedStateSlot.isCaptured)

            val savedState = savedStateSlot.captured
            assertEquals(PetType.DOLPHIN, savedState.selectedPet)
            assertEquals(OnboardingPhase.TUTORIAL, savedState.currentPhase)
        }

    @Test
    fun `invoke selects CAT and transitions to TUTORIAL`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.PET_SELECTION,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(PetType.CAT)

            // Then
            assertEquals(PetType.CAT, result.selectedPet)
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
        }

    @Test
    fun `invoke selects DOG and transitions to TUTORIAL`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(PetType.DOG)

            // Then
            assertEquals(PetType.DOG, result.selectedPet)
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
        }

    @Test
    fun `invoke selects FOX and transitions to TUTORIAL`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(PetType.FOX)

            // Then
            assertEquals(PetType.FOX, result.selectedPet)
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
        }

    // === State Persistence Tests ===

    @Test
    fun `invoke saves updated state to repository`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            useCase(PetType.DOLPHIN)

            // Then
            coVerify(exactly = 1) { repository.saveOnboardingState(any()) }
        }

    @Test
    fun `invoke updates updatedAt timestamp`() =
        runTest {
            // Given
            val initialTime = 1000L
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                    updatedAt = initialTime,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase(PetType.CAT)

            // Then
            val savedState = savedStateSlot.captured
            assertTrue(savedState.updatedAt > initialTime)
        }

    @Test
    fun `invoke preserves completedTutorialWords count`() =
        runTest {
            // Given - simulating scenario where user returns and changes pet
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.PET_SELECTION,
                    selectedPet = null,
                    completedTutorialWords = 2,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase(PetType.FOX)

            // Then
            assertEquals(2, savedStateSlot.captured.completedTutorialWords)
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
                useCase(PetType.DOLPHIN)
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
                useCase(PetType.CAT)
            } catch (e: IllegalStateException) {
                // Expected
            }

            // Then - save should not be called
            coVerify(exactly = 0) { repository.saveOnboardingState(any()) }
        }

    // === isValidPet Tests ===

    @Test
    fun `isValidPet returns true for DOLPHIN`() {
        assertTrue(useCase.isValidPet(PetType.DOLPHIN))
    }

    @Test
    fun `isValidPet returns true for CAT`() {
        assertTrue(useCase.isValidPet(PetType.CAT))
    }

    @Test
    fun `isValidPet returns true for DOG`() {
        assertTrue(useCase.isValidPet(PetType.DOG))
    }

    @Test
    fun `isValidPet returns true for FOX`() {
        assertTrue(useCase.isValidPet(PetType.FOX))
    }

    @Test
    fun `isValidPet returns true for all PetType entries`() {
        PetType.entries.forEach { petType ->
            assertTrue("$petType should be valid", useCase.isValidPet(petType))
        }
    }

    // === Pet Change Scenarios ===

    @Test
    fun `invoke can change pet from DOLPHIN to CAT`() =
        runTest {
            // Given - user previously selected DOLPHIN
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

            // When - user changes to CAT
            val result = useCase(PetType.CAT)

            // Then
            assertEquals(PetType.CAT, result.selectedPet)
            assertEquals(PetType.CAT, savedStateSlot.captured.selectedPet)
        }

    @Test
    fun `invoke preserves progress when changing pet`() =
        runTest {
            // Given - user with some progress changing pet
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 3,
                    totalStars = 7,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase(PetType.FOX)

            // Then
            val savedState = savedStateSlot.captured
            assertEquals(3, savedState.completedTutorialWords)
            assertEquals(7, savedState.totalStars)
        }

    // === Phase Transition Tests ===

    @Test
    fun `invoke always transitions to TUTORIAL phase`() =
        runTest {
            // Given - starting from WELCOME
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(PetType.DOLPHIN)

            // Then
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
        }

    @Test
    fun `invoke transitions from PET_SELECTION to TUTORIAL`() =
        runTest {
            // Given - already in PET_SELECTION phase
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.PET_SELECTION,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(PetType.CAT)

            // Then
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
        }

    @Test
    fun `invoke can handle re-selection from TUTORIAL phase`() =
        runTest {
            // Given - user goes back and changes pet during tutorial
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 2,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(PetType.FOX)

            // Then
            assertEquals(PetType.FOX, result.selectedPet)
            assertEquals(OnboardingPhase.TUTORIAL, result.currentPhase)
            assertEquals(2, result.completedTutorialWords)
        }
}
