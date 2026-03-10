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
 * Unit tests for StartOnboardingUseCase
 * Tests first launch detection, state initialization, and state restoration
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StartOnboardingUseCaseTest {
    private lateinit var useCase: StartOnboardingUseCase
    private lateinit var repository: OnboardingRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        useCase = StartOnboardingUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === First Launch Tests ===

    @Test
    fun `invoke creates initial state when none exists`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            val result = useCase()

            // Then
            assertEquals(OnboardingPhase.WELCOME, result.currentPhase)
            assertEquals("user_001", result.userId)
            assertNull(result.selectedPet)
            assertEquals(0, result.completedTutorialWords)
            assertTrue(savedStateSlot.isCaptured)

            val savedState = savedStateSlot.captured
            assertEquals("user_001", savedState.userId)
            assertEquals(OnboardingPhase.WELCOME, savedState.currentPhase)
        }

    @Test
    fun `invoke saves initial state to repository`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            useCase()

            // Then
            coVerify(exactly = 1) { repository.saveOnboardingState(any()) }
        }

    @Test
    fun `invoke sets correct default values for initial state`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase()

            // Then - verify all default values
            assertEquals("user_001", result.userId)
            assertEquals(OnboardingPhase.WELCOME, result.currentPhase)
            assertNull(result.selectedPet)
            assertEquals(0, result.completedTutorialWords)
            assertEquals(0L, result.lastOpenedChest)
            assertEquals(0, result.totalStars)
            assertTrue(result.createdAt > 0)
            assertTrue(result.updatedAt > 0)
        }

    // === State Restoration Tests ===

    @Test
    fun `invoke returns existing state when available`() =
        runTest {
            // Given
            val existingState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.PET_SELECTION,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 2,
                )
            coEvery { repository.getOnboardingState() } returns existingState

            // When
            val result = useCase()

            // Then
            assertEquals(existingState, result)
            assertEquals(OnboardingPhase.PET_SELECTION, result.currentPhase)
            assertEquals(PetType.DOLPHIN, result.selectedPet)
            assertEquals(2, result.completedTutorialWords)
        }

    @Test
    fun `invoke does not save when state already exists`() =
        runTest {
            // Given
            val existingState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.CAT,
                )
            coEvery { repository.getOnboardingState() } returns existingState

            // When
            useCase()

            // Then - should NOT call save
            coVerify(exactly = 0) { repository.saveOnboardingState(any()) }
        }

    @Test
    fun `invoke preserves completed tutorial words count`() =
        runTest {
            // Given
            val existingState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns existingState

            // When
            val result = useCase()

            // Then
            assertEquals(5, result.completedTutorialWords)
        }

    @Test
    fun `invoke preserves total stars`() =
        runTest {
            // Given
            val existingState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 3,
                    totalStars = 9,
                )
            coEvery { repository.getOnboardingState() } returns existingState

            // When
            val result = useCase()

            // Then
            assertEquals(9, result.totalStars)
        }

    // === isCompleted Tests ===

    @Test
    fun `isCompleted returns true when phase is COMPLETED`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.COMPLETED,
                    selectedPet = PetType.DOLPHIN,
                )

            // When
            val result = useCase.isCompleted()

            // Then
            assertTrue(result)
        }

    @Test
    fun `isCompleted returns false when phase is not COMPLETED`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                )

            // When
            val result = useCase.isCompleted()

            // Then
            assertFalse(result)
        }

    @Test
    fun `isCompleted returns false when state is null`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            val result = useCase.isCompleted()

            // Then
            assertFalse(result)
        }

    @Test
    fun `isCompleted returns false for TUTORIAL phase`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.CAT,
                )

            // When
            val result = useCase.isCompleted()

            // Then
            assertFalse(result)
        }

    // === Edge Cases ===

    @Test
    fun `handles FIRST_CHEST phase correctly`() =
        runTest {
            // Given
            val existingState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns existingState

            // When
            val result = useCase()

            // Then
            assertEquals(OnboardingPhase.FIRST_CHEST, result.currentPhase)
            assertFalse(useCase.isCompleted())
        }

    @Test
    fun `handles PET_SELECTION phase correctly`() =
        runTest {
            // Given
            val existingState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.PET_SELECTION,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns existingState

            // When
            val result = useCase()

            // Then
            assertEquals(OnboardingPhase.PET_SELECTION, result.currentPhase)
        }

    @Test
    fun `invoke returns state with PET_SELECTION phase when restoring`() =
        runTest {
            // Given - simulating user who selected pet but didn't complete tutorial
            val existingState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.PET_SELECTION,
                    selectedPet = null,
                )
            coEvery { repository.getOnboardingState() } returns existingState

            // When
            val result = useCase()

            // Then
            assertEquals(OnboardingPhase.PET_SELECTION, result.currentPhase)
            assertNull(result.selectedPet)
        }
}
