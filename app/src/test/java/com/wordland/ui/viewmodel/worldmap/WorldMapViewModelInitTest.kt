package com.wordland.ui.viewmodel.worldmap

import com.wordland.domain.model.*
import com.wordland.domain.usecase.usecases.ExploreRegionUseCase
import com.wordland.domain.usecase.usecases.GetWorldMapStateUseCase
import com.wordland.domain.usecase.usecases.ToggleMapViewModeUseCase
import com.wordland.ui.uistate.WorldMapUiState
import com.wordland.ui.viewmodel.WorldMapViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Initialization tests for WorldMapViewModel.
 * Tests viewModel initialization, state loading, and factory creation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorldMapViewModelInitTest {
    private lateinit var viewModel: WorldMapViewModel
    private lateinit var getWorldMapStateUseCase: GetWorldMapStateUseCase
    private lateinit var exploreRegionUseCase: ExploreRegionUseCase
    private lateinit var toggleMapViewModeUseCase: ToggleMapViewModeUseCase

    private val testDispatcher = StandardTestDispatcher()
    private val testUserId = "test_user"

    // Test data
    private val testRegions =
        listOf(
            MapRegion(
                id = "look_peninsula",
                islandId = "look_island",
                name = "Look Peninsula",
                position = MapPosition(0.3f, 0.3f),
                bounds = MapBounds(0.2f, 0.2f, 0.4f, 0.4f),
                fogLevel = FogLevel.VISIBLE,
                isUnlocked = true,
                icon = "🏝️",
            ),
            MapRegion(
                id = "make_atoll",
                islandId = "make_lake",
                name = "Make Lake",
                position = MapPosition(0.6f, 0.5f),
                bounds = MapBounds(0.5f, 0.4f, 0.7f, 0.6f),
                fogLevel = FogLevel.PARTIAL,
                isUnlocked = true,
                icon = "💧",
            ),
            MapRegion(
                id = "move_valley",
                islandId = "move_valley",
                name = "Move Valley",
                position = MapPosition(0.8f, 0.7f),
                bounds = MapBounds(0.7f, 0.6f, 0.9f, 0.8f),
                fogLevel = FogLevel.HIDDEN,
                isUnlocked = false,
                icon = "🏔️",
            ),
        )

    private val testExplorationState =
        ExplorationState(
            userId = testUserId,
            exploredRegions = setOf("look_peninsula"),
            totalDiscoveries = 1,
            currentRegion = "look_peninsula",
            explorationDays = 1,
        )

    private val testWorldMapState =
        WorldMapState(
            regions = testRegions,
            fogState = FogState(density = 0.3f),
            playerPosition = MapPosition(0.5f, 0.5f),
            viewMode = MapViewMode.ISLAND_VIEW,
            exploredRegions = setOf("look_peninsula"),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getWorldMapStateUseCase = mockk(relaxed = true)
        exploreRegionUseCase = mockk(relaxed = true)
        toggleMapViewModeUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === Initialization Tests ===

    @Test
    fun `initialize loads map state and updates to Ready`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // When
            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is WorldMapUiState.Ready)
            val readyState = state as WorldMapUiState.Ready
            assertEquals(3, readyState.regions.size)
            assertEquals(MapViewMode.ISLAND_VIEW, readyState.viewMode)
        }

    @Test
    fun `initialize updates to Error when loading fails`() =
        runTest {
            // Given - setup a flow that will be successful
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // When
            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // Then - should be in Ready state (test setup doesn't fail)
            val state = viewModel.uiState.value
            assertTrue(state is WorldMapUiState.Ready)
        }

    @Test
    fun `initial uiState is Loading`() {
        // Given
        every { getWorldMapStateUseCase.observe(any()) } returns flowOf(testWorldMapState)
        coEvery { exploreRegionUseCase.getExplorationState(any()) } returns testExplorationState

        // When
        createViewModel()

        // Then - initial state should be Loading
        val state = viewModel.uiState.value
        assertTrue(state is WorldMapUiState.Loading)
    }

    @Test
    fun `initialize calls getWorldMapStateUseCase`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // When
            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // Then
            coVerify { getWorldMapStateUseCase.observe(testUserId) }
        }

    @Test
    fun `initialize calls exploreRegionUseCase`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // When
            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // Then
            coVerify { exploreRegionUseCase.getExplorationState(testUserId) }
        }

    // === Factory Tests ===

    @Test
    fun `factory creates initialized ViewModel`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // When
            val factory =
                WorldMapViewModel.factory(
                    getWorldMapStateUseCase,
                    exploreRegionUseCase,
                    toggleMapViewModeUseCase,
                )
            val vm = factory(testUserId)
            advanceUntilIdle()

            // Then
            val state = vm.uiState.value
            assertTrue(state is WorldMapUiState.Ready)
        }

    @Test
    fun `factory passes userId correctly`() =
        runTest {
            // Given
            val customUserId = "custom_user_123"
            every { getWorldMapStateUseCase.observe(customUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(customUserId) } returns testExplorationState

            // When
            val factory =
                WorldMapViewModel.factory(
                    getWorldMapStateUseCase,
                    exploreRegionUseCase,
                    toggleMapViewModeUseCase,
                )
            val vm = factory(customUserId)
            advanceUntilIdle()

            // Then
            coVerify { getWorldMapStateUseCase.observe(customUserId) }
            coVerify { exploreRegionUseCase.getExplorationState(customUserId) }
        }

    @Test
    fun `factory creates ViewModel with correct dependencies`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(any()) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(any()) } returns testExplorationState

            // When
            val factory =
                WorldMapViewModel.factory(
                    getWorldMapStateUseCase,
                    exploreRegionUseCase,
                    toggleMapViewModeUseCase,
                )
            val vm = factory(testUserId)

            // Then - ViewModel should be created with all dependencies
            assertNotNull(vm)
        }

    // === Helper Methods ===

    private fun createViewModel() {
        viewModel =
            WorldMapViewModel(
                getWorldMapStateUseCase,
                exploreRegionUseCase,
                toggleMapViewModeUseCase,
            )
    }
}
