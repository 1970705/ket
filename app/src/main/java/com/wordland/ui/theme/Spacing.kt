package com.wordland.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing tokens for consistent layout across the app
 *
 * Following Material Design 3 spacing guidelines:
 * - 4dp base unit
 * - Consistent increments of 4dp
 *
 * @since 1.6 (Epic #8.4)
 */

// Base spacing unit (4dp as per Material Design)
private const val BASE_SPACING = 4

// Spacing scale
val spacingNone = 0.dp
val spacingExtraSmall = 4.dp
val spacingSmall = 8.dp
val spacingMedium = 16.dp
val spacingLarge = 24.dp
val spacingExtraLarge = 32.dp
val spacingHuge = 48.dp

// Content padding (screen edges)
val contentPaddingHorizontal = 16.dp
val contentPaddingVertical = 16.dp
val contentPaddingCompact = 12.dp

// Card padding
val cardPaddingHorizontal = 20.dp
val cardPaddingVertical = 20.dp
val cardPaddingCompact = 16.dp

// Button spacing
val buttonSpacingHorizontal = 24.dp
val buttonSpacingVertical = 16.dp

// List item spacing
val listItemSpacing = 8.dp
val listItemPadding = 16.dp

// Icon button size (minimum 48dp for touch target)
val iconButtonSize = 48.dp
val touchTargetMinimum = 48.dp

/**
 * Screen padding values
 */
data class ScreenPadding(
    val horizontal: Dp = contentPaddingHorizontal,
    val vertical: Dp = contentPaddingVertical,
) {
    companion object {
        val Default = ScreenPadding()
        val Compact = ScreenPadding(12.dp, 12.dp)
        val Comfortable = ScreenPadding(20.dp, 20.dp)
    }
}

/**
 * Card padding values
 */
data class CardPadding(
    val horizontal: Dp = cardPaddingHorizontal,
    val vertical: Dp = cardPaddingVertical,
) {
    companion object {
        val Default = CardPadding()
        val Compact = CardPadding(16.dp, 16.dp)
        val Spacious = CardPadding(24.dp, 24.dp)
    }
}

/**
 * Composition local for spacing tokens
 */
val LocalSpacing = compositionLocalOf { spacingMedium }

/**
 * Padding values for different contexts
 */
object Padding {
    val Screen = ScreenPadding.Default
    val Card = CardPadding.Default

    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val ExtraLarge = 32.dp
}

/**
 * Common spacing values for modifiers
 */
@Composable
fun screenPadding() =
    PaddingValues(
        horizontal = Padding.Screen.horizontal,
        vertical = Padding.Screen.vertical,
    )

@Composable
fun cardPadding() =
    PaddingValues(
        horizontal = Padding.Card.horizontal,
        vertical = Padding.Card.vertical,
    )
