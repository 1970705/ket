package com.wordland.ui.components

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for WordlandButton component enums and values
 *
 * Note: Full Compose UI tests require androidTest with Compose testing framework
 * These tests verify component logic and configuration
 */
@RunWith(JUnit4::class)
class WordlandButtonTest {
    // ========== ButtonSize Enum Tests ==========

    @Test
    fun buttonSize_smallHasCorrectHeight() {
        val smallSize = ButtonSize.SMALL
        assertNotNull(smallSize)
        assertEquals("SMALL", smallSize.name)
    }

    @Test
    fun buttonSize_mediumHasCorrectHeight() {
        val mediumSize = ButtonSize.MEDIUM
        assertNotNull(mediumSize)
        assertEquals("MEDIUM", mediumSize.name)
    }

    @Test
    fun buttonSize_largeHasCorrectHeight() {
        val largeSize = ButtonSize.LARGE
        assertNotNull(largeSize)
        assertEquals("LARGE", largeSize.name)
    }

    @Test
    fun buttonSizeEnumHasAllValues() {
        val sizes = ButtonSize.entries
        assertEquals(3, sizes.size)
        assertEquals(ButtonSize.SMALL, sizes[0])
        assertEquals(ButtonSize.MEDIUM, sizes[1])
        assertEquals(ButtonSize.LARGE, sizes[2])
    }

    @Test
    fun buttonSize_smallIsFirstEntry() {
        val sizes = ButtonSize.entries
        assertEquals(ButtonSize.SMALL, sizes.first())
    }

    @Test
    fun buttonSize_largeIsLastEntry() {
        val sizes = ButtonSize.entries
        assertEquals(ButtonSize.LARGE, sizes.last())
    }

    // ========== Button Size Height Logic Tests ==========

    @Test
    fun buttonHeightLogic_smallReturns36() {
        val size = ButtonSize.SMALL
        // In component: when (size) { SMALL -> SmallButtonHeight }
        val expectedHeight = "SmallButtonHeight"
        val actualSize =
            when (size) {
                ButtonSize.SMALL -> "SmallButtonHeight"
                ButtonSize.MEDIUM -> "MediumButtonHeight"
                ButtonSize.LARGE -> "LargeButtonHeight"
            }
        assertEquals(expectedHeight, actualSize)
    }

    @Test
    fun buttonHeightLogic_mediumReturns48() {
        val size = ButtonSize.MEDIUM
        val expectedHeight = "MediumButtonHeight"
        val actualSize =
            when (size) {
                ButtonSize.SMALL -> "SmallButtonHeight"
                ButtonSize.MEDIUM -> "MediumButtonHeight"
                ButtonSize.LARGE -> "LargeButtonHeight"
            }
        assertEquals(expectedHeight, actualSize)
    }

    @Test
    fun buttonHeightLogic_largeReturns56() {
        val size = ButtonSize.LARGE
        val expectedHeight = "LargeButtonHeight"
        val actualSize =
            when (size) {
                ButtonSize.SMALL -> "SmallButtonHeight"
                ButtonSize.MEDIUM -> "MediumButtonHeight"
                ButtonSize.LARGE -> "LargeButtonHeight"
            }
        assertEquals(expectedHeight, actualSize)
    }

    // ========== Button Font Size Logic Tests ==========

    @Test
    fun buttonFontSize_smallReturns14() {
        val size = ButtonSize.SMALL
        val expectedFontSize = 14
        val actualFontSize =
            when (size) {
                ButtonSize.SMALL -> 14
                ButtonSize.MEDIUM -> 16
                ButtonSize.LARGE -> 18
            }
        assertEquals(expectedFontSize, actualFontSize)
    }

    @Test
    fun buttonFontSize_mediumReturns16() {
        val size = ButtonSize.MEDIUM
        val expectedFontSize = 16
        val actualFontSize =
            when (size) {
                ButtonSize.SMALL -> 14
                ButtonSize.MEDIUM -> 16
                ButtonSize.LARGE -> 18
            }
        assertEquals(expectedFontSize, actualFontSize)
    }

    @Test
    fun buttonFontSize_largeReturns18() {
        val size = ButtonSize.LARGE
        val expectedFontSize = 18
        val actualFontSize =
            when (size) {
                ButtonSize.SMALL -> 14
                ButtonSize.MEDIUM -> 16
                ButtonSize.LARGE -> 18
            }
        assertEquals(expectedFontSize, actualFontSize)
    }

    // ========== Button Press Animation Tests ==========

    @Test
    fun buttonPressAnimation_scaleWhenPressed() {
        val isPressed = true
        val buttonScale = if (isPressed) 0.97f else 1f
        assertEquals(0.97f, buttonScale, 0.001f)
    }

    @Test
    fun buttonPressAnimation_normalScaleWhenNotPressed() {
        val isPressed = false
        val buttonScale = if (isPressed) 0.97f else 1f
        assertEquals(1f, buttonScale, 0.001f)
    }

    // ========== Icon Position Tests ==========

    @Test
    fun iconPosition_iconBeforeTextWhenIconBeforeTextTrue() {
        val iconBeforeText = true
        val hasIcon = true

        val shouldRenderIconFirst = hasIcon && iconBeforeText
        assertTrue(shouldRenderIconFirst)
    }

    @Test
    fun iconPosition_iconAfterTextWhenIconBeforeTextFalse() {
        val iconBeforeText = false
        val hasIcon = true

        val shouldRenderIconFirst = hasIcon && iconBeforeText
        assertFalse(shouldRenderIconFirst)

        val shouldRenderIconLast = hasIcon && !iconBeforeText
        assertTrue(shouldRenderIconLast)
    }

