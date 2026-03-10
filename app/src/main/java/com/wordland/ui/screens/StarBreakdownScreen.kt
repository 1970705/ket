package com.wordland.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Star Breakdown Screen
 *
 * Shows detailed breakdown of star rating calculation with child-friendly design.
 * Displays accuracy, hints used, time taken, and errors in visual format.
 *
 * Features:
 * - Progress bars with color coding (green/yellow/red)
 * - Animated reveal for each factor
 * - Child-friendly encouraging messages
 * - Material 3 design
 * - Accessibility support
 *
 * @param starRating Final star rating (1-3)
 * @param accuracy Accuracy percentage (0-100)
 * @param hintsUsed Number of hints used
 * @param timeTaken Time taken in seconds
 * @param errorCount Number of wrong answers
 * @param onClose Callback when user closes screen
 * @param modifier Modifier for the screen
 *
 * @author Wordland Team
 * @since 1.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarBreakdownScreen(
    starRating: Int,
    accuracy: Int,
    hintsUsed: Int,
    timeTaken: Int,
    errorCount: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animation state for fade-in
    val fadeInAnim =
        animateFloatAsState(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing,
                ),
            label = "fade_in",
        )

    // Animation state for each factor's progress bar
    val accuracyProgress =
        animateFloatAsState(
            targetValue = accuracy / 100f,
            animationSpec =
                tween(
                    durationMillis = 800,
                    easing = EaseOutCubic,
                ),
            label = "accuracy_progress",
        )

    val timeProgress =
        animateFloatAsState(
            targetValue = calculateTimeScore(timeTaken) / 100f,
            animationSpec =
                tween(
                    durationMillis = 800,
                    delayMillis = 200,
                    easing = EaseOutCubic,
                ),
            label = "time_progress",
        )

    val errorProgress =
        animateFloatAsState(
            targetValue = calculateErrorScore(errorCount) / 100f,
            animationSpec =
                tween(
                    durationMillis = 800,
                    delayMillis = 400,
                    easing = EaseOutCubic,
                ),
            label = "error_progress",
        )

    val hintProgress =
        animateFloatAsState(
            targetValue = calculateHintScore(hintsUsed) / 100f,
            animationSpec =
                tween(
                    durationMillis = 800,
                    delayMillis = 600,
                    easing = EaseOutCubic,
                ),
            label = "hint_progress",
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "星级详情",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
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
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .scale(fadeInAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Title with stars
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            when (starRating) {
                                3 -> MaterialTheme.colorScheme.tertiaryContainer
                                2 -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                    ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Animated stars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp),
                    ) {
                        repeat(3) { index ->
                            val isEarned = index < starRating
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription =
                                    if (isEarned) "获得星星" else "未获得星星",
                                tint =
                                    if (isEarned) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                modifier =
                                    Modifier
                                        .size(32.dp)
                                        .scale(
                                            if (isEarned) 1f else 0.8f,
                                        ),
                            )
                        }
                    }

                    // Encouraging message
                    Text(
                        text =
                            when (starRating) {
                                3 -> "太棒了！继续加油！"
                                2 -> "做得不错！下次会更好！"
                                else -> "继续努力！你能做到的！"
                            },
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color =
                            when (starRating) {
                                3 -> MaterialTheme.colorScheme.tertiary
                                2 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }

            // Accuracy Score
            ScoreFactorCard(
                icon = Icons.Filled.CheckCircle,
                title = "准确率",
                value = "$accuracy%",
                progress = { accuracyProgress.value },
                maxValue = 100f,
                color =
                    when {
                        accuracy >= 80 -> MaterialTheme.colorScheme.tertiary
                        accuracy >= 60 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    },
                description =
                    when {
                        accuracy >= 80 -> "太棒了！准确率很高！"
                        accuracy >= 60 -> "继续努力，争取更高！"
                        else -> "多练习，你会进步的！"
                    },
                isGood = accuracy >= 60,
            )

            // Hints Used
            ScoreFactorCard(
                icon = Icons.Filled.Star, // Using Star as hint icon
                title = "提示使用",
                value = "$hintsUsed 次",
                progress = { hintProgress.value },
                maxValue = 100f,
                color =
                    when {
                        hintsUsed == 0 -> MaterialTheme.colorScheme.tertiary
                        hintsUsed <= 2 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    },
                description =
                    when {
                        hintsUsed == 0 -> "完全没有用提示，真厉害！"
                        hintsUsed <= 2 -> "用了一些提示，没关系！"
                        else -> "下次尽量少用提示哦！"
                    },
                isGood = hintsUsed <= 2,
            )

            // Time Taken
            ScoreFactorCard(
                icon = Icons.Filled.CheckCircle, // Using CheckCircle as time icon
                title = "用时",
                value = formatTime(timeTaken),
                progress = { timeProgress.value },
                maxValue = 100f,
                color =
                    when {
                        timeTaken < 30 -> MaterialTheme.colorScheme.tertiary
                        timeTaken < 60 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    },
                description =
                    when {
                        timeTaken < 30 -> "速度很快，节奏很好！"
                        timeTaken < 60 -> "节奏不错，继续保持！"
                        else -> "慢慢来，准确率更重要！"
                    },
                isGood = timeTaken < 60,
            )

            // Error Count
            ScoreFactorCard(
                icon = Icons.Filled.Warning, // Using Warning as error icon
                title = "错误次数",
                value = "$errorCount 次",
                progress = { errorProgress.value },
                maxValue = 100f,
                color =
                    when {
                        errorCount == 0 -> MaterialTheme.colorScheme.tertiary
                        errorCount <= 2 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    },
                description =
                    when {
                        errorCount == 0 -> "完美！一个错误都没有！"
                        errorCount <= 2 -> "只有几个错误，做得好！"
                        else -> "别灰心，多练习就会减少错误！"
                    },
                isGood = errorCount <= 2,
            )

            // Encouragement Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "小贴士",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text =
                            buildString {
                                if (accuracy < 80) {
                                    append("• 仔细思考再回答\n")
                                }
                                if (hintsUsed > 2) {
                                    append("• 尽量自己拼写单词\n")
                                }
                                if (errorCount > 2) {
                                    append("• 错误是学习的一部分\n")
                                }
                                if (timeTaken > 60) {
                                    append("• 熟练后速度会提升\n")
                                }
                                if (isEmpty()) {
                                    append("继续保持这个状态！")
                                }
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Score Factor Card Component
 *
 * Displays a single scoring factor with progress bar and description.
 * Animated reveal with child-friendly design.
 *
 * @param icon Icon for the factor
 * @param title Title of the factor
 * @param value Display value (e.g., "83%", "2 次")
 * @param progress Progress value (0-1) for the progress bar
 * @param maxValue Maximum value for progress bar
 * @param color Color based on performance
 * @param description Encouraging message
 * @param isGood Whether the performance is good
 */
@Composable
private fun ScoreFactorCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    progress: () -> Float,
    maxValue: Float,
    color: Color,
    description: String,
    isGood: Boolean,
) {
    val currentProgress = progress()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isGood) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    },
            ),
        shape = RoundedCornerShape(16.dp),
        border =
            if (isGood) {
                null
            } else {
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                )
            },
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Title row with icon and value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = title,
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
                Text(
                    text = value,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = color,
                )
            }

            // Progress bar
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                        )
                        .clip(RoundedCornerShape(6.dp)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(currentProgress)
                            .fillMaxHeight()
                            .background(
                                brush =
                                    Brush.horizontalGradient(
                                        colors =
                                            listOf(
                                                color,
                                                color.copy(alpha = 0.7f),
                                            ),
                                    ),
                                shape = RoundedCornerShape(6.dp),
                            ),
                )
            }

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
    }
}

