package com.wordland.domain.performance

import com.wordland.domain.model.AnimationQuality

/**
 * Quality settings configuration for different performance tiers
 *
 * Part of Task #20: Device Performance Detection and Performance Baseline
 *
 * This class maps performance tiers to concrete quality settings that control
 * visual fidelity, animations, and rendering features.
 *
 * Design Philosophy:
 * - Performance-first (60fps target for HIGH tier)
 * - Graceful degradation for lower-end devices
 * - User-override capability (future feature)
 *
 * Integration:
 * - Maps to AnimationQuality in domain/model/AnimationQuality.kt
 * - Used by UI components to adjust rendering quality
 */
sealed class QualitySettings(
    open val tier: PerformanceTier,
    open val animationQuality: AnimationQuality,
) {
    /**
     * High quality settings
     * Target: Flagship devices (60fps)
     */
    data class High(
        override val tier: PerformanceTier = PerformanceTier.HIGH,
        override val animationQuality: AnimationQuality = AnimationQuality.High,
        val enableParticleEffects: Boolean = true,
        val enableScreenShake: Boolean = true,
        val enableFogAnimation: Boolean = true,
        val enableTransitionAnimations: Boolean = true,
        val textureResolution: Float = 1.0f,
        val maxConcurrentAnimations: Int = 5,
        val fogQuality: FogQuality = FogQuality.SHADER,
        val shadowQuality: ShadowQuality = ShadowQuality.HIGH,
    ) : QualitySettings(tier, animationQuality)

    /**
     * Medium quality settings
     * Target: Mid-range devices (30fps)
     */
    data class Medium(
        override val tier: PerformanceTier = PerformanceTier.MEDIUM,
        override val animationQuality: AnimationQuality = AnimationQuality.Medium,
        val enableParticleEffects: Boolean = true,
        val enableScreenShake: Boolean = false,
        val enableFogAnimation: Boolean = false,
        val enableTransitionAnimations: Boolean = true,
        val textureResolution: Float = 0.75f,
        val maxConcurrentAnimations: Int = 3,
        val fogQuality: FogQuality = FogQuality.BITMAP,
        val shadowQuality: ShadowQuality = ShadowQuality.MEDIUM,
    ) : QualitySettings(tier, animationQuality)

    /**
     * Low quality settings
     * Target: Budget devices (30fps, minimal animations)
     */
    data class Low(
        override val tier: PerformanceTier = PerformanceTier.LOW,
        override val animationQuality: AnimationQuality = AnimationQuality.Low,
        val enableParticleEffects: Boolean = false,
        val enableScreenShake: Boolean = false,
        val enableFogAnimation: Boolean = false,
        val enableTransitionAnimations: Boolean = false,
        val textureResolution: Float = 0.5f,
        val maxConcurrentAnimations: Int = 1,
        val fogQuality: FogQuality = FogQuality.STATIC,
        val shadowQuality: ShadowQuality = ShadowQuality.NONE,
    ) : QualitySettings(tier, animationQuality)

    /**
     * Get target FPS for this quality level
     */
    val targetFps: Int
        get() = animationQuality.targetFps

    /**
     * Check if animations are enabled
     */
    val isAnimationEnabled: Boolean
        get() = animationQuality.animationEnabled

    /**
     * Get particle count for effects
     */
    val particleCount: Int
        get() = animationQuality.particleCount

    companion object {
        /**
         * Get quality settings for a performance tier
         */
        fun forTier(tier: PerformanceTier): QualitySettings {
            return when (tier) {
                PerformanceTier.HIGH -> High()
                PerformanceTier.MEDIUM -> Medium()
                PerformanceTier.LOW -> Low()
            }
        }

        /**
         * Get quality settings from animation quality
         */
        fun fromAnimationQuality(quality: AnimationQuality): QualitySettings {
            return when (quality) {
                is AnimationQuality.High -> High()
                is AnimationQuality.Medium -> Medium()
                is AnimationQuality.Low -> Low()
            }
        }
    }
}

/**
 * Fog quality settings
 *
 * Determines how the fog of war is rendered
 */
enum class FogQuality {
    /**
     * GPU-accelerated shader-based fog
     * Best visual quality, requires OpenGL ES 3.0+
     */
    SHADER,

    /**
     * Pre-rendered bitmap fog overlay
     * Good balance of quality and performance
     */
    BITMAP,

    /**
     * Static overlay with no animation
     * Best performance, minimal visual quality
     */
    STATIC,
}

/**
 * Shadow quality settings
 *
 * Determines shadow rendering quality
 */
enum class ShadowQuality {
    /**
     * Full shadow rendering with soft edges
     */
    HIGH,

    /**
     * Basic shadow rendering with hard edges
     */
    MEDIUM,