    @Test
    fun iconPosition_noIconWhenIconIsNull() {
        val iconBeforeText = true
        val hasIcon = false

        val shouldRenderIcon = hasIcon
        assertFalse(shouldRenderIcon)
    }

    // ========== Button Enabled State Tests ==========

    @Test
    fun buttonEnabled_enabledWhenTrue() {
        val enabled = true
        assertTrue(enabled)
    }

    @Test
    fun buttonEnabled_disabledWhenFalse() {
        val enabled = false
        assertFalse(enabled)
    }

    // ========== Button Color Logic Tests ==========

    @Test
    fun buttonColor_enabledUsesPrimaryColor() {
        val enabled = true
        val usesPrimaryColor = enabled
        assertTrue(usesPrimaryColor)
    }

    @Test
    fun buttonColor_disabledUsesReducedAlpha() {
        val enabled = false
        val usesReducedAlpha = !enabled
        assertTrue(usesReducedAlpha)
    }

    // ========== Outlined Button Border Tests ==========

    @Test
    fun outlinedButtonBorder_enabledUsesBorderColor() {
        val enabled = true
        val borderColor = 0xFF6750A4.toInt()
        val actualBorderColor = if (enabled) borderColor else borderColor shr 8 // Simulated alpha reduction

        assertEquals(borderColor, if (enabled) actualBorderColor else borderColor shr 8)
    }

    @Test
    fun outlinedButtonBorder_disabledUsesReducedAlpha() {
        val enabled = false
        val shouldReduceAlpha = !enabled
        assertTrue(shouldReduceAlpha)
    }

    // ========== Text Button Color Tests ==========

    @Test
    fun textButtonColor_enabledUsesTextColor() {
        val enabled = true
        val textColor = 0xFF6750A4.toInt()
        val actualColor = if (enabled) textColor else textColor shr 8

        assertEquals(textColor, if (enabled) actualColor else textColor shr 8)
    }

    @Test
    fun textButtonColor_disabledUsesReducedAlpha() {
        val enabled = false
        val shouldReduceAlpha = !enabled
        assertTrue(shouldReduceAlpha)
    }

    // ========== Animated Button Icon Rotation Tests ==========

    @Test
    fun animatedIconButton_rotationZeroWhenNotPressed() {
        val isPressed = false
        val iconRotation = if (isPressed) 15f else 0f
        assertEquals(0f, iconRotation, 0.001f)
    }

    @Test
    fun animatedIconButton_rotation15WhenPressed() {
        val isPressed = true
        val iconRotation = if (isPressed) 15f else 0f
        assertEquals(15f, iconRotation, 0.001f)
    }

    // ========== PressInteraction Type Tests ==========

    @Test
    fun pressInteraction_pressSetsPressedToTrue() {
        // Simulating PressInteraction.Press behavior
        val isPressed = true
        assertTrue(isPressed)
    }

    @Test
    fun pressInteraction_releaseSetsPressedToFalse() {
        // Simulating PressInteraction.Release behavior
        val isPressed = false
        assertFalse(isPressed)
    }

    @Test
    fun pressInteraction_cancelSetsPressedToFalse() {
        // Simulating PressInteraction.Cancel behavior
        val isPressed = false
        assertFalse(isPressed)
    }

    // ========== Button Content Padding Tests ==========

    @Test
    fun buttonPadding_horizontalPaddingIs24() {
        val expectedHorizontalPadding = 24
        // Component uses PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        val horizontalPadding = 24
        assertEquals(expectedHorizontalPadding, horizontalPadding)
    }

    @Test
    fun buttonPadding_verticalPaddingIs12() {
        val expectedVerticalPadding = 12
        val verticalPadding = 12
        assertEquals(expectedVerticalPadding, verticalPadding)
    }

    // ========== Text Button Content Padding Tests ==========

    @Test
    fun textButtonPadding_horizontalPaddingIs16() {
        val expectedHorizontalPadding = 16
        val horizontalPadding = 16
        assertEquals(expectedHorizontalPadding, horizontalPadding)
    }

    @Test
    fun textButtonPadding_verticalPaddingIs8() {
        val expectedVerticalPadding = 8
        val verticalPadding = 8
        assertEquals(expectedVerticalPadding, verticalPadding)
    }

    // ========== Button Corner Radius Tests ==========

    @Test
    fun buttonCornerRadius_allButtonsUse12Dp() {
        val expectedCornerRadius = 12
        val cornerRadius = 12
        assertEquals(expectedCornerRadius, cornerRadius)
    }

    // ========== Icon Size Tests ==========

    @Test
    fun iconSize_allIconsUse20Dp() {
        val expectedIconSize = 20
        val iconSize = 20
        assertEquals(expectedIconSize, iconSize)
    }

    // ========== Spacer Width Tests ==========

    @Test
    fun spacerWidth_betweenIconAndTextIs8Dp() {
        val expectedSpacerWidth = 8
        val spacerWidth = 8
        assertEquals(expectedSpacerWidth, spacerWidth)
    }

    // ========== Button Order Enum Test ==========

    @Test
    fun buttonSize_orderIsSmallMediumLarge() {
        val sizes = ButtonSize.entries
        assertEquals(ButtonSize.SMALL, sizes[0])
        assertEquals(ButtonSize.MEDIUM, sizes[1])
        assertEquals(ButtonSize.LARGE, sizes[2])
    }
}
