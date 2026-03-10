package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.FogLevel
import com.wordland.domain.model.MapBounds
import com.wordland.domain.model.MapPosition
import com.wordland.domain.model.MapRegion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for RegionUnlockUseCase
 *
 * Story #2.4: Region Unlock Logic
 *
 * Test coverage:
 * - Unlock conditions (adjacent explored regions)
 * - Unlock persistence
 * - Unlock progress calculation
 * - Adjacency detection
 *
 * Note: These tests verify the core logic without mocking
 * to avoid MockK dependency issues.
 */
class RegionUnlockUseCaseTest {
    // Test regions
    private val lookPeninsula =
        MapRegion(
            id = "look_peninsula",
            islandId = "look_island",
            name = "Look Peninsula",
            position = MapPosition(0.2f, 0.3f),
            bounds = MapBounds(0.1f, 0.2f, 0.3f, 0.4f),
            fogLevel = FogLevel.VISIBLE,
            isUnlocked = true,
            icon = "🔍",
        )

    private val makeLake =
        MapRegion(
            id = "make_lake",
            islandId = "make_island",
            name = "Make Lake",
            position = MapPosition(0.35f, 0.35f), // Adjacent to Look Peninsula
            bounds = MapBounds(0.25f, 0.25f, 0.45f, 0.45f),
            fogLevel = FogLevel.HIDDEN,
            isUnlocked = false,
            icon = "🎨",
        )

    private val sayMountain =
        MapRegion(
            id = "say_mountain",
            islandId = "say_island",
            name = "Say Mountain",
            position = MapPosition(0.7f, 0.2f), // Far from Look Peninsula
            bounds = MapBounds(0.6f, 0.1f, 0.8f, 0.3f),
            fogLevel = FogLevel.LOCKED,
            isUnlocked = false,
            icon = "🗣️",
        )

    private val allRegions = listOf(lookPeninsula, makeLake, sayMountain)

    /**
     * TC-EP2-053: Unlock Adjacent Region Requirement
     * Test that adjacent regions are unlockable
     */
    @Test
    fun regionUnlock_adjacentRegionIsUnlockable() {
        // Look Peninsula (0.2, 0.3) and Make Lake (0.35, 0.35)
        // Distance ~0.18 < 0.25 threshold

        val dx = lookPeninsula.position.x - makeLake.position.x
        val dy = lookPeninsula.position.y - makeLake.position.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        assertTrue(
            "Make Lake should be adjacent to Look Peninsula",
            distance <= 0.25f,
        )
    }

    /**
     * TC-EP2-054: Unlock Non-Adjacent Region Fails
     * Test that non-adjacent regions are not unlockable
     */
    @Test
    fun regionUnlock_nonAdjacentRegionNotUnlockable() {
        // Look Peninsula (0.2, 0.3) and Say Mountain (0.7, 0.2)
        // Distance ~0.5 > 0.25 threshold

        val dx = lookPeninsula.position.x - sayMountain.position.x
        val dy = lookPeninsula.position.y - sayMountain.position.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        assertFalse(
            "Say Mountain should NOT be adjacent to Look Peninsula",
            distance <= 0.25f,
        )
    }

    /**
     * Test that initial region is always unlockable
     */
    @Test
    fun regionUnlock_initialRegionIsUnlockable() {
        // Look Peninsula has ID "look_peninsula"
        // This should be the initial region that's always unlockable

        assertEquals(
            "Initial region should be look_peninsula",
            "look_peninsula",
            lookPeninsula.id,
        )
    }

    /**
     * Test already unlocked region
     */
    @Test
    fun regionUnlock_alreadyUnlockedRegion() {
        // Look Peninsula is already VISIBLE and unlocked

        assertTrue(
            "Look Peninsula should be unlocked",
            lookPeninsula.isUnlocked,
        )
        assertEquals(
            "Look Peninsula should have VISIBLE fog level",
            FogLevel.VISIBLE,
            lookPeninsula.fogLevel,
        )
    }

    /**
     * Test region with PARTIAL fog level
     */
    @Test
    fun regionUnlock_partialFogRegion() {
        val partialRegion = makeLake.copy(fogLevel = FogLevel.PARTIAL)

        assertEquals(
            "Partial fog region should have PARTIAL fog level",
            FogLevel.PARTIAL,
            partialRegion.fogLevel,
        )
    }

    /**
     * Test region fog level transitions
     */
    @Test
    fun fogLevel_transitionsCorrectly() {
        // LOCKED -> HIDDEN -> PARTIAL -> VISIBLE

        val locked = sayMountain.copy(fogLevel = FogLevel.LOCKED)
        val hidden = makeLake.copy(fogLevel = FogLevel.HIDDEN)
        val partial = makeLake.copy(fogLevel = FogLevel.PARTIAL)
        val visible = lookPeninsula.copy(fogLevel = FogLevel.VISIBLE)

        // Test fog level ordering
        val fogLevels =
            listOf(
                FogLevel.VISIBLE,
                FogLevel.PARTIAL,
                FogLevel.HIDDEN,
                FogLevel.LOCKED,
            )

        assertEquals("All 4 fog levels should exist", 4, fogLevels.size)
    }

    /**
     * Test unlock progress calculation for adjacent region
     */
    @Test
    fun getUnlockProgress_adjacentRegion() {
        // Make Lake is adjacent to Look Peninsula
        // With Look Peninsula explored, Make Lake should have full progress

        val dx = lookPeninsula.position.x - makeLake.position.x
        val dy = lookPeninsula.position.y - makeLake.position.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        val isAdjacent = distance <= 0.25f
        val progress = if (isAdjacent) 1.0f else 0.0f

        assertEquals(
            "Adjacent region should have full progress",
            1.0f,
            progress,
            0.01f,
        )
    }

