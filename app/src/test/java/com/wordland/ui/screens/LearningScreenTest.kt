package com.wordland.ui.screens

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for LearningScreen
 *
 * Tests screen logic and helper functions.
 * Full UI tests are in LearningScreenUiTest (androidTest).
 */
@RunWith(JUnit4::class)
class LearningScreenTest {
    // === getLevelDisplayName Tests ===

    @Test
    fun getLevelDisplayName_singleWordCapitalizesFirstLetter() {
        // Given
        val levelId = "level"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Level", result)
    }

    @Test
    fun getLevelDisplayName_underscoreSeparatedCapitalizesEachWord() {
        // Given
        val levelId = "look_level_01"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Look Level 01", result)
    }

    @Test
    fun getLevelDisplayName_makeLakeLevelFormatsCorrectly() {
        // Given
        val levelId = "make_lake_level_01"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Make Lake Level 01", result)
    }

    @Test
    fun getLevelDisplayName_allCapsConvertsToTitleCase() {
        // Given
        val levelId = "LOOK_LEVEL_01"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("LOOK LEVEL 01", result)
    }

    @Test
    fun getLevelDisplayName_emptyStringReturnsEmpty() {
        // Given
        val levelId = ""

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("", result)
    }

    @Test
    fun getLevelDisplayName_singleUnderscoreReturnsSingleSpace() {
        // Given
        val levelId = "_"

        // When
        val result = getLevelDisplayName(levelId)

        // Then - Single underscore becomes a single space after split/join
        assertEquals(" ", result)
    }

    @Test
    fun getLevelDisplayName_multipleConsecutiveUnderscoresHandlesCorrectly() {
        // Given
        val levelId = "look__level__01"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Look  Level  01", result)
    }

    @Test
    fun getLevelDisplayName_trailingUnderscoreHandlesCorrectly() {
        // Given
        val levelId = "look_level_01_"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Look Level 01 ", result)
    }

    @Test
    fun getLevelDisplayName_leadingUnderscoreHandlesCorrectly() {
        // Given
        val levelId = "_look_level_01"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals(" Look Level 01", result)
    }

    @Test
    fun getLevelDisplayName_numbersRemainUnchanged() {
        // Given
        val levelId = "level_123"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Level 123", result)
    }

    @Test
    fun getLevelDisplayName_realLevelIdLook01() {
        // Given
        val levelId = "look_level_01"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Look Level 01", result)
    }

    @Test
    fun getLevelDisplayName_realLevelIdLook02() {
        // Given
        val levelId = "look_level_02"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Look Level 02", result)
    }

    @Test
    fun getLevelDisplayName_realLevelIdMakeLake01() {
        // Given
        val levelId = "make_lake_level_01"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("Make Lake Level 01", result)
    }

    @Test
    fun getLevelDisplayName_mixedCaseWordsCapitalizesFirstLetter() {
        // Given
        val levelId = "lOoK_leVeL_01"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("LOoK LeVeL 01", result)
    }

    @Test
    fun getLevelDisplayName_singleNumberWorks() {
        // Given
        val levelId = "1"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("1", result)
    }

    @Test
    fun getLevelDisplayName_justUnderscoresReturnsJustSpaces() {
        // Given
        val levelId = "___"

        // When
        val result = getLevelDisplayName(levelId)

        // Then
        assertEquals("   ", result)
    }
}
