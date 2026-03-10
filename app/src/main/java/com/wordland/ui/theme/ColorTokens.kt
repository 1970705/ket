package com.wordland.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color tokens for Material Design 3 compliance
 *
 * These colors follow the Material Design 3 color system
 * with proper tonal palettes and contrast ratios.
 *
 * Color roles:
 * - Primary: Brand colors, main interactive elements
 * - Secondary: Accent colors, supplementary elements
 * - Tertiary: Highlight colors, special states
 * - Error: Error states, destructive actions
 * - Surface: Backgrounds, cards, sheets
 * - On-colors: Content colors on top of surfaces
 *
 * @since 1.6 (Epic #8.4)
 */

// ============================================================================
// Primary Color Palette (Nature/Island theme - Green)
// ============================================================================

val Primary = Color(0xFF4CAF50) // Base primary (MD3 Green)
val OnPrimary = Color(0xFFFFFFFF) // Content on primary
val PrimaryContainer = Color(0xFF83E088) // Light container
val OnPrimaryContainer = Color(0xFF002205) // Content on container

val PrimaryFixed = Color(0xFF70DE75)
val PrimaryFixedDim = Color(0xFF4CAF50)

// ============================================================================
// Secondary Color Palette (Ocean/Water theme - Blue)
// ============================================================================

val Secondary = Color(0xFF4DB6AC) // Muted teal for secondary
val OnSecondary = Color(0xFF003731)
val SecondaryContainer = Color(0xFF6FF7E6)
val OnSecondaryContainer = Color(0xFF00201C)

// ============================================================================
// Tertiary Color Palette (Accent - Orange)
// ============================================================================

val Tertiary = Color(0xFFFF9800) // Accent orange for stars, rewards
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFFFCC80)
val OnTertiaryContainer = Color(0xFF2E1500)

// ============================================================================
// Error Color Palette
// ============================================================================

val Error = Color(0xFFBA1A1A) // MD3 error red
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

// ============================================================================
// Neutral Color Palette (Surfaces, backgrounds)
// ============================================================================

val Background = Color(0xFFFFFBF8) // Warm off-white
val OnBackground = Color(0xFF1C1B1B) // Near black

val Surface = Color(0xFFFFFBF8) // Same as background
val OnSurface = Color(0xFF1C1B1B)

val SurfaceVariant = Color(0xFFE1E3E1) // Muted surface
val OnSurfaceVariant = Color(0xFF444746)

val SurfaceTint = Primary // Surface tint for elevation

// Outline colors for borders and dividers
val Outline = Color(0xFF747776)
val OutlineVariant = Color(0xFFC4C7C5)

// ============================================================================
// Semantic Colors (Status, feedback)
// ============================================================================

// Success (correct answers)
val Success = Color(0xFF4CAF50)
val onSuccess = Color(0xFFFFFFFF)
val successContainer = Color(0xFFB9F4BC)
val onSuccessContainer = Color(0xFF002205)

// Warning (hints used, warnings)
val Warning = Color(0xFFFF9800)
val onWarning = Color(0xFFFFFFFF)
val warningContainer = Color(0xFFFFD9B3)
val onWarningContainer = Color(0xFF331C00)

// Info (informational messages)
val Info = Color(0xFF2196F3)
val onInfo = Color(0xFFFFFFFF)
val infoContainer = Color(0xFFB3DAFE)
val onInfoContainer = Color(0xFF001D38)

// ============================================================================
// Island-Specific Colors (Thematic for different islands)
// ============================================================================

object IslandColors {
    val LookIsland = Color(0xFF66BB6A) // Green - observation
    val MakeLake = Color(0xFF26C6DA) // Cyan - creation
    val MoveValley = Color(0xFF42A5F5) // Blue - movement
    val SayMountain = Color(0xFFAB47BC) // Purple - speech
    val FeelGarden = Color(0xFFFFA726) // Orange - emotions
    val ThinkForest = Color(0xFF26A69A) // Teal - thinking
    val GoVolcano = Color(0xFFEF5350) // Red - action
    val ListenCave = Color(0xFF7E57C2) // Purple - listening
    val ReadCastle = Color(0xFFEC407A) // Pink - reading
    val WriteTower = Color(0xFF5C6BC0) // Indigo - writing
}

// ============================================================================
// Dark Mode Colors
// ============================================================================

object DarkColors {
    val Primary = Color(0xFF4CAF50) // Same green for dark mode
    val OnPrimary = Color(0xFF00390E)
    val PrimaryContainer = Color(0xFF005218)
    val OnPrimaryContainer = Color(0xFF83E088)

    val Secondary = Color(0xFF4DB6AC)
    val OnSecondary = Color(0xFF003731)
    val SecondaryContainer = Color(0xFF004F46)
    val OnSecondaryContainer = Color(0xFF6FF7E6)

    val Tertiary = Color(0xFFFFB951)
    val OnTertiary = Color(0xFF4A2800)
    val TertiaryContainer = Color(0xFF6A3C00)
    val OnTertiaryContainer = Color(0xFFFFD9B3)

    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF690005)
    val ErrorContainer = Color(0xFF93000A)
    val OnErrorContainer = Color(0xFFFFDAD6)

    val Background = Color(0xFF1C1B1B)
    val OnBackground = Color(0xFFE2E2E6)

    val Surface = Color(0xFF1C1B1B)
    val OnSurface = Color(0xFFE2E2E6)

    val SurfaceVariant = Color(0xFF444746)
    val OnSurfaceVariant = Color(0xFFC4C7C5)

    val Outline = Color(0xFF8E918F)
    val OutlineVariant = Color(0xFF444746)
}

// ============================================================================
// Container Colors (for elevated surfaces)
// ============================================================================

val ContainerLow = Color(0xFFF4F4F9) // Level 1 elevation
val ContainerMedium = Color(0xFFE8E8EF) // Level 2 elevation
val ContainerHigh = Color(0xFFD1D1DB) // Level 3 elevation

// ============================================================================
// Overlay Colors
// ============================================================================

val Scrim = Color(0xDD000000) // Overlay for modals (87% opacity)
val ScrimLight = Color(0x66000000) // Light overlay (40% opacity)

// ============================================================================
// Progress Colors
// ============================================================================

object ProgressColors {
    val Background = Color(0xFFE0E0E0)
    val Fill = Primary
    val Low = Error // Below 50%
    val Medium = Warning // 50-79%
    val High = Success // 80-100%
}

// ============================================================================
// Star Rating Colors
// ============================================================================

object StarColors {
    val Inactive = Color(0xFFBDBDBD) // Gray for empty stars
    val Active = AccentOrange // Orange for filled stars
    val Gold = Color(0xFFFFD700) // Gold for perfect score
    val Silver = Color(0xFFC0C0C0) // Silver for 2 stars
    val Bronze = Color(0xFFCD7F32) // Bronze for 1 star
}