/**
 * Calculate time score for progress bar
 *
 * @param seconds Time taken in seconds
 * @return Score percentage (0-100)
 */
private fun calculateTimeScore(seconds: Int): Int {
    return when {
        seconds < 30 -> 100 // Fast: 100%
        seconds < 60 -> 80 // Good: 80%
        seconds < 90 -> 60 // Okay: 60%
        else -> 40 // Slow: 40%
    }
}

/**
 * Calculate error score for progress bar
 *
 * @param errors Number of errors
 * @return Score percentage (0-100)
 */
private fun calculateErrorScore(errors: Int): Int {
    return when {
        errors == 0 -> 100 // Perfect: 100%
        errors <= 2 -> 70 // Good: 70%
        errors <= 4 -> 50 // Okay: 50%
        else -> 30 // Too many: 30%
    }
}

/**
 * Calculate hint score for progress bar
 *
 * @param hints Number of hints used
 * @return Score percentage (0-100)
 */
private fun calculateHintScore(hints: Int): Int {
    return when {
        hints == 0 -> 100 // No hints: 100%
        hints <= 2 -> 70 // Few hints: 70%
        hints <= 4 -> 50 // Some hints: 50%
        else -> 30 // Many hints: 30%
    }
}

/**
 * Format time in child-friendly way
 *
 * @param seconds Time in seconds
 * @return Formatted time string
 */
private fun formatTime(seconds: Int): String {
    return if (seconds < 60) {
        "${seconds}秒"
    } else {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        if (remainingSeconds == 0) {
            "${minutes}分钟"
        } else {
            "${minutes}分${remainingSeconds}秒"
        }
    }
}
