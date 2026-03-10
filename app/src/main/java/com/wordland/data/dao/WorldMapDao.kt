package com.wordland.data.dao

import androidx.room.*
import com.wordland.domain.model.ExplorationState
import kotlinx.coroutines.flow.Flow

/**
 * DAO for world map exploration data
 * Handles persistence of exploration progress and discovered regions
 */
@Dao
interface WorldMapDao {
    /**
     * Get exploration state for a user
     */
    @Query("SELECT * FROM world_map_exploration WHERE userId = :userId LIMIT 1")
    suspend fun getExplorationState(userId: String): WorldMapExplorationEntity?

    /**
     * Observe exploration state for a user
     */
    @Query("SELECT * FROM world_map_exploration WHERE userId = :userId LIMIT 1")
    fun getExplorationStateFlow(userId: String): Flow<WorldMapExplorationEntity?>

    /**
     * Insert or update exploration state
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExplorationState(state: WorldMapExplorationEntity)

    /**
     * Update explored regions for a user
     */
    @Query(
        """
        UPDATE world_map_exploration
        SET exploredRegions = :exploredRegionsJson,
            totalDiscoveries = :totalDiscoveries,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """,
    )
    suspend fun updateExploredRegions(
        userId: String,
        exploredRegionsJson: String,
        totalDiscoveries: Int,
        updatedAt: Long,
    )

    /**
     * Set current region for a user
     */
    @Query(
        """
        UPDATE world_map_exploration
        SET currentRegion = :currentRegion,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """,
    )
    suspend fun updateCurrentRegion(
        userId: String,
        currentRegion: String?,
        updatedAt: Long,
    )

    /**
     * Increment exploration days
     */
    @Query(
        """
        UPDATE world_map_exploration
        SET explorationDays = explorationDays + 1,
            updatedAt = :updatedAt
        WHERE userId = :userId
    """,
    )
    suspend fun incrementExplorationDays(
        userId: String,
        updatedAt: Long,
    )

    /**
     * Delete exploration state for a user
     */
    @Query("DELETE FROM world_map_exploration WHERE userId = :userId")
    suspend fun deleteExplorationState(userId: String)

    /**
     * Get all exploration states (for debugging)
     */
    @Query("SELECT * FROM world_map_exploration")
    suspend fun getAllExplorationStates(): List<WorldMapExplorationEntity>
}

/**
 * Entity for world map exploration persistence
 */
@Entity(tableName = "world_map_exploration")
data class WorldMapExplorationEntity(
    @PrimaryKey val userId: String,
    val exploredRegions: String, // JSON array of region IDs
    val totalDiscoveries: Int = 0,
    val currentRegion: String? = null,
    val explorationDays: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    /**
     * Convert to domain ExplorationState
     */
    fun toDomain(): ExplorationState {
        val regionSet =
            try {
                exploredRegions
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotEmpty() }
                    .toSet()
            } catch (e: Exception) {
                emptySet()
            }

        return ExplorationState(
            userId = userId,
            exploredRegions = regionSet,
            totalDiscoveries = totalDiscoveries,
            currentRegion = currentRegion,
            explorationDays = explorationDays,
        )
    }

    companion object {
        /**
         * Create entity from domain ExplorationState
         */
        fun fromDomain(state: ExplorationState): WorldMapExplorationEntity {
            val now = System.currentTimeMillis()
            return WorldMapExplorationEntity(
                userId = state.userId,
                exploredRegions =
                    state.exploredRegions.joinToString(
                        separator = ",",
                        prefix = "[",
                        postfix = "]",
                    ) { "\"$it\"" },
                totalDiscoveries = state.totalDiscoveries,
                currentRegion = state.currentRegion,
                explorationDays = state.explorationDays,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
