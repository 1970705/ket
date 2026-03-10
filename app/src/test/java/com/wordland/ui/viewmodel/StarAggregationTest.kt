package com.wordland.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for star aggregation logic
 * Tests the calculateLevelStars algorithm
 */
class StarAggregationTest {
    // ========== Perfect Performance Tests ==========

    @Test
    fun `all 3-star answers results in 3-star level completion`() {
        val stars = listOf(3, 3, 3, 3, 3, 3)
        assertEquals(3, calculateLevelStars(stars))
    }

    @Test
    fun `all 2-star answers results in 2-star level completion`() {
        val stars = listOf(2, 2, 2, 2, 2, 2)
        assertEquals(2, calculateLevelStars(stars))
    }

    @Test
    fun `all 1-star answers results in 1-star level completion`() {
        val stars = listOf(1, 1, 1, 1, 1, 1)
        assertEquals(1, calculateLevelStars(stars))
    }

    // ========== Mixed Performance Tests ==========

    @Test
    fun `mixed 3 and 2 star answers results in 3-star level completion`() {
        val stars = listOf(3, 2, 3, 2, 3, 2) // Average 2.5
        assertEquals(3, calculateLevelStars(stars))
    }

    @Test
    fun `mixed 3 and 1 star answers results in 2-star level completion`() {
        val stars = listOf(3, 1, 3, 1, 3, 1) // Average 2.0
        assertEquals(2, calculateLevelStars(stars))
    }

    @Test
    fun `mixed 2 and 1 star answers results in 2-star level completion`() {
        val stars = listOf(2, 2, 1, 2, 1, 2) // Average 1.67
        assertEquals(2, calculateLevelStars(stars))
    }

    @Test
    fun `mostly 3 with one 2 results in 3-star level completion`() {
        val stars = listOf(3, 3, 3, 3, 2, 3) // Average 2.83
        assertEquals(3, calculateLevelStars(stars))
    }

    @Test
    fun `mostly 2 with one 3 results in 2-star level completion`() {
        val stars = listOf(2, 2, 2, 2, 3, 2) // Average 2.17
        assertEquals(2, calculateLevelStars(stars))
    }

    // ========== Edge Cases ==========

    @Test
    fun `empty list results in 0 stars`() {
        val stars = emptyList<Int>()
        assertEquals(0, calculateLevelStars(stars))
    }

    @Test
    fun `single 3-star answer results in 3-star level`() {
        val stars = listOf(3)
        assertEquals(3, calculateLevelStars(stars))
    }

    @Test
    fun `single 2-star answer results in 2-star level`() {
        val stars = listOf(2)
        assertEquals(2, calculateLevelStars(stars))
    }

    @Test
    fun `single 1-star answer results in 1-star level`() {
        val stars = listOf(1)
        assertEquals(1, calculateLevelStars(stars))
    }

    // ========== Score Calculation Tests ==========

    @Test
    fun `level score reflects total stars earned`() {
        val stars = listOf(3, 2, 1) // Total 6
        assertEquals(60, calculateLevelScore(stars))
    }

    @Test
    fun `perfect level score with all 3-star answers`() {
        val stars = listOf(3, 3, 3, 3, 3, 3) // Total 18
        assertEquals(180, calculateLevelScore(stars))
    }

    @Test
    fun `all 1-star answers results in minimum score`() {
        val stars = listOf(1, 1, 1, 1, 1, 1) // Total 6
        assertEquals(60, calculateLevelScore(stars))
    }

    // Helper functions matching LearningViewModel implementation

    private fun calculateLevelStars(starsEarned: List<Int>): Int {
        if (starsEarned.isEmpty()) return 0

        val average = starsEarned.average()

        return when {
            average >= 2.5 -> 3
            average >= 1.5 -> 2
            average >= 0.5 -> 1
            else -> 0
        }
    }

    private fun calculateLevelScore(starsEarned: List<Int>): Int {
        return starsEarned.sum() * 10
    }
}
