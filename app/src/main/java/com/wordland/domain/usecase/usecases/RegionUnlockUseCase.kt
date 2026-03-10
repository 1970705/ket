package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.FogLevel
import com.wordland.domain.model.MapRegion
import com.wordland.domain.model.Result
import javax.inject.Inject

/**
 * Use case for managing region unlock logic
 *
 * Story #2.4: Region Unlock Logic
 *
 * Unlock rules:
 * - A region can be unlocked if it's adjacent to an explored region
 * - Initial region (look_peninsula) is always unlocked
 * - Unlock state persists across sessions
 *
 * @param exploreRegionUseCase Use case for marking regions as explored
 */
class RegionUnlockUseCase
    @Inject
    constructor(
        private val exploreRegionUseCase: ExploreRegionUseCase,
    ) {
        /**
         * Check if a region can be unlocked
         *
         * @param userId User ID
         * @param targetRegion Region to check for unlock
         * @param allRegions All regions on the map
         * @return Result with true if unlockable, false with reason if not
         */
        suspend fun canUnlockRegion(
            userId: String,
            targetRegion: MapRegion,
            allRegions: List<MapRegion>,
        ): Result<Boolean> {
            // Already unlocked or visible
            if (targetRegion.fogLevel == FogLevel.VISIBLE ||
                targetRegion.fogLevel == FogLevel.PARTIAL ||
                targetRegion.isUnlocked
            ) {
                return Result.Success(true)
            }

            // Initial region is always unlockable
            if (targetRegion.id == "look_peninsula") {
                return Result.Success(true)
            }

            // Check if adjacent to an explored region
            val exploredRegions = getExploredRegions(userId, allRegions)
            val hasAdjacentExplored =
                exploredRegions.any { explored ->
                    areRegionsAdjacent(targetRegion, explored)
                }

            return if (hasAdjacentExplored) {
                Result.Success(true)
            } else {
                Result.Error(
                    IllegalStateException(
                        "Region '${targetRegion.name}' cannot be unlocked. " +
                            "Explore nearby areas first!",
                    ),
                )
            }
        }

        /**
         * Unlock a region
         *
         * @param userId User ID
         * @param regionId Region to unlock
         * @param allRegions All regions on the map
         * @return Result with updated region if successful
         */
        suspend fun unlockRegion(
            userId: String,
            regionId: String,
            allRegions: List<MapRegion>,
        ): Result<MapRegion> {
            val targetRegion =
                allRegions.find { it.id == regionId }
                    ?: return Result.Error(IllegalArgumentException("Region not found: $regionId"))

            // Check if can unlock
            val canUnlock = canUnlockRegion(userId, targetRegion, allRegions)
            if (canUnlock !is Result.Success || !canUnlock.data) {
                val errorMessage =
                    when (canUnlock) {
                        is Result.Error -> canUnlock.exception.message ?: "Unknown error"
                        else -> "Cannot unlock region"
                    }
                return Result.Error(IllegalStateException("Cannot unlock region: $errorMessage"))
            }

            // Mark as explored (this will update fog level to VISIBLE)
            exploreRegionUseCase(userId, regionId)

            // Return updated region
            val updatedRegion =
                targetRegion.copy(
                    fogLevel = FogLevel.PARTIAL,
                    isUnlocked = true,
                )

            return Result.Success(updatedRegion)
        }

        /**
         * Get list of unlockable regions for a user
         *
         * @param userId User ID
         * @param allRegions All regions on the map
         * @return List of regions that can be unlocked
         */
        suspend fun getUnlockableRegions(
            userId: String,
            allRegions: List<MapRegion>,
        ): List<MapRegion> {
            return allRegions.filterNotNull().filter { region ->
                val canUnlock = canUnlockRegion(userId, region, allRegions)
                canUnlock is Result.Success && canUnlock.data &&
                    region.fogLevel != FogLevel.VISIBLE &&
                    !region.isUnlocked
            }
        }

        /**
         * Check if two regions are adjacent on the map
         * Based on distance between their positions
         *
         * @param region1 First region
         * @param region2 Second region
         * @return True if regions are considered adjacent
         */
        private fun areRegionsAdjacent(
            region1: MapRegion,
            region2: MapRegion,
        ): Boolean {
            // Calculate distance between regions
            val dx = region1.position.x - region2.position.x
            val dy = region1.position.y - region2.position.y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)

            // Regions are adjacent if within 25% of map distance
            // This allows for diagonal adjacency
            return distance <= 0.25f
        }

        /**
         * Get explored regions for a user
         *
         * @param userId User ID
         * @param allRegions All regions on the map
         * @return List of explored regions
         */
        private suspend fun getExploredRegions(
            userId: String,
            allRegions: List<MapRegion>,
        ): List<MapRegion> {
            val explorationState = exploreRegionUseCase.getExplorationState(userId)
            val exploredIds = explorationState?.exploredRegions ?: emptySet()

            return allRegions.filter { region ->
                exploredIds.contains(region.id) ||
                    region.fogLevel == FogLevel.VISIBLE
            }
        }

        /**
         * Calculate unlock progress for a region
         *
         * @param userId User ID
         * @param regionId Region to check
         * @param allRegions All regions on the map
         * @return Float from 0.0 to 1.0 representing unlock progress
         */
        suspend fun getUnlockProgress(
            userId: String,
            regionId: String,
            allRegions: List<MapRegion>,
        ): Float {
            val targetRegion =
                allRegions.find { it.id == regionId }
                    ?: return 0f

            // Already unlocked
            if (targetRegion.isUnlocked || targetRegion.fogLevel == FogLevel.VISIBLE) {
                return 1f
            }

            // Check adjacent regions
            val exploredRegions = getExploredRegions(userId, allRegions)
            val adjacentExplored =
                exploredRegions.count { explored ->
                    areRegionsAdjacent(targetRegion, explored)
                }

            // Need at least one adjacent explored region
            // Progress based on adjacent explored count (max needed: 1)
            return (adjacentExplored.toFloat() / 1f).coerceAtMost(1f)
        }
    }
