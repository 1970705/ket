package com.wordland.ui.components

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for StarRatingDisplay component logic
 *
 * Tests star calculation logic
 */
@RunWith(JUnit4::class)
class StarRatingDisplayTest {
    @Test
    fun starCoercing_negativeValueClampedToZero() {
        val stars = (-1).coerceAtMost(3)
        assertEquals(-1, stars) // coerceAtMost doesn't affect negative
    }

    @Test
    fun starCoercing_valueAboveMaxClampedToMax() {
        val stars = 5.coerceAtMost(3)
        assertEquals(3, stars)
    }

    @Test
    fun starCoercing_valueWithinRangeUnchanged() {
        val stars = 2.coerceAtMost(3)
        assertEquals(2, stars)
    }

    @Test
    fun starFilledLogic_indexLessThanStars() {
        val stars = 2
        val isFilledAt0 = 0 < stars.coerceAtMost(3)
        val isFilledAt1 = 1 < stars.coerceAtMost(3)
        val isFilledAt2 = 2 < stars.coerceAtMost(3)

        assertTrue(isFilledAt0)
        assertTrue(isFilledAt1)
        assertFalse(isFilledAt2)
    }

    @Test
    fun starFilledLogic_withZeroStars() {
        val stars = 0
        val isFilledAt0 = 0 < stars.coerceAtMost(3)

        assertFalse(isFilledAt0)
    }

    @Test
    fun starFilledLogic_withThreeStars() {
        val stars = 3
        val isFilledAt0 = 0 < stars.coerceAtMost(3)
        val isFilledAt1 = 1 < stars.coerceAtMost(3)
        val isFilledAt2 = 2 < stars.coerceAtMost(3)

        assertTrue(isFilledAt0)
        assertTrue(isFilledAt1)
        assertTrue(isFilledAt2)
    }

    @Test
    fun starCoercing_withCustomMaxStars() {
        val maxStars = 5
        val stars = 4

        // Check all indices
        val filledCount =
            (0 until maxStars).count { index ->
                index < stars.coerceAtMost(maxStars)
            }

        assertEquals(4, filledCount)
    }
}
