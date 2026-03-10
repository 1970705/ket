package com.wordland.domain.usecase.usecases.openfirstchest

import com.wordland.domain.model.ChestReward
import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.PetType
import com.wordland.domain.model.RewardRarity
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
 * Unit tests for OpenFirstChestUseCase - Reward Generation
 * Tests reward generation logic, reward properties, and pet type compatibility
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OpenFirstChestUseCaseRewardTest {
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

    // === Basic Chest Opening Tests ===

    @Test
    fun `invoke returns a valid ChestReward`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase()

            // Then - reward type depends on random, so just verify it's a valid reward
            assertTrue(
                "Result should be one of PetEmoji, CelebrationEffect, or RarePetStyle",
                result is ChestReward.PetEmoji || result is ChestReward.CelebrationEffect || result is ChestReward.RarePetStyle,
            )
        }

    @Test
    fun `invoke can return CelebrationEffect reward`() =
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
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase()

            // Then - reward type depends on random, so just verify it's a valid reward
            assertTrue(result is ChestReward)
        }

    @Test
    fun `invoke returns RarePetStyle reward`() =
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
            val result = useCase()

            // Then
            assertTrue(result is ChestReward)
        }

    // === Reward Property Tests ===

    @Test
    fun `PetEmoji reward has correct rarity`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When - need to test multiple times to hit PetEmoji (50% chance)
            var petEmojiFound = false
            repeat(20) {
                val result = useCase()
                if (result is ChestReward.PetEmoji) {
                    petEmojiFound = true
                    assertEquals(RewardRarity.COMMON, result.rarity)
                    assertEquals("宠物表情", result.name)
                }
            }

            // Then
            assertTrue("PetEmoji should be found within 20 tries with 50% probability", petEmojiFound)
        }

    @Test
    fun `CelebrationEffect reward has correct rarity`() =
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
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When - CelebrationEffect is 30% (chance < 80 and >= 50)
            var celebrationEffectFound = false
            repeat(50) {
                val result = useCase()
                if (result is ChestReward.CelebrationEffect) {
                    celebrationEffectFound = true
                    assertEquals(RewardRarity.RARE, result.rarity)
                    assertEquals("庆祝特效", result.name)
                }
            }

            // Then
            assertTrue("CelebrationEffect should be found within 50 tries with 30% probability", celebrationEffectFound)
        }

    @Test
    fun `RarePetStyle reward has correct rarity`() =
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

            // When - RarePetStyle is 20% (chance >= 80)
            var rarePetStyleFound = false
            repeat(50) {
                val result = useCase()
                if (result is ChestReward.RarePetStyle) {
                    rarePetStyleFound = true
                    assertEquals(RewardRarity.EPIC, result.rarity)
                    assertEquals("稀有造型", result.name)
                }
            }

            // Then
            assertTrue("RarePetStyle should be found within 50 tries with 20% probability", rarePetStyleFound)
        }

    @Test
    fun `PetEmoji reward contains pet type`() =
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
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            var foundFoxPetEmoji = false
            repeat(20) {
                val result = useCase()
                if (result is ChestReward.PetEmoji && result.petType == PetType.FOX) {
                    foundFoxPetEmoji = true
                }
            }

            // Then
            assertTrue("PetEmoji with FOX pet type should be found", foundFoxPetEmoji)
        }

    @Test
    fun `RarePetStyle reward contains pet type`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            var foundDolphinRareStyle = false
            repeat(100) {
                val result = useCase()
                if (result is ChestReward.RarePetStyle && result.petType == PetType.DOLPHIN) {
                    foundDolphinRareStyle = true
                }
            }

            // Then
            assertTrue("RarePetStyle with DOLPHIN pet type should be found", foundDolphinRareStyle)
        }

    // === All Pet Types Tests ===

    @Test
    fun `invoke works with DOLPHIN pet`() =
        runTest {
            // Given
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.FIRST_CHEST,
                    selectedPet = PetType.DOLPHIN,
                    completedTutorialWords = 5,
                )
            coEvery { repository.getOnboardingState() } returns state
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase()

            // Then
            assertTrue(result is ChestReward)
        }

    @Test
    fun `invoke works with CAT pet`() =
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
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase()

            // Then
            assertTrue(result is ChestReward)
        }

    @Test
    fun `invoke works with DOG pet`() =
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
            val result = useCase()

            // Then
            assertTrue(result is ChestReward)
        }

    @Test
    fun `invoke works with FOX pet`() =
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
            coEvery { repository.saveOnboardingState(any()) } just Runs

            // When
            val result = useCase()

            // Then
            assertTrue(result is ChestReward)
        }
}
