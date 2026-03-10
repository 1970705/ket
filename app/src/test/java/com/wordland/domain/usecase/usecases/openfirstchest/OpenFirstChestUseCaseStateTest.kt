package com.wordland.domain.usecase.usecases.openfirstchest

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.PetType
import com.wordland.domain.repository.OnboardingRepository
import com.wordland.domain.usecase.usecases.OpenFirstChestUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OpenFirstChestUseCase - State Management
 * Tests state updates, error handling, and edge cases
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OpenFirstChestUseCaseStateTest {
    private lateinit var useCase: OpenFirstChestUseCase
    private lateinit var repository: OnboardingRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        useCase = OpenFirstChestUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === State Update Tests ===

    @Test
    fun `invoke preserves phase as FIRST_CHEST`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase()

            // Then - phase is preserved (not changed to COMPLETED)
            // COMPLETED phase will be set when user clicks "太酷了！" button
            assertEquals(OnboardingPhase.FIRST_CHEST, savedStateSlot.captured.currentPhase)
        }

    @Test
    fun `invoke updates lastOpenedChest timestamp`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 5,
                    lastOpenedChest = 0L,
                )
            coEvery { repository.getOnboardingState() } returns state
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase()

            // Then
            val savedState = savedStateSlot.captured
            assertTrue(savedState.lastOpenedChest > 0)
        }

    @Test
    fun `invoke preserves selected pet in saved state`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase()

            // Then
            assertEquals(PetType.CAT, savedStateSlot.captured.selectedPet)
        }

    @Test
    fun `invoke preserves completedTutorialWords count`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase()

            // Then
            assertEquals(5, savedStateSlot.captured.completedTutorialWords)
        }

    @Test
    fun `invoke preserves totalStars count`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 5,
                    totalStars = 12,
                )
            coEvery { repository.getOnboardingState() } returns state
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase()

            // Then
            assertEquals(12, savedStateSlot.captured.totalStars)
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
                useCase()
            } catch (e: IllegalStateException) {
                exception = e
            }
            assertNotNull(exception)
            assertEquals("Onboarding not started", exception?.message)
        }

    @Test
    fun `invoke throws IllegalStateException when no pet selected`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = null,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state

            // When & Then
            var exception: IllegalStateException? = null
            try {
                useCase()
            } catch (e: IllegalStateException) {
                exception = e
            }
            assertNotNull(exception)
            assertEquals("No pet selected", exception?.message)
        }

    @Test
    fun `invoke does not save when state is null`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            try {
                useCase()
            } catch (e: IllegalStateException) {
                // Expected
            }

            // Then
            coVerify(exactly = 0) { repository.saveOnboardingState(any()) }
        }

    @Test
    fun `invoke does not save when no pet selected`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = null,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state

            // When
            try {
                useCase()
            } catch (e: IllegalStateException) {
                // Expected
            }

            // Then
            coVerify(exactly = 0) { repository.saveOnboardingState(any()) }
        }

    // === Edge Cases ===

    @Test
    fun `invoke updates updatedAt timestamp`() =
        runTest {
            // Given
            val initialTime = 1000L
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 5,
                    updatedAt = initialTime,
                )
            coEvery { repository.getOnboardingState() } returns state
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase()

            // Then
            assertTrue(savedStateSlot.captured.updatedAt > initialTime)
        }

    @Test
    fun `invoke preserves userId`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            val savedStateSlot = slot<OnboardingState>()
            coEvery { repository.saveOnboardingState(capture(savedStateSlot)) } just Runs

            // When
            useCase()

            // Then
            assertEquals("user_001", savedStateSlot.captured.userId)
        }

    @Test
    fun `invoke calls saveOnboardingState exactly once`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            useCase()

            // Then
            coVerify(exactly = 1) { repository.saveOnboardingState(any()) }
        }
}
