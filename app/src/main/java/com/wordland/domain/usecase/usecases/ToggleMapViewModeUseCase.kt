package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.MapViewMode

/**
 * Use case for toggling between island view and world view
 * This is a simple state toggle that could be persisted in future
 */
class ToggleMapViewModeUseCase {
    /**
     * Toggle to the opposite view mode
     */
    operator fun invoke(currentMode: MapViewMode): MapViewMode {
        return when (currentMode) {
            MapViewMode.ISLAND_VIEW -> MapViewMode.WORLD_VIEW
            MapViewMode.WORLD_VIEW -> MapViewMode.ISLAND_VIEW
        }
    }
}
