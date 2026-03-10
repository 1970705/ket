@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.ui.components.ComboIndicator
import com.wordland.ui.components.CompactPetAnimation
import com.wordland.ui.components.LevelProgressBarEnhanced
import com.wordland.ui.components.StarRatingDisplay
import com.wordland.ui.components.WordlandButton
import com.wordland.ui.uistate.QuickJudgeUiState
import com.wordland.ui.viewmodel.QuickJudgeViewModel
import kotlinx.coroutines.delay

/**
 * Quick Judge Screen
 *
 * A speed-based game mode where users judge if translations are correct.
 *
 * Features:
 * - Time-limited questions (5 seconds default)
 * - Combo system for consecutive correct answers
 * - Time bonus for fast answers
 * - Dynamic star rating
 */
@Composable
fun QuickJudgeScreen(
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
    onLevelComplete: () -> Unit = {},
    viewModel: QuickJudgeViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()
    val petAnimationState by viewModel.petAnimationState.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()

    // Auto-advance to next level on completion
    LaunchedEffect(uiState) {
        if (uiState is QuickJudgeUiState.LevelComplete) {
            delay(2000)
            onLevelComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "快速判断",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Pet animation
            selectedPet?.let { pet ->
                CompactPetAnimation(
                    pet = pet,
                    animationState = petAnimationState,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(80.dp),
                )
            }

            when (val state = uiState) {
                is QuickJudgeUiState.Loading -> {
                    LoadingState()
                }

                is QuickJudgeUiState.Ready -> {
                    QuestionState(
                        question = state.question,
                        currentQuestionIndex = state.currentQuestionIndex,
                        totalQuestions = state.totalQuestions,
                        comboState = state.comboState,
                        timeRemaining = timeRemaining,
                        currentScore = state.currentScore,
                        onJudge = { userSaidTrue ->
                            // Only accept judgment if time hasn't expired
                            if (timeRemaining > 0) {
                                viewModel.submitJudgment(userSaidTrue)
                            }
                        },
                    )
                }

                is QuickJudgeUiState.Feedback -> {
                    FeedbackState(
                        result = state.result,
                        progress = state.progress,
                        comboState = state.comboState,
                        currentScore = state.currentScore,
                        onNext = {
                            viewModel.onNextQuestion()
                        },
                    )
                }

                is QuickJudgeUiState.TimeUp -> {
                    TimeUpState(
                        correctAnswer = state.correctAnswer,
                        translation = state.translation,
                        progress = state.progress,
                        onNext = {
                            viewModel.onNextQuestion()
                        },
                    )
                }

                is QuickJudgeUiState.LevelComplete -> {
                    LevelCompleteState(
                        stars = state.stars,
                        score = state.score,
                        correctCount = state.correctCount,
                        totalQuestions = state.totalQuestions,
                        avgTimePerQuestion = state.avgTimePerQuestion,
                        maxCombo = state.maxCombo,
                        onNavigateBack = onNavigateBack,
                    )
                }

                is QuickJudgeUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onNavigateBack = onNavigateBack,
                    )
                }
            }
        }
    }
}

/**
 * Loading state
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "加载问题中...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Question state - shows the word, translation, and judgment buttons
 */
