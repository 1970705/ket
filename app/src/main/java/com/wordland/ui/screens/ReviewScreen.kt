@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.Word
import com.wordland.ui.components.WordlandCard
import com.wordland.ui.uistate.ReviewUiState
import com.wordland.ui.viewmodel.ReviewViewModel

@Composable
fun ReviewScreen(
    onNavigateBack: () -> Unit,
    onStartReview: (String) -> Unit,
    viewModel: ReviewViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val reviewWords by viewModel.reviewWords.collectAsState(initial = emptyList())
    val learningWords by viewModel.learningWords.collectAsState(initial = emptyList())

    // Load review words on first composition
    LaunchedEffect(Unit) {
        viewModel.loadReviewWords(userId = "user_001") // TODO: Get actual user ID
    }

    Scaffold(
        topBar = {
            ReviewAppBar(onNavigateBack = onNavigateBack)
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is ReviewUiState.Loading -> {
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

            is ReviewUiState.Ready -> {
                if (!state.hasWords) {
                    EmptyReviewContent(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                    )
                } else {
                    ReviewQueueContent(
                        reviewWords = reviewWords,
                        learningWords = learningWords,
                        totalDue = state.totalDue,
                        totalLearning = state.totalLearning,
                        onStartReview = onStartReview,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                    )
                }
            }

            is ReviewUiState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "每日复习",
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
private fun ReviewQueueContent(
    reviewWords: List<com.wordland.domain.model.ReviewWordItem>,
    learningWords: List<com.wordland.domain.model.ReviewWordItem>,
    totalDue: Int,
    totalLearning: Int,
    onStartReview: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ReviewSummaryCard(
                totalDue = totalDue,
                totalLearning = totalLearning,
            )
        }

        if (reviewWords.isNotEmpty()) {
            item {
                Text(
                    text = "待复习单词 ($totalDue)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            items(reviewWords) { item ->
                ReviewWordCard(
                    word = item.word,
                    memoryStrength = item.memoryStrength,
                    onClick = { onStartReview(item.word.id) },
                )
            }
        }

        if (learningWords.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "学习中的单词 ($totalLearning)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            items(learningWords) { item ->
                ReviewWordCard(
                    word = item.word,
                    memoryStrength = item.memoryStrength,
                    onClick = { onStartReview(item.word.id) },
                )
            }
        }
    }
}

@Composable
private fun ReviewSummaryCard(
    totalDue: Int,
    totalLearning: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$totalDue",
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = "待复习",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Divider(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$totalLearning",
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = "学习中",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ReviewWordCard(
    word: com.wordland.domain.model.Word,
    memoryStrength: Int,
    onClick: () -> Unit,
) {
    // Determine if word is weak (needs review)
    val isWeakWord = memoryStrength < 40
    val strengthColor =
        when {
            memoryStrength >= 80 -> MaterialTheme.colorScheme.primary
            memoryStrength >= 50 -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.error
        }

    // Strength level description
    val strengthLevel =
        when {
            memoryStrength >= 80 -> "熟练"
            memoryStrength >= 50 -> "掌握"
            else -> "需加强"
        }

    WordlandCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        borderColor = if (isWeakWord) MaterialTheme.colorScheme.error else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        // Weak word indicator
                        if (isWeakWord) {
                            Text(
                                text = "⚠️",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Text(
                        text = word.translation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "$memoryStrength",
                        style = MaterialTheme.typography.titleMedium,
                        color = strengthColor,
                    )
                    Text(
                        text = strengthLevel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visual strength indicator bar
            MemoryStrengthIndicator(
                strength = memoryStrength,
                color = strengthColor,
            )
        }
    }
}

@Composable
private fun MemoryStrengthIndicator(
    strength: Int,
    color: Color,
) {
    val strengthPercent = strength.coerceIn(0, 100) / 100f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Progress bar background
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        color = color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp),
                    ),
        ) {
            // Progress bar fill
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(strengthPercent)
                        .fillMaxHeight()
                        .background(
                            color = color,
                            shape = RoundedCornerShape(3.dp),
                        ),
            )
        }
    }
}

@Composable
private fun EmptyReviewContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "暂时没有需要复习的单词",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "太棒了！继续保持",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