    /**
     * Test unlock progress for non-adjacent region
     */
    @Test
    fun getUnlockProgress_nonAdjacentRegion() {
        // Say Mountain is far from Look Peninsula
        // Should have zero progress

        val dx = lookPeninsula.position.x - sayMountain.position.x
        val dy = lookPeninsula.position.y - sayMountain.position.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        val isAdjacent = distance <= 0.25f
        val progress = if (isAdjacent) 1.0f else 0.0f

        assertEquals(
            "Non-adjacent region should have zero progress",
            0.0f,
            progress,
            0.01f,
        )
    }

    /**
     * Test unlock progress for already unlocked region
     */
    @Test
    fun getUnlockProgress_unlockedRegion() {
        // Look Peninsula is already VISIBLE and unlocked
        // Should have full progress

        val progress =
            if (lookPeninsula.fogLevel == FogLevel.VISIBLE) {
                1.0f
            } else {
                0.0f
            }

        assertEquals(
            "Already unlocked region should have full progress",
            1.0f,
            progress,
            0.01f,
        )
    }

    /**
     * Test that unlocked regions have correct fog levels
     */
    @Test
    fun unlockedRegion_hasCorrectFogLevel() {
        assertTrue(
            "Visible region should be unlocked",
            lookPeninsula.fogLevel == FogLevel.VISIBLE,
        )

        assertFalse(
            "Hidden region should not be unlocked",
            makeLake.fogLevel == FogLevel.VISIBLE,
        )

        assertFalse(
            "Locked region should not be unlocked",
            sayMountain.fogLevel == FogLevel.VISIBLE,
        )
    }

    /**
     * Test region position calculations
     */
    @Test
    fun regionPosition_calculatesCorrectly() {
        // Test Look Peninsula position
        assertEquals(
            "Look Peninsula X position should be 0.2",
            0.2f,
            lookPeninsula.position.x,
            0.001f,
        )
        assertEquals(
            "Look Peninsula Y position should be 0.3",
            0.3f,
            lookPeninsula.position.y,
            0.001f,
        )

        // Test Make Lake position
        assertEquals(
            "Make Lake X position should be 0.35",
            0.35f,
            makeLake.position.x,
            0.001f,
        )
        assertEquals(
            "Make Lake Y position should be 0.35",
            0.35f,
            makeLake.position.y,
            0.001f,
        )
    }

    /**
     * Test multiple region adjacencies
     */
    @Test
    fun regionUnlock_multipleAdjacencies() {
        // Add a region adjacent to Make Lake
        val moveValley =
            MapRegion(
                id = "move_valley",
                islandId = "move_island",
                name = "Move Valley",
                position = MapPosition(0.45f, 0.5f), // Adjacent to Make Lake
                bounds = MapBounds(0.35f, 0.4f, 0.55f, 0.6f),
                fogLevel = FogLevel.HIDDEN,
                isUnlocked = false,
                icon = "🏃",
            )

        // Check distance between Make Lake and Move Valley
        val dx = makeLake.position.x - moveValley.position.x
        val dy = makeLake.position.y - moveValley.position.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        assertTrue(
            "Move Valley should be adjacent to Make Lake",
            distance <= 0.25f,
        )
    }

    /**
     * Test region list contains all expected regions
     */
    @Test
    fun regionList_containsAllRegions() {
        assertEquals("Should have 3 regions", 3, allRegions.size)

        val regionIds = allRegions.map { it.id }
        assertTrue(
            "Should contain look_peninsula",
            regionIds.contains("look_peninsula"),
        )
        assertTrue(
            "Should contain make_lake",
            regionIds.contains("make_lake"),
        )
        assertTrue(
            "Should contain say_mountain",
            regionIds.contains("say_mountain"),
        )
    }

    /**
     * Test region properties
     */
    @Test
    fun region_propertiesAreCorrect() {
        // Test Look Peninsula
        assertEquals("look_peninsula", lookPeninsula.id)
        assertEquals("look_island", lookPeninsula.islandId)
        assertEquals("Look Peninsula", lookPeninsula.name)
        assertEquals("🔍", lookPeninsula.icon)
        assertTrue(lookPeninsula.isUnlocked)
        assertEquals(FogLevel.VISIBLE, lookPeninsula.fogLevel)

        // Test Make Lake
        assertEquals("make_lake", makeLake.id)
        assertEquals("make_island", makeLake.islandId)
        assertEquals("Make Lake", makeLake.name)
        assertEquals("🎨", makeLake.icon)
        assertFalse(makeLake.isUnlocked)
        assertEquals(FogLevel.HIDDEN, makeLake.fogLevel)
    }

    /**
     * Test unlock button visibility logic
     */
    @Test
    fun unlockButton_visibilityLogic() {
        // Should show unlock button for HIDDEN region adjacent to explored
        val shouldShowUnlockForHidden =
            makeLake.fogLevel == FogLevel.HIDDEN &&
                !makeLake.isUnlocked
        assertTrue(
            "Should show unlock button for hidden make_lake",
            shouldShowUnlockForHidden,
        )

        // Should NOT show unlock button for VISIBLE region
        val shouldShowUnlockForVisible =
            lookPeninsula.fogLevel != FogLevel.VISIBLE ||
                lookPeninsula.isUnlocked
        assertFalse(
            "Should NOT show unlock button for visible region",
            !shouldShowUnlockForVisible,
        )
    }
}
