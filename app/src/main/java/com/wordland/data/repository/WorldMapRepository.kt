package com.wordland.data.repository

import com.wordland.data.dao.WorldMapDao
import com.wordland.domain.model.ExplorationState
import com.wordland.domain.model.MapRegion
import com.wordland.domain.model.WorldMapState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository interface for world map operations
 */
interface WorldMapRepository {
    /**
     * Get world map state for a user
     */
    suspend fun getWorldMapState(userId: String): WorldMapState

    /**
     * Observe world map state changes
     */
    fun getWorldMapStateFlow(userId: String): Flow<WorldMapState>

    /**
     * Get exploration state for a user
     */
    suspend fun getExplorationState(userId: String): ExplorationState?

    /**
     * Observe exploration state changes
     */
    fun getExplorationStateFlow(userId: String): Flow<ExplorationState?>

    /**
     * Mark a region as explored
     */
    suspend fun markRegionExplored(
        userId: String,
        regionId: String,
    )

    /**
     * Set current region for a user
     */
    suspend fun setCurrentRegion(
        userId: String,
        regionId: String?,
    )

    /**
     * Get all available map regions
     */
    suspend fun getMapRegions(): List<MapRegion>

    /**
     * Get a specific region by ID
     */
    suspend fun getRegion(regionId: String): MapRegion?
}

/**
 * Implementation of WorldMapRepository
 *
 * This repository combines:
 * 1. Static map region definitions (from configuration)
 * 2. User exploration progress (from database)
 * 3. Current player position and fog state
 */
