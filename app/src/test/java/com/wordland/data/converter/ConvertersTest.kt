package com.wordland.data.converter

import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.LevelStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Converters
 */
class ConvertersTest {
    private val converters = Converters()

    // String List Converters
    @Test
    fun `fromStringList converts list to comma-separated string`() {
        // Given
        val list = listOf("apple", "banana", "orange")

        // When
        val result = converters.fromStringList(list)

        // Then
        assertEquals("apple,banana,orange", result)
    }

    @Test
    fun `fromStringList returns null for null input`() {
        // When
        val result = converters.fromStringList(null)

        // Then
        assertNull(result)
    }

    @Test
    fun `fromStringList returns null for empty list`() {
        // When
        val result = converters.fromStringList(emptyList())

        // Then
        assertEquals("", result)
    }

    @Test
    fun `toStringList converts comma-separated string to list`() {
        // Given
        val string = "apple,banana,orange"

        // When
        val result = converters.toStringList(string)

        // Then
        assertEquals(listOf("apple", "banana", "orange"), result)
    }

    @Test
    fun `toStringList returns null for null input`() {
        // When
        val result = converters.toStringList(null)

        // Then
        assertNull(result)
    }

    @Test
    fun `toStringList handles single element`() {
        // Given
        val string = "apple"

        // When
        val result = converters.toStringList(string)

        // Then
        assertEquals(listOf("apple"), result)
    }

    @Test
    fun `toStringList trims whitespace`() {
        // Given
        val string = "apple, banana , orange "

        // When
        val result = converters.toStringList(string)

        // Then
        assertEquals(listOf("apple", "banana", "orange"), result)
    }

    // Long List Converters
    @Test
    fun `fromLongList converts list to comma-separated string`() {
        // Given
        val list = listOf(1L, 2L, 3L, 4L, 5L)

        // When
        val result = converters.fromLongList(list)

        // Then
        assertEquals("1,2,3,4,5", result)
    }

    @Test
    fun `fromLongList returns null for null input`() {
        // When
        val result = converters.fromLongList(null)

        // Then
        assertNull(result)
    }

    @Test
    fun `toLongList converts string to long list`() {
        // Given
        val string = "1,2,3,4,5"

        // When
        val result = converters.toLongList(string)

        // Then
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), result)
    }

    @Test
    fun `toLongList returns null for null input`() {
        // When
        val result = converters.toLongList(null)

        // Then
        assertNull(result)
    }

    @Test
    fun `toLongList handles invalid numbers gracefully`() {
        // Given
        val string = "1,abc,3"

        // When
        val result = converters.toLongList(string)

        // Then - mapNotNull filters out null values from toLongOrNull
        assertEquals(listOf(1L, 3L), result)
    }

    @Test
    fun `toLongList handles empty string`() {
        // Given
        val string = ""

        // When
        val result = converters.toLongList(string)

        // Then
        assertEquals(listOf<Long>(), result)
    }

    // LevelStatus Converters
    @Test
    fun `fromLevelStatus converts LOCKED to string`() {
        // When
        val result = converters.fromLevelStatus(LevelStatus.LOCKED)

        // Then
        assertEquals("LOCKED", result)
    }

    @Test
    fun `fromLevelStatus converts UNLOCKED to string`() {
        // When
        val result = converters.fromLevelStatus(LevelStatus.UNLOCKED)

        // Then
        assertEquals("UNLOCKED", result)
    }

    @Test
    fun `fromLevelStatus converts COMPLETED to string`() {
        // When
        val result = converters.fromLevelStatus(LevelStatus.COMPLETED)

        // Then
        assertEquals("COMPLETED", result)
    }

    @Test
    fun `fromLevelStatus returns default for null`() {
        // When
        val result = converters.fromLevelStatus(null)

        // Then
        assertEquals("LOCKED", result)
    }

    @Test
    fun `toLevelStatus converts valid string`() {
        // When
        val result = converters.toLevelStatus("UNLOCKED")

        // Then
        assertEquals(LevelStatus.UNLOCKED, result)
    }

    @Test
    fun `toLevelStatus returns LOCKED for null`() {
        // When
        val result = converters.toLevelStatus(null)

        // Then
        assertEquals(LevelStatus.LOCKED, result)
    }

    @Test
    fun `toLevelStatus returns LOCKED for invalid string`() {
        // When
        val result = converters.toLevelStatus("INVALID_STATUS")

        // Then
        assertEquals(LevelStatus.LOCKED, result)
    }

    @Test
    fun `toLevelStatus handles all valid statuses`() {
        // Test all valid LevelStatus values
        assertEquals(LevelStatus.LOCKED, converters.toLevelStatus("LOCKED"))
        assertEquals(LevelStatus.UNLOCKED, converters.toLevelStatus("UNLOCKED"))
        assertEquals(LevelStatus.IN_PROGRESS, converters.toLevelStatus("IN_PROGRESS"))
        assertEquals(LevelStatus.COMPLETED, converters.toLevelStatus("COMPLETED"))
        assertEquals(LevelStatus.PERFECT, converters.toLevelStatus("PERFECT"))
    }

    // LearningStatus Converters
    @Test
    fun `fromLearningStatus converts NEW to string`() {
        // When
        val result = converters.fromLearningStatus(LearningStatus.NEW)

        // Then
        assertEquals("NEW", result)
    }

    @Test
    fun `fromLearningStatus converts LEARNING to string`() {
        // When
        val result = converters.fromLearningStatus(LearningStatus.LEARNING)

        // Then
        assertEquals("LEARNING", result)
    }

    @Test
    fun `fromLearningStatus converts MASTERED to string`() {
        // When
        val result = converters.fromLearningStatus(LearningStatus.MASTERED)

        // Then
        assertEquals("MASTERED", result)
    }

    @Test
    fun `fromLearningStatus returns default for null`() {
        // When
        val result = converters.fromLearningStatus(null)

        // Then
        assertEquals("NEW", result)
    }

    @Test
    fun `toLearningStatus converts valid string`() {
        // When
        val result = converters.toLearningStatus("MASTERED")

        // Then
        assertEquals(LearningStatus.MASTERED, result)
    }

    @Test
    fun `toLearningStatus returns NEW for null`() {
        // When
        val result = converters.toLearningStatus(null)

        // Then
        assertEquals(LearningStatus.NEW, result)
    }

    @Test
    fun `toLearningStatus returns NEW for invalid string`() {
        // When
        val result = converters.toLearningStatus("INVALID_STATUS")

        // Then
        assertEquals(LearningStatus.NEW, result)
    }

    @Test
    fun `toLearningStatus handles all valid statuses`() {
        // Test all valid LearningStatus values
        assertEquals(LearningStatus.NEW, converters.toLearningStatus("NEW"))
        assertEquals(LearningStatus.LEARNING, converters.toLearningStatus("LEARNING"))
        assertEquals(LearningStatus.MASTERED, converters.toLearningStatus("MASTERED"))
        assertEquals(LearningStatus.NEED_REVIEW, converters.toLearningStatus("NEED_REVIEW"))
    }
}
