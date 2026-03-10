package com.wordland.ui.screens

import com.wordland.domain.model.BubbleColor
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for MatchGameScreen
 *
 * Tests screen logic and BubbleColor enum properties.
 * Full UI tests are in MatchGameScreenUiTest (androidTest).
 */
@RunWith(JUnit4::class)
class MatchGameScreenTest {
    // === BubbleColor Color Value Tests ===

    @Test
    fun bubbleColorPinkHasCorrectValue() {
        // Given/When
        val color = BubbleColor.PINK

        // Then - PINK is 0xFFFFB6C1u
        assertEquals(0xFFFFB6C1u.toULong(), color.colorValue)
    }

    @Test
    fun bubbleColorGreenHasCorrectValue() {
        // Given/When
        val color = BubbleColor.GREEN

        // Then - GREEN is 0xFF90EE90u
        assertEquals(0xFF90EE90u.toULong(), color.colorValue)
    }

    @Test
    fun bubbleColorPurpleHasCorrectValue() {
        // Given/When
        val color = BubbleColor.PURPLE

        // Then - PURPLE is 0xFFDDA0DDu
        assertEquals(0xFFDDA0DDu.toULong(), color.colorValue)
    }

    @Test
    fun bubbleColorOrangeHasCorrectValue() {
        // Given/When
        val color = BubbleColor.ORANGE

        // Then - ORANGE is 0xFFFFA500u
        assertEquals(0xFFFFA500u.toULong(), color.colorValue)
    }

    @Test
    fun bubbleColorBrownHasCorrectValue() {
        // Given/When
        val color = BubbleColor.BROWN

        // Then - BROWN is 0xFFD2691Eu
        assertEquals(0xFFD2691Eu.toULong(), color.colorValue)
    }

    @Test
    fun bubbleColorBlueHasCorrectValue() {
        // Given/When
        val color = BubbleColor.BLUE

        // Then - BLUE is 0xFF87CEEBu
        assertEquals(0xFF87CEEBu.toULong(), color.colorValue)
    }

    @Test
    fun bubbleColorAllColorsHaveDifferentValues() {
        // Given
        val colors = BubbleColor.entries

        // When - Get all color values
        val colorValues = colors.map { it.colorValue }

        // Then - All should be unique
        val uniqueValues = colorValues.toSet()
        assertEquals(colors.size, uniqueValues.size)
    }

    @Test
    fun bubbleColorAllEntriesHaveValidValues() {
        // Given
        val colors = BubbleColor.entries

        // When/Then - All should have non-zero values
        colors.forEach { color ->
            assertTrue(color.colorValue > 0u)
        }
    }

    // === Color Utility Tests ===

    @Test
    fun bubbleColorEnumSize() {
        // Given/When
        val colors = BubbleColor.entries

        // Then - Should have 6 colors
        assertEquals(6, colors.size)
    }

    @Test
    fun bubbleColorEnumHasExpectedColors() {
        // Given/When
        val colors = BubbleColor.entries

        // Then
        assertTrue(colors.contains(BubbleColor.PINK))
        assertTrue(colors.contains(BubbleColor.GREEN))
        assertTrue(colors.contains(BubbleColor.PURPLE))
        assertTrue(colors.contains(BubbleColor.ORANGE))
        assertTrue(colors.contains(BubbleColor.BROWN))
        assertTrue(colors.contains(BubbleColor.BLUE))
    }

    @Test
    fun bubbleColorEnumOrder() {
        // Given/When
        val colors = BubbleColor.entries

        // Then - Verify order matches definition
        assertEquals(BubbleColor.PINK, colors[0])
        assertEquals(BubbleColor.GREEN, colors[1])
        assertEquals(BubbleColor.PURPLE, colors[2])
        assertEquals(BubbleColor.ORANGE, colors[3])
        assertEquals(BubbleColor.BROWN, colors[4])
        assertEquals(BubbleColor.BLUE, colors[5])
    }

    @Test
    fun bubbleColorRandomReturnsValidColor() {
        // When
        val color = BubbleColor.random()

        // Then
        assertTrue(BubbleColor.entries.contains(color))
    }

    @Test
    fun bubbleColorRandomCanBeCalledMultipleTimes() {
        // When
        val colors = List(100) { BubbleColor.random() }

        // Then - All should be valid
        colors.forEach { color ->
            assertTrue(BubbleColor.entries.contains(color))
        }
    }
}
