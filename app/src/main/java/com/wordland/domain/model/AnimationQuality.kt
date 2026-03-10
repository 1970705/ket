package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Animation quality settings for performance-aware visual feedback
 *
 * Part of P0 Phase 1: Visual Feedback Enhancements
 *
 * Design Philosophy:
 * - Performance-first approach (60fps target)
 * - Graceful degradation for lower-end devices
 * - User can override in settings (future)
 */
@Immutable
sealed class AnimationQuality {
    /**
     * High quality - All features enabled
     * Target: Flagship devices with good GPU
     */
    @Immutable
    data object High : AnimationQuality()

    /**
     * Medium quality - Reduced animations
     * Target: Mid-range devices
     */
    @Immutable
    data object Medium : AnimationQuality()

    /**
     * Low quality - Static feedback only
     * Target: Budget devices or battery saver mode
     */
    @Immutable
    data object Low : AnimationQuality()

    /**
     * Number of particles for confetti effect
     */
    val particleCount: Int
        get() =
            when (this) {
                is High -> 100
                is Medium -> 40
                is Low -> 0
            }

    /**
     * Whether animations are enabled at all
     */
    val animationEnabled: Boolean
        get() = this != Low

    /**
     * Target FPS for animations
     */
    val targetFps: Int
        get() =
            when (this) {
                is High -> 60
                is Medium -> 30
                is Low -> 30
            }

    /**
     * Animation duration multiplier (1.0 = normal)
     */
    val durationMultiplier: Float
        get() =
            when (this) {
                is High -> 1.0f
                is Medium -> 0.7f
                is Low -> 0.0f
            }
}

/**
 * Device performance tier detection
 */
enum class PerformanceTier {
    HIGH, // All features, 60fps
    MEDIUM, // Reduced animations, 30fps
    LOW, // Static rendering, no animations
}

/**
 * Get animation quality based on performance tier
 */
fun getAnimationQuality(tier: PerformanceTier): AnimationQuality =
    when (tier) {
        PerformanceTier.HIGH -> AnimationQuality.High
        PerformanceTier.MEDIUM -> AnimationQuality.Medium
        PerformanceTier.LOW -> AnimationQuality.Low
    }
