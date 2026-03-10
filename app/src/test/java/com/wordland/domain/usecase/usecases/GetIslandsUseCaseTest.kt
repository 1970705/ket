package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.domain.model.IslandMastery
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetIslandsUseCase
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetIslandsUseCaseTest {
    private lateinit var getIslandsUseCase: GetIslandsUseCase
    private lateinit var islandRepository: IslandMasteryRepository

    private val testDispatcher = StandardTestDispatcher()

    private val testMasteryList =
        listOf(
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 18,
                masteredWords = 9,
                totalLevels = 3,
                completedLevels = 1,
                crossSceneScore = 0.5,
                masteryPercentage = 50.0,
                unlockedAt = 1000L,
                masteredAt = null,
            ),
            IslandMastery(
                islandId = "move_valley",
                userId = "user_001",
                totalWords = 60,
                masteredWords = 0,
                totalLevels = 10,
                completedLevels = 0,
                crossSceneScore = 0.0,
                masteryPercentage = 0.0,
                unlockedAt = null,
                masteredAt = null,
            ),
            IslandMastery(
                islandId = "make_lake",
                userId = "user_001",
                totalWords = 60,
                masteredWords = 30,
                totalLevels = 10,
                completedLevels = 5,
                crossSceneScore = 0.6,
                masteryPercentage = 60.0,
                unlockedAt = 2000L,
                masteredAt = null,
            ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        islandRepository = mockk()
        getIslandsUseCase = GetIslandsUseCase(islandRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke returns islands with correct structure`() =
        runTest {
            // Given
            coEvery { islandRepository.getAllIslandMastery("user_001") } returns testMasteryList

            // When
            val result = getIslandsUseCase("user_001").first()

            // Then
            assertEquals(3, result.size)
            assertEquals("look_island", result[0].islandId)
            assertEquals("Look Island", result[0].islandName)
            assertEquals(50.0, result[0].masteryPercentage, 0.01)
            assertTrue(result[0].isUnlocked)
            assertEquals(18, result[0].totalWords)
            assertEquals(9, result[0].masteredWords)
        }

    @Test
    fun `invoke maps island names correctly`() =
        runTest {
            // Given
            coEvery { islandRepository.getAllIslandMastery("user_001") } returns testMasteryList

            // When
            val result = getIslandsUseCase("user_001").first()

            // Then
            assertEquals("Look Island", result[0].islandName)
            assertEquals("Move Valley", result[1].islandName)
            assertEquals("Make Lake", result[2].islandName)
        }

    @Test
    fun `invoke sets unlocked status correctly`() =
        runTest {
            // Given
            coEvery { islandRepository.getAllIslandMastery("user_001") } returns testMasteryList

            // When
            val result = getIslandsUseCase("user_001").first()

            // Then
            assertTrue(result[0].isUnlocked) // has unlockedAt
            assertFalse(result[1].isUnlocked) // no unlockedAt
            assertTrue(result[2].isUnlocked) // has unlockedAt
        }

    @Test
    fun `invoke calculates mastery percentage correctly`() =
        runTest {
            // Given
            coEvery { islandRepository.getAllIslandMastery("user_001") } returns testMasteryList

            // When
            val result = getIslandsUseCase("user_001").first()

            // Then - masteryPercentage is calculated by IslandMastery
            assertTrue(result[0].masteryPercentage > 0)
            assertEquals(0.0, result[1].masteryPercentage, 0.01)
            assertTrue(result[2].masteryPercentage > 0)
        }

    @Test
    fun `invoke returns empty list when no islands`() =
        runTest {
            // Given
            coEvery { islandRepository.getAllIslandMastery("user_001") } returns emptyList()

            // When
            val result = getIslandsUseCase("user_001").first()

            // Then
            assertTrue(result.isEmpty())
        }

    @Test
    fun `extractIslandName handles known islands`() =
        runTest {
            // Given
            val knownIslands =
                listOf(
                    IslandMastery("look_island", "user", 10, 5, 1, 0, 0.5, masteryPercentage = 50.0, isNextIslandUnlocked = false, 1000L, null),
                    IslandMastery("move_valley", "user", 10, 5, 1, 0, 0.5, masteryPercentage = 50.0, isNextIslandUnlocked = false, 1000L, null),
                    IslandMastery("make_lake", "user", 10, 5, 1, 0, 0.5, masteryPercentage = 50.0, isNextIslandUnlocked = false, 1000L, null),
                    IslandMastery("say_mountain", "user", 10, 5, 1, 0, 0.5, masteryPercentage = 50.0, isNextIslandUnlocked = false, 1000L, null),
                    IslandMastery("think_forest", "user", 10, 5, 1, 0, 0.5, masteryPercentage = 50.0, isNextIslandUnlocked = false, 1000L, null),
                    IslandMastery("feel_garden", "user", 10, 5, 1, 0, 0.5, masteryPercentage = 50.0, isNextIslandUnlocked = false, 1000L, null),
                    IslandMastery("go_volcano", "user", 10, 5, 1, 0, 0.5, masteryPercentage = 50.0, isNextIslandUnlocked = false, 1000L, null),
                )

            coEvery { islandRepository.getAllIslandMastery("user_001") } returns knownIslands

            // When
            val result = getIslandsUseCase("user_001").first()

            // Then
            assertEquals("Look Island", result[0].islandName)
            assertEquals("Move Valley", result[1].islandName)
            assertEquals("Make Lake", result[2].islandName)
            assertEquals("Say Mountain", result[3].islandName)
            assertEquals("Think Forest", result[4].islandName)
            assertEquals("Feel Garden", result[5].islandName)
            assertEquals("Go Volcano", result[6].islandName)
        }

    @Test
    fun `extractIslandName formats unknown island IDs`() =
        runTest {
            // Given
            val unknownIsland =
                IslandMastery("unknown_island", "user", 10, 5, 1, 0, 0.5, masteryPercentage = 50.0, isNextIslandUnlocked = false, 1000L, null)
            coEvery { islandRepository.getAllIslandMastery("user_001") } returns listOf(unknownIsland)

            // When
            val result = getIslandsUseCase("user_001").first()

            // Then - should capitalize and replace underscores with spaces
            assertEquals("Unknown island", result[0].islandName)
        }
}
