package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for OnboardingState model
 * Tests data class properties and enum values
 */
class OnboardingStateTest {
    // === OnboardingState Tests ===

    @Test
    fun `OnboardingState has correct default values`() {
        val state =
            OnboardingState(
                userId = "user_001",
                currentPhase = OnboardingPhase.WELCOME,
                selectedPet = null,
            )

        assertEquals("user_001", state.userId)
        assertEquals(OnboardingPhase.WELCOME, state.currentPhase)
        assertNull(state.selectedPet)
        assertEquals(0, state.completedTutorialWords)
        assertEquals(0L, state.lastOpenedChest)
        assertEquals(0, state.totalStars)
        assertTrue(state.createdAt > 0)
        assertTrue(state.updatedAt > 0)
    }

    @Test
    fun `OnboardingState copy with completedTutorialWords updates correctly`() {
        val original =
            OnboardingState(
                userId = "user_001",
                currentPhase = OnboardingPhase.TUTORIAL,
                selectedPet = PetType.DOLPHIN,
                completedTutorialWords = 2,
            )

        val updated = original.copy(completedTutorialWords = 5)

        assertEquals(5, updated.completedTutorialWords)
        assertEquals("user_001", updated.userId) // Other fields preserved
        assertEquals(PetType.DOLPHIN, updated.selectedPet)
    }

    @Test
    fun `OnboardingState copy with selectedPet updates correctly`() {
        val original =
            OnboardingState(
                userId = "user_001",
                currentPhase = OnboardingPhase.WELCOME,
                selectedPet = null,
            )

        val updated = original.copy(selectedPet = PetType.CAT)

        assertEquals(PetType.CAT, updated.selectedPet)
        assertEquals(OnboardingPhase.WELCOME, updated.currentPhase) // Other fields preserved
    }

    @Test
    fun `OnboardingState copy with currentPhase updates correctly`() {
        val original =
            OnboardingState(
                userId = "user_001",
                currentPhase = OnboardingPhase.TUTORIAL,
                selectedPet = PetType.DOG,
            )

        val updated = original.copy(currentPhase = OnboardingPhase.FIRST_CHEST)

        assertEquals(OnboardingPhase.FIRST_CHEST, updated.currentPhase)
        assertEquals(PetType.DOG, updated.selectedPet) // Other fields preserved
    }

    @Test
    fun `OnboardingState copy with totalStars accumulates correctly`() {
        val original =
            OnboardingState(
                userId = "user_001",
                currentPhase = OnboardingPhase.TUTORIAL,
                selectedPet = PetType.FOX,
                completedTutorialWords = 1,
                totalStars = 3,
            )

        val updated = original.copy(totalStars = 6)

        assertEquals(6, updated.totalStars)
    }

    // === OnboardingPhase Enum Tests ===

    @Test
    fun `OnboardingPhase has all required values`() {
        val expectedValues =
            listOf(
                OnboardingPhase.NOT_STARTED,
                OnboardingPhase.WELCOME,
                OnboardingPhase.PET_SELECTION,
                OnboardingPhase.TUTORIAL,
                OnboardingPhase.FIRST_CHEST,
                OnboardingPhase.COMPLETED,
            )

        assertEquals(6, expectedValues.size)
    }

    @Test
    fun `OnboardingPhase entries contains all values`() {
        val entries = OnboardingPhase.entries

        assertEquals(6, entries.size)
        assertTrue(OnboardingPhase.NOT_STARTED in entries)
        assertTrue(OnboardingPhase.WELCOME in entries)
        assertTrue(OnboardingPhase.PET_SELECTION in entries)
        assertTrue(OnboardingPhase.TUTORIAL in entries)
        assertTrue(OnboardingPhase.FIRST_CHEST in entries)
        assertTrue(OnboardingPhase.COMPLETED in entries)
    }

    @Test
    fun `OnboardingPhase enum constants are ordered`() {
        val entries = OnboardingPhase.entries

        assertEquals(OnboardingPhase.NOT_STARTED, entries[0])
        assertEquals(OnboardingPhase.WELCOME, entries[1])
        assertEquals(OnboardingPhase.PET_SELECTION, entries[2])
        assertEquals(OnboardingPhase.TUTORIAL, entries[3])
        assertEquals(OnboardingPhase.FIRST_CHEST, entries[4])
        assertEquals(OnboardingPhase.COMPLETED, entries[5])
    }

    // === PetType Enum Tests ===

    @Test
    fun `PetType has exactly 4 values`() {
        assertEquals(4, PetType.entries.size)
    }

    @Test
    fun `PetType contains all required pets`() {
        val entries = PetType.entries

        assertTrue(PetType.DOLPHIN in entries)
        assertTrue(PetType.CAT in entries)
        assertTrue(PetType.DOG in entries)
        assertTrue(PetType.FOX in entries)
    }

    @Test
    fun `PetType DOLPHIN is first entry`() {
        assertEquals(PetType.DOLPHIN, PetType.entries[0])
    }

    @Test
    fun `PetType FOX is last entry`() {
        assertEquals(PetType.FOX, PetType.entries[3])
    }

    // === PetType Extension Function Tests ===

    @Test
    fun `getDisplayName returns correct name for DOLPHIN`() {
        assertEquals("海豚", PetType.DOLPHIN.getDisplayName())
    }

    @Test
    fun `getDisplayName returns correct name for CAT`() {
        assertEquals("猫咪", PetType.CAT.getDisplayName())
    }

    @Test
    fun `getDisplayName returns correct name for DOG`() {
        assertEquals("小狗", PetType.DOG.getDisplayName())
    }

    @Test
    fun `getDisplayName returns correct name for FOX`() {
        assertEquals("狐狸", PetType.FOX.getDisplayName())
    }

    @Test
    fun `getEmoji returns correct emoji for DOLPHIN`() {
        assertEquals("\uD83D\uDC2C", PetType.DOLPHIN.getEmoji()) // 🐬
    }

    @Test
    fun `getEmoji returns correct emoji for CAT`() {
        assertEquals("\uD83D\uDC31", PetType.CAT.getEmoji()) // 🐱
    }

    @Test
    fun `getEmoji returns correct emoji for DOG`() {
        assertEquals("\uD83D\uDC36", PetType.DOG.getEmoji()) // 🐶
    }

    @Test
    fun `getEmoji returns correct emoji for FOX`() {
        assertEquals("\uD83E\uDD8A", PetType.FOX.getEmoji()) // 🦊
    }

    // === Combination Tests ===

    @Test
    fun `OnboardingState with all pet types`() {
        PetType.entries.forEach { petType ->
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = OnboardingPhase.TUTORIAL,
                    selectedPet = petType,
                )

            assertEquals(petType, state.selectedPet)
        }
    }

    @Test
    fun `OnboardingState with all phases`() {
        OnboardingPhase.entries.forEach { phase ->
            val state =
                OnboardingState(
                    userId = "user_001",
                    currentPhase = phase,
                    selectedPet = PetType.DOLPHIN,
                )

            assertEquals(phase, state.currentPhase)
        }
    }
}
