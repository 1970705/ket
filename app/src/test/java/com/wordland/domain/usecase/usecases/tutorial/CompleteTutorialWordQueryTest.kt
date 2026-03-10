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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Query method tests for CompleteTutorialWordUseCase.
 * Tests isTutorialComplete and getRemainingWords helper methods.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompleteTutorialWordQueryTest {
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

    @Test
    fun `isTutorialComplete returns true at boundary of 5 words`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.DOG,
                    completedTutorialWords = 5,
                )

            // When
            val result = useCase.isTutorialComplete()

            // Then
            assertTrue(result)
        }

    @Test
    fun `isTutorialComplete returns false at boundary of 4 words`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = PetType.FOX,
                    completedTutorialWords = 4,
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

    @Test
    fun `getRemainingWords calculation is correct`() =
        runTest {
            // Given - test various values
            val testCases =
                mapOf(
                    0 to 5,
                    1 to 4,
                    2 to 3,
                    3 to 2,
                    4 to 1,
                    5 to 0,
                    6 to 0, // Beyond required
                    10 to 0, // Way beyond required
                )

            testCases.forEach { (completed, expected) ->
                coEvery { repository.getOnboardingState() } returns
                    OnboardingState(
                        userId = "user_001",
                        currentPhase = OnboardingPhase.TUTORIAL,
                        selectedPet = PetType.DOLPHIN,
                        completedTutorialWords = completed,
                    )

                val result = useCase.getRemainingWords()
                assertEquals(expected, result)
            }
        }
}
