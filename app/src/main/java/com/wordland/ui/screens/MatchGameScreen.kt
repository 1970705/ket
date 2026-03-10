package com.wordland.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.BubbleColor
import com.wordland.domain.model.BubbleState
import com.wordland.domain.model.MatchGameState
import com.wordland.ui.components.WordlandButton
import com.wordland.ui.viewmodel.MatchGameViewModel
import kotlinx.coroutines.delay

/**
 * 单词消消乐游戏主屏幕
 *
 * Features:
 * - 游戏状态显示（倒计时、分数）
 * - 泡泡网格布局
 * - 暂停/继续功能
 * - 退出确认对话框
 */
@Composable
fun MatchGameScreen(
    viewModel: MatchGameViewModel =
        viewModel(
            factory = AppServiceLocator.provideFactory(),
        ),
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    // Initialize game when screen loads
    LaunchedEffect(Unit) {
        viewModel.initializeGame(levelId, islandId)
    }

    Scaffold(
        topBar = {
            MatchGameTopBar(
                gameState = gameState,
                onPauseClick = { viewModel.pauseGame() },
                onExitClick = { viewModel.showExitConfirmation() },
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            // Game content based on state
            when (val state = gameState) {
                is MatchGameState.Idle -> {
                    LoadingView("准备游戏...")
                }
                is MatchGameState.Preparing -> {
                    LoadingView("加载单词中...")
                }
                is MatchGameState.Ready -> {
                    ReadyView(
                        pairs = state.pairs,
                        onStartGame = { viewModel.startGame() },
                        onBack = onNavigateBack,
                    )
                }
                is MatchGameState.Playing -> {
                    PlayingView(
                        bubbles = state.bubbles,
                        selectedBubbleIds = state.selectedBubbleIds,
                        matchedPairs = state.matchedPairs,
                        progress = state.progress,
                        onBubbleClick = { bubbleId ->
                            viewModel.selectBubble(bubbleId)
                        },
                    )
                }
                is MatchGameState.Paused -> {
                    PausedView(
                        onResume = { viewModel.resumeGame() },
                        onExit = onNavigateBack,
                    )
                }
                is MatchGameState.Completed -> {
                    CompletedView(
                        elapsedTime = state.elapsedTime,
                        pairs = state.pairs,
                        accuracy = state.accuracy,
                        onPlayAgain = { viewModel.initializeGame(levelId, islandId) },
                        onBack = onNavigateBack,
                    )
                }
                is MatchGameState.GameOver -> {
                    GameOverView(
                        onRestart = { viewModel.initializeGame(levelId, islandId) },
                        onBack = onNavigateBack,
                    )
                }
                is MatchGameState.Error -> {
                    ErrorView(
                        message = state.message,
                        onBack = onNavigateBack,
                    )
                }
            }

            // Exit confirmation dialog
            ExitConfirmationDialog(
                show = viewModel.showExitDialog,
                onConfirm = onNavigateBack,
                onDismiss = { viewModel.hideExitConfirmation() },
            )
        }
    }
}

/**
 * Top bar with game info
 */
@Composable
private fun MatchGameTopBar(
    gameState: MatchGameState,
    onPauseClick: () -> Unit,
    onExitClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Exit button
        IconButton(onClick = onExitClick) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "退出",
                tint = Color.White,
            )
        }

        // Game info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "单词消消乐",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            when (val state = gameState) {
                is MatchGameState.Playing -> {
                    Text(
                        text = "配对: ${state.matchedPairs}",
                        color = Color.White,
                        fontSize = 14.sp,
                    )
                }
                else -> {}
            }
        }

        // Pause button
        if (gameState is MatchGameState.Playing) {
            IconButton(onClick = onPauseClick) {
                Text(
                    text = "⏸",
                    fontSize = 24.sp,
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

/**
 * Loading view
 */
@Composable
private fun LoadingView(message: String) {
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
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Ready view - show start button
 */
@Composable
private fun ReadyView(
    pairs: Int,
    onStartGame: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "单词消消乐",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "配对数量: $pairs",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            WordlandButton(
                onClick = onStartGame,
                text = "开始游戏",
                modifier = Modifier.fillMaxWidth(0.7f),
            )
            WordlandButton(
                onClick = onBack,
                text = "返回",
                modifier = Modifier.fillMaxWidth(0.7f),
                size = com.wordland.ui.components.ButtonSize.MEDIUM,
            )
        }
    }
}

/**
 * Playing view - main game interface
 */
@Composable
private fun PlayingView(
    bubbles: List<BubbleState>,
    selectedBubbleIds: Set<String>,
    matchedPairs: Int,
    progress: Float,
    onBubbleClick: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = progress,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bubble grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = bubbles,
                key = { it.id },
            ) { bubble ->
                BubbleTile(
                    bubble = bubble,
                    isSelected = bubble.id in selectedBubbleIds,
                    isMatching = false, // TODO: Add animation state tracking in ViewModel
                    matchFailed = false, // TODO: Add animation state tracking in ViewModel
                    onClick = { onBubbleClick(bubble.id) },
                )
            }
        }
    }
}

