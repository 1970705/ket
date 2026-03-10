package com.wordland.domain.performance

import com.wordland.domain.model.AnimationQuality
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for QualitySettings
 *
 * Part of Task #20: Device Performance Detection and Performance Baseline
 *
 * Tests:
 * - Quality tier mapping
 * - Quality preset retrieval
 * - Performance budget calculation
 * - Settings properties
 */
class QualitySettingsTest {
    @Test
    fun `forTier returns High settings for HIGH tier`() {
        // Act
        val settings = QualitySettings.forTier(PerformanceTier.HIGH)

        // Assert
        assertTrue(settings is QualitySettings.High)
        assertEquals(PerformanceTier.HIGH, settings.tier)
        assertEquals(AnimationQuality.High, settings.animationQuality)
    }

    @Test
    fun `forTier returns Medium settings for MEDIUM tier`() {
        // Act
        val settings = QualitySettings.forTier(PerformanceTier.MEDIUM)

        // Assert
        assertTrue(settings is QualitySettings.Medium)
        assertEquals(PerformanceTier.MEDIUM, settings.tier)
        assertEquals(AnimationQuality.Medium, settings.animationQuality)
    }

    @Test
    fun `forTier returns Low settings for LOW tier`() {
        // Act
        val settings = QualitySettings.forTier(PerformanceTier.LOW)

        // Assert
        assertTrue(settings is QualitySettings.Low)
        assertEquals(PerformanceTier.LOW, settings.tier)
        assertEquals(AnimationQuality.Low, settings.animationQuality)
    }

    @Test
    fun `fromAnimationQuality returns High settings for High quality`() {
        // Act
        val settings = QualitySettings.fromAnimationQuality(AnimationQuality.High)

        // Assert
        assertTrue(settings is QualitySettings.High)
    }

    @Test
    fun `fromAnimationQuality returns Medium settings for Medium quality`() {
        // Act
        val settings = QualitySettings.fromAnimationQuality(AnimationQuality.Medium)

        // Assert
        assertTrue(settings is QualitySettings.Medium)
    }

    @Test
    fun `fromAnimationQuality returns Low settings for Low quality`() {
        // Act
        val settings = QualitySettings.fromAnimationQuality(AnimationQuality.Low)

        // Assert
        assertTrue(settings is QualitySettings.Low)
    }

    @Test
    fun `High quality settings have correct default values`() {
        // Act
        val settings = QualitySettings.High()

        // Assert
        assertTrue(settings.enableParticleEffects)
        assertTrue(settings.enableScreenShake)
        assertTrue(settings.enableFogAnimation)
        assertTrue(settings.enableTransitionAnimations)
        assertEquals(1.0f, settings.textureResolution)
        assertEquals(5, settings.maxConcurrentAnimations)
        assertEquals(FogQuality.SHADER, settings.fogQuality)
        assertEquals(ShadowQuality.HIGH, settings.shadowQuality)
        assertEquals(60, settings.targetFps)
        assertTrue(settings.isAnimationEnabled)
        assertEquals(100, settings.particleCount)
    }

    @Test
    fun `Medium quality settings have correct default values`() {
        // Act
        val settings = QualitySettings.Medium()

        // Assert
        assertTrue(settings.enableParticleEffects)
        assertFalse(settings.enableScreenShake)
        assertFalse(settings.enableFogAnimation)
        assertTrue(settings.enableTransitionAnimations)
        assertEquals(0.75f, settings.textureResolution)
        assertEquals(3, settings.maxConcurrentAnimations)
        assertEquals(FogQuality.BITMAP, settings.fogQuality)
        assertEquals(ShadowQuality.MEDIUM, settings.shadowQuality)
        assertEquals(30, settings.targetFps)
        assertTrue(settings.isAnimationEnabled)
        assertEquals(40, settings.particleCount)
    }

