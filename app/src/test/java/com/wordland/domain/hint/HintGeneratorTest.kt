package com.wordland.domain.hint

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for HintGenerator
 */
class HintGeneratorTest {
    private val generator = HintGenerator()

    // Level 1 Tests
    @Test
    fun `Level 1 hint shows first letter for regular word`() {
        // When
        val hint = generator.generateHint("apple", 1)

        // Then
        assertEquals("首字母: A", hint)
    }

    @Test
    fun `Level 1 hint handles empty string`() {
        // When
        val hint = generator.generateHint("", 1)

        // Then
        assertEquals("", hint)
    }

    @Test
    fun `Level 1 hint handles single character`() {
        // When
        val hint = generator.generateHint("a", 1)

        // Then
        assertEquals("首字母: A", hint)
    }

    // Level 2 Tests
    @Test
    fun `Level 2 hint shows first half for even length word`() {
        // When
        val hint = generator.generateHint("banana", 2)

        // Then
        assertTrue(hint.startsWith("前半部分: ban"))
        assertTrue(hint.contains("_"))
    }

    @Test
    fun `Level 2 hint rounds up for odd length word`() {
        // When
        val hint = generator.generateHint("apple", 2) // 5 letters, (5+1)/2 = 3

        // Then
        assertTrue(hint.startsWith("前半部分: app"))
    }

    @Test
    fun `Level 2 hint shows full word for single letter`() {
        // When
        val hint = generator.generateHint("a", 2)

        // Then
        assertEquals("前半部分: a", hint)
    }

    // Level 3 Tests
    @Test
    fun `Level 3 hint masks vowels`() {
        // When
        val hint = generator.generateHint("apple", 3)

        // Then
        assertTrue(hint.contains("_")) // Vowels should be masked
        assertTrue(hint.contains("p")) // Consonants shown
        assertTrue(hint.contains("l"))
    }

    @Test
    fun `Level 3 hint masks all vowels in banana`() {
        // When
        val hint = generator.generateHint("banana", 3)

        // Then
        assertEquals("完整单词（元音隐藏）: b_n_n_", hint)
    }

    @Test
    fun `Level 3 hint handles word with no vowels`() {
        // When
        val hint = generator.generateHint("rhythm", 3)

        // Then - no vowels to mask, should show full word
        assertEquals("完整单词（元音隐藏）: rhythm", hint)
    }

    // Adaptive Hint Tests
    @Test
    fun `adaptive hint for short words skips to level 2`() {
        // When
        val hint1 = generator.generateAdaptiveHint("cat", 1) // 3 letters

        // Then - Should skip to level 2
        assertTrue(hint1.contains("前半部分"))
    }

    @Test
    fun `adaptive hint for short words jumps to level 3 on second use`() {
        // When
        val hint2 = generator.generateAdaptiveHint("cat", 2)

        // Then
        assertTrue(hint2.contains("_"))
    }

    @Test
    fun `adaptive hint for medium words uses normal progression`() {
        // When
        val hint1 = generator.generateAdaptiveHint("banana", 1)
        val hint2 = generator.generateAdaptiveHint("banana", 2)

        // Then
        assertTrue(hint1.contains("首字母"))
        assertTrue(hint2.contains("前半部分"))
    }

    @Test
    fun `adaptive hint for long words adds intermediate level`() {
        // When
        val hint1 = generator.generateAdaptiveHint("international", 1)
        val hint2 = generator.generateAdaptiveHint("international", 2)

        // Then
        assertTrue(hint1.contains("首字母"))
        assertTrue(hint2.contains("部分提示"))
    }

    // Partial Hint Tests
    @Test
    fun `partial hint with 50 percent reveal`() {
        // When
        val hint = generator.generatePartialHint("computer", 0.5f)

        // Then
        assertTrue(hint.startsWith("提示: comp"))
        assertTrue(hint.endsWith("____"))
    }

    @Test
    fun `partial hint with 25 percent reveal`() {
        // When
        val hint = generator.generatePartialHint("computer", 0.25f)

        // Then - 25% of 8 letters = 2 letters
        assertTrue(hint.startsWith("提示: co"))
    }

    @Test
    fun `partial hint with zero percent returns empty`() {
        // When
        val hint = generator.generatePartialHint("test", 0f)

        // Then
        assertEquals("", hint)
    }

    @Test
    fun `partial hint with 100 percent reveals full word`() {
        // When
        val hint = generator.generatePartialHint("test", 1.0f)

        // Then
        assertEquals("提示: test", hint)
    }

    // Letter Count Hint Tests
    @Test
    fun `letter count hint returns correct length`() {
        // When
        val hint = generator.generateLetterCountHint("apple")

        // Then
        assertEquals("字母数量: 5", hint)
    }

    @Test
    fun `letter count hint handles empty string`() {
        // When
        val hint = generator.generateLetterCountHint("")

        // Then
        assertEquals("字母数量: 0", hint)
    }

    // Position Hint Tests
    @Test
    fun `position hint reveals selected positions`() {
        // When
        val hint = generator.generatePositionHint("apple", listOf(0, 2, 4))

        // Then - positions [0, 2, 4] in "apple" are a, p, e
        assertEquals("位置提示: a _ p _ e", hint)
    }

    @Test
    fun `position hint handles out of bounds positions`() {
        // When
        val hint = generator.generatePositionHint("cat", listOf(0, 5))

        // Then
        assertEquals("位置提示: c _ _", hint)
    }

    @Test
    fun `position hint handles empty positions list`() {
        // When
        val hint = generator.generatePositionHint("test", emptyList())

        // Then
        assertEquals("位置提示: _ _ _ _", hint)
    }

    // Edge Cases
    @Test
    fun `handles special characters in word`() {
        // When
        val hint1 = generator.generateHint("hello-world", 1)
        val hint2 = generator.generateHint("test", 2)

        // Then - should handle special characters
        assertTrue(hint1.contains("首字母"))
        assertTrue(hint2.isNotEmpty())
    }

    @Test
    fun `handles uppercase word input`() {
        // When
        val hint = generator.generateHint("APPLE", 1)

        // Then
        assertEquals("首字母: A", hint)
    }
}
