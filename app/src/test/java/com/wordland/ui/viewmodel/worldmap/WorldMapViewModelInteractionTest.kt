package com.wordland.ui.viewmodel.worldmap

import com.wordland.domain.model.*
import com.wordland.domain.usecase.usecases.ExploreRegionUseCase
import com.wordland.domain.usecase.usecases.GetWorldMapStateUseCase
import com.wordland.domain.usecase.usecases.ToggleMapViewModeUseCase
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
 * Map interaction tests for WorldMapViewModel.
 * Tests map dragging, scaling, and interaction state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorldMapViewModelInteractionTest {
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

    // === Map Interaction Tests ===

    @Test
    fun `resetMapInteraction resets interaction state`() {
        // Given
        setupReadyState()
        viewModel.updateMapDrag(10f, 20f)
        viewModel.updateMapScale(1.5f)

        // When
        viewModel.resetMapInteraction()

        // Then
        val interactionState = viewModel.mapInteractionState.value
        assertEquals(1f, interactionState.scale, 0.01f)
        assertEquals(0f, interactionState.offsetX, 0.01f)
        assertEquals(0f, interactionState.offsetY, 0.01f)
        assertFalse(interactionState.isDragging)
    }

    @Test
    fun `updateMapDrag updates drag offset`() {
        // Given
        setupReadyState()

        // When
        viewModel.updateMapDrag(10f, 20f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertEquals(10f, state.offsetX, 0.01f)
        assertEquals(20f, state.offsetY, 0.01f)
    }

    @Test
    fun `updateMapDrag accumulates offset`() {
        // Given
        setupReadyState()

        // When - drag twice
        viewModel.updateMapDrag(10f, 20f)
        viewModel.updateMapDrag(5f, 10f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertEquals(15f, state.offsetX, 0.01f)
        assertEquals(30f, state.offsetY, 0.01f)
    }

    @Test
    fun `updateMapScale updates scale with coercion`() {
        // Given
        setupReadyState()

        // When
        viewModel.updateMapScale(2.5f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertEquals(2.5f, state.scale, 0.01f)
    }

    @Test
    fun `updateMapScale coerces to minimum scale`() {
        // Given
        setupReadyState()

        // When - try to set below minimum
        viewModel.updateMapScale(0.1f)

        // Then - should be coerced to 0.5f minimum
        val state = viewModel.mapInteractionState.value
        assertEquals(0.5f, state.scale, 0.01f)
    }

    @Test
    fun `updateMapScale coerces to maximum scale`() {
        // Given
        setupReadyState()

        // When - try to set above maximum
        viewModel.updateMapScale(5.0f)

        // Then - should be coerced to 3.0f maximum
        val state = viewModel.mapInteractionState.value
        assertEquals(3.0f, state.scale, 0.01f)
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

    @Test
    fun `MapInteractionState isTransformed returns true when scaled and dragged`() {
        // Given
        setupReadyState()

        // When - apply both scale and drag
        viewModel.updateMapScale(1.5f)
        viewModel.updateMapDrag(10f, 10f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertTrue(state.isTransformed)
    }

    @Test
    fun `MapInteractionState isTransformed returns false after reset`() {
        // Given
        setupReadyState()
        viewModel.updateMapScale(1.5f)

        // When
        viewModel.resetMapInteraction()

        // Then
        val state = viewModel.mapInteractionState.value
        assertFalse(state.isTransformed)
    }

    // === Scale Boundary Tests ===

    @Test
    fun `scale at minimum boundary is not coerced`() {
        // Given
        setupReadyState()

        // When - exactly at minimum
        viewModel.updateMapScale(0.5f)

        // Then - should not be coerced
        val state = viewModel.mapInteractionState.value
        assertEquals(0.5f, state.scale, 0.01f)
    }

    @Test
    fun `scale at maximum boundary is not coerced`() {
        // Given
        setupReadyState()

        // When - exactly at maximum
        viewModel.updateMapScale(3.0f)

        // Then - should not be coerced
        val state = viewModel.mapInteractionState.value
        assertEquals(3.0f, state.scale, 0.01f)
    }

    @Test
    fun `scale below minimum is coerced to minimum`() {
        // Given
        setupReadyState()

        // When - well below minimum
        viewModel.updateMapScale(0.01f)

        // Then - should be coerced to minimum
        val state = viewModel.mapInteractionState.value
        assertEquals(0.5f, state.scale, 0.01f)
    }

    @Test
    fun `scale above maximum is coerced to maximum`() {
        // Given
        setupReadyState()

        // When - well above maximum
        viewModel.updateMapScale(10.0f)

        // Then - should be coerced to maximum
        val state = viewModel.mapInteractionState.value
        assertEquals(3.0f, state.scale, 0.01f)
    }

    // === Drag Tests ===

    @Test
    fun `drag with positive values updates offset correctly`() {
        // Given
        setupReadyState()

        // When
        viewModel.updateMapDrag(100f, 50f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertEquals(100f, state.offsetX, 0.01f)
        assertEquals(50f, state.offsetY, 0.01f)
    }

    @Test
    fun `drag with negative values updates offset correctly`() {
        // Given
        setupReadyState()

        // When
        viewModel.updateMapDrag(-50f, -100f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertEquals(-50f, state.offsetX, 0.01f)
        assertEquals(-100f, state.offsetY, 0.01f)
    }

    @Test
    fun `drag with zero values updates offset correctly`() {
        // Given
        setupReadyState()
        viewModel.updateMapScale(2.0f)

        // When
        viewModel.updateMapDrag(0f, 0f)

        // Then
        val state = viewModel.mapInteractionState.value
        assertEquals(0f, state.offsetX, 0.01f)
        assertEquals(0f, state.offsetY, 0.01f)
        // But scale should still be transformed
        assertEquals(2.0f, state.scale, 0.01f)
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