@Composable
private fun QuestionState(
    question: com.wordland.domain.model.QuickJudgeQuestion,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    comboState: com.wordland.domain.model.ComboState,
    timeRemaining: Int,
    currentScore: Int,
    onJudge: (Boolean) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 80.dp), // Space for cat animation in top-right corner
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Progress bar and combo
        LevelProgressBarEnhanced(
            currentWord = currentQuestionIndex + 1,
            totalWords = totalQuestions,
            comboState = comboState,
            modifier = Modifier.fillMaxWidth(),
        )

        // Score and combo display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Score
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "得分: $currentScore",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            // Timer
            TimerDisplay(timeRemaining = timeRemaining)

            // Combo
            if (comboState.consecutiveCorrect > 0) {
                ComboIndicator(comboState = comboState)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Word card
        WordCard(
            word = question.word,
            translation = question.translation,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Judgment buttons
        JudgmentButtons(
            onCorrect = { onJudge(true) },
            onIncorrect = { onJudge(false) },
            enabled = timeRemaining > 0,
        )

        Text(
            text = "快速判断翻译是否正确！",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Timer display with color animation
 */
@Composable
private fun TimerDisplay(timeRemaining: Int) {
    val timerColor by
        animateColorAsState(
            targetValue =
                when {
                    timeRemaining <= 2 -> MaterialTheme.colorScheme.error
                    timeRemaining <= 3 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
            animationSpec = tween(durationMillis = 300),
            label = "timerColor",
        )

    val scale by
        animateFloatAsState(
            targetValue =
                when {
                    timeRemaining <= 2 -> 1.2f
                    else -> 1f
                },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "timerScale",
        )

    Surface(
        shape = CircleShape,
        color = timerColor.copy(alpha = 0.2f),
        border = BorderStroke(2.dp, timerColor),
    ) {
        Text(
            text = "${timeRemaining}s",
            modifier =
                Modifier
                    .padding(12.dp)
                    .scale(scale),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = timerColor,
        )
    }
}

/**
 * Word card display
 */
@Composable
private fun WordCard(
    word: String,
    translation: String,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Word
            Text(
                text = word,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Divider(
                modifier = Modifier.padding(horizontal = 32.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Translation
            Text(
                text = translation,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Judgment buttons - Correct and Incorrect
 */
@Composable
private fun JudgmentButtons(
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Incorrect button (X)
        JudgementButton(
            text = "错误",
            icon = Icons.Default.Close,
            color = MaterialTheme.colorScheme.error,
            onClick = onIncorrect,
            modifier = Modifier.weight(1f),
            enabled = enabled,
        )

        // Correct button (Check)
        JudgementButton(
            text = "正确",
            icon = Icons.Default.Check,
            color = MaterialTheme.colorScheme.primary,
            onClick = onCorrect,
            modifier = Modifier.weight(1f),
            enabled = enabled,
        )
    }
}

/**
 * Individual judgment button with scale animation
 */
@Composable
private fun JudgementButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue =
                if (isPressed) {
                    0.95f
                } else {
                    1f
                },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "buttonScale",
        )

    Button(
        onClick = {
            if (enabled) {
                isPressed = true
                onClick()
            }
        },
        enabled = enabled,
        modifier =
            modifier
                .height(80.dp)
                .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = MaterialTheme.colorScheme.onError,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Feedback state - shows result after judgment
 */
@Composable
private fun FeedbackState(
    result: com.wordland.domain.model.QuickJudgeResult,
    progress: Float,
    comboState: com.wordland.domain.model.ComboState,
    currentScore: Int,
    onNext: () -> Unit,
) {
    val isCorrect = result.isCorrect
    val backgroundColor =
        if (isCorrect) {
            Color(0xFF4CAF50).copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.errorContainer
        }

    LaunchedEffect(Unit) {
        // Auto-advance after delay
        delay(1500)
        onNext()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Result icon
            Icon(
                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
            )

            // Result text
            Text(
                text =
                    if (isCorrect) {
                        "判断正确！"
                    } else {
                        "判断错误！"
                    },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
            )

            // Score breakdown
            Card(
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isCorrect) {
                        Text(
                            text = "基础分: ${result.baseScore}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (result.timeBonus > 0) {
                            Text(
                                text = "时间奖励: +${result.timeBonus}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = "总分: +${result.totalScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Progress
            LevelProgressBarEnhanced(
                currentWord = (progress * 6).toInt().coerceIn(1, 6),
                totalWords = 6,
                comboState = comboState,
                modifier = Modifier.fillMaxWidth(0.8f),
            )
        }
    }
}

/**
 * Time up state - shows when timer expires
 */
@Composable
private fun TimeUpState(
    correctAnswer: Boolean,
    translation: String,
    progress: Float,
    onNext: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onNext()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "⏰ 时间到！",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )

            Card(
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "这个翻译其实",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (correctAnswer) "✓ 正确" else "✗ 错误",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (correctAnswer) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    )
                }
            }

            LevelProgressBarEnhanced(
                currentWord = (progress * 6).toInt().coerceIn(1, 6),
                totalWords = 6,
                modifier = Modifier.fillMaxWidth(0.8f),
            )
        }
    }
}

/**
 * Level complete state
 */
@Composable
private fun LevelCompleteState(
    stars: Int,
    score: Int,
    correctCount: Int,
    totalQuestions: Int,
    avgTimePerQuestion: Long,
    maxCombo: Int,
    onNavigateBack: () -> Unit,
) {
    val starScale by
        animateFloatAsState(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            label = "starScale",
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            // Title
            Text(
                text = "关卡完成！",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            // Stars
            StarRatingDisplay(
                stars = stars,
                modifier = Modifier.scale(starScale),
            )

            // Stats card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatRow("总得分", score.toString())
                    StatRow("正确题数", "$correctCount/$totalQuestions")
                    StatRow(
                        "平均用时",
                        "${(avgTimePerQuestion / 1000.0).format(1)}秒",
                    )
                    if (maxCombo > 1) {
                        StatRow("最高连击", "$maxCombo")
                    }
                }
            }

            // Back button
            WordlandButton(
                text = "返回",
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(0.6f),
            )
        }
    }
}

/**
 * Stat row for level complete
 */
@Composable
private fun StatRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Error state
 */
@Composable
private fun ErrorState(
    message: String,
    onNavigateBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "出错了",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            WordlandButton(
                text = "返回",
                onClick = onNavigateBack,
            )
        }
    }
}

/**
 * Format decimal value
 */
private fun Double.format(digits: Int): String {
    return "%.${digits}f".format(this)
}
