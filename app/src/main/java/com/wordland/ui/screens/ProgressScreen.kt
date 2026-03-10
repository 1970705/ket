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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.UserWordProgress
import com.wordland.ui.uistate.ProgressUiState
import com.wordland.ui.viewmodel.ProgressViewModel

@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProgressViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load progress on first composition
    LaunchedEffect(Unit) {
        viewModel.loadProgress(userId = "user_001") // TODO: Get actual user ID
    }

    Scaffold(
        topBar = {
            ProgressAppBar(onNavigateBack = onNavigateBack)
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is ProgressUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProgressUiState.Success -> {
                ProgressContent(
                    progressList = state.progress,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                )
            }

            is ProgressUiState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (uiState as ProgressUiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "学习进度",
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

@Composable
private fun ProgressContent(
    progressList: List<UserWordProgress>,
    modifier: Modifier = Modifier,
) {
    if (progressList.isEmpty()) {
        // Empty state
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "📚",
                style = MaterialTheme.typography.displayLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "还没有学习记录",
                style = MaterialTheme.typography.headlineMedium,
            )

            Text(
                text = "开始学习来查看进度吧！",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        // Progress list
        Column(modifier = modifier.padding(16.dp)) {
            // Summary card
            ProgressSummaryCard(
                totalWords = progressList.size,
                masteredWords = progressList.count { it.memoryStrength >= 80 },
                averageStrength =
                    if (progressList.isNotEmpty()) {
                        progressList.map { it.memoryStrength }.average().toInt()
                    } else {
                        0
                    },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress list
            Text(
                text = "所有单词 (${progressList.size})",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(progressList) { progress ->
                    ProgressItem(progress = progress)
                }
            }
        }
    }
}

@Composable
private fun ProgressSummaryCard(
    totalWords: Int,
    masteredWords: Int,
    averageStrength: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "学习概况",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalWords",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Text(
                        text = "总单词",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$masteredWords",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "已掌握",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$averageStrength",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Text(
                        text = "平均强度",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressItem(progress: UserWordProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = progress.wordId,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "最后复习: ${formatTime(progress.lastReviewTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${progress.memoryStrength}",
                    style = MaterialTheme.typography.titleLarge,
                    color = getStrengthColor(progress.memoryStrength),
                )
                Text(
                    text = getStrengthLabel(progress.memoryStrength),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long?): String {
    if (timestamp == null) return "未开始"

    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000} 分钟前"
        diff < 86400000 -> "${diff / 3600000} 小时前"
        else -> "${diff / 86400000} 天前"
    }
}

private fun getStrengthColor(strength: Int): androidx.compose.ui.graphics.Color {
    return when {
        strength >= 80 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        strength >= 60 -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
        strength >= 40 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        else -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
    }
}

private fun getStrengthLabel(strength: Int): String {
    return when {
        strength >= 80 -> "熟练"
        strength >= 60 -> "良好"
        strength >= 40 -> "学习中"
        else -> "待学习"
    }
}