    /**
     * No shadow rendering
     */
    NONE,
}

/**
 * Quality preset for specific features
 *
 * Allows fine-grained control over individual features
 */
data class QualityPreset(
    val name: String,
    val description: String,
    val settings: QualitySettings,
    val isRecommended: Boolean = false,
) {
    companion object {
        /**
         * Get all available quality presets
         */
        fun getAllPresets(): List<QualityPreset> {
            return listOf(
                QualityPreset(
                    name = "Ultra",
                    description = "Maximum visual quality (flagship devices only)",
                    settings =
                        QualitySettings.High(
                            enableFogAnimation = true,
                            enableScreenShake = true,
                            textureResolution = 1.0f,
                            fogQuality = FogQuality.SHADER,
                            shadowQuality = ShadowQuality.HIGH,
                        ),
                    isRecommended = false,
                ),
                QualityPreset(
                    name = "High",
                    description = "High quality visuals (recommended for most devices)",
                    settings =
                        QualitySettings.High(
                            enableFogAnimation = true,
                            enableScreenShake = false,
                            textureResolution = 1.0f,
                            fogQuality = FogQuality.SHADER,
                            shadowQuality = ShadowQuality.MEDIUM,
                        ),
                    isRecommended = true,
                ),
                QualityPreset(
                    name = "Balanced",
                    description = "Balance between quality and performance",
                    settings =
                        QualitySettings.Medium(
                            enableFogAnimation = false,
                            enableScreenShake = false,
                            textureResolution = 0.75f,
                            fogQuality = FogQuality.BITMAP,
                            shadowQuality = ShadowQuality.MEDIUM,
                        ),
                    isRecommended = true,
                ),
                QualityPreset(
                    name = "Performance",
                    description = "Maximum performance (budget devices)",
                    settings =
                        QualitySettings.Low(
                            enableFogAnimation = false,
                            enableScreenShake = false,
                            textureResolution = 0.5f,
                            fogQuality = FogQuality.STATIC,
                            shadowQuality = ShadowQuality.NONE,
                        ),
                    isRecommended = false,
                ),
            )
        }

        /**
         * Get recommended preset for a performance tier
         */
        fun getRecommendedForTier(tier: PerformanceTier): QualityPreset {
            return when (tier) {
                PerformanceTier.HIGH -> getAllPresets().first { it.name == "High" }
                PerformanceTier.MEDIUM -> getAllPresets().first { it.name == "Balanced" }
                PerformanceTier.LOW -> getAllPresets().first { it.name == "Performance" }
            }
        }
    }
}

/**
 * Performance budget for quality settings
 *
 * Defines frame time budgets for different operations
 */
data class PerformanceBudget(
    val totalFrameTimeMs: Float,
    val mapRenderMs: Float,
    val fogRenderMs: Float,
    val animationMs: Float,
    val uiRenderMs: Float,
) {
    /**
     * Check if budget is valid (sum of parts <= total)
     */
    fun isValid(): Boolean {
        return (mapRenderMs + fogRenderMs + animationMs + uiRenderMs) <= totalFrameTimeMs
    }

    companion object {
        /**
         * Get performance budget for a quality level
         */
        fun forQuality(settings: QualitySettings): PerformanceBudget {
            val totalMs = 1000f / settings.targetFps

            return when (settings) {
                is QualitySettings.High ->
                    PerformanceBudget(
                        totalFrameTimeMs = totalMs,
                        mapRenderMs = totalMs * 0.35f, // 35%
                        fogRenderMs = totalMs * 0.25f, // 25%
                        animationMs = totalMs * 0.25f, // 25%
                        uiRenderMs = totalMs * 0.15f, // 15%
                    )
                is QualitySettings.Medium ->
                    PerformanceBudget(
                        totalFrameTimeMs = totalMs,
                        mapRenderMs = totalMs * 0.40f, // 40%
                        fogRenderMs = totalMs * 0.15f, // 15%
                        animationMs = totalMs * 0.20f, // 20%
                        uiRenderMs = totalMs * 0.25f, // 25%
                    )
                is QualitySettings.Low ->
                    PerformanceBudget(
                        totalFrameTimeMs = totalMs,
                        mapRenderMs = totalMs * 0.50f, // 50%
                        fogRenderMs = totalMs * 0.05f, // 5%
                        animationMs = totalMs * 0.10f, // 10%
                        uiRenderMs = totalMs * 0.35f, // 35%
                    )
            }
        }

        /**
         * Get strict 60fps budget
         */
        fun strict60Fps() =
            PerformanceBudget(
                totalFrameTimeMs = 16.6f,
                mapRenderMs = 5f,
                fogRenderMs = 3f,
                animationMs = 5f,
                uiRenderMs = 3.6f,
            )
    }
}
