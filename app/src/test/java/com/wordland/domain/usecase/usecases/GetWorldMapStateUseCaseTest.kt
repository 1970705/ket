package com.wordland.domain.usecase.usecases

import com.wordland.data.dao.WorldMapDao
import com.wordland.data.dao.WorldMapExplorationEntity
import com.wordland.data.repository.WorldMapRepositoryImpl
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetWorldMapStateUseCase
 */
class GetWorldMapStateUseCaseTest {
    private lateinit var mockDao: WorldMapDao
    private lateinit var repository: WorldMapRepositoryImpl
    private lateinit var useCase: GetWorldMapStateUseCase

    @Before
    fun setup() {
        mockDao = mockk(relaxed = true)
        repository = WorldMapRepositoryImpl(mockDao)
        useCase = GetWorldMapStateUseCase(repository)
    }

    @Test
    fun `invoke returns WorldMapState with correct data`() =
        runTest {
            // Given
            val userId = "user1"
            val explorationEntity =
                WorldMapExplorationEntity(
                    userId = userId,
                    exploredRegions = "[\"region1\"]",
                    totalDiscoveries = 1,
                    currentRegion = "region1",
                    explorationDays = 1,
                )
            coEvery { mockDao.getExplorationState(userId) } returns explorationEntity

            // When
            val result = useCase(userId)

            // Then
            assertNotNull(result)
            assertFalse(result.regions.isEmpty())
            assertEquals(setOf("region1"), result.exploredRegions)
        }

    @Test
    fun `invoke returns empty explored regions when user has no exploration state`() =
        runTest {
            // Given
            val userId = "new_user"
            coEvery { mockDao.getExplorationState(userId) } returns null

            // When
            val result = useCase(userId)

            // Then
            assertTrue(result.exploredRegions.isEmpty())
            assertEquals(0.15f, result.fogState.visibilityRadius, 0.001f)
        }

    @Test
    fun `observe returns flow that emits WorldMapState`() =
        runTest {
            // Given
            val userId = "user1"
            // Mock the DAO Flow to return the entity, then the repository will map it
            coEvery { mockDao.getExplorationStateFlow(userId) } returns
                flowOf(
                    WorldMapExplorationEntity(
                        userId = userId,
                        exploredRegions = "[\"region1\", \"region2\"]",
                        totalDiscoveries = 2,
                        currentRegion = "region1",
                        explorationDays = 3,
                    ),
                )

            // When
            val result = useCase.observe(userId).first()

            // Then
            assertNotNull(result)
            assertEquals(2, result.exploredRegions.size)
            assertTrue(result.exploredRegions.contains("region1"))
            assertTrue(result.exploredRegions.contains("region2"))
        }
}
