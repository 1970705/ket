package com.wordland.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for enhanced animation components
 *
 * Epic #8.3: Enhanced Animation Effects
 *
 * Test coverage:
 * - AnimatedWordSwitch component
 * - SharedAxisTransition component
 * - ReduceMotionHelper utility
 * - ComboMilestoneEffects component
 * - OptimizedConfettiRenderer component
 *
 * TEMPORARILY DISABLED: Compose UI tests disabled due to Robolectric 4.13+
 * ComponentActivity compatibility issue.
 * See: docs/planning/epics/EPIC12_REAL_DEVICE_UI_AUTOMATION/TECH_DEBT.md
 *
 * @since 1.6
 */
@Ignore("Robolectric 4.13+ ComponentActivity issue - see TECH_DEBT.md")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AnimationEnhancementsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // ===== ReduceMotionHelper Tests =====

    @Test
    fun `AnimationPreference Default is correct`() {
        val preference = AnimationPreference.Default

        assertFalse(
            "Reduce motion should be disabled by default",
            preference.reduceMotion,
        )
        assertEquals(
            "Animation scale should be 1.0 (normal)",
            1.0f,
            preference.animationDurationScale,
        )
        assertTrue(
            "Animations should be enabled",
            preference.animationsEnabled,
        )
    }

    @Test
    fun `AnimationPreference NoAnimations is correct`() {
        val preference = AnimationPreference.NoAnimations

        assertTrue(
            "Reduce motion should be enabled",
            preference.reduceMotion,
        )
        assertFalse(
            "Animations should be disabled",
            preference.animationsEnabled,
        )
        assertEquals(
            "Animation scale should be 0.0",
            0.0f,
            preference.animationDurationScale,
        )
    }

    @Test
    fun `ReduceMotionHelper correctly adjusts animation duration`() {
        val preference =
            AnimationPreference(
                reduceMotion = false,
                animationDurationScale = 0.5f,
            )

        val adjustedDuration = ReduceMotionHelper.adjustDuration(400, preference)

        assertEquals(
            "Duration should be halved when scale is 0.5",
            200,
            adjustedDuration,
        )
    }

    @Test
    fun `ReduceMotionHelper returns zero duration when animations disabled`() {
        val preference =
            AnimationPreference(
                reduceMotion = true,
                animationDurationScale = 0.0f,
            )

        val adjustedDuration = ReduceMotionHelper.adjustDuration(400, preference)

        assertEquals(
            "Duration should be 0 when animations are disabled",
            0,
            adjustedDuration,
        )
    }

    @Test
    fun `AnimationPreference useReducedDuration is correct`() {
        val preference =
            AnimationPreference(
                reduceMotion = false,
                animationDurationScale = 0.5f,
            )

        assertTrue(
            "Should use reduced duration when scale is 0.5",
            preference.useReducedDuration,
        )
        assertTrue(
            "Animations should still be enabled",
            preference.animationsEnabled,
        )
    }

    @Test
    fun `AnimationPreference isReduced is correct for reduceMotion`() {
        val preference =
            AnimationPreference(
                reduceMotion = true,
                animationDurationScale = 1.0f,
            )

        assertTrue(
            "Should be reduced when reduceMotion is true",
            preference.isReduced,
        )
    }

    @Test
    fun `AnimationPreference durationMultiplier is correct`() {
        assertEquals(
            "Full multiplier",
            1.0f,
            AnimationPreference.Default.durationMultiplier,
            0.001f,
        )
        assertEquals(
            "Zero multiplier when disabled",
            0f,
            AnimationPreference.NoAnimations.durationMultiplier,
        )
        assertEquals(
            "Reduced multiplier",
            0.3f,
            AnimationPreference(
                reduceMotion = true,
                animationDurationScale = 1.0f,
            ).durationMultiplier,
            0.001f,
        )
    }

    @Test
    fun `animationQualityFromPreference returns LOW for reduced motion`() {
        val preference =
            AnimationPreference(
                reduceMotion = true,
                animationDurationScale = 1.0f,
            )

        val quality = animationQualityFromPreference(preference)

        assertTrue(
            "Should return Low quality for reduced motion",
            quality is com.wordland.domain.model.AnimationQuality.Low,
        )
    }

    @Test
    fun `animationQualityFromPreference returns MEDIUM for reduced duration`() {
        val preference =
            AnimationPreference(
                reduceMotion = false,
                animationDurationScale = 0.5f,
            )

        val quality = animationQualityFromPreference(preference)

        assertTrue(
            "Should return Medium quality for reduced duration",
            quality is com.wordland.domain.model.AnimationQuality.Medium,
        )
    }

    @Test
    fun `animationQualityFromPreference returns HIGH for normal settings`() {
        val quality = animationQualityFromPreference(AnimationPreference.Default)

        assertTrue(
            "Should return High quality for normal settings",
            quality is com.wordland.domain.model.AnimationQuality.High,
        )
    }

    // ===== SharedAxisTransition Tests =====

    @Test
    fun `SharedAxis enum has correct values`() {
        assertEquals("Z axis exists", SharedAxis.Z, SharedAxis.valueOf("Z"))
        assertEquals("X axis exists", SharedAxis.X, SharedAxis.valueOf("X"))
        assertEquals("Y axis exists", SharedAxis.Y, SharedAxis.valueOf("Y"))
    }

    // ===== PerformanceMode Tests =====

    @Test
    fun `PerformanceMode enum has correct values`() {
        assertEquals("HIGH mode exists", PerformanceMode.HIGH, PerformanceMode.valueOf("HIGH"))
        assertEquals(
            "BALANCED mode exists",
            PerformanceMode.BALANCED,
            PerformanceMode.valueOf("BALANCED"),
        )
        assertEquals("LOW mode exists", PerformanceMode.LOW, PerformanceMode.valueOf("LOW"))
    }

    // ===== ButtonSize Tests =====

    @Test
    fun `ButtonSize enum has correct values`() {
        assertEquals("Small size exists", ButtonSize.SMALL, ButtonSize.valueOf("SMALL"))
        assertEquals(
            "Medium size exists",
            ButtonSize.MEDIUM,
            ButtonSize.valueOf("MEDIUM"),
        )
        assertEquals("Large size exists", ButtonSize.LARGE, ButtonSize.valueOf("LARGE"))
    }

    // ===== AnimatedWordSwitch Composition Tests =====
    // NOTE: These tests are disabled due to Compose test setup issues
    // See: docs/planning/epics/EPIC12_REAL_DEVICE_UI_AUTOMATION/TECH_DEBT.md

    @Ignore("Compose test setup issue - composeTestRule not defined - see TECH_DEBT.md")
    @Test
    fun `AnimatedWordSwitch renders content with reduceMotion`() {
        // var contentRendered = false
        // composeTestRule.setContent {
        //     AnimatedWordSwitch(
        //         wordKey = "test",
        //         reduceMotion = true,
        //     ) {
        //         contentRendered = true
        //     }
        // }
        // assertTrue("Content should be rendered even with reduce motion", contentRendered)
    }

    @Ignore("Compose test setup issue - composeTestRule not defined - see TECH_DEBT.md")
    @Test
    fun `AnimatedWordSwitch renders content without reduceMotion`() {
        // var contentRendered = false
        // composeTestRule.setContent {
        //     AnimatedWordSwitch(
        //         wordKey = "test",
        //         reduceMotion = false,
        //     ) {
        //         contentRendered = true
        //     }
        // }
        // assertTrue("Content should be rendered with animations", contentRendered)
    }

    @Ignore("Compose test setup issue - composeTestRule not defined - see TECH_DEBT.md")
    @Test
    fun `AnimatedSlideOut respects reduceMotion`() {
        // var contentRendered = false
        // composeTestRule.setContent {
        //     AnimatedSlideOut(
        //         isVisible = true,
        //         reduceMotion = true,
        //     ) {
        //         contentRendered = true
        //     }
        // }
        // assertTrue("Content should be rendered with reduce motion", contentRendered)
    }

    @Ignore("Compose test setup issue - composeTestRule not defined - see TECH_DEBT.md")
    @Test
    fun `AnimatedSlideIn respects reduceMotion`() {
        // var contentRendered = false
        // composeTestRule.setContent {
        //     AnimatedSlideIn(
        //         isVisible = true,
        //         reduceMotion = true,
        //     ) {
        //         contentRendered = true
        //     }
        // }
        // assertTrue("Content should be rendered with reduce motion", contentRendered)
    }

    // ===== OptimizedConfettiParticle Tests =====

    @Test
    fun `OptimizedConfettiParticle data class has correct properties`() {
        val particle =
            OptimizedConfettiParticle(
                x = 100f,
                y = 200f,
                velocityX = 1f,
                velocityY = 2f,
                color = androidx.compose.ui.graphics.Color.Red,
                rotation = 45f,
                rotationSpeed = 5f,
                width = 10f,
                height = 6f,
                alpha = 1f,
            )

        assertEquals("X position should be 100", 100f, particle.x, 0f)
        assertEquals("Y position should be 200", 200f, particle.y, 0f)
        assertEquals("Velocity X should be 1", 1f, particle.velocityX, 0f)
        assertEquals("Velocity Y should be 2", 2f, particle.velocityY, 0f)
        assertEquals("Rotation should be 45", 45f, particle.rotation, 0f)
        assertEquals("Rotation speed should be 5", 5f, particle.rotationSpeed, 0f)
        assertEquals("Width should be 10", 10f, particle.width, 0f)
        assertEquals("Height should be 6", 6f, particle.height, 0f)
        assertEquals("Alpha should be 1", 1f, particle.alpha, 0f)
    }

    @Test
    fun `OptimizedConfettiParticle can be copied with modifications`() {
        val original =
            OptimizedConfettiParticle(
                x = 100f,
                y = 200f,
                velocityX = 1f,
                velocityY = 2f,
                color = androidx.compose.ui.graphics.Color.Red,
            )

        val modified = original.copy(x = 150f, alpha = 0.5f)

        assertEquals("Original should be unchanged", 100f, original.x, 0f)
        assertEquals("Modified should have new X", 150f, modified.x, 0f)
        assertEquals("Modified should have new alpha", 0.5f, modified.alpha, 0f)
        assertEquals("Other properties should be preserved", 200f, modified.y, 0f)
    }
}