    @Test
    fun `Low quality settings have correct default values`() {
        // Act
        val settings = QualitySettings.Low()

        // Assert
        assertFalse(settings.enableParticleEffects)
        assertFalse(settings.enableScreenShake)
        assertFalse(settings.enableFogAnimation)
        assertFalse(settings.enableTransitionAnimations)
        assertEquals(0.5f, settings.textureResolution)
        assertEquals(1, settings.maxConcurrentAnimations)
        assertEquals(FogQuality.STATIC, settings.fogQuality)
        assertEquals(ShadowQuality.NONE, settings.shadowQuality)
        assertEquals(30, settings.targetFps)
        assertFalse(settings.isAnimationEnabled)
        assertEquals(0, settings.particleCount)
    }

    @Test
    fun `High settings can be customized`() {
        // Act
        val settings =
            QualitySettings.High(
                enableScreenShake = false,
                textureResolution = 0.9f,
            )

        // Assert
        assertTrue(settings.enableParticleEffects)
        assertFalse(settings.enableScreenShake)
        assertEquals(0.9f, settings.textureResolution)
    }

    @Test
    fun `Medium settings can be customized`() {
        // Act
        val settings =
            QualitySettings.Medium(
                enableParticleEffects = false,
                maxConcurrentAnimations = 2,
            )

        // Assert
        assertFalse(settings.enableParticleEffects)
        assertEquals(2, settings.maxConcurrentAnimations)
    }

    @Test
    fun `Low settings can be customized`() {
        // Act
        val settings =
            QualitySettings.Low(
                enableTransitionAnimations = true,
                textureResolution = 0.75f,
            )

        // Assert
        assertTrue(settings.enableTransitionAnimations)
        assertEquals(0.75f, settings.textureResolution)
    }

    @Test
    fun `QualityPreset getAllPresets returns all presets`() {
        // Act
        val presets = QualityPreset.getAllPresets()

        // Assert
        assertEquals(4, presets.size)
        assertEquals("Ultra", presets[0].name)
        assertEquals("High", presets[1].name)
        assertEquals("Balanced", presets[2].name)
        assertEquals("Performance", presets[3].name)
    }

    @Test
    fun `QualityPreset getRecommendedForTier returns correct preset`() {
        // Act & Assert
        val highPreset = QualityPreset.getRecommendedForTier(PerformanceTier.HIGH)
        assertEquals("High", highPreset.name)
        assertTrue(highPreset.isRecommended)

        val mediumPreset = QualityPreset.getRecommendedForTier(PerformanceTier.MEDIUM)
        assertEquals("Balanced", mediumPreset.name)
        assertTrue(mediumPreset.isRecommended)

        val lowPreset = QualityPreset.getRecommendedForTier(PerformanceTier.LOW)
        assertEquals("Performance", lowPreset.name)
        assertFalse(lowPreset.isRecommended) // Not marked as recommended in our list
    }

    @Test
    fun `PerformanceBudget forQuality returns valid budget for High`() {
        // Act
        val budget = PerformanceBudget.forQuality(QualitySettings.High())

        // Assert
        assertEquals(16.6f, budget.totalFrameTimeMs, 0.1f)
        assertTrue(budget.mapRenderMs > 0)
        assertTrue(budget.fogRenderMs > 0)
        assertTrue(budget.animationMs > 0)
        assertTrue(budget.uiRenderMs > 0)
        assertTrue(budget.isValid())
    }

    @Test
    fun `PerformanceBudget forQuality returns valid budget for Medium`() {
        // Act
        val budget = PerformanceBudget.forQuality(QualitySettings.Medium())

        // Assert
        assertEquals(33.3f, budget.totalFrameTimeMs, 0.1f)
        assertTrue(budget.isValid())
    }

    @Test
    fun `PerformanceBudget forQuality returns valid budget for Low`() {
        // Act
        val budget = PerformanceBudget.forQuality(QualitySettings.Low())

        // Assert
        assertEquals(33.3f, budget.totalFrameTimeMs, 0.1f)
        assertTrue(budget.isValid())
    }

    @Test
    fun `PerformanceBudget strict60Fps returns valid budget`() {
        // Act
        val budget = PerformanceBudget.strict60Fps()

        // Assert
        assertEquals(16.6f, budget.totalFrameTimeMs, 0.1f)
        assertEquals(5f, budget.mapRenderMs, 0.1f)
        assertEquals(3f, budget.fogRenderMs, 0.1f)
        assertEquals(5f, budget.animationMs, 0.1f)
        assertEquals(3.6f, budget.uiRenderMs, 0.1f)
        assertTrue(budget.isValid())
    }

