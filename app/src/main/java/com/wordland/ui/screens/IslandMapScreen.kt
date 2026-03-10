@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.ui.components.IslandCard
import com.wordland.ui.uistate.IslandMapUiState
import com.wordland.ui.viewmodel.IslandMapViewModel

@Composable
fun IslandMapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelSelect: (String) -> Unit,
    viewModel: IslandMapViewModel =
        viewModel(
            factory = AppServiceLocator.provideFactory(),
        ),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            IslandMapAppBar(onNavigateBack = onNavigateBack)
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is IslandMapUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is IslandMapUiState.Success -> {
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
                            text = "探索岛屿 🏝️",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                    }

                    items(state.islands) { island ->
                        IslandCard(
                            islandName = island.name,
                            islandColor = island.color,
                            masteryPercentage = island.masteryPercentage,
                            isUnlocked = island.isUnlocked,
                            onClick = { onNavigateToLevelSelect(island.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            is IslandMapUiState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "出错了",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IslandMapAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "岛屿地图",
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
