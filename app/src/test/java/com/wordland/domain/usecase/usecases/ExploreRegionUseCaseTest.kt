package com.wordland.domain.usecase.usecases

import com.wordland.data.dao.WorldMapDao
import com.wordland.data.dao.WorldMapExplorationEntity
import com.wordland.data.repository.WorldMapRepositoryImpl
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ExploreRegionUseCase
 */
class ExploreRegionUseCaseTest {
    private lateinit var mockDao: WorldMapDao
    private lateinit var repository: WorldMapRepositoryImpl
    private lateinit var useCase: ExploreRegionUseCase

    @Before
    fun setup() {
        mockDao = mockk(relaxed = true)
        repository = WorldMapRepositoryImpl(mockDao)
        useCase = ExploreRegionUseCase(repository)
    }

    @Test
    fun `invoke marks new region as explored`() =
        runTest {
            // Given
            val userId = "user1"
            val regionId = "region1"
            coEvery { mockDao.getExplorationState(userId) } returns null

            // When
            val result = useCase(userId, regionId)

            // Then
            assertTrue(result.isNewDiscovery)
            assertEquals(1, result.totalDiscoveries)
        }

    @Test
    fun `invoke handles already explored region`() =
        runTest {
            // Given
            val userId = "user1"
            val regionId = "region1"
            val existingState =
                WorldMapExplorationEntity(
                    userId = userId,
                    exploredRegions = "[\"region1\"]",
                    totalDiscoveries = 1,
                    currentRegion = "region1",
                    explorationDays = 1,
                )
            coEvery { mockDao.getExplorationState(userId) } returns existingState

            // When
            val result = useCase(userId, regionId)

            // Then
            assertFalse(result.isNewDiscovery)
            assertEquals(1, result.totalDiscoveries)
        }

    @Test
    fun `invoke increments discoveries for new region`() =
        runTest {
            // Given
            val userId = "user1"
            val regionId = "region2"
            val existingState =
                WorldMapExplorationEntity(
                    userId = userId,
                    exploredRegions = "[\"region1\"]",
                    totalDiscoveries = 1,
                    currentRegion = "region1",
                    explorationDays = 1,
                )
            coEvery { mockDao.getExplorationState(userId) } returns existingState

            // When
            val result = useCase(userId, regionId)

            // Then
            assertTrue(result.isNewDiscovery)
            assertEquals(2, result.totalDiscoveries)
        }

    @Test
    fun `getExplorationState returns null for new user`() =
        runTest {
            // Given
            val userId = "new_user"
            coEvery { mockDao.getExplorationState(userId) } returns null

            // When
            val result = useCase.getExplorationState(userId)

            // Then
            assertNull(result)
        }

    @Test
    fun `getExplorationState returns state for existing user`() =
        runTest {
            // Given
            val userId = "user1"
            val entity =
                WorldMapExplorationEntity(
                    userId = userId,
                    exploredRegions = "[\"region1\", \"region2\"]",
                    totalDiscoveries = 2,
                    currentRegion = "region1",
                    explorationDays = 1,
                )
            coEvery { mockDao.getExplorationState(userId) } returns entity

            // When
            val result = useCase.getExplorationState(userId)

            // Then
            assertNotNull(result)
            assertEquals(userId, result!!.userId)
            assertEquals(2, result.exploredRegions.size)
            assertTrue(result.exploredRegions.contains("region1"))
            assertTrue(result.exploredRegions.contains("region2"))
        }
}
