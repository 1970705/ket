package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for WorldMapState and related models
 */
class WorldMapStateTest {
    @Test
    fun `MapPosition fromPercent creates correct normalized coordinates`() {
        val position = MapPosition.fromPercent(50, 25)

        assertEquals(0.5f, position.x, 0.001f)
        assertEquals(0.25f, position.y, 0.001f)
    }

    @Test
    fun `MapPosition fromPercent throws for invalid xPercent`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                MapPosition.fromPercent(150, 50)
            }
        assertTrue(exception.message!!.contains("xPercent must be 0-100"))
    }

    @Test
    fun `MapPosition fromPercent throws for invalid yPercent`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                MapPosition.fromPercent(50, -1)
            }
        assertTrue(exception.message!!.contains("yPercent must be 0-100"))
    }

    @Test
    fun `MapBounds centeredAt creates correct bounds`() {
        val bounds = MapBounds.centeredAt(0.5f, 0.5f, 0.2f, 0.1f)

        assertEquals(0.4f, bounds.left, 0.001f)
        assertEquals(0.45f, bounds.top, 0.001f)
        assertEquals(0.6f, bounds.right, 0.001f)
        assertEquals(0.55f, bounds.bottom, 0.001f)
    }

    @Test
    fun `MapBounds contains returns true for point inside bounds`() {
        val bounds = MapBounds(0.2f, 0.3f, 0.8f, 0.7f)

        assertTrue(bounds.contains(0.5f, 0.5f))
        assertTrue(bounds.contains(0.2f, 0.5f))
        assertTrue(bounds.contains(0.8f, 0.5f))
        assertTrue(bounds.contains(0.5f, 0.3f))
        assertTrue(bounds.contains(0.5f, 0.7f))
    }

    @Test
    fun `MapBounds contains returns false for point outside bounds`() {
        val bounds = MapBounds(0.2f, 0.3f, 0.8f, 0.7f)

        assertFalse(bounds.contains(0.1f, 0.5f))
        assertFalse(bounds.contains(0.9f, 0.5f))
        assertFalse(bounds.contains(0.5f, 0.2f))
        assertFalse(bounds.contains(0.5f, 0.8f))
    }

    @Test
    fun `FogState calculateVisibilityRadius returns correct values`() {
        assertEquals(0.15f, FogState.calculateVisibilityRadius(1), 0.001f)
        assertEquals(0.15f, FogState.calculateVisibilityRadius(3), 0.001f)
        assertEquals(0.30f, FogState.calculateVisibilityRadius(4), 0.001f)
        assertEquals(0.30f, FogState.calculateVisibilityRadius(6), 0.001f)
        assertEquals(0.50f, FogState.calculateVisibilityRadius(7), 0.001f)
        assertEquals(0.50f, FogState.calculateVisibilityRadius(10), 0.001f)
    }

    @Test
    fun `FogState forPlayerLevel creates correct fog state`() {
        val fogState = FogState.forPlayerLevel(5)

        assertEquals(0.3f, fogState.density, 0.001f)
        assertTrue(fogState.animationEnabled)
        assertEquals(0.30f, fogState.visibilityRadius, 0.001f)
    }

    @Test
    fun `FogState isPositionVisible returns true for nearby position`() {
        val fogState = FogState(visibilityRadius = 0.2f)
        val playerPos = MapPosition(0.5f, 0.5f)
        val nearbyPos = MapPosition(0.55f, 0.5f)

        assertTrue(fogState.isPositionVisible(nearbyPos, playerPos))
    }

    @Test
    fun `FogState isPositionVisible returns false for distant position`() {
        val fogState = FogState(visibilityRadius = 0.1f)
        val playerPos = MapPosition(0.5f, 0.5f)
        val distantPos = MapPosition(0.8f, 0.5f)

        assertFalse(fogState.isPositionVisible(distantPos, playerPos))
    }

    @Test
    fun `ExplorationState hasExplored returns correct status`() {
        val state =
            ExplorationState(
                userId = "user1",
                exploredRegions = setOf("region1", "region2"),
                totalDiscoveries = 2,
                currentRegion = "region1",
            )

        assertTrue(state.hasExplored("region1"))
        assertTrue(state.hasExplored("region2"))
        assertFalse(state.hasExplored("region3"))
    }

    @Test
    fun `ExplorationState getExplorationPercentage returns correct value`() {
        val state =
            ExplorationState(
                userId = "user1",
                exploredRegions = setOf("region1", "region2", "region3"),
                totalDiscoveries = 3,
            )

        assertEquals(0.5f, state.getExplorationPercentage(6), 0.001f)
        assertEquals(1.0f, state.getExplorationPercentage(3), 0.001f)
    }

    @Test
    fun `ExplorationState getExplorationPercentage returns zero when_no_regions`() {
        val state =
            ExplorationState(
                userId = "user1",
                exploredRegions = setOf("region1", "region2"),
                totalDiscoveries = 2,
            )

        assertEquals(0f, state.getExplorationPercentage(0), 0.001f)
    }

    @Test
    fun `MapRegion contains returns true for point within bounds`() {
        val region =
            MapRegion(
                id = "test_region",
                islandId = "test_island",
                name = "Test Region",
                position = MapPosition(0.5f, 0.5f),
                bounds = MapBounds(0.3f, 0.3f, 0.7f, 0.7f),
                fogLevel = FogLevel.VISIBLE,
                isUnlocked = true,
                icon = "🏝️",
            )

        assertTrue(region.contains(0.5f, 0.5f))
        assertTrue(region.contains(0.3f, 0.5f))
        assertFalse(region.contains(0.2f, 0.5f))
    }

    @Test
    fun `WorldMapState isRegionVisible returns true for explored regions`() {
        val regions =
            listOf(
                MapRegion(
                    id = "region1",
                    islandId = "island1",
                    name = "Region 1",
                    position = MapPosition(0.5f, 0.5f),
                    bounds = MapBounds.centeredAt(0.5f, 0.5f, 0.2f, 0.2f),
                    fogLevel = FogLevel.HIDDEN,
                    isUnlocked = true,
                    icon = "🏝️",
                ),
            )

        val state =
            WorldMapState(
                regions = regions,
                fogState = FogState(),
                playerPosition = MapPosition(0.5f, 0.5f),
                viewMode = MapViewMode.WORLD_VIEW,
                exploredRegions = setOf("region1"),
            )

        assertTrue(state.isRegionVisible("region1"))
    }

    @Test
    fun `WorldMapState isRegionVisible returns true for VISIBLE fog level regions`() {
        val regions =
            listOf(
                MapRegion(
                    id = "region1",
                    islandId = "island1",
                    name = "Region 1",
                    position = MapPosition(0.5f, 0.5f),
                    bounds = MapBounds.centeredAt(0.5f, 0.5f, 0.2f, 0.2f),
                    fogLevel = FogLevel.VISIBLE,
                    isUnlocked = true,
                    icon = "🏝️",
                ),
            )

        val state =
            WorldMapState(
                regions = regions,
                fogState = FogState(),
                playerPosition = MapPosition(0.5f, 0.5f),
                viewMode = MapViewMode.WORLD_VIEW,
                exploredRegions = emptySet(),
            )

        assertTrue(state.isRegionVisible("region1"))
    }

    @Test
    fun `WorldMapState getRegionsByFogLevel filters correctly`() {
        val regions =
            listOf(
                MapRegion(
                    id = "region1",
                    islandId = "island1",
                    name = "Region 1",
                    position = MapPosition(0.5f, 0.5f),
                    bounds = MapBounds.centeredAt(0.5f, 0.5f, 0.2f, 0.2f),
                    fogLevel = FogLevel.VISIBLE,
                    isUnlocked = true,
                    icon = "🏝️",
                ),
                MapRegion(
                    id = "region2",
                    islandId = "island2",
                    name = "Region 2",
                    position = MapPosition(0.2f, 0.5f),
                    bounds = MapBounds.centeredAt(0.2f, 0.5f, 0.2f, 0.2f),
                    fogLevel = FogLevel.HIDDEN,
                    isUnlocked = false,
                    icon = "🏝️",
                ),
                MapRegion(
                    id = "region3",
                    islandId = "island3",
                    name = "Region 3",
                    position = MapPosition(0.8f, 0.5f),
                    bounds = MapBounds.centeredAt(0.8f, 0.5f, 0.2f, 0.2f),
                    fogLevel = FogLevel.VISIBLE,
                    isUnlocked = true,
                    icon = "🏝️",
                ),
            )

        val state =
            WorldMapState(
                regions = regions,
                fogState = FogState(),
                playerPosition = MapPosition(0.5f, 0.5f),
                viewMode = MapViewMode.WORLD_VIEW,
                exploredRegions = emptySet(),
            )

        val visibleRegions = state.getRegionsByFogLevel(FogLevel.VISIBLE)
        assertEquals(2, visibleRegions.size)
        assertTrue(visibleRegions.all { it.fogLevel == FogLevel.VISIBLE })
    }
}
