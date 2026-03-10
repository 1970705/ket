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
 * Unit tests for OnboardingViewModel pet selection
 * Tests pet ID mapping, pet selection flow, and pet type conversion
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelPetSelectionTest {
    private lateinit var viewModel: OnboardingViewModel

    private val startOnboardingUseCase: StartOnboardingUseCase = mockk()
    private val selectPetUseCase: SelectPetUseCase = mockk()
    private val getTutorialWordsUseCase: GetTutorialWordsUseCase = mockk()
    private val completeTutorialWordUseCase: CompleteTutorialWordUseCase = mockk()
    private val openFirstChestUseCase: OpenFirstChestUseCase = mockk()
    private val completeOnboardingUseCase: CompleteOnboardingUseCase = mockk()

    private val testDispatcher = StandardTestDispatcher()

    // Test data
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

    // === selectPet(String) Tests ===

    @Test
    fun `selectPet with pet_dolphin maps to DOLPHIN`() =
        runTest {
            // Given
            coEvery { selectPetUseCase(PetType.DOLPHIN) } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_dolphin")
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { selectPetUseCase(PetType.DOLPHIN) }
        }

    @Test
    fun `selectPet with pet_cat maps to CAT`() =
        runTest {
            // Given
            val state = tutorialState.copy(selectedPet = PetType.CAT)
            coEvery { selectPetUseCase(PetType.CAT) } returns state
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_cat")
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { selectPetUseCase(PetType.CAT) }
        }

    @Test
    fun `selectPet with pet_dog maps to DOG`() =
        runTest {
            // Given
            val state = tutorialState.copy(selectedPet = PetType.DOG)
            coEvery { selectPetUseCase(PetType.DOG) } returns state
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_dog")
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { selectPetUseCase(PetType.DOG) }
        }

    @Test
    fun `selectPet with pet_fox maps to FOX`() =
        runTest {
            // Given
            val state = tutorialState.copy(selectedPet = PetType.FOX)
            coEvery { selectPetUseCase(PetType.FOX) } returns state
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_fox")
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { selectPetUseCase(PetType.FOX) }
        }

    @Test
    fun `selectPet with unknown ID defaults to DOLPHIN`() =
        runTest {
            // Given
            coEvery { selectPetUseCase(PetType.DOLPHIN) } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("unknown_pet")
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { selectPetUseCase(PetType.DOLPHIN) }
        }

    @Test
    fun `selectPet loads tutorial words after selection`() =
        runTest {
            // Given
            coEvery { selectPetUseCase(PetType.DOLPHIN) } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_dolphin")
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { getTutorialWordsUseCase() }
            assertTrue(viewModel.uiState.value is OnboardingUiState.Tutorial)
        }

    @Test
    fun `selectPet on exception emits Error state`() =
        runTest {
            // Given
            coEvery { selectPetUseCase(any()) } throws IllegalStateException("Pet selection failed")

            // When
            viewModel.selectPet("pet_dolphin")
            advanceUntilIdle()

            // Then
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Error)
            val error = result as OnboardingUiState.Error
            assertEquals("Pet selection failed", error.message)
            assertTrue(error.recoverable)
        }

    // === selectPetByType Tests ===

    @Test
    fun `selectPetByType with DOLPHIN calls useCase`() =
        runTest {
            // Given
            coEvery { selectPetUseCase(PetType.DOLPHIN) } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPetByType(PetType.DOLPHIN)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { selectPetUseCase(PetType.DOLPHIN) }
            assertTrue(viewModel.uiState.value is OnboardingUiState.Tutorial)
        }

    @Test
    fun `selectPetByType with CAT calls useCase`() =
        runTest {
            // Given
            val state = tutorialState.copy(selectedPet = PetType.CAT)
            coEvery { selectPetUseCase(PetType.CAT) } returns state
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPetByType(PetType.CAT)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { selectPetUseCase(PetType.CAT) }
        }

    @Test
    fun `selectPetByType on exception emits Error state`() =
        runTest {
            // Given
            coEvery { selectPetUseCase(any()) } throws IllegalStateException("Failed")

            // When
            viewModel.selectPetByType(PetType.DOLPHIN)
            advanceUntilIdle()

            // Then
            val result = viewModel.uiState.value
            assertTrue(result is OnboardingUiState.Error)
            val error = result as OnboardingUiState.Error
            assertEquals("Failed", error.message)
        }

    // === petIdToPetType Tests (via selectPet) ===

    @Test
    fun `pet_dolphin maps to DOLPHIN via selectPet`() =
        runTest {
            // Given
            coEvery { selectPetUseCase(PetType.DOLPHIN) } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_dolphin")
            advanceUntilIdle()

            // Then
            coVerify { selectPetUseCase(PetType.DOLPHIN) }
        }

    @Test
    fun `pet_cat maps to CAT via selectPet`() =
        runTest {
            // Given
            val state = tutorialState.copy(selectedPet = PetType.CAT)
            coEvery { selectPetUseCase(PetType.CAT) } returns state
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_cat")
            advanceUntilIdle()

            // Then
            coVerify { selectPetUseCase(PetType.CAT) }
        }

    @Test
    fun `pet_dog maps to DOG via selectPet`() =
        runTest {
            // Given
            val state = tutorialState.copy(selectedPet = PetType.DOG)
            coEvery { selectPetUseCase(PetType.DOG) } returns state
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_dog")
            advanceUntilIdle()

            // Then
            coVerify { selectPetUseCase(PetType.DOG) }
        }

    @Test
    fun `pet_fox maps to FOX via selectPet`() =
        runTest {
            // Given
            val state = tutorialState.copy(selectedPet = PetType.FOX)
            coEvery { selectPetUseCase(PetType.FOX) } returns state
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("pet_fox")
            advanceUntilIdle()

            // Then
            coVerify { selectPetUseCase(PetType.FOX) }
        }

    @Test
    fun `unknown pet ID defaults to DOLPHIN via selectPet`() =
        runTest {
            // Given
            coEvery { selectPetUseCase(PetType.DOLPHIN) } returns tutorialState
            coEvery { getTutorialWordsUseCase() } returns tutorialWords

            // When
            viewModel.selectPet("unknown")
            advanceUntilIdle()

            // Then
            coVerify { selectPetUseCase(PetType.DOLPHIN) }
        }

    // === petTypeToId Tests ===

    @Test
    fun `petTypeToId DOLPHIN returns pet_dolphin`() {
        assertEquals("pet_dolphin", viewModel.petTypeToId(PetType.DOLPHIN))
    }

    @Test
    fun `petTypeToId CAT returns pet_cat`() {
        assertEquals("pet_cat", viewModel.petTypeToId(PetType.CAT))
    }

    @Test
    fun `petTypeToId DOG returns pet_dog`() {
        assertEquals("pet_dog", viewModel.petTypeToId(PetType.DOG))
    }

    @Test
    fun `petTypeToId FOX returns pet_fox`() {
        assertEquals("pet_fox", viewModel.petTypeToId(PetType.FOX))
    }
}
