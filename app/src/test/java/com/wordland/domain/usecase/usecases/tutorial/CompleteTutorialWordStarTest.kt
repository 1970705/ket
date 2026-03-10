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
import org.junit.Before
import org.junit.Test

/**
 * Star coercion tests for CompleteTutorialWordUseCase.
 * Tests star value validation and coercion to valid range.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompleteTutorialWordStarTest {
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

    // === Star Coercion Tests ==========

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

    @Test
    fun `invoke accumulates stars correctly with coercion`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 0,
                    totalStars = 8,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase(3)

            // Then
            assertEquals(11, result.totalStars) // 8 + 3 = 11
        }

    @Test
    fun `invoke handles edge case of exactly 3 stars`() =
        runTest {
            // Given
            val initialState =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 0,
                    totalStars = 0,
                )
            coEvery { repository.getOnboardingState() } returns initialState
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When - exactly at the boundary
            val result = useCase(3)

            // Then
            assertEquals(3, result.totalStars) // 3 is valid, no coercion needed
        }

    @Test
    fun `invoke handles edge case of exactly 0 stars`() =
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

            // When - exactly at the boundary
            val result = useCase(0)

            // Then
            assertEquals(0, result.totalStars) // 0 is valid, no coercion needed
        }
}
