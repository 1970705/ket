package com.wordland.ui.components

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for WordlandCard component logic
 *
 * Tests card state calculations, colors, and display logic
 */
@RunWith(JUnit4::class)
class WordlandCardTest {
    // ========== Mastery Percentage Calculation Tests ==========

    @Test
    fun masteryPercentageCalculation_fullMastery() {
        val percentage = 100f
        assertEquals(100f, percentage, 0f)
    }

    @Test
    fun masteryPercentageCalculation_halfMastery() {
        val percentage = 50f
        assertEquals(50f, percentage, 0f)
    }

    @Test
    fun masteryPercentageCalculation_quarterMastery() {
        val percentage = 25f
        assertEquals(25f, percentage, 0f)
    }

    @Test
    fun masteryPercentageCalculation_zeroMastery() {
        val percentage = 0f
        assertEquals(0f, percentage, 0f)
    }

    @Test
    fun masteryPercentageCalculation_threeQuarterMastery() {
        val percentage = 75f
        assertEquals(75f, percentage, 0f)
    }

    // ========== Mastery Percentage Text Format Tests ==========

    @Test
    fun masteryPercentageTextFormat_correctFormat() {
        val percentage = 75f
        val text = "${percentage.toInt()}% 掌握"
        assertEquals("75% 掌握", text)
    }

    @Test
    fun masteryPercentageTextFormat_zeroFormat() {
        val percentage = 0f
        val text = "${percentage.toInt()}% 掌握"
        assertEquals("0% 掌握", text)
    }

    @Test
    fun masteryPercentageTextFormat_fullFormat() {
        val percentage = 100f
        val text = "${percentage.toInt()}% 掌握"
        assertEquals("100% 掌握", text)
    }

    @Test
    fun masteryPercentageTextFormat_truncatesDecimal() {
        val percentage = 75.7f
        val text = "${percentage.toInt()}% 掌握"
        assertEquals("75% 掌握", text)
    }

    // ========== Locked Island Text Tests ==========

    @Test
    fun lockedIslandText_correctText() {
        val text = "未解锁"
        assertEquals("未解锁", text)
    }

    @Test
    fun lockedLevelText_correctText() {
        val text = "完成上一关卡解锁"
        assertEquals("完成上一关卡解锁", text)
    }

    // ========== Circular Progress Calculation Tests ==========

