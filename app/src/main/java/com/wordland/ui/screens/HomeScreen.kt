@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wordland.ui.components.WordlandCard
import com.wordland.ui.theme.AccentOrange
import com.wordland.ui.theme.LookIslandGreen

@Composable
fun HomeScreen(
    onNavigateToIslandMap: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToMultipleChoice: () -> Unit = {},
    onNavigateToFillBlank: () -> Unit = {},
    onNavigateToMatchGame: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            HomeAppBar()
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Welcome message
            Text(
                text = "欢迎来到 Wordland! 🏝️",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "开始你的英语单词冒险吧",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Main action card
            WordlandCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                onClick = onNavigateToIslandMap,
                borderColor = LookIslandGreen,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "🗺️",
                        style = MaterialTheme.typography.displayLarge,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "开始冒险",
                        style = MaterialTheme.typography.headlineMedium,
                        color = LookIslandGreen,
                    )

                    Text(
                        text = "继续探索岛屿",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Secondary actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WordlandCard(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(150.dp),
                    onClick = onNavigateToReview,
                    borderColor = AccentOrange,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "📚",
                            style = MaterialTheme.typography.displayMedium,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "每日复习",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                WordlandCard(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(150.dp),
                    onClick = onNavigateToProgress,
                    borderColor = MaterialTheme.colorScheme.primary,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.displayMedium,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "学习进度",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            // Test button for Multiple Choice
            WordlandCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                onClick = onNavigateToMultipleChoice,
                borderColor = MaterialTheme.colorScheme.tertiary,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "🎯",
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "选择题模式（测试）",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            // Test button for Fill Blank
            WordlandCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                onClick = onNavigateToFillBlank,
                borderColor = MaterialTheme.colorScheme.secondary,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "✏️",
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "填空模式（测试）",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            // Test button for Match Game
            WordlandCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                onClick = onNavigateToMatchGame,
                borderColor = AccentOrange,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "🎮",
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Match Game（测试）",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentOrange,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeAppBar() {
    TopAppBar(
        title = {
            Text(
                text = "Wordland",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* TODO: Open menu */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
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
