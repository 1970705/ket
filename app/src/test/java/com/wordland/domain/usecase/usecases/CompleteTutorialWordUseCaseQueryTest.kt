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
 * Unit tests for CompleteTutorialWordUseCase query methods
 * Tests isTutorialComplete() and getRemainingWords()
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompleteTutorialWordUseCaseQueryTest {
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

    // === isTutorialComplete Tests ===

    @Test
    fun `isTutorialComplete returns true when completed 5 words`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 5,
                )

            // When
            val result = useCase.isTutorialComplete()

            // Then
            assertTrue(result)
        }

    @Test
    fun `isTutorialComplete returns true when completed more than 5 words`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.COMPLETED,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 7,
                )

            // When
            val result = useCase.isTutorialComplete()

            // Then
            assertTrue(result)
        }

    @Test
    fun `isTutorialComplete returns false when completed less than 5 words`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 3,
                )

            // When
            val result = useCase.isTutorialComplete()

            // Then
            assertFalse(result)
        }

    @Test
    fun `isTutorialComplete returns false when state is null`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            val result = useCase.isTutorialComplete()

            // Then
            assertFalse(result)
        }

    @Test
    fun `isTutorialComplete returns false at exactly 0 words`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.WELCOME,
                    selectedPet = null,
                    completedTutorialWords = 0,
                )

            // When
            val result = useCase.isTutorialComplete()

            // Then
            assertFalse(result)
        }

    // === getRemainingWords Tests ===

    @Test
    fun `getRemainingWords returns 5 when no words completed`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 0,
                )

            // When
            val result = useCase.getRemainingWords()

            // Then
            assertEquals(5, result)
        }

    @Test
    fun `getRemainingWords returns 3 when 2 words completed`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 2,
                )

            // When
            val result = useCase.getRemainingWords()

            // Then
            assertEquals(3, result)
        }

    @Test
    fun `getRemainingWords returns 1 when 4 words completed`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 4,
                )

            // When
            val result = useCase.getRemainingWords()

            // Then
            assertEquals(1, result)
        }

    @Test
    fun `getRemainingWords returns 0 when 5 words completed`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.CAT,
                    completedTutorialWords = 5,
                )

            // When
            val result = useCase.getRemainingWords()

            // Then
            assertEquals(0, result)
        }

    @Test
    fun `getRemainingWords returns 0 when more than 5 completed`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.COMPLETED,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 7,
                )

            // When
            val result = useCase.getRemainingWords()

            // Then
            assertEquals(0, result) // Should be coerced to at least 0
        }

    @Test
    fun `getRemainingWords returns 5 when state is null`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            val result = useCase.getRemainingWords()

            // Then
            assertEquals(5, result) // REQUIRED_WORDS_FOR_CHEST
        }

    // === Constant Tests ===

    @Test
    fun `REQUIRED_WORDS_FOR_CHEST is 5`() {
        assertEquals(5, CompleteTutorialWordUseCase.REQUIRED_WORDS_FOR_CHEST)
    }
}
