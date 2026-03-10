package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.usecase.usecases.GetIslandsUseCase
import com.wordland.ui.theme.FeelGardenOrange
import com.wordland.ui.theme.LookIslandGreen
import com.wordland.ui.theme.MakeLakeCyan
import com.wordland.ui.theme.MoveValleyBlue
import com.wordland.ui.theme.SayMountainPurple
import com.wordland.ui.uistate.IslandInfo
import com.wordland.ui.uistate.IslandMapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for island map screen
 * Refactored to use UseCases
 */
@HiltViewModel
class IslandMapViewModel
    @Inject
    constructor(
        private val getIslands: GetIslandsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<IslandMapUiState>(IslandMapUiState.Loading)
        val uiState: StateFlow<IslandMapUiState> = _uiState.asStateFlow()

        private val userId: String = "user_001" // TODO: Get from user session

        init {
            loadIslands()
        }

        /**
         * Load islands from UseCase
         */
        private fun loadIslands() {
            viewModelScope.launch {
                _uiState.value = IslandMapUiState.Loading

                try {
                    getIslands(userId).collect { islands ->
                        _uiState.value =
                            IslandMapUiState.Success(
                                islands = islands.map { it.toIslandInfo() },
                            )
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        IslandMapUiState.Error(
                            e.message ?: "Failed to load islands",
                        )
                }
            }
        }

        /**
         * Refresh island data
         */
        fun refresh() {
            loadIslands()
        }
    }

/**
 * Extension function to convert IslandWithProgress to IslandInfo
 */
private fun com.wordland.domain.model.IslandWithProgress.toIslandInfo(): IslandInfo {
    return IslandInfo(
        id = islandId,
        name = islandName,
        color = getIslandColor(islandId),
        masteryPercentage = masteryPercentage.toFloat(),
        isUnlocked = isUnlocked,
    )
}

/**
 * Get island color based on island ID
 */
private fun getIslandColor(islandId: String): androidx.compose.ui.graphics.Color {
    return when (islandId) {
        "look_island" -> LookIslandGreen
        "make_lake" -> MakeLakeCyan
        "move_valley" -> MoveValleyBlue
        "say_mountain" -> SayMountainPurple
        "feel_garden" -> FeelGardenOrange
        else -> androidx.compose.ui.graphics.Color.Gray
    }
}
