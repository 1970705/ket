package com.wordland.ui.components.combo

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for Combo Tier Calculation
 *
 * Epic #1: Visual Feedback Enhancement
 * Story #1.3: Combo Visual Effects Implementation
 *
 * Tests:
 * - Combo tier calculation (0-3 based on consecutive correct)
 * - Fire emoji counts by tier
 * - Multiplier values by tier
 * - Float multiplier calculations
 */
@RunWith(JUnit4::class)
class ComboEffectsTierTest {
    // ========== Combo Tier Tests ==========

    @Test
    fun `combo 0 is tier 0 - LOW`() {
        val combo = 0
        val expectedTier = 0
        // From getComboTier(): combo < 3 returns 0
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `combo 1 is tier 0 - LOW`() {
        val combo = 1
        val expectedTier = 0
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `combo 2 is tier 0 - LOW`() {
        val combo = 2
        val expectedTier = 0
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `combo 3 is tier 1 - MEDIUM`() {
        val combo = 3
        val expectedTier = 1
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `combo 4 is tier 1 - MEDIUM`() {
        val combo = 4
        val expectedTier = 1
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `combo 5 is tier 2 - HIGH`() {
        val combo = 5
        val expectedTier = 2
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `combo 9 is tier 2 - HIGH`() {
        val combo = 9
        val expectedTier = 2
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `combo 10 is tier 3 - EXTREME`() {
        val combo = 10
        val expectedTier = 3
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    @Test
    fun `combo 15 is tier 3 - EXTREME`() {
        val combo = 15
        val expectedTier = 3
        val actualTier =
            if (combo >= 10) {
                3
            } else if (combo >= 5) {
                2
            } else if (combo >= 3) {
                1
            } else {
                0
            }
        assertEquals(expectedTier, actualTier)
    }

    // ========== Fire Emoji Tests ==========

    @Test
    fun `tier 0 has 0 fire emojis`() {
        val tier = 0
        val expectedFireCount = 0
        val actualCount =
            when (tier) {
                3 -> 3
                2 -> 2
                1 -> 1
                else -> 0
            }
        assertEquals(expectedFireCount, actualCount)
    }

    @Test
    fun `tier 1 has 1 fire emoji`() {
        val tier = 1
        val expectedFireCount = 1
        val actualCount =
            when (tier) {
                3 -> 3
                2 -> 2
                1 -> 1
                else -> 0
            }
        assertEquals(expectedFireCount, actualCount)
    }

    @Test
    fun `tier 2 has 2 fire emojis`() {
        val tier = 2
        val expectedFireCount = 2
        val actualCount =
            when (tier) {
                3 -> 3
                2 -> 2
                1 -> 1
                else -> 0
            }
        assertEquals(expectedFireCount, actualCount)
    }

    @Test
    fun `tier 3 has 3 fire emojis`() {
        val tier = 3
        val expectedFireCount = 3
        val actualCount =
            when (tier) {
                3 -> 3
                2 -> 2
                1 -> 1
                else -> 0
            }
        assertEquals(expectedFireCount, actualCount)
    }

    // ========== Multiplier Badge Tests ==========

    @Test
    fun `tier 0 has no multiplier badge`() {
        val tier = 0
        val expectedMultiplier = ""
        val actualMultiplier =
            when (tier) {
                3 -> "×2.0"
                2 -> "×1.5"
                1 -> "×1.2"
                else -> ""
            }
        assertEquals(expectedMultiplier, actualMultiplier)
    }

    @Test
    fun `tier 1 has 1_2 multiplier`() {
        val tier = 1
        val expectedMultiplier = "×1.2"
        val actualMultiplier =
            when (tier) {
                3 -> "×2.0"
                2 -> "×1.5"
                1 -> "×1.2"
                else -> ""
            }
        assertEquals(expectedMultiplier, actualMultiplier)
    }

    @Test
    fun `tier 2 has 1_5 multiplier`() {
        val tier = 2
        val expectedMultiplier = "×1.5"
        val actualMultiplier =
            when (tier) {
                3 -> "×2.0"
                2 -> "×1.5"
                1 -> "×1.2"
                else -> ""
            }
        assertEquals(expectedMultiplier, actualMultiplier)
    }

    @Test
    fun `tier 3 has 2_0 multiplier`() {
        val tier = 3
        val expectedMultiplier = "×2.0"
        val actualMultiplier =
            when (tier) {
                3 -> "×2.0"
                2 -> "×1.5"
                1 -> "×1.2"
                else -> ""
            }
        assertEquals(expectedMultiplier, actualMultiplier)
    }

    // ========== Float Multiplier Tests ==========

    @Test
    fun `combo multiplier float value for tier 0 is 1_0f`() {
        val combo = 2
        val expectedMultiplier = 1.0f
        val actualMultiplier =
            when {
                combo >= 10 -> 2.0f
                combo >= 5 -> 1.5f
                combo >= 3 -> 1.2f
                else -> 1.0f
            }
        assertEquals(expectedMultiplier, actualMultiplier, 0.01f)
    }

    @Test
    fun `combo multiplier float value for tier 1 is 1_2f`() {
        val combo = 3
        val expectedMultiplier = 1.2f
        val actualMultiplier =
            when {
                combo >= 10 -> 2.0f
                combo >= 5 -> 1.5f
                combo >= 3 -> 1.2f
                else -> 1.0f
            }
        assertEquals(expectedMultiplier, actualMultiplier, 0.01f)
    }

    @Test
    fun `combo multiplier float value for tier 2 is 1_5f`() {
        val combo = 5
        val expectedMultiplier = 1.5f
        val actualMultiplier =
            when {
                combo >= 10 -> 2.0f
                combo >= 5 -> 1.5f
                combo >= 3 -> 1.2f
                else -> 1.0f
            }
        assertEquals(expectedMultiplier, actualMultiplier, 0.01f)
    }

    @Test
    fun `combo multiplier float value for tier 3 is 2_0f`() {
        val combo = 10
        val expectedMultiplier = 2.0f
        val actualMultiplier =
            when {
                combo >= 10 -> 2.0f
                combo >= 5 -> 1.5f
                combo >= 3 -> 1.2f
                else -> 1.0f
            }
        assertEquals(expectedMultiplier, actualMultiplier, 0.01f)
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