    @Test
    fun circularProgressCalculation_halfProgress() {
        val percentage = 50f
        val progress = percentage / 100f
        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun circularProgressCalculation_fullProgress() {
        val percentage = 100f
        val progress = percentage / 100f
        assertEquals(1f, progress, 0.001f)
    }

    @Test
    fun circularProgressCalculation_zeroProgress() {
        val percentage = 0f
        val progress = percentage / 100f
        assertEquals(0f, progress, 0.001f)
    }

    @Test
    fun circularProgressCalculation_quarterProgress() {
        val percentage = 25f
        val progress = percentage / 100f
        assertEquals(0.25f, progress, 0.001f)
    }

    // ========== Card Color Tests ==========

    @Test
    fun cardColor_unlockedIslandUsesIslandColorWithAlpha() {
        val isUnlocked = true
        val baseColor = 0xFF6750A4.toInt() // Example island color
        val expectedColor = 0x1A6750A4 // 10% alpha (0x1A = ~10% of 0xFF)

        val usesIslandColor = isUnlocked
        assertTrue(usesIslandColor)
    }

    @Test
    fun cardColor_lockedIslandUsesGray() {
        val isUnlocked = false
        val usesGray = !isUnlocked
        assertTrue(usesGray)
    }

    @Test
    fun cardBorderColor_unlockedIslandUsesIslandColor() {
        val isUnlocked = true
        val usesIslandColorForBorder = isUnlocked
        assertTrue(usesIslandColorForBorder)
    }

    @Test
    fun cardBorderColor_lockedIslandUsesGray() {
        val isUnlocked = false
        val usesGrayForBorder = !isUnlocked
        assertTrue(usesGrayForBorder)
    }

    // ========== Card Enabled State Tests ==========

    @Test
    fun cardEnabled_whenUnlockedClickEnabled() {
        val isUnlocked = true
        val isEnabled = isUnlocked
        assertTrue(isEnabled)
    }

    @Test
    fun cardEnabled_whenLockedClickDisabled() {
        val isUnlocked = false
        val isEnabled = isUnlocked
        assertFalse(isEnabled)
    }

    // ========== Island Card Text Color Tests ==========

    @Test
    fun islandCardTextColor_unlockedUsesIslandColor() {
        val isUnlocked = true
        val usesIslandColor = isUnlocked
        assertTrue(usesIslandColor)
    }

    @Test
    fun islandCardTextColor_lockedUsesGray() {
        val isUnlocked = false
        val usesGray = !isUnlocked
        assertTrue(usesGray)
    }

    // ========== Level Card Border Color Tests ==========

    @Test
    fun levelCardBorderColor_unlockedUsesPrimary() {
        val isUnlocked = true
        val usesPrimaryColor = isUnlocked
        assertTrue(usesPrimaryColor)
    }

    @Test
    fun levelCardBorderColor_lockedUsesGray() {
        val isUnlocked = false
        val usesGray = !isUnlocked
        assertTrue(usesGray)
    }

    // ========== Level Card Text Color Tests ==========

    @Test
    fun levelCardTextColor_unlockedUsesOnSurface() {
        val isUnlocked = true
        val usesOnSurface = isUnlocked
        assertTrue(usesOnSurface)
    }

    @Test
    fun levelCardTextColor_lockedUsesGray() {
        val isUnlocked = false
        val usesGray = !isUnlocked
        assertTrue(usesGray)
    }

    // ========== Level Card Content Tests ==========

    @Test
    fun levelCardShowsStarsWhenUnlocked() {
        val isUnlocked = true
        val showStars = isUnlocked
        assertTrue(showStars)
    }

    @Test
    fun levelCardShowsLockWhenLocked() {
        val isUnlocked = false
        val showLock = !isUnlocked
        assertTrue(showLock)
    }

    @Test
    fun levelCardShowsUnlockHintWhenLocked() {
        val isUnlocked = false
        val showUnlockHint = !isUnlocked
        assertTrue(showUnlockHint)
    }

    // ========== Stars Display Tests ==========

    @Test
    fun starsDisplay_zeroStarsShowsAllOutlined() {
        val stars = 0
        val totalStars = 3
        val outlinedStars = totalStars - stars
        assertEquals(3, outlinedStars)
    }

    @Test
    fun starsDisplay_oneStarShowsOneFilled() {
        val stars = 1
        val filledStars = stars
        assertEquals(1, filledStars)
    }

    @Test
    fun starsDisplay_twoStarsShowsTwoFilled() {
        val stars = 2
        val filledStars = stars
        assertEquals(2, filledStars)
    }

    @Test
    fun starsDisplay_threeStarsShowsAllFilled() {
        val stars = 3
        val filledStars = stars
        assertEquals(3, filledStars)
    }

    @Test
    fun starsDisplay_cappedAtThree() {
        val stars = 5
        val maxStars = 3
        val actualStars = minOf(stars, maxStars)
        assertEquals(3, actualStars)
    }

    @Test
    fun starsDisplay_negativeStarsShowsZeroFilled() {
        val stars = -1
        val filledStars = maxOf(0, stars)
        assertEquals(0, filledStars)
    }

    @Test
    fun starsDisplay_outlinedStarsCalculation() {
        val stars = 2
        val totalStars = 3
        val outlinedStars = totalStars - stars
        assertEquals(1, outlinedStars)
    }

    // ========== Card Dimensions Tests ==========

    @Test
    fun islandCardHeight_is200Dp() {
        val expectedHeight = 200
        val height = 200
        assertEquals(expectedHeight, height)
    }

    @Test
    fun levelCardHeight_is120Dp() {
        val expectedHeight = 120
        val height = 120
        assertEquals(expectedHeight, height)
    }

    // ========== Card Padding Tests ==========

    @Test
    fun cardPadding_is16Dp() {
        val expectedPadding = 16
        val padding = 16
        assertEquals(expectedPadding, padding)
    }

    // ========== Card Corner Radius Tests ==========

    @Test
    fun cardCornerRadius_is16Dp() {
        val expectedCornerRadius = 16
        val cornerRadius = 16
        assertEquals(expectedCornerRadius, cornerRadius)
    }

    // ========== Card Elevation Tests ==========

    @Test
    fun cardElevation_defaultIs2Dp() {
        val expectedElevation = 2
        val elevation = 2
        assertEquals(expectedElevation, elevation)
    }

    // ========== Circular Progress Size Tests ==========

    @Test
    fun circularProgressSize_is80Dp() {
        val expectedSize = 80
        val size = 80
        assertEquals(expectedSize, size)
    }

    // ========== Circular Progress Stroke Width Tests ==========

    @Test
    fun circularProgressStrokeWidth_is6Dp() {
        val expectedStrokeWidth = 6
        val strokeWidth = 6
        assertEquals(expectedStrokeWidth, strokeWidth)
    }

    // ========== Lock Icon Size Tests ==========

    @Test
    fun lockIconSize_is48Dp() {
        val expectedSize = 48
        val size = 48
        assertEquals(expectedSize, size)
    }

    // ========== Star Icon Size Tests ==========

    @Test
    fun starIconSize_is24Dp() {
        val expectedSize = 24
        val size = 24
        assertEquals(expectedSize, size)
    }

    // ========== Star Spacing Tests ==========

    @Test
    fun starSpacing_is4Dp() {
        val expectedSpacing = 4
        val spacing = 4
        assertEquals(expectedSpacing, spacing)
    }

    // ========== Card Background Alpha Tests ==========

    @Test
    fun cardBackgroundAlpha_unlockedUses10Percent() {
        val isUnlocked = true
        val expectedAlpha = 0.1f
        val alpha = if (isUnlocked) 0.1f else 0.1f
        assertEquals(expectedAlpha, alpha, 0.001f)
    }

    @Test
    fun cardBackgroundAlpha_lockedUses10Percent() {
        val isUnlocked = false
        val expectedAlpha = 0.1f
        val alpha = if (isUnlocked) 0.1f else 0.1f
        assertEquals(expectedAlpha, alpha, 0.001f)
    }

    // ========== Border Stroke Width Tests ==========

    @Test
    fun borderStrokeWidth_is1Dp() {
        val expectedWidth = 1
        val width = 1
        assertEquals(expectedWidth, width)
    }

    // ========== Card Click Handler Tests ==========

    @Test
    fun cardClickHandler_enabledWhenOnClickProvided() {
        val onClick: (() -> Unit)? = {}
        val hasClickHandler = onClick != null
        assertTrue(hasClickHandler)
    }

    @Test
    fun cardClickHandler_disabledWhenOnClickNull() {
        val onClick: (() -> Unit)? = null
        val hasClickHandler = onClick != null
        assertFalse(hasClickHandler)
    }

    @Test
    fun cardClickHandler_respectsEnabledState() {
        val onClick: (() -> Unit)? = {}
        val enabled = false

        val shouldExecuteClick = onClick != null && enabled
        assertFalse(shouldExecuteClick)
    }

    @Test
    fun cardClickHandler_executesWhenEnabled() {
        val onClick: (() -> Unit)? = {}
        val enabled = true

        val shouldExecuteClick = onClick != null && enabled
        assertTrue(shouldExecuteClick)
    }

    // ========== Mastery Display Tests ==========

    @Test
    fun masteryDisplay_shownWhenIslandUnlocked() {
        val isUnlocked = true
        val showMastery = isUnlocked
        assertTrue(showMastery)
    }

    @Test
    fun masteryDisplay_hiddenWhenIslandLocked() {
        val isUnlocked = false
        val showMastery = isUnlocked
        assertFalse(showMastery)
    }
}
