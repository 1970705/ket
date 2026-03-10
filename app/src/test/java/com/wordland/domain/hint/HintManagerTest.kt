package com.wordland.domain.hint

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for HintManager
 */
class HintManagerTest {
    @Test
    fun `initially allows hint usage`() {
        // Given
        val manager = HintManager()

        // When
        val (canUse, reason) = manager.canUseHint("word_001")

        // Then
        assertTrue(canUse)
        assertNull(reason)
    }

    @Test
    fun `tracks hint usage count correctly`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.maxHintsPerWord = 3

            // When
            manager.useHint("word_001")
            manager.useHint("word_001")
            manager.useHint("word_001")

            // Then
            val (canUse, _) = manager.canUseHint("word_001")
            assertFalse(canUse) // Should be at max

            val (_, remaining, total) = manager.getHintStats("word_001")
            assertEquals(0, remaining) // All 3 hints used
            assertEquals(3, total)
        }

    @Test
    fun `enforces max hints limit`() {
        // Given
        val manager = HintManager()
        manager.maxHintsPerWord = 2

        // When - use all hints
        manager.useHint("word_001")
        manager.useHint("word_001")

        // Then
        val (canUse, reason) = manager.canUseHint("word_001")
        assertFalse(canUse)
        assertNotNull(reason)
        assertTrue(reason?.contains("上限") == true)
    }

    @Test
    fun `enforces cooldown period`() {
        // Given
        val manager = HintManager()
        manager.hintCooldownMs = 2000 // 2 seconds for reliable testing

        // When - use hint
        manager.useHint("word_001")

        // Then - should be in cooldown period
        val (canUse, reason) = manager.canUseHint("word_001")
        assertFalse(canUse)
        assertNotNull(reason)
        assertTrue(reason?.contains("秒") == true)
    }

    @Test
    fun `cooldown expires after specified time`() {
        // Given
        val manager = HintManager()
        manager.hintCooldownMs = 100

        manager.useHint("word_001")

        // When - wait for cooldown
        Thread.sleep(150)

        // Then
        val (canUse, _) = manager.canUseHint("word_001")
        assertTrue(canUse)
    }

    @Test
    fun `returns correct hint level progression`() =
        runTest {
            // Given
            val manager = HintManager()

            // When
            val level1 = manager.useHint("word_001")
            val level2 = manager.useHint("word_001")
            val level3 = manager.useHint("word_001")

            // Then
            assertEquals(1, level1)
            assertEquals(2, level2)
            assertEquals(3, level3)
        }

    @Test
    fun `hint level caps at 3`() =
        runTest {
            // Given
            val manager = HintManager()

            // When - use many hints
            repeat(10) { manager.useHint("word_001") }

            // Then
            val level = manager.getCurrentHintLevel("word_001")
            assertEquals(3, level)
        }

    @Test
    fun `getRemainingHints returns correct count`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.maxHintsPerWord = 5

            // When
            manager.useHint("word_001")
            manager.useHint("word_001")

            // Then
            val remaining = manager.getRemainingHints("word_001")
            assertEquals(3, remaining)
        }

    @Test
    fun `resetHints clears usage`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.useHint("word_001")
            manager.useHint("word_001")

            // When
            manager.resetHints("word_001")

            // Then
            val (canUse, _) = manager.canUseHint("word_001")
            assertTrue(canUse)

            val level = manager.getCurrentHintLevel("word_001")
            assertEquals(0, level)
        }

    @Test
    fun `tracks consecutive usage`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.hintCooldownMs = 50 // Very short for testing

            // When - use hints in quick succession
            manager.useHint("word_001")
            Thread.sleep(60) // Wait for cooldown
            manager.useHint("word_001")

            // Then
            val usageInfo = manager.getHintStats("word_001")
            assertTrue(usageInfo.third >= 2) // At least 2 uses
        }

    @Test
    fun `isOverusingHints detects overuse pattern`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.maxHintsPerWord = 3

            // When - use all hints
            repeat(3) { manager.useHint("overused_word") }

            // Then
            assertTrue(manager.isOverusingHints("overused_word"))
        }

    @Test
    fun `isOverusingHints returns false for minimal usage`() =
        runTest {
            // Given
            val manager = HintManager()

            // When - use only 1 hint
            manager.useHint("normal_word")

            // Then
            assertFalse(manager.isOverusingHints("normal_word"))
        }

    @Test
    fun `getHintDependencyScore returns zero for no usage`() {
        // Given
        val manager = HintManager()

        // When
        val score = manager.getHintDependencyScore("new_word")

        // Then
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun `getHintDependencyScore increases with usage`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.maxHintsPerWord = 5

            // When - use hints
            manager.useHint("word_001")
            val score1 = manager.getHintDependencyScore("word_001")

            manager.useHint("word_001")
            val score2 = manager.getHintDependencyScore("word_001")

            // Then
            assertTrue(score2 > score1)
        }

    @Test
    fun `setMaxHintsForDifficulty adjusts limits correctly`() {
        // Given
        val manager = HintManager()

        // When
        manager.setMaxHintsForDifficulty(1)
        assertEquals(5, manager.maxHintsPerWord)

        manager.setMaxHintsForDifficulty(5)
        assertEquals(1, manager.maxHintsPerWord)

        manager.setMaxHintsForDifficulty(3)
        assertEquals(3, manager.maxHintsPerWord)
    }

    @Test
    fun `getHintStats returns correct triple`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.maxHintsPerWord = 4

            // When
            manager.useHint("word_001")
            manager.useHint("word_001")

            // Then
            val (used, remaining, total) = manager.getHintStats("word_001")
            assertEquals(2, used)
            assertEquals(2, remaining)
            assertEquals(4, total)
        }

    @Test
    fun `resetAll clears all usage tracking`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.useHint("word_001")
            manager.useHint("word_002")

            // When
            manager.resetAll()

            // Then
            val (canUse1, _) = manager.canUseHint("word_001")
            val (canUse2, _) = manager.canUseHint("word_002")
            assertTrue(canUse1)
            assertTrue(canUse2)
        }

    @Test
    fun `handles multiple words independently`() =
        runTest {
            // Given
            val manager = HintManager()
            manager.maxHintsPerWord = 2
            manager.hintCooldownMs = 0 // Disable cooldown for this test

            // When
            manager.useHint("word_001")
            manager.useHint("word_001")
            manager.useHint("word_002") // Different word

            // Then
            val (canUse1, _) = manager.canUseHint("word_001")
            val (canUse2, _) = manager.canUseHint("word_002")

            assertFalse(canUse1) // word_001 at max
            assertTrue(canUse2) // word_002 has hints remaining
        }
}
