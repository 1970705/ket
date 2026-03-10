package com.wordland.ui.viewmodel.worldmap

import com.wordland.domain.model.*
import com.wordland.domain.usecase.usecases.ExploreRegionResult
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
 * Unit tests for WorldMapViewModel - State Management
 * Tests selected region, exploration state, and factory methods
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorldMapViewModelStateTest {
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

    // === Selected Region Tests ===

    @Test
    fun `clearSelectedRegion clears the selected region`() =
        runTest {
            // Given
            setupReadyState()
            val exploreResult =
                ExploreRegionResult(
                    regionId = "look_peninsula",
                    isNewDiscovery = false,
                    totalDiscoveries = 1,
                )
            coEvery { exploreRegionUseCase(testUserId, "look_peninsula") } returns exploreResult
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // First select a region using islandId
            viewModel.onRegionTap(testUserId, "look_island")
            advanceUntilIdle()

            var state = viewModel.uiState.value as WorldMapUiState.Ready
            assertNotNull(state.selectedRegion)

            // When
            viewModel.clearSelectedRegion()

            // Then
            state = viewModel.uiState.value as WorldMapUiState.Ready
            assertNull(state.selectedRegion)
        }

    @Test
    fun `clearSelectedRegion does nothing when no region selected`() =
        runTest {
            // Given
            setupReadyState()

            // When - no region selected
            viewModel.clearSelectedRegion()

            // Then - should not crash
            val state = viewModel.uiState.value
            assertTrue(state is WorldMapUiState.Ready)
            assertNull((state as WorldMapUiState.Ready).selectedRegion)
        }

    // === Exploration State Tests ===

    @Test
    fun `Ready state contains correct exploration state`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // When
            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as WorldMapUiState.Ready
            assertNotNull(state.explorationState)
            assertEquals(1, state.explorationState?.totalDiscoveries)
            assertTrue(state.explorationState?.hasExplored("look_peninsula") == true)
        }

    @Test
    fun `isRegionVisible returns true for explored regions`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // When
            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as WorldMapUiState.Ready
            assertTrue(state.isRegionVisible("look_peninsula"))
        }

    @Test
    fun `getExplorationPercentage returns correct percentage`() =
        runTest {
            // Given
            every { getWorldMapStateUseCase.observe(testUserId) } returns flowOf(testWorldMapState)
            coEvery { exploreRegionUseCase.getExplorationState(testUserId) } returns testExplorationState

            // When
            createViewModel()
            viewModel.initialize(testUserId)
            advanceUntilIdle()

            // Then - 1 explored out of 3 total regions = 33.33%
            val state = viewModel.uiState.value as WorldMapUiState.Ready
            val percentage = state.getExplorationPercentage()
            assertEquals(1f / 3f, percentage, 0.01f)
        }

    // === MapInteractionState Tests ===

    @Test
    fun `MapInteractionState isTransformed returns true when transformed`() {
        // Given
        setupReadyState()

        // When - apply transformation
        viewModel.updateMapScale(1.5f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertTrue(state.isTransformed)
    }

    @Test
    fun `MapInteractionState isTransformed returns false when not transformed`() {
        // Given
        setupReadyState()

        // Then - default state has no transformation
        val state = viewModel.mapInteractionState.value
        assertFalse(state.isTransformed)
    }

    @Test
    fun `MapInteractionState isTransformed returns true when dragged`() {
        // Given
        setupReadyState()

        // When - apply drag
        viewModel.updateMapDrag(10f, 0f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertTrue(state.isTransformed)
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
