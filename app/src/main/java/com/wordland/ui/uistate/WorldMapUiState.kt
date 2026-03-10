package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import com.wordland.domain.model.ExplorationState
import com.wordland.domain.model.MapRegion
import com.wordland.domain.model.MapViewMode

/**
 * UI State for World Map Screen
 */
sealed class WorldMapUiState {
    /**
     * Initial loading state
     */
    object Loading : WorldMapUiState()

    /**
     * Success state with map data
     */
    data class Ready(
        val regions: List<MapRegion>,
        val explorationState: ExplorationState?,
        val viewMode: MapViewMode,
        val selectedRegion: MapRegion? = null,
    ) : WorldMapUiState() {
        /**
         * Check if a region is visible based on exploration
         */
        fun isRegionVisible(regionId: String): Boolean {
            return explorationState?.hasExplored(regionId) == true ||
                regions.find { it.id == regionId }?.fogLevel == com.wordland.domain.model.FogLevel.VISIBLE
        }

        /**
         * Get exploration percentage
         */
        fun getExplorationPercentage(): Float {
            return explorationState?.getExplorationPercentage(regions.size) ?: 0f
        }
    }

    /**
     * Region selected state
     */
    data class RegionSelected(
        val region: MapRegion,
        val baseState: Ready,
    ) : WorldMapUiState()

    /**
     * Error state
     */
    data class Error(
        val message: String,
        val retry: (() -> Unit)? = null,
    ) : WorldMapUiState()
}

/**
 * Sub-state for map interaction
 */
@Immutable
data class MapInteractionState(
    val isDragging: Boolean = false,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
) {
    /**
     * Check if map is in transformed state
     */
    val isTransformed: Boolean
        get() = scale != 1f || offsetX != 0f || offsetY != 0f

    /**
     * Create reset state
     */
    fun reset(): MapInteractionState = MapInteractionState()

    /**
     * Apply drag offset
     */
    fun withDrag(
        dx: Float,
        dy: Float,
    ): MapInteractionState =
        copy(
            offsetX = offsetX + dx,
            offsetY = offsetY + dy,
        )

    /**
     * Apply scale
     */
    fun withScale(newScale: Float): MapInteractionState =
        copy(
            scale = newScale.coerceIn(0.5f, 3f),
        )
}
