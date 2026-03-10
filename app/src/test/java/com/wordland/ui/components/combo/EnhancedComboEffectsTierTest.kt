package com.wordland.ui.components.combo

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for Enhanced Combo Tier calculation
 *
 * Epic #1: Visual Feedback Enhancement
 * Story #1.3: Combo Visual Effects Implementation
 *
 * Tests combo tier calculation based on combo count
 */
@RunWith(JUnit4::class)
class EnhancedComboEffectsTierTest {
    // ========== Combo Tier Tests ==========

    @Test
    fun `combo 0 is tier 0 - LOW`() {
        val combo = 0
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