/**
 * Paused view
 */
@Composable
private fun PausedView(
    onResume: () -> Unit,
    onExit: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth(0.8f)
                    .padding(32.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "游戏暂停",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                WordlandButton(
                    onClick = onResume,
                    text = "继续游戏",
                    modifier = Modifier.fillMaxWidth(),
                )
                WordlandButton(
                    onClick = onExit,
                    text = "退出游戏",
                    modifier = Modifier.fillMaxWidth(),
                    size = com.wordland.ui.components.ButtonSize.MEDIUM,
                )
            }
        }
    }
}

/**
 * Completed view
 */
@Composable
private fun CompletedView(
    elapsedTime: Long,
    pairs: Int,
    accuracy: Float,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "🎉 恭喜完成！",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "用时: ${elapsedTime / 1000}秒",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "配对: $pairs",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "准确率: ${(accuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                WordlandButton(
                    onClick = onPlayAgain,
                    text = "再玩一次",
                    modifier = Modifier.fillMaxWidth(),
                )
                WordlandButton(
                    onClick = onBack,
                    text = "返回",
                    modifier = Modifier.fillMaxWidth(),
                    size = com.wordland.ui.components.ButtonSize.MEDIUM,
                )
            }
        }
    }
}

/**
 * Game over view
 */
@Composable
private fun GameOverView(
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "游戏结束",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                WordlandButton(
                    onClick = onRestart,
                    text = "重新开始",
                    modifier = Modifier.fillMaxWidth(),
                )
                WordlandButton(
                    onClick = onBack,
                    text = "返回",
                    modifier = Modifier.fillMaxWidth(),
                    size = com.wordland.ui.components.ButtonSize.MEDIUM,
                )
            }
        }
    }
}

/**
 * Error view
 */
@Composable
private fun ErrorView(
    message: String,
    onBack: () -> Unit,
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
                text = "⚠️",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "出错了",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            WordlandButton(
                onClick = onBack,
                text = "返回",
            )
        }
    }
}

/**
 * Bubble tile component with full animation support
 *
 * Features:
 * - Selection animation (scale 1.15x + blue glow effect with border)
 * - Match success animation (fade out + scale down + particle burst)
 * - Match fail animation (red border + shake)
 * - 6 color themes from BubbleColor enum
 * - Performance optimized (60fps target)
 *
 * Per GAME_DESIGN_OUTPUT.md Section 3.2.1:
 * - Click: Scale 1.2x + glow (300ms)
 * - Match success: Explosion + fade out (800ms)
 * - Match fail: Red flash + shake (500ms)
 *
 * Performance Optimizations:
 * - Cached computations with remember
 * - Minimal drawBehind calls
 * - Efficient animation state management
 *
 * @param bubble The bubble state containing word, color, and status
 * @param isSelected Whether this bubble is currently selected
 * @param isMatching Whether this bubble is currently being matched (animation trigger)
 * @param matchFailed Whether the match attempt failed (triggers shake animation)
 * @param onClick Callback when bubble is clicked
 */
