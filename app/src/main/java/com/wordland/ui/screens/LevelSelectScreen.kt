@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.LevelStatus
import com.wordland.ui.components.GameModeSelector
import com.wordland.ui.components.LevelCard
import com.wordland.ui.uistate.LevelSelectUiState
import com.wordland.ui.viewmodel.LevelSelectViewModel

data class LevelInfo(
    val id: String,
    val name: String,
    val stars: Int,
    val isUnlocked: Boolean,
)

@Composable
fun LevelSelectScreen(
    islandId: String,
    onNavigateBack: () -> Unit,
    onStartLevel: (String) -> Unit = {},
    onStartSpellBattle: (String, String) -> Unit = { _, _ -> },
    onStartQuickJudge: (String, String) -> Unit = { _, _ -> },
    viewModel: LevelSelectViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
) {
    // Load levels when screen is first shown
    LaunchedEffect(islandId) {
        viewModel.loadLevels(islandId)
    }

    val uiState by viewModel.uiState.collectAsState()

    // State for game mode selector
    var showGameModeSelector by rememberSaveable { mutableStateOf(false) }
    var selectedLevelId by rememberSaveable { mutableStateOf("") }
    var selectedLevelName by rememberSaveable { mutableStateOf("") }

    // Show game mode selector when level is clicked
    fun onLevelClick(
        levelId: String,
        levelName: String,
    ) {
        selectedLevelId = levelId
        selectedLevelName = levelName
        showGameModeSelector = true
    }

    // Show loading, error, or content
    when (val state = uiState) {
        is LevelSelectUiState.Loading -> {
            // Show loading indicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is LevelSelectUiState.Error -> {
            // Show error message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        is LevelSelectUiState.Success -> {
            // Convert LevelProgress to LevelInfo
            val levels =
                state.levels.map { levelProgress ->
                    LevelInfo(
                        id = levelProgress.levelId,
                        name =
                            levelProgress.levelId.let {
                                // Extract level number from ID like "look_island_level_01" -> "Level 1"
                                val levelNumber = it.substringAfterLast("_").toIntOrNull() ?: 1
                                "Level $levelNumber"
                            },
                        stars = levelProgress.stars,
                        isUnlocked = isLevelUnlocked(levelProgress),
                    )
                }

            LevelSelectContent(
                islandId = islandId,
                levels = levels,
                onNavigateBack = onNavigateBack,
                onStartLevel = onStartLevel,
                onLevelClick = ::onLevelClick,
            )
        }
    }

    // Game mode selector dialog
    if (showGameModeSelector && selectedLevelId.isNotEmpty()) {
        GameModeSelector(
            levelName = selectedLevelName,
            levelId = selectedLevelId,
            islandId = islandId,
            onDismiss = {
                showGameModeSelector = false
                selectedLevelId = ""
                selectedLevelName = ""
            },
            onStartSpellBattle = onStartSpellBattle,
            onStartQuickJudge = onStartQuickJudge,
        )
    }
}

@Composable
private fun LevelSelectContent(
    islandId: String,
    levels: List<LevelInfo>,
    onNavigateBack: () -> Unit,
    onStartLevel: (String) -> Unit,
    onLevelClick: (String, String) -> Unit,
) {
    Scaffold(
        topBar = {
            LevelSelectAppBar(
                islandName = getIslandDisplayName(islandId),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "选择关卡",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            items(levels) { level ->
                LevelCard(
                    levelName = level.name,
                    stars = level.stars,
                    isUnlocked = level.isUnlocked,
                    onClick = {
                        if (level.isUnlocked) {
                            onLevelClick(level.id, level.name)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Check if a level is unlocked
 * A level is unlocked if:
 * 1. It's the first level (level_01)
 * 2. OR the previous level is completed (stars > 0)
 */
private fun isLevelUnlocked(levelProgress: com.wordland.domain.model.LevelProgress): Boolean {
    // First level is always unlocked
    if (levelProgress.levelId.endsWith("_01")) {
        return levelProgress.status != LevelStatus.LOCKED
    }

    // Other levels are unlocked if:
    // 1. Status is UNLOCKED or COMPLETED
    // 2. OR has stars (meaning it was played before)
    return levelProgress.status != LevelStatus.LOCKED || levelProgress.stars > 0
}

@Composable
private fun LevelSelectAppBar(
    islandName: String,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = islandName,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    )
}

private fun getIslandDisplayName(islandId: String): String {
    return when (islandId) {
        "look_island" -> "观看岛"
        "make_lake" -> "制作湖"
        "move_valley" -> "移动谷"
        "say_mountain" -> "说话山"
        "feel_garden" -> "感觉花园"
        else -> "岛屿"
    }
}
