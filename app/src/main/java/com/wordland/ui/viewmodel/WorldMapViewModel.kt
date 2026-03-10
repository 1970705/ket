package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.model.MapViewMode
import com.wordland.domain.usecase.usecases.ExploreRegionUseCase
import com.wordland.domain.usecase.usecases.GetWorldMapStateUseCase
import com.wordland.domain.usecase.usecases.ToggleMapViewModeUseCase
import com.wordland.ui.uistate.MapInteractionState
import com.wordland.ui.uistate.WorldMapUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * View transition state for animated view mode switching
 *
 * @param isTransitioning Whether a transition animation is currently in progress
 * @param progress Animation progress (0f = fully hidden, 1f = fully visible)
 * @param fromMode The view mode we're transitioning from
 * @param toMode The view mode we're transitioning to
 */
data class ViewTransitionState(
    val isTransitioning: Boolean = false,
    val progress: Float = 0f,
    val fromMode: MapViewMode? = null,
    val toMode: MapViewMode? = null,
)

/**
 * ViewModel for World Map Screen
 * Handles map state, exploration tracking, and view mode toggling
 */
class WorldMapViewModel(
    private val getWorldMapStateUseCase: GetWorldMapStateUseCase,
    private val exploreRegionUseCase: ExploreRegionUseCase,
    private val toggleMapViewModeUseCase: ToggleMapViewModeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorldMapUiState>(WorldMapUiState.Loading)
    val uiState: StateFlow<WorldMapUiState> = _uiState.asStateFlow()

    private val _mapInteractionState = MutableStateFlow(MapInteractionState())
    val mapInteractionState: StateFlow<MapInteractionState> = _mapInteractionState.asStateFlow()

    // View transition state for animated mode switching
    private val _viewTransitionState = MutableStateFlow<ViewTransitionState>(ViewTransitionState())
    val viewTransitionState: StateFlow<ViewTransitionState> = _viewTransitionState.asStateFlow()

    private var currentViewMode = MapViewMode.ISLAND_VIEW

    /**
     * Initialize the world map
     */
    fun initialize(userId: String) {
        viewModelScope.launch {
            getWorldMapStateUseCase.observe(userId)
                .catch { error ->
                    _uiState.value =
                        WorldMapUiState.Error(
                            message = error.message ?: "Failed to load map",
                            retry = { initialize(userId) },
                        )
                }
                .collect { worldMapState ->
                    _uiState.value =
                        WorldMapUiState.Ready(
                            regions = worldMapState.regions,
                            explorationState = exploreRegionUseCase.getExplorationState(userId),
                            viewMode = worldMapState.viewMode,
                        )
                    currentViewMode = worldMapState.viewMode
                }
        }
    }

    /**
     * Toggle between island view and world view
     * Legacy method - simple toggle without animation
     */
    fun toggleViewMode() {
        val newMode = toggleMapViewModeUseCase(currentViewMode)
        currentViewMode = newMode

        val currentState = _uiState.value
        if (currentState is WorldMapUiState.Ready) {
            _uiState.value = currentState.copy(viewMode = newMode)
        } else if (currentState is WorldMapUiState.RegionSelected) {
            _uiState.value = currentState.baseState.copy(viewMode = newMode)
        }
    }

    /**
     * Toggle view mode with smooth transition animation
     * Animates for 500ms with crossfade and slide effects
     */
    fun toggleViewModeWithAnimation() {
        val currentState = _uiState.value
        if (currentState !is WorldMapUiState.Ready) return

        val newMode = toggleMapViewModeUseCase(currentViewMode)
        val fromMode = currentViewMode

        // Start transition animation
        viewModelScope.launch {
            _viewTransitionState.value =
                ViewTransitionState(
                    isTransitioning = true,
                    progress = 0f,
                    fromMode = fromMode,
                    toMode = newMode,
                )

            // Animation duration 500ms
            delay(500)

            _viewTransitionState.value =
                ViewTransitionState(
                    isTransitioning = false,
                    progress = 1f,
                    fromMode = null,
                    toMode = null,
                )
        }

        // Update view mode immediately for UI state
        currentViewMode = newMode
        _uiState.value = currentState.copy(viewMode = newMode)
    }

    /**
     * Handle region tap/click
     * Accepts islandId and finds the corresponding region
     */
    fun onRegionTap(
        userId: String,
        islandId: String,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is WorldMapUiState.Ready) {
                // Find region by islandId (not region.id)
                val region = currentState.regions.find { it.islandId == islandId }
                if (region != null && region.isUnlocked) {
                    // Mark region as explored using region.id
                    exploreRegionUseCase(userId, region.id)

                    // Update state with new exploration
                    val updatedExploration = exploreRegionUseCase.getExplorationState(userId)
                    _uiState.value =
                        currentState.copy(
                            explorationState = updatedExploration,
                            selectedRegion = region,
                        )
                }
            }
        }
    }

    /**
     * Reset map interaction state (zoom/pan)
     */
    fun resetMapInteraction() {
        _mapInteractionState.value = MapInteractionState()
    }

    /**
     * Update map drag offset
     */
    fun updateMapDrag(
        dx: Float,
        dy: Float,
    ) {
        _mapInteractionState.value = _mapInteractionState.value.withDrag(dx, dy)
    }

    /**
     * Update map scale
     */
    fun updateMapScale(scale: Float) {
        _mapInteractionState.value = _mapInteractionState.value.withScale(scale)
    }

    /**
     * Clear selected region
     */
    fun clearSelectedRegion() {
        val currentState = _uiState.value
        if (currentState is WorldMapUiState.Ready && currentState.selectedRegion != null) {
            _uiState.value = currentState.copy(selectedRegion = null)
        }
    }

    companion object {
        /**
         * Factory for creating WorldMapViewModel with default use cases
         * This is used by AppServiceLocator
         */
        fun factory(
            getWorldMapStateUseCase: GetWorldMapStateUseCase,
            exploreRegionUseCase: ExploreRegionUseCase,
            toggleMapViewModeUseCase: ToggleMapViewModeUseCase,
        ): (userId: String) -> WorldMapViewModel =
            { userId ->
                WorldMapViewModel(
                    getWorldMapStateUseCase,
                    exploreRegionUseCase,
                    toggleMapViewModeUseCase,
                ).apply { initialize(userId) }
            }
    }
}
