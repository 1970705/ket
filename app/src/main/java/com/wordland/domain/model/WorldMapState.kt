package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * World map state model
 * Represents the complete state of the world map including fog, regions, and player position
 *
 * @param regions List of all map regions
 * @param fogState Current fog configuration
 * @param playerPosition Current player position on the map
 * @param viewMode Current view mode (island list or world map)
 * @param exploredRegions Set of region IDs that have been explored
 */
@Immutable
data class WorldMapState(
    val regions: List<MapRegion>,
    val fogState: FogState,
    val playerPosition: MapPosition,
    val viewMode: MapViewMode,
    val exploredRegions: Set<String> = emptySet(),
) {
    /**
     * Check if a specific region is visible based on fog state
     */
    fun isRegionVisible(regionId: String): Boolean {
        val region = regions.find { it.id == regionId } ?: return false
        return when {
            exploredRegions.contains(regionId) -> true
            region.isUnlocked -> true
            region.fogLevel == FogLevel.VISIBLE -> true
            else -> false
        }
    }

    /**
     * Get regions grouped by fog level
     */
    fun getRegionsByFogLevel(fogLevel: FogLevel): List<MapRegion> {
        return regions.filter { it.fogLevel == fogLevel }
    }
}

/**
 * Map region representation
 * Each region corresponds to an island and has position, bounds, and fog state
 *
 * @param id Unique region identifier (e.g., "look_peninsula")
 * @param islandId Corresponding island ID (e.g., "look_island")
 * @param name Display name for the region
 * @param position Normalized position (0-1) on the map
 * @param bounds Touch-able area for the region
 * @param fogLevel Current fog level for this region
 * @param isUnlocked Whether the region is accessible to the player
 * @param icon Resource name or emoji for region icon
 */
@Immutable
data class MapRegion(
    val id: String,
    val islandId: String,
    val name: String,
    val position: MapPosition,
    val bounds: MapBounds,
    val fogLevel: FogLevel,
    val isUnlocked: Boolean,
    val icon: String,
) {
    /**
     * Check if a point is within this region's bounds
     */
    fun contains(
        x: Float,
        y: Float,
    ): Boolean {
        return bounds.contains(x, y)
    }
}

/**
 * Normalized map position
 * Coordinates are 0-1 relative to map bounds for screen-size independence
 *
 * @param x Horizontal position (0.0 = left, 1.0 = right)
 * @param y Vertical position (0.0 = top, 1.0 = bottom)
 * @param anchor How to position the region at its coordinates
 */
@Immutable
data class MapPosition(
    val x: Float,
    val y: Float,
    val anchor: PositionAnchor = PositionAnchor.CENTER,
) {
    companion object {
        /**
         * Create a position using percentage values (0-100)
         */
        fun fromPercent(
            xPercent: Int,
            yPercent: Int,
        ): MapPosition {
            require(xPercent in 0..100) { "xPercent must be 0-100" }
            require(yPercent in 0..100) { "yPercent must be 0-100" }
            return MapPosition(
                x = xPercent / 100f,
                y = yPercent / 100f,
            )
        }
    }
}

/**
 * Position anchor for map elements
 * Defines how the element is positioned relative to its coordinates
 */
enum class PositionAnchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
}

/**
 * Touch-able bounds for a map region
 * All coordinates are normalized (0-1)
 *
 * @param left Left boundary
 * @param top Top boundary
 * @param right Right boundary
 * @param bottom Bottom boundary
 */
@Immutable
data class MapBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    /**
     * Check if a normalized point is within bounds
     */
    fun contains(
        x: Float,
        y: Float,
    ): Boolean {
        return x >= left && x <= right && y >= top && y <= bottom
    }

    /**
     * Create bounds centered at a position with given size
     */
    companion object {
        fun centeredAt(
            centerX: Float,
            centerY: Float,
            width: Float,
            height: Float,
        ): MapBounds {
            return MapBounds(
                left = centerX - width / 2,
                top = centerY - height / 2,
                right = centerX + width / 2,
                bottom = centerY + height / 2,
            )
        }
    }
}

/**
 * Fog level for map regions
 * Determines how much of the region is visible to the player
 */
enum class FogLevel {
    /**
     * Fully visible - no fog
     */
    VISIBLE,

    /**
     * Partially visible - outline shown, details obscured
     */
    PARTIAL,

    /**
     * Hidden - under dense fog, only silhouette
     */
    HIDDEN,

    /**
     * Locked - not accessible, shown as locked icon
     */
    LOCKED,
}

/**
 * Global fog state configuration
 *
 * @param density Overall fog density (0.0 = clear, 1.0 = dense)
 * @param animationEnabled Whether fog animations are enabled
 * @param visibilityRadius Radius around player that's always visible (normalized 0-1)
 */
@Immutable
data class FogState(
    val density: Float = 0.0f,
    val animationEnabled: Boolean = true,
    val visibilityRadius: Float = 0.15f,
) {
    /**
     * Calculate if a position is visible based on distance to player
     */
    fun isPositionVisible(
        position: MapPosition,
        playerPosition: MapPosition,
    ): Boolean {
        val dx = position.x - playerPosition.x
        val dy = position.y - playerPosition.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance <= visibilityRadius
    }

    companion object {
        /**
         * Calculate visibility radius based on player level
         */
        fun calculateVisibilityRadius(playerLevel: Int): Float {
            return when {
                playerLevel <= 3 -> 0.15f // 15% of map
                playerLevel <= 6 -> 0.30f // 30% of map
                else -> 0.50f // 50% of map
            }
        }

        /**
         * Create fog state for player level
         */
        fun forPlayerLevel(playerLevel: Int): FogState {
            return FogState(
                density = 0.3f,
                animationEnabled = playerLevel > 3,
                visibilityRadius = calculateVisibilityRadius(playerLevel),
            )
        }
    }
}

/**
 * Map view mode
 * Determines whether to show the traditional island list or the world map
 */
enum class MapViewMode {
    /**
     * Traditional island list view (LazyColumn of cards)
     */
    ISLAND_VIEW,

    /**
     * World map canvas view with regions and fog
     */
    WORLD_VIEW,
}

/**
 * Exploration state for a user
 * Tracks overall exploration progress
 *
 * @param userId User identifier
 * @param exploredRegions Set of region IDs that have been discovered
 * @param totalDiscoveries Total number of discoveries made
 * @param currentRegion Current player region ID
 * @param explorationDays Number of days the user has explored
 */
@Immutable
data class ExplorationState(
    val userId: String,
    val exploredRegions: Set<String> = emptySet(),
    val totalDiscoveries: Int = 0,
    val currentRegion: String? = null,
    val explorationDays: Int = 0,
) {
    /**
     * Check if a region has been explored
     */
    fun hasExplored(regionId: String): Boolean {
        return exploredRegions.contains(regionId)
    }

    /**
     * Get exploration percentage
     */
    fun getExplorationPercentage(totalRegions: Int): Float {
        if (totalRegions == 0) return 0f
        return exploredRegions.size.toFloat() / totalRegions.toFloat()
    }
}