    @Test
    fun `PerformanceBudget isValid returns false for invalid budget`() {
        // Arrange - Create a budget that exceeds total
        val budget =
            PerformanceBudget(
                totalFrameTimeMs = 16.6f,
                mapRenderMs = 10f,
                fogRenderMs = 5f,
                animationMs = 5f,
                uiRenderMs = 5f,
            )

        // Assert
        assertFalse(budget.isValid())
    }

    @Test
    fun `PerformanceBudget isValid returns true for valid budget`() {
        // Arrange
        val budget =
            PerformanceBudget(
                totalFrameTimeMs = 16.6f,
                mapRenderMs = 5f,
                fogRenderMs = 3f,
                animationMs = 5f,
                uiRenderMs = 3.6f,
            )

        // Assert
        assertTrue(budget.isValid())
    }

    @Test
    fun `FogQuality enum values are accessible`() {
        val qualities =
            listOf(
                FogQuality.SHADER,
                FogQuality.BITMAP,
                FogQuality.STATIC,
            )

        assertEquals(3, qualities.size)
    }

    @Test
    fun `ShadowQuality enum values are accessible`() {
        val qualities =
            listOf(
                ShadowQuality.HIGH,
                ShadowQuality.MEDIUM,
                ShadowQuality.NONE,
            )

        assertEquals(3, qualities.size)
    }

    @Test
    fun `High quality settings have correct animation count`() {
        // Act
        val settings = QualitySettings.High()

        // Assert
        assertEquals(100, settings.particleCount)
        assertEquals(5, settings.maxConcurrentAnimations)
    }

    @Test
    fun `Medium quality settings have correct animation count`() {
        // Act
        val settings = QualitySettings.Medium()

        // Assert
        assertEquals(40, settings.particleCount)
        assertEquals(3, settings.maxConcurrentAnimations)
    }

    @Test
    fun `Low quality settings have correct animation count`() {
        // Act
        val settings = QualitySettings.Low()

        // Assert
        assertEquals(0, settings.particleCount)
        assertEquals(1, settings.maxConcurrentAnimations)
    }

    @Test
    fun `QualityPreset contains valid settings for all presets`() {
        // Act
        val presets = QualityPreset.getAllPresets()

        // Assert
        presets.forEach { preset ->
            assertNotNull(preset.name)
            assertNotNull(preset.description)
            assertNotNull(preset.settings)
            assertTrue(preset.settings.targetFps > 0)
        }
    }

    @Test
    fun `PerformanceBudget allocation sums correctly for High quality`() {
        // Act
        val budget = PerformanceBudget.forQuality(QualitySettings.High())

        // Assert
        val sum = budget.mapRenderMs + budget.fogRenderMs + budget.animationMs + budget.uiRenderMs
        assertTrue(sum <= budget.totalFrameTimeMs)
        assertTrue(sum > budget.totalFrameTimeMs * 0.8f) // Uses most of the budget
    }

    @Test
    fun `PerformanceBudget allocates more to map rendering for Low quality`() {
        // Act
        val lowBudget = PerformanceBudget.forQuality(QualitySettings.Low())
        val highBudget = PerformanceBudget.forQuality(QualitySettings.High())

        // Assert - Low quality should allocate higher percentage to map rendering
        val lowMapRatio = lowBudget.mapRenderMs / lowBudget.totalFrameTimeMs
        val highMapRatio = highBudget.mapRenderMs / highBudget.totalFrameTimeMs

        assertTrue(lowMapRatio > highMapRatio)
    }

    @Test
    fun `PerformanceBudget allocates less to animations for Low quality`() {
        // Act
        val lowBudget = PerformanceBudget.forQuality(QualitySettings.Low())
        val highBudget = PerformanceBudget.forQuality(QualitySettings.High())

        // Assert - Low quality should allocate less to animations
        val lowAnimRatio = lowBudget.animationMs / lowBudget.totalFrameTimeMs
        val highAnimRatio = highBudget.animationMs / highBudget.totalFrameTimeMs

        assertTrue(lowAnimRatio < highAnimRatio)
    }
}
