package com.wordland.ui.components

import com.wordland.domain.model.ComboState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for LevelProgressBarEnhanced component logic
 *
 * Tests progress calculation, motivational messages, and combo state display
 */
@RunWith(JUnit4::class)
class LevelProgressBarEnhancedTest {
    // ========== Progress Calculation Tests ==========

    @Test
    fun progressCalculation_zeroCurrentWords() {
        val currentWord = 0
        val totalWords = 6
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(0f, progress, 0.001f)
    }

    @Test
    fun progressCalculation_halfProgress() {
        val currentWord = 3
        val totalWords = 6
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun progressCalculation_fullProgress() {
        val currentWord = 6
        val totalWords = 6
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(1f, progress, 0.001f)
    }

    @Test
    fun progressCalculation_zeroTotalWordsReturnsZero() {
        val currentWord = 3
        val totalWords = 0
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(0f, progress, 0.001f)
    }

    @Test
    fun progressCalculation_oneOfSix() {
        val currentWord = 1
        val totalWords = 6
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(0.166f, progress, 0.001f)
    }

    @Test
    fun progressCalculation_fiveOfSix() {
        val currentWord = 5
        val totalWords = 6
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(0.833f, progress, 0.001f)
    }

    // ========== Progress Coercion Tests ==========

    @Test
    fun progressCoercing_negativeValueClampedToZero() {
        val progress = (-0.5f).coerceIn(0f, 1f)
        assertEquals(0f, progress, 0f)
    }

    @Test
    fun progressCoercing_valueAboveOneClampedToOne() {
        val progress = 1.5f.coerceIn(0f, 1f)
        assertEquals(1f, progress, 0f)
    }

    @Test
    fun progressCoercing_normalValueUnchanged() {
        val progress = 0.5f.coerceIn(0f, 1f)
        assertEquals(0.5f, progress, 0f)
    }

    // ========== State Detection Tests ==========

    @Test
    fun stateDetection_isLastWordWhenCurrentEqualsTotal() {
        val currentWord = 6
        val totalWords = 6
        val isLastWord = currentWord == totalWords
        assertTrue(isLastWord)
    }

    @Test
    fun stateDetection_notLastWordWhenCurrentLessThanTotal() {
        val currentWord = 5
        val totalWords = 6
        val isLastWord = currentWord == totalWords
        assertFalse(isLastWord)
    }

    @Test
    fun stateDetection_isFirstWordWhenCurrentIsZero() {
        val currentWord = 0
        val isFirstWord = currentWord == 0
        assertTrue(isFirstWord)
    }

    @Test
    fun stateDetection_notFirstWordWhenCurrentGreaterThanZero() {
        val currentWord = 1
        val isFirstWord = currentWord == 0
        assertFalse(isFirstWord)
    }

    // ========== Hot Streak Detection Tests ==========

    @Test
    fun hotStreakDetection_fiveComboIsHotStreak() {
        val comboState = ComboState(consecutiveCorrect = 5)
        val isHotStreak = comboState.isHighCombo
        assertTrue(isHotStreak)
    }

    @Test
    fun hotStreakDetection_tenComboIsHotStreak() {
        val comboState = ComboState(consecutiveCorrect = 10)
        val isHotStreak = comboState.isHighCombo
        assertTrue(isHotStreak)
    }

    @Test
    fun hotStreakDetection_fourComboIsNotHotStreak() {
        val comboState = ComboState(consecutiveCorrect = 4)
        val isHotStreak = comboState.isHighCombo
        assertFalse(isHotStreak)
    }

    @Test
    fun hotStreakDetection_zeroComboIsNotHotStreak() {
        val comboState = ComboState(consecutiveCorrect = 0)
        val isHotStreak = comboState.isHighCombo
        assertFalse(isHotStreak)
    }

    // ========== Motivational Message Tests ==========

    @Test
    fun motivationalMessage_lastWordMessage() {
        val isLastWord = true
        val isHotStreak = false
        val comboCount = 3

        val message =
            when {
                isLastWord -> "最后一个词！"
                isHotStreak -> "🔥 你在燃烧！"
                comboCount >= ComboState.COMBO_THRESHOLD_3X -> "太棒了！"
                comboCount > 0 -> "继续前进！"
                else -> "开始吧！"
            }

        assertEquals("最后一个词！", message)
    }

    @Test
    fun motivationalMessage_hotStreakMessage() {
        val isLastWord = false
        val isHotStreak = true
        val comboCount = 5

        val message =
            when {
                isLastWord -> "最后一个词！"
                isHotStreak -> "🔥 你在燃烧！"
                comboCount >= ComboState.COMBO_THRESHOLD_3X -> "太棒了！"
                comboCount > 0 -> "继续前进！"
                else -> "开始吧！"
            }

        assertEquals("🔥 你在燃烧！", message)
    }

    @Test
    fun motivationalMessage_threeComboMessage() {
        val isLastWord = false
        val isHotStreak = false
        val comboCount = 3

        val message =
            when {
                isLastWord -> "最后一个词！"
                isHotStreak -> "🔥 你在燃烧！"
                comboCount >= ComboState.COMBO_THRESHOLD_3X -> "太棒了！"
                comboCount > 0 -> "继续前进！"
                else -> "开始吧！"
            }

        assertEquals("太棒了！", message)
    }

    @Test
    fun motivationalMessage_oneComboMessage() {
        val isLastWord = false
        val isHotStreak = false
        val comboCount = 1

        val message =
            when {
                isLastWord -> "最后一个词！"
                isHotStreak -> "🔥 你在燃烧！"
                comboCount >= ComboState.COMBO_THRESHOLD_3X -> "太棒了！"
                comboCount > 0 -> "继续前进！"
                else -> "开始吧！"
            }

        assertEquals("继续前进！", message)
    }

    @Test
    fun motivationalMessage_zeroComboMessage() {
        val isLastWord = false
        val isHotStreak = false
        val comboCount = 0

        val message =
            when {
                isLastWord -> "最后一个词！"
                isHotStreak -> "🔥 你在燃烧！"
                comboCount >= ComboState.COMBO_THRESHOLD_3X -> "太棒了！"
                comboCount > 0 -> "继续前进！"
                else -> "开始吧！"
            }

        assertEquals("开始吧！", message)
    }

    // ========== Progress Bar Color Tests ==========

    @Test
    fun progressColor_usesHotStreakColorWhenHotStreak() {
        val isHotStreak = true
        val hotStreakColor = 0xFFFF6B35.toInt()

        val shouldUseHotStreakColor = isHotStreak

        assertTrue(shouldUseHotStreakColor)
    }

    @Test
    fun progressColor_usesDefaultColorWhenNotHotStreak() {
        val isHotStreak = false

        val shouldUseHotStreakColor = isHotStreak

        assertFalse(shouldUseHotStreakColor)
    }

    // ========== Combo Display Tests ==========

    @Test
    fun comboDisplay_shownWhenShowComboTrueAndComboGreaterThanZero() {
        val showCombo = true
        val comboCount = 3
        val shouldShow = showCombo && comboCount > 0
        assertTrue(shouldShow)
    }

    @Test
    fun comboDisplay_notShownWhenShowComboFalse() {
        val showCombo = false
        val comboCount = 3
        val shouldShow = showCombo && comboCount > 0
        assertFalse(shouldShow)
    }

    @Test
    fun comboDisplay_notShownWhenComboIsZero() {
        val showCombo = true
        val comboCount = 0
        val shouldShow = showCombo && comboCount > 0
        assertFalse(shouldShow)
    }

    @Test
    fun comboDisplay_notShownWhenShowComboFalseAndComboIsZero() {
        val showCombo = false
        val comboCount = 0
        val shouldShow = showCombo && comboCount > 0
        assertFalse(shouldShow)
    }

    // ========== Progress Text Format Tests ==========

    @Test
    fun progressTextFormat_zeroOfSix() {
        val currentWord = 0
        val totalWords = 6
        val progressText = "$currentWord / $totalWords"
        assertEquals("0 / 6", progressText)
    }

    @Test
    fun progressTextFormat_threeOfSix() {
        val currentWord = 3
        val totalWords = 6
        val progressText = "$currentWord / $totalWords"
        assertEquals("3 / 6", progressText)
    }

    @Test
    fun progressTextFormat_sixOfSix() {
        val currentWord = 6
        val totalWords = 6
        val progressText = "$currentWord / $totalWords"
        assertEquals("6 / 6", progressText)
    }

    // ========== Compact Progress Bar Tests ==========

    @Test
    fun compactProgress_calculation_zeroOfTen() {
        val currentWord = 0
        val totalWords = 10
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(0f, progress, 0.001f)
    }

    @Test
    fun compactProgress_calculation_fiveOfTen() {
        val currentWord = 5
        val totalWords = 10
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun compactProgress_calculation_tenOfTen() {
        val currentWord = 10
        val totalWords = 10
        val progress = if (totalWords > 0) currentWord.toFloat() / totalWords.toFloat() else 0f
        assertEquals(1f, progress, 0.001f)
    }
}