@Composable
private fun BubbleTile(
    bubble: BubbleState,
    isSelected: Boolean,
    isMatching: Boolean = false,
    matchFailed: Boolean = false,
    onClick: () -> Unit,
) {
    // Animation states
    var shakeIteration by remember { mutableIntStateOf(0) }

    // Selection scale animation (1.0 -> 1.15 bouncy)
    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 300f,
            ),
        label = "bubble_selection_scale_${bubble.id}",
    )

    // Match disappear animation (fade out + scale down)
    val matchAlpha by animateFloatAsState(
        targetValue = if (bubble.isMatched || isMatching) 0f else 1f,
        animationSpec =
            tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing,
            ),
        label = "bubble_match_alpha_${bubble.id}",
    )

    val matchScale by animateFloatAsState(
        targetValue = if (isMatching) 0.3f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.8f,
                stiffness = 400f,
            ),
        label = "bubble_match_scale_${bubble.id}",
    )

    // Shake animation for mismatch
    val shakeOffset by animateFloatAsState(
        targetValue =
            when (shakeIteration) {
                1 -> -12f // Left
                2 -> 12f // Right
                3 -> -8f // Left (smaller)
                4 -> 8f // Right (smaller)
                5 -> -4f // Left (smallest)
                6 -> 4f // Right (smallest)
                else -> 0f // Center
            },
        animationSpec =
            tween(
                durationMillis = 60,
                easing = LinearEasing,
            ),
        label = "bubble_shake_${bubble.id}",
    )

    // Glow effect animation for selection
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.7f else 0f,
        animationSpec =
            tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing,
            ),
        label = "bubble_glow_alpha_${bubble.id}",
    )

    // Error border flash animation
    val errorBorderAlpha by animateFloatAsState(
        targetValue = if (matchFailed) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 100,
                easing = FastOutSlowInEasing,
            ),
        label = "bubble_error_border_${bubble.id}",
    )

    // Trigger shake animation on match failure
    LaunchedEffect(matchFailed) {
        if (matchFailed) {
            for (i in 1..6) {
                shakeIteration = i
                delay(60)
            }
            shakeIteration = 0
        }
    }

    // Particle explosion effect for match success
    if (isMatching) {
        BubbleExplosionEffect(
            color = bubble.color.toComposeColor(),
        )
    }

    // Performance: Cache background color computation
    val backgroundColor =
        remember(bubble.isMatched) {
            if (bubble.isMatched) Color.Transparent else bubble.color.toComposeColor()
        }

    // Combined scale
    val combinedScale = selectedScale * matchScale

    // Performance: Cache font size computation
    val fontSize =
        remember(bubble.word.length) {
            when {
                bubble.word.length <= 5 -> 15.sp
                bubble.word.length <= 8 -> 13.sp
                else -> 11.sp
            }
        }

    // Performance: Cache shadow elevation
    val shadowElevation = if (isSelected) 8.dp else 4.dp

    Box(
        modifier =
            Modifier
                .size(80.dp)
                .scale(combinedScale)
                .offset(x = shakeOffset.dp)
                .alpha(matchAlpha)
                .then(
                    if (isSelected || matchFailed) {
                        Modifier.drawBehind {
                            // Performance: Single drawBehind call for both states
                            if (isSelected) {
                                // Draw blue glow effect
                                val glowRadius = size.width * 0.15f
                                drawCircle(
                                    color = Color(0xFF2196F3).copy(alpha = glowAlpha * 0.3f),
                                    radius = size.width / 2 + glowRadius,
                                )
                                // Draw blue border
                                drawCircle(
                                    color = Color(0xFF2196F3).copy(alpha = 0.8f),
                                    radius = size.width / 2 - 2.dp.toPx(),
                                    style = Stroke(width = 4.dp.toPx()),
                                )
                            }
                            if (matchFailed) {
                                // Draw red error border
                                drawCircle(
                                    color = Color(0xFFF44336).copy(alpha = errorBorderAlpha),
                                    radius = size.width / 2 - 2.dp.toPx(),
                                    style = Stroke(width = 4.dp.toPx()),
                                )
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        // Main bubble surface
        Box(
            modifier =
                Modifier
                    .size(72.dp)
                    .background(
                        color = backgroundColor,
                        shape = CircleShape,
                    )
                    .then(
                        if (!isSelected && !matchFailed) {
                            Modifier.border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (!bubble.isMatched && !isMatching) {
                            Modifier.clickable(onClick = onClick)
                        } else {
                            Modifier
                        },
                    )
                    .shadow(shadowElevation, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = bubble.word,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

/**
 * Particle explosion effect for successful match
 * Creates small particles radiating from center
 *
 * Performance optimized: Uses animateFloatAsState instead of manual loop
 * - Single animation state instead of continuous updates
 * - Compose handles optimal frame timing
 * - Reduces recomposition overhead
 */
@Composable
private fun BubbleExplosionEffect(
    color: Color,
    modifier: Modifier = Modifier,
) {
    val particleCount = 12

    // Optimized: Use animateFloatAsState instead of manual while loop
    // This leverages Compose's optimized animation engine
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            tween(
                durationMillis = 600,
                easing = LinearEasing,
            ),
        label = "explosion_progress",
    )

    // Auto-hide when animation completes (alpha = 0)
    val alpha = ((1f - animationProgress) * 0.8f).coerceIn(0f, 1f)

    if (alpha > 0f) {
        Box(modifier = modifier.size(80.dp)) {
            repeat(particleCount) { index ->
                val angle = (index * 360f / particleCount) * (Math.PI / 180f)
                val distance = animationProgress * 60

                val offsetX = (kotlin.math.cos(angle) * distance).dp
                val offsetY = (kotlin.math.sin(angle) * distance).dp

                val particleScale = ((1f - animationProgress) * 1.2f).coerceIn(0f, 1f)

                Box(
                    modifier =
                        Modifier
                            .offset(x = offsetX, y = offsetY)
                            .size(6.dp)
                            .scale(particleScale)
                            .alpha(alpha)
                            .background(
                                color = color.copy(alpha = alpha),
                                shape = CircleShape,
                            ),
                )
            }
        }
    }
}

/**
 * Extension function to convert BubbleColor to Compose Color
 * Extracts ARGB components from ULong and creates Color
 * Internal visibility for testing access
 */
internal fun BubbleColor.toComposeColor(): Color {
    val argb = colorValue
    val alpha = ((argb shr 24) and 0xFFu).toFloat() / 255f
    val red = ((argb shr 16) and 0xFFu).toFloat() / 255f
    val green = ((argb shr 8) and 0xFFu).toFloat() / 255f
    val blue = (argb and 0xFFu).toFloat() / 255f
    return Color(red, green, blue, alpha)
}

/**
 * Exit confirmation dialog
 */
@Composable
private fun ExitConfirmationDialog(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth(0.8f)
                        .padding(32.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "退出游戏",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "确定要退出吗？当前进度将会丢失。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(onClick = onConfirm) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
            ),
    ) {
        content()
    }
}
