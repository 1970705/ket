package com.wordland.data.repository

import com.wordland.data.dao.WorldMapDao
import com.wordland.domain.model.ExplorationState
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for WorldMapRepository
 */
class WorldMapRepositoryTest {
    private lateinit var mockDao: WorldMapDao
    private lateinit var repository: WorldMapRepositoryImpl

    @Before
    fun setup() {
        mockDao = mockk(relaxed = true)
        repository = WorldMapRepositoryImpl(mockDao)
    }

    @Test
    fun `getExplorationState returns null when no exploration data exists`() =
        runTest {
            // Given
            val userId = "new_user"
            coEvery { mockDao.getExplorationState(userId) } returns null

            // When
            val result = repository.getExplorationState(userId)

            // Then
            assertNull(result)
        }

    @Test
    fun `getExplorationState returns mapped domain state`() =
        runTest {
            // Given
            val userId = "user1"
            val entity =
                com.wordland.data.dao.WorldMapExplorationEntity(
                    userId = userId,
                    exploredRegions = "[\"region1\"]",
                    totalDiscoveries = 1,
                    currentRegion = "region1",
                    explorationDays = 1,
                )
            coEvery { mockDao.getExplorationState(userId) } returns entity

            // When
            val result = repository.getExplorationState(userId)

            // Then
            assertNotNull(result)
            assertEquals(userId, result!!.userId)
            assertEquals(1, result.exploredRegions.size)
            assertTrue(result.hasExplored("region1"))
        }

    @Test
    fun `getMapRegions returns predefined regions`() =
        runTest {
            // When
            val regions = repository.getMapRegions()

            // Then
            assertFalse(regions.isEmpty())
            assertTrue(regions.any { it.id == "look_peninsula" })
            assertTrue(regions.any { it.id == "make_atoll" })
            assertTrue(regions.any { it.id == "listen_cove" })
        }

    @Test
    fun `getRegion returns correct region by id`() =
        runTest {
            // When
            val region = repository.getRegion("look_peninsula")

            // Then
            assertNotNull(region)
            assertEquals("look_peninsula", region!!.id)
            assertEquals("look_island", region.islandId)
        }

    @Test
    fun `getRegion returns null for unknown id`() =
        runTest {
            // When
            val region = repository.getRegion("unknown_region")

            // Then
            assertNull(region)
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
}
