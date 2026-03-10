package com.wordland.ui.components

import androidx.compose.ui.graphics.Color
import com.wordland.domain.model.FogLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FogOverlay component
 *
 * Story #2.2: Fog System Enhancement
 *
 * Test coverage:
 * - Fog level calculation
 * - Fog configuration
 * - Visibility radius calculation
 * - Fog state transitions
 */
class FogOverlayTest {
    /**
     * TC-EP2-021: Fog State Transitions
     * Test fog state transitions from unexplored to completed
     */
    @Test
    fun fogState_transitionsCorrectly() {
        // Test: Unexplored (LOCKED) → HIDDEN → PARTIAL → VISIBLE

        // Initial state: locked
        val lockedFog =
            calculateFogLevel(
                explored = false,
                adjacentToExplored = false,
                isUnlocked = false,
            )
        assertEquals(
            "Unexplored and locked should be LOCKED",
            FogLevel.LOCKED,
            lockedFog,
        )

        // Unlocked but not explored: hidden
        val hiddenFog =
            calculateFogLevel(
                explored = false,
                adjacentToExplored = false,
                isUnlocked = true,
            )
        assertEquals(
            "Unlocked but unexplored should be HIDDEN",
            FogLevel.HIDDEN,
            hiddenFog,
        )

        // Adjacent to explored: partial
        val partialFog =
            calculateFogLevel(
                explored = false,
                adjacentToExplored = true,
                isUnlocked = true,
            )
        assertEquals(
            "Adjacent to explored should be PARTIAL",
            FogLevel.PARTIAL,
            partialFog,
        )

        // Explored: visible
        val visibleFog =
            calculateFogLevel(
                explored = true,
                adjacentToExplored = false,
                isUnlocked = true,
            )
        assertEquals(
            "Explored should be VISIBLE",
            FogLevel.VISIBLE,
            visibleFog,
        )
    }

    /**
     * Test that explored regions always show visible fog
     */
    @Test
    fun fogState_exploredAlwaysVisible() {
        val result =
            calculateFogLevel(
                explored = true,
                adjacentToExplored = false,
                isUnlocked = false,
            )
        assertEquals(
            "Explored regions should always be visible",
            FogLevel.VISIBLE,
            result,
        )
    }

    /**
     * Test fog configuration defaults
     */
    @Test
    fun fogConfig_hasCorrectDefaults() {
        val config = FogConfig.Default

        assertEquals(
            "Default hidden color should be #696969",
            Color(0xFF696969),
            config.hiddenColor,
        )
        assertEquals(
            "Default hidden alpha should be 0.6f",
            0.6f,
            config.hiddenAlpha,
            0.001f,
        )
        assertEquals(
            "Default locked color should be #4A4A4A",
            Color(0xFF4A4A4A),
            config.lockedColor,
        )
        assertEquals(
            "Default locked alpha should be 0.7f",
            0.7f,
            config.lockedAlpha,
            0.001f,
        )
        assertEquals(
            "Default partial color should be #D3D3D3",
            Color(0xFFD3D3D3),
            config.partialColor,
        )
        assertEquals(
            "Default partial alpha should be 0.4f",
            0.4f,
            config.partialAlpha,
            0.001f,
        )
        assertTrue(
            "Drift should be enabled by default",
            config.driftEnabled,
        )
        assertEquals(
            "Drift duration should be 30 seconds",
            30000,
            config.driftDuration,
        )
    }

    /**
     * Test high contrast configuration
     */
    @Test
    fun fogConfig_highContrastHasCorrectSettings() {
        val config = FogConfig.HighContrast

        assertTrue(
            "High contrast alpha should be higher",
            config.hiddenAlpha > FogConfig.Default.hiddenAlpha,
        )
        assertTrue(
            "High contrast locked alpha should be higher",
            config.lockedAlpha > FogConfig.Default.lockedAlpha,
        )
        assertFalse(
            "Drift should be disabled for high contrast",
            config.driftEnabled,
        )
        assertTrue(
            "High contrast should have fewer cloud layers",
            config.cloudLayers < FogConfig.Default.cloudLayers,
        )
    }

    /**
     * TC-EP2-029 to TC-EP2-031: Visibility Radius Tests
     * Test visibility radius based on player level
     */
    @Test
    fun visibilityRadius_calculatesCorrectly() {
        // Level 1-3: 15% of map
        assertEquals(
            0.15f,
            com.wordland.domain.model.FogState.calculateVisibilityRadius(1),
            0.001f,
        )
        assertEquals(
            0.15f,
            com.wordland.domain.model.FogState.calculateVisibilityRadius(3),
            0.001f,
        )

        // Level 4-6: 30% of map
        assertEquals(
            0.30f,
            com.wordland.domain.model.FogState.calculateVisibilityRadius(4),
            0.001f,
        )
        assertEquals(
            0.30f,
            com.wordland.domain.model.FogState.calculateVisibilityRadius(6),
            0.001f,
        )

        // Level 7+: 50% of map
        assertEquals(
            0.50f,
            com.wordland.domain.model.FogState.calculateVisibilityRadius(7),
            0.001f,
        )
        assertEquals(
            0.50f,
            com.wordland.domain.model.FogState.calculateVisibilityRadius(10),
            0.001f,
        )
        assertEquals(
            0.50f,
            com.wordland.domain.model.FogState.calculateVisibilityRadius(100),
            0.001f,
        )
    }