class WorldMapRepositoryImpl(
    private val worldMapDao: WorldMapDao,
    private val regionDataProvider: MapRegionDataProvider = MapRegionDataProvider(),
) : WorldMapRepository {
    override suspend fun getWorldMapState(userId: String): WorldMapState {
        val explorationState = getExplorationState(userId)
        val regions = getMapRegions()

        return WorldMapState(
            regions = regions,
            fogState =
                com.wordland.domain.model.FogState.forPlayerLevel(
                    getPlayerLevel(userId, explorationState),
                ),
            playerPosition = getPlayerPosition(explorationState),
            viewMode = com.wordland.domain.model.MapViewMode.ISLAND_VIEW,
            exploredRegions = explorationState?.exploredRegions ?: emptySet(),
        )
    }

    override fun getWorldMapStateFlow(userId: String): Flow<WorldMapState> {
        return getExplorationStateFlow(userId).map { explorationState ->
            val regions = regionDataProvider.getAllRegions()
            WorldMapState(
                regions = regions,
                fogState =
                    com.wordland.domain.model.FogState.forPlayerLevel(
                        getPlayerLevel(userId, explorationState),
                    ),
                playerPosition = getPlayerPosition(explorationState),
                viewMode = com.wordland.domain.model.MapViewMode.ISLAND_VIEW,
                exploredRegions = explorationState?.exploredRegions ?: emptySet(),
            )
        }
    }

    override suspend fun getExplorationState(userId: String): ExplorationState? {
        return worldMapDao.getExplorationState(userId)?.toDomain()
    }

    override fun getExplorationStateFlow(userId: String): Flow<ExplorationState?> {
        return worldMapDao.getExplorationStateFlow(userId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun markRegionExplored(
        userId: String,
        regionId: String,
    ) {
        val current = getExplorationState(userId)
        val now = System.currentTimeMillis()

        if (current == null) {
            // Create new exploration state
            val newEntity =
                com.wordland.data.dao.WorldMapExplorationEntity(
                    userId = userId,
                    exploredRegions = "[$regionId]",
                    totalDiscoveries = 1,
                    currentRegion = regionId,
                    explorationDays = 1,
                    createdAt = now,
                    updatedAt = now,
                )
            worldMapDao.insertExplorationState(newEntity)
        } else {
            // Update existing state
            val newRegions = current.exploredRegions + regionId
            val newDiscoveries =
                if (current.exploredRegions.contains(regionId)) {
                    current.totalDiscoveries
                } else {
                    current.totalDiscoveries + 1
                }

            worldMapDao.updateExploredRegions(
                userId = userId,
                exploredRegionsJson =
                    newRegions.joinToString(
                        separator = ",",
                        prefix = "[",
                        postfix = "]",
                    ) { "\"$it\"" },
                totalDiscoveries = newDiscoveries,
                updatedAt = now,
            )
        }
    }

    override suspend fun setCurrentRegion(
        userId: String,
        regionId: String?,
    ) {
        val current = getExplorationState(userId)
        val now = System.currentTimeMillis()

        if (current == null) {
            val newEntity =
                com.wordland.data.dao.WorldMapExplorationEntity(
                    userId = userId,
                    exploredRegions = "[]",
                    totalDiscoveries = 0,
                    currentRegion = regionId,
                    explorationDays = 0,
                    createdAt = now,
                    updatedAt = now,
                )
            worldMapDao.insertExplorationState(newEntity)
        } else {
            worldMapDao.updateCurrentRegion(
                userId = userId,
                currentRegion = regionId,
                updatedAt = now,
            )
        }
    }

    override suspend fun getMapRegions(): List<MapRegion> {
        return regionDataProvider.getAllRegions()
    }

    override suspend fun getRegion(regionId: String): MapRegion? {
        return regionDataProvider.getRegion(regionId)
    }

    /**
     * Get player position based on exploration state
     */
    private fun getPlayerPosition(explorationState: ExplorationState?): com.wordland.domain.model.MapPosition {
        val currentRegionId = explorationState?.currentRegion ?: "look_peninsula"
        val region = regionDataProvider.getRegion(currentRegionId)

        return region?.position ?: com.wordland.domain.model.MapPosition(
            x = 0.5f,
            y = 0.5f,
        )
    }

    /**
     * Calculate player level from exploration state
     * TODO: This should come from actual user progress data
     */
    private fun getPlayerLevel(
        userId: String,
        explorationState: ExplorationState?,
    ): Int {
        // For MVP, calculate from exploration days and discoveries
        val discoveries = explorationState?.totalDiscoveries ?: 0
        val days = explorationState?.explorationDays ?: 0

        return when {
            discoveries >= 10 || days >= 7 -> 7
            discoveries >= 5 || days >= 3 -> 5
            discoveries >= 2 -> 3
            else -> 1
        }
    }
}

/**
 * Provider for static map region data
 * In a full implementation, this could load from configuration files
 */
class MapRegionDataProvider {
    private val regions =
        listOf(
            // Look Island - Look Peninsula (top center)
            MapRegion(
                id = "look_peninsula",
                islandId = "look_island",
                name = "Look Peninsula",
                position =
                    com.wordland.domain.model.MapPosition(
                        x = 0.5f,
                        y = 0.2f,
                    ),
                bounds =
                    com.wordland.domain.model.MapBounds.centeredAt(
                        centerX = 0.5f,
                        centerY = 0.2f,
                        width = 0.25f,
                        height = 0.15f,
                    ),
                fogLevel = com.wordland.domain.model.FogLevel.VISIBLE,
                isUnlocked = true,
                icon = "👁️",
            ),
            // Make Island - Make Lake (bottom left) - corresponds to make_lake islandId
            MapRegion(
                id = "make_atoll",
                islandId = "make_lake",
                name = "Make Lake",
                position =
                    com.wordland.domain.model.MapPosition(
                        x = 0.2f,
                        y = 0.6f,
                    ),
                bounds =
                    com.wordland.domain.model.MapBounds.centeredAt(
                        centerX = 0.2f,
                        centerY = 0.6f,
                        width = 0.25f,
                        height = 0.15f,
                    ),
                fogLevel = com.wordland.domain.model.FogLevel.VISIBLE,
                isUnlocked = true,
                icon = "🛠️",
            ),
            // Listen Valley - Listen Cove (bottom right)
            MapRegion(
                id = "listen_cove",
                islandId = "listen_island",
                name = "Listen Cove",
                position =
                    com.wordland.domain.model.MapPosition(
                        x = 0.8f,
                        y = 0.6f,
                    ),
                bounds =
                    com.wordland.domain.model.MapBounds.centeredAt(
                        centerX = 0.8f,
                        centerY = 0.6f,
                        width = 0.25f,
                        height = 0.15f,
                    ),
                fogLevel = com.wordland.domain.model.FogLevel.HIDDEN,
                isUnlocked = false,
                icon = "👂",
            ),
            // Future regions (locked)
            MapRegion(
                id = "speak_bay",
                islandId = "speak_island",
                name = "Speak Bay",
                position =
                    com.wordland.domain.model.MapPosition(
                        x = 0.35f,
                        y = 0.75f,
                    ),
                bounds =
                    com.wordland.domain.model.MapBounds.centeredAt(
                        centerX = 0.35f,
                        centerY = 0.75f,
                        width = 0.2f,
                        height = 0.12f,
                    ),
                fogLevel = com.wordland.domain.model.FogLevel.LOCKED,
                isUnlocked = false,
                icon = "🗣️",
            ),
            MapRegion(
                id = "read_archipelago",
                islandId = "read_island",
                name = "Read Archipelago",
                position =
                    com.wordland.domain.model.MapPosition(
                        x = 0.65f,
                        y = 0.75f,
                    ),
                bounds =
                    com.wordland.domain.model.MapBounds.centeredAt(
                        centerX = 0.65f,
                        centerY = 0.75f,
                        width = 0.2f,
                        height = 0.12f,
                    ),
                fogLevel = com.wordland.domain.model.FogLevel.LOCKED,
                isUnlocked = false,
                icon = "📖",
            ),
        )

    /**
     * Get all regions
     */
    fun getAllRegions(): List<MapRegion> = regions

    /**
     * Get region by ID
     */
    fun getRegion(regionId: String): MapRegion? {
        return regions.find { it.id == regionId }
    }

    /**
     * Get regions by island ID
     */
    fun getRegionsByIsland(islandId: String): List<MapRegion> {
        return regions.filter { it.islandId == islandId }
    }
}
