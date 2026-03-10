package com.wordland.ui.components

import com.wordland.domain.model.ComboState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for ComboIndicator component logic
 *
 * Tests combo state calculations
 */
@RunWith(JUnit4::class)
class ComboIndicatorTest {
    @Test
    fun comboState_zeroComboNotActive() {
        val comboState =
            ComboState(
                consecutiveCorrect = 0,
            )

        assertFalse(comboState.isActive)
        assertEquals(0, comboState.consecutiveCorrect)
    }

    @Test
    fun comboState_singleComboIsActive() {
        val comboState =
            ComboState(
                consecutiveCorrect = 1,
            )

        assertTrue(comboState.isActive)
        assertEquals(1, comboState.consecutiveCorrect)
    }

    @Test
    fun comboState_threeComboIsAtMilestone() {
        val comboState =
            ComboState(
                consecutiveCorrect = 3,
            )

        assertTrue(comboState.isAtMilestone)
    }

    @Test
    fun comboState_fiveComboIsHighCombo() {
        val comboState =
            ComboState(
                consecutiveCorrect = 5,
            )

        assertTrue(comboState.isHighCombo)
    }

    @Test
    fun comboState_tenComboIsHighCombo() {
        val comboState =
            ComboState(
                consecutiveCorrect = 10,
            )

        assertTrue(comboState.isHighCombo)
    }

    @Test
    fun comboState_multiplayerBadge_forThreeCombo() {
        val comboState =
            ComboState(
                consecutiveCorrect = 3,
            )

        val multiplierBadge =
            if (comboState.consecutiveCorrect >= 5) {
                "×1.5"
            } else if (comboState.consecutiveCorrect >= 3) {
                "×1.2"
            } else {
                null
            }

        assertEquals("×1.2", multiplierBadge)
    }

    @Test
    fun comboState_multiplayerBadge_forFiveCombo() {
        val comboState =
            ComboState(
                consecutiveCorrect = 5,
            )

        val multiplierBadge =
            if (comboState.consecutiveCorrect >= 5) {
                "×1.5"
            } else if (comboState.consecutiveCorrect >= 3) {
                "×1.2"
            } else {
                null
            }

        assertEquals("×1.5", multiplierBadge)
    }

    @Test
    fun comboState_noMultiplierForLowCombo() {
        val comboState =
            ComboState(
                consecutiveCorrect = 2,
            )

        val multiplierBadge =
            if (comboState.consecutiveCorrect >= 5) {
                "×1.5"
            } else if (comboState.consecutiveCorrect >= 3) {
                "×1.2"
            } else {
                null
            }

        assertEquals(null, multiplierBadge)
    }

    @Test
    fun comboMilestone_threeIsMilestone() {
        val comboCount = 3
        val isMilestone = comboCount in listOf(3, 5)
        assertTrue(isMilestone)
    }

    @Test
    fun comboMilestone_fiveIsMilestone() {
        val comboCount = 5
        val isMilestone = comboCount in listOf(3, 5)
        assertTrue(isMilestone)
    }

    @Test
    fun comboMilestone_twoIsNotMilestone() {
        val comboCount = 2
        val isMilestone = comboCount in listOf(3, 5)
        assertFalse(isMilestone)
    }
}
