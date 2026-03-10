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
 * View mode toggle tests for WorldMapViewModel.
 * Tests view mode switching between ISLAND_VIEW and WORLD_VIEW.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorldMapViewModelViewModeTest {
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

    // === View Mode Tests ===

    @Test
    fun `toggleViewMode switches from ISLAND_VIEW to WORLD_VIEW`() =
        runTest {
            // Given
            setupReadyState()
            coEvery { toggleMapViewModeUseCase(MapViewMode.ISLAND_VIEW) } returns MapViewMode.WORLD_VIEW

            // When
            viewModel.toggleViewMode()

            // Then
            val state = viewModel.uiState.value as WorldMapUiState.Ready
            assertEquals(MapViewMode.WORLD_VIEW, state.viewMode)
        }

    @Test
    fun `toggleViewMode switches from WORLD_VIEW to ISLAND_VIEW`() =
        runTest {
            // Given
            val worldViewState = testWorldMapState.copy(viewMode = MapViewMode.WORLD_VIEW)
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(worldViewState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            coEvery { toggleMapViewModeUseCase(MapViewMode.WORLD_VIEW) } returns MapViewMode.ISLAND_VIEW

            // When
            viewModel.toggleViewMode()

            // Then
            val state = viewModel.uiState.value as WorldMapUiState.Ready
            assertEquals(MapViewMode.ISLAND_VIEW, state.viewMode)
        }

    @Test
    fun `toggleViewMode calls useCase with current mode`() =
        runTest {
            // Given
            setupReadyState()
            coEvery { toggleMapViewModeUseCase(MapViewMode.ISLAND_VIEW) } returns MapViewMode.WORLD_VIEW

            // When
            viewModel.toggleViewMode()

            // Then
            coVerify { toggleMapViewModeUseCase(MapViewMode.ISLAND_VIEW) }
        }

    @Test
    fun `toggleViewMode updates state immediately`() =
        runTest {
            // Given
            setupReadyState()
            coEvery { toggleMapViewModeUseCase(MapViewMode.ISLAND_VIEW) } returns MapViewMode.WORLD_VIEW

            // When
            viewModel.toggleViewMode()

            // Then - state should be updated synchronously
            val state = viewModel.uiState.value as WorldMapUiState.Ready
            assertEquals(MapViewMode.WORLD_VIEW, state.viewMode)
        }

    @Test
    fun `toggleViewMode handles WORLD_VIEW to ISLAND_VIEW transition`() =
        runTest {
            // Given
            val worldViewState = testWorldMapState.copy(viewMode = MapViewMode.WORLD_VIEW)
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(worldViewState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            coEvery { toggleMapViewModeUseCase(MapViewMode.WORLD_VIEW) } returns MapViewMode.ISLAND_VIEW

            // When
            viewModel.toggleViewMode()

            // Then
            val state = viewModel.uiState.value as WorldMapUiState.Ready
            assertEquals(MapViewMode.ISLAND_VIEW, state.viewMode)
        }

    @Test
    fun `toggleViewMode preserves other state properties`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState
            coEvery { toggleMapViewModeUseCase(MapViewMode.ISLAND_VIEW) } returns MapViewMode.WORLD_VIEW

            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // When
            viewModel.toggleViewMode()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as WorldMapUiState.Ready
            assertEquals(2, state.regions.size) // Regions unchanged (testRegions has 2 elements)
            assertNotNull(state.explorationState) // Exploration state unchanged
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

    private fun setupReadyState() =
        runTest {
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // Reset mocks for subsequent tests
            clearMocks(toggleMapViewModeUseCase, exploreRegionUseCase, answers = false, recordedCalls = false)
        }
}
