package com.wordland.data.dao

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for WorldMapExplorationEntity
 */
class WorldMapDaoTest {
    @Test
    fun `WorldMapExplorationEntity toDomain returns correct ExplorationState`() {
        // Given
        val entity =
            WorldMapExplorationEntity(
                userId = "user1",
                exploredRegions = "[\"region1\", \"region2\", \"region3\"]",
                totalDiscoveries = 3,
                currentRegion = "region1",
                explorationDays = 5,
            )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals("user1", domain.userId)
        assertEquals(3, domain.exploredRegions.size)
        assertTrue(domain.exploredRegions.contains("region1"))
        assertTrue(domain.exploredRegions.contains("region2"))
        assertTrue(domain.exploredRegions.contains("region3"))
        assertEquals(3, domain.totalDiscoveries)
        assertEquals("region1", domain.currentRegion)
        assertEquals(5, domain.explorationDays)
    }

    @Test
    fun `WorldMapExplorationEntity toDomain handles empty explored regions`() {
        // Given
        val entity =
            WorldMapExplorationEntity(
                userId = "user1",
                exploredRegions = "[]",
                totalDiscoveries = 0,
                currentRegion = null,
                explorationDays = 0,
            )

        // When
        val domain = entity.toDomain()

        // Then
        assertTrue(domain.exploredRegions.isEmpty())
        assertEquals(0, domain.totalDiscoveries)
        assertNull(domain.currentRegion)
        assertEquals(0, domain.explorationDays)
    }

    @Test
    fun `WorldMapExplorationEntity fromDomain creates correct entity`() {
        // Given
        val domain =
            com.wordland.domain.model.ExplorationState(
                userId = "user1",
                exploredRegions = setOf("region1", "region2"),
                totalDiscoveries = 2,
                currentRegion = "region1",
                explorationDays = 3,
            )

        // When
        val entity = WorldMapExplorationEntity.fromDomain(domain)

        // Then
        assertEquals("user1", entity.userId)
        assertTrue(entity.exploredRegions.contains("\"region1\""))
        assertTrue(entity.exploredRegions.contains("\"region2\""))
        assertEquals(2, entity.totalDiscoveries)
        assertEquals("region1", entity.currentRegion)
        assertEquals(3, entity.explorationDays)
    }

    @Test
    fun `WorldMapExplorationEntity fromDomain handles empty regions`() {
        // Given
        val domain =
            com.wordland.domain.model.ExplorationState(
                userId = "user1",
                exploredRegions = emptySet(),
                totalDiscoveries = 0,
                currentRegion = null,
                explorationDays = 0,
            )

        // When
        val entity = WorldMapExplorationEntity.fromDomain(domain)

        // Then
        assertEquals("user1", entity.userId)
        assertEquals("[]", entity.exploredRegions)
        assertEquals(0, entity.totalDiscoveries)
        assertNull(entity.currentRegion)
        assertEquals(0, entity.explorationDays)
    }

    @Test
    fun `WorldMapExplorationEntity round-trip conversion preserves data`() {
        // Given
        val original =
            com.wordland.domain.model.ExplorationState(
                userId = "user1",
                exploredRegions = setOf("region1", "region2", "region3"),
                totalDiscoveries = 3,
                currentRegion = "region2",
                explorationDays = 5,
            )

        // When
        val entity = WorldMapExplorationEntity.fromDomain(original)
        val converted = entity.toDomain()

        // Then
        assertEquals(original.userId, converted.userId)
        assertEquals(original.exploredRegions, converted.exploredRegions)
        assertEquals(original.totalDiscoveries, converted.totalDiscoveries)
        assertEquals(original.currentRegion, converted.currentRegion)
        assertEquals(original.explorationDays, converted.explorationDays)
    }
}