    /**
     * Test fog state creation for player level
     */
    @Test
    fun fogState_forPlayerLevel() {
        val level1Fog = com.wordland.domain.model.FogState.forPlayerLevel(1)
        assertEquals(
            "Level 1 should have 15% visibility radius",
            0.15f,
            level1Fog.visibilityRadius,
            0.001f,
        )
        assertTrue(
            "Level 1 should have animation disabled",
            !level1Fog.animationEnabled,
        )

        val level5Fog = com.wordland.domain.model.FogState.forPlayerLevel(5)
        assertEquals(
            "Level 5 should have 30% visibility radius",
            0.30f,
            level5Fog.visibilityRadius,
            0.001f,
        )
        assertTrue(
            "Level 5 should have animation enabled (level > 3)",
            level5Fog.animationEnabled,
        )

        val level7Fog = com.wordland.domain.model.FogState.forPlayerLevel(7)
        assertEquals(
            "Level 7 should have 50% visibility radius",
            0.50f,
            level7Fog.visibilityRadius,
            0.001f,
        )
        assertTrue(
            "Level 7 should have animation enabled",
            level7Fog.animationEnabled,
        )
    }

    /**
     * Test position visibility calculation
     */
    @Test
    fun fogState_isPositionVisible() {
        val playerPos = com.wordland.domain.model.MapPosition(0.5f, 0.5f)
        val fogState =
            com.wordland.domain.model.FogState(
                density = 0.3f,
                animationEnabled = true,
                visibilityRadius = 0.15f,
            )

        // Position at player location should be visible
        assertTrue(
            "Player position should be visible",
            fogState.isPositionVisible(playerPos, playerPos),
        )

        // Position within visibility radius should be visible
        val nearbyPos = com.wordland.domain.model.MapPosition(0.55f, 0.5f)
        assertTrue(
            "Nearby position should be visible",
            fogState.isPositionVisible(nearbyPos, playerPos),
        )

        // Position outside visibility radius should not be visible
        val farPos = com.wordland.domain.model.MapPosition(0.8f, 0.5f)
        assertFalse(
            "Far position should not be visible",
            fogState.isPositionVisible(farPos, playerPos),
        )
    }

    /**
     * Test position visibility with different radii
     */
    @Test
    fun fogState_visibilityRadiusAffectsDetection() {
        val playerPos = com.wordland.domain.model.MapPosition(0.5f, 0.5f)
        val testPos = com.wordland.domain.model.MapPosition(0.7f, 0.5f)

        // Small radius: position not visible
        val smallRadius =
            com.wordland.domain.model.FogState(
                density = 0.3f,
                animationEnabled = true,
                visibilityRadius = 0.10f,
            )
        assertFalse(
            "Position should not be visible with small radius",
            smallRadius.isPositionVisible(testPos, playerPos),
        )

        // Large radius: position visible
        val largeRadius =
            com.wordland.domain.model.FogState(
                density = 0.3f,
                animationEnabled = true,
                visibilityRadius = 0.30f,
            )
        assertTrue(
            "Position should be visible with large radius",
            largeRadius.isPositionVisible(testPos, playerPos),
        )
    }

    /**
     * Test edge cases for fog level calculation
     */
    @Test
    fun fogLevel_edgeCases() {
        // Adjacent but locked: should be LOCKED
        val adjacentLocked =
            calculateFogLevel(
                explored = false,
                adjacentToExplored = true,
                isUnlocked = false,
            )
        assertEquals(
            "Adjacent but locked should remain LOCKED",
            FogLevel.LOCKED,
            adjacentLocked,
        )

        // Adjacent and unlocked but not explored: PARTIAL
        val adjacentUnlocked =
            calculateFogLevel(
                explored = false,
                adjacentToExplored = true,
                isUnlocked = true,
            )
        assertEquals(
            "Adjacent and unlocked should be PARTIAL",
            FogLevel.PARTIAL,
            adjacentUnlocked,
        )
    }

    /**
     * Test fog level progression for typical game flow
     */
    @Test
    fun fogLevel_typicalGameFlow() {
        // Initial state: locked
        var fog = calculateFogLevel(explored = false, isUnlocked = false)
        assertEquals(FogLevel.LOCKED, fog)

        // Level 1 completed: unlocks next region (hidden)
        fog = calculateFogLevel(explored = false, isUnlocked = true)
        assertEquals(FogLevel.HIDDEN, fog)

        // Adjacent region explored: becomes partial
        fog = calculateFogLevel(explored = false, adjacentToExplored = true, isUnlocked = true)
        assertEquals(FogLevel.PARTIAL, fog)

        // Region fully explored: visible
        fog = calculateFogLevel(explored = true, isUnlocked = true)
        assertEquals(FogLevel.VISIBLE, fog)
    }
}
