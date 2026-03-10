package com.wordland.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shape tokens for consistent border radius across the app
 *
 * Following Material Design 3 shape guidelines:
 * - Small: 8dp (buttons, chips)
 * - Medium: 12dp (cards, dialogs)
 * - Large: 16dp (bottom sheets, side sheets)
 * - Extra Large: 28dp (full-screen modals)
 *
 * @since 1.6 (Epic #8.4)
 */

// Small radius (buttons, chips, tags)
val ShapeSmall = 8.dp

// Medium radius (cards, dialogs, input fields)
val ShapeMedium = 12.dp

// Large radius (bottom sheets, side sheets)
val ShapeLarge = 16.dp

// Extra large radius (full-screen modals, hero cards)
val ShapeExtraLarge = 28.dp

// Full rounded (pills, floating action buttons)
val ShapeFull = 50.dp

// Predefined CornerBasedShape instances
val SmallCornerShape: CornerBasedShape =
    androidx.compose.foundation.shape.RoundedCornerShape(ShapeSmall)

val MediumCornerShape: CornerBasedShape =
    androidx.compose.foundation.shape.RoundedCornerShape(ShapeMedium)

val LargeCornerShape: CornerBasedShape =
    androidx.compose.foundation.shape.RoundedCornerShape(ShapeLarge)

val ExtraLargeCornerShape: CornerBasedShape =
    androidx.compose.foundation.shape.RoundedCornerShape(ShapeExtraLarge)

/**
 * Shape classes for different components
 */
object AppShapes {
    // Button shapes
    val ButtonShape = MediumCornerShape
    val IconButtonShape = CircleShape

    // Card shapes
    val CardShape = MediumCornerShape
    val CardShapeLarge = LargeCornerShape

    // Dialog shapes
    val DialogShape = MediumCornerShape

    // Input field shapes
    val TextFieldShape = MediumCornerShape
    val TextFieldOutlineShape = MediumCornerShape

    // Chip/Badge shapes
    val ChipShape = SmallCornerShape
    val BadgeShape = SmallCornerShape

    // Bottom sheet
    val BottomSheetShape = LargeCornerShape

    // Custom shapes for special components
    val FloatingShape = androidx.compose.foundation.shape.CircleShape
    val HeroCardShape = ExtraLargeCornerShape
}
