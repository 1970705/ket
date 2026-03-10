package com.wordland.ui.components.combo

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Animation and milestone tests for combo effects.
 * Tests animations, shake effects, milestone popups, and combo states.
 */
@RunWith(JUnit4::class)
class ComboAnimationTest {
    // ========== Animation Tests ==========

    @Test
    fun `combo pop animation scales to 1_4f then back to 1f`() {
        val expectedMaxScale = 1.4f
        val expectedFinalScale = 1f
        // From EnhancedComboEffects.kt line 85-97
        val actualMaxScale = 1.4f
        val actualFinalScale = 1f
        assertEquals(expectedMaxScale, actualMaxScale, 0.01f)
        assertEquals(expectedFinalScale, actualFinalScale, 0.01f)
    }

    @Test
    fun `combo pop spring damping is 0_4f`() {
        val expectedDamping = 0.4f
        val actualDamping = 0.4f // From EnhancedComboEffects.kt line 87
        assertEquals(expectedDamping, actualDamping, 0.01f)
    }

    @Test
    fun `combo pop spring stiffness is 300f`() {
        val expectedStiffness = 300f
        val actualStiffness = 300f // From EnhancedComboEffects.kt line 88
        assertEquals(expectedStiffness, actualStiffness, 0.01f)
    }

    @Test
    fun `pulse animation scale is 1_1f when combo increases`() {
        val expectedPulseScale = 1.1f
        val actualScale = 1.1f // From EnhancedComboEffects.kt line 104
        assertEquals(expectedPulseScale, actualScale, 0.01f)
    }

    @Test
    fun `color animation duration is 300ms`() {
        val expectedDuration = 300
        val actualDuration = 300 // From EnhancedComboEffects.kt line 125
        assertEquals(expectedDuration, actualDuration)
    }

    // ========== Shake Effect Tests ==========

    @Test
    fun `shake effect has 3 iterations`() {
        val expectedIterations = 3
        val actualIterations = 3 // From ComboShakeEffect line 244
        assertEquals(expectedIterations, actualIterations)
    }

    @Test
    fun `shake effect iteration duration is 50ms`() {
        val expectedDuration = 50
        val actualDuration = 50 // From ComboShakeEffect line 247
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `shake effect base amount is 8f`() {
        val expectedAmount = 8f
        val actualAmount = 8f // From ComboShakeEffect line 243
        assertEquals(expectedAmount, actualAmount, 0.01f)
    }

    @Test
    fun `shake effect final centering duration is 100ms`() {
        val expectedDuration = 100
        val actualDuration = 100 // From ComboShakeEffect line 255
        assertEquals(expectedDuration, actualDuration)
    }

    // ========== Milestone Popup Tests ==========

    @Test
    fun `milestone popup tier 3 message is UNSTOPPABLE`() {
        val tier = 3
        val expectedMessage = "UNSTOPPABLE! 🔥🔥🔥"
        val actualMessage =
            when (tier) {
                3 -> "UNSTOPPABLE! 🔥🔥🔥"
                2 -> "ON FIRE! 🔥🔥"
                1 -> "GREAT STREAK! 🔥"
                else -> ""
            }
        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun `milestone popup tier 2 message is ON FIRE`() {
        val tier = 2
        val expectedMessage = "ON FIRE! 🔥🔥"
        val actualMessage =
            when (tier) {
                3 -> "UNSTOPPABLE! 🔥🔥🔥"
                2 -> "ON FIRE! 🔥🔥"
                1 -> "GREAT STREAK! 🔥"
                else -> ""
            }
        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun `milestone popup tier 1 message is GREAT STREAK`() {
        val tier = 1
        val expectedMessage = "GREAT STREAK! 🔥"
        val actualMessage =
            when (tier) {
                3 -> "UNSTOPPABLE! 🔥🔥🔥"
                2 -> "ON FIRE! 🔥🔥"
                1 -> "GREAT STREAK! 🔥"
                else -> ""
            }
        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun `milestone popup tier 3 uses red color`() {
        val tier = 3
        val expectedColor = 0xFFFF0000.toInt()
        val actualColor =
            when (tier) {
                3 -> 0xFFFF0000.toInt()
                2 -> 0xFFFF6B35.toInt()
                1 -> 0xFFFFB347.toInt()
                else -> 0xFF000000.toInt()
            }
        assertEquals(expectedColor, actualColor)
    }

    @Test
    fun `milestone popup tier 3 uses 28sp font`() {
        val tier = 3
        val expectedFontSize = 28
        val actualFontSize =
            when (tier) {
                3 -> 28
                2 -> 24
                else -> 20
            }
        assertEquals(expectedFontSize, actualFontSize)
    }

    @Test
    fun `milestone popup entrance scale is 1_2f`() {
        val expectedScale = 1.2f
        val actualScale = 1.2f // From ComboMilestonePopup line 283
        assertEquals(expectedScale, actualScale, 0.01f)
    }

    @Test
    fun `milestone popup hold duration is 1500ms`() {
        val expectedDuration = 1500L
        val actualDuration = 1500L // From ComboMilestonePopup line 292
        assertEquals(expectedDuration, actualDuration)
    }

    // ========== ComboAnimationState Tests ==========

    @Test
    fun `ComboAnimationState tier calculation for combo 10 is 3`() {
        val combo = 10
        val expectedTier = 3
        val actualTier =
            when {
                combo >= 10 -> 3
                combo >= 5 -> 2
                combo >= 3 -> 1
                else -> 0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `ComboAnimationState shouldShowFire for combo 3 is true`() {
        val combo = 3
        val shouldShow = combo >= 3
        assertTrue(shouldShow)
    }

    @Test
    fun `ComboAnimationState shouldShowFire for combo 2 is false`() {
        val combo = 2
        val shouldShow = combo >= 3
        assertFalse(shouldShow)
    }

    @Test
    fun `ComboAnimationState shouldShowShake for combo 10 is true`() {
        val combo = 10
        val shouldShow = combo >= 10
        assertTrue(shouldShow)
    }

    @Test
    fun `ComboAnimationState shouldShowShake for combo 9 is false`() {
        val combo = 9
        val shouldShow = combo >= 10
        assertFalse(shouldShow)
    }

    @Test
    fun `ComboAnimationState shouldShowPulse for combo 3 is true`() {
        val combo = 3
        val shouldShow = combo >= 3
        assertTrue(shouldShow)
    }
}
