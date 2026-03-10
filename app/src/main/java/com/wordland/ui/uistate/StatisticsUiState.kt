package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.wordland.domain.model.statistics.GameMode

/**
 * UI State for Statistics Screen
 *
 * Main statistics screen with tabs for history, levels, and achievements
 */
@Stable
sealed class StatisticsUiState {
    @Immutable
    object Loading : StatisticsUiState()

    @Immutable
    data class Ready(
        val globalStats: GlobalStatsUiModel,
        val selectedTab: StatisticsTab = StatisticsTab.HISTORY,
    ) : StatisticsUiState()

    @Immutable
    data class Error(val message: String) : StatisticsUiState()
}

/**
 * Global statistics UI model
 */
@Immutable
data class GlobalStatsUiModel(
    val totalGames: Int = 0,
    val totalScore: Int = 0,
    val totalStudyTime: Long = 0, // in milliseconds
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWordsMastered: Int = 0,
    val totalLevelsCompleted: Int = 0,
)

/**
 * Statistics tabs
 */
enum class StatisticsTab {
    HISTORY,
    LEVELS,
    ACHIEVEMENTS,
}

/**
 * UI State for Game History Screen
 */
@Stable
sealed class GameHistoryUiState {
    @Immutable
    object Loading : GameHistoryUiState()

    @Immutable
    data class Ready(
        val history: List<GameHistoryItemUiModel>,
        val filter: GameMode? = null,
        val hasMore: Boolean = false,
    ) : GameHistoryUiState()

    @Immutable
    data class Error(val message: String) : GameHistoryUiState()
}

/**
 * Game history item UI model
 */
@Immutable
data class GameHistoryItemUiModel(
    val gameId: String,
    val levelId: String,
    val levelName: String,
    val islandId: String,
    val gameMode: GameMode,
    val score: Int,
    val stars: Int,
    val accuracy: Float,
    val duration: Long, // in milliseconds
    val timestamp: Long,
    val maxCombo: Int = 0,
)

/**
 * UI State for Level Statistics Screen
 */
@Stable
sealed class LevelStatisticsUiState {
    @Immutable
    object Loading : LevelStatisticsUiState()

    @Immutable
    data class Ready(
        val levels: List<LevelStatisticsItemUiModel>,
        val selectedIsland: String = "all",
        val islands: List<IslandOption> = emptyList(),
    ) : LevelStatisticsUiState()

    @Immutable
    data class Error(val message: String) : LevelStatisticsUiState()
}

/**
 * Level statistics item UI model
 */
@Immutable
data class LevelStatisticsItemUiModel(
    val levelId: String,
    val levelName: String,
    val islandId: String,
    val totalGames: Int,
    val completedGames: Int,
    val perfectGames: Int,
    val highestScore: Int,
    val averageScore: Float,
    val bestTime: Long?, // in milliseconds
    val overallAccuracy: Float,
    val bestCombo: Int,
)

/**
 * Island selector option
 */
@Immutable
data class IslandOption(
    val islandId: String,
    val islandName: String,
    val icon: String,
)

/**
 * UI State for Achievement Screen
 */
@Stable
sealed class AchievementUiState {
    @Immutable
    object Loading : AchievementUiState()

    @Immutable
    data class Ready(
        val achievements: List<AchievementItemUiModel>,
        val selectedCategory: AchievementCategory = AchievementCategory.ALL,
        val unlockedCount: Int = 0,
        val totalCount: Int = 0,
    ) : AchievementUiState()

    @Immutable
    data class Error(val message: String) : AchievementUiState()
}

/**
 * Achievement item UI model
 */
@Immutable
data class AchievementItemUiModel(
    val achievementId: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val isUnlocked: Boolean,
    val progress: Int,
    val target: Int,
    val unlockedAt: Long? = null,
    val reward: String? = null,
)

/**
 * Achievement categories
 */
enum class AchievementCategory {
    ALL,
    LEARNING,
    SOCIAL,
    CHALLENGE,
    MILESTONE,
}
