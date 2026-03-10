@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wordland.ui.theme.SurfaceLight

@Composable
fun WordlandCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    backgroundColor: Color = SurfaceLight,
    borderColor: Color? = null,
    elevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = { if (enabled) onClick() },
            modifier = modifier,
            enabled = enabled,
            colors =
                CardDefaults.cardColors(
                    containerColor = backgroundColor,
                ),
            border =
                borderColor?.let {
                    BorderStroke(1.dp, it)
                },
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = elevation,
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    } else {
        Card(
            modifier = modifier,
            colors =
                CardDefaults.cardColors(
                    containerColor = backgroundColor,
                ),
            border =
                borderColor?.let {
                    BorderStroke(1.dp, it)
                },
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = elevation,
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    }
}

@Composable
fun IslandCard(
    islandName: String,
    islandColor: Color,
    masteryPercentage: Float,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WordlandCard(
        modifier =
            modifier
                .fillMaxWidth()
                .height(200.dp),
        onClick = onClick,
        enabled = isUnlocked,
        backgroundColor =
            if (isUnlocked) {
                islandColor.copy(alpha = 0.1f)
            } else {
                Color(0xFF808080).copy(alpha = 0.1f)
            },
        borderColor =
            if (isUnlocked) {
                islandColor
            } else {
                Color(0xFF808080)
            },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = islandName,
                style = MaterialTheme.typography.headlineMedium,
                color = if (isUnlocked) islandColor else Color(0xFF808080),
            )

            if (isUnlocked) {
                // Mastery progress
                CircularProgress(
                    percentage = masteryPercentage,
                    color = islandColor,
                    size = 80.dp,
                )

                Text(
                    text = "${masteryPercentage.toInt()}% 掌握",
                    style = MaterialTheme.typography.bodyMedium,
                    color = islandColor,
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF808080),
                )
                Text(
                    text = "未解锁",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF808080),
                )
            }
        }
    }
}

@Composable
fun LevelCard(
    levelName: String,
    stars: Int,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WordlandCard(
        modifier =
            modifier
                .fillMaxWidth()
                .height(120.dp),
        onClick = onClick,
        enabled = isUnlocked,
        borderColor =
            if (isUnlocked) {
                com.wordland.ui.theme.PrimaryLight
            } else {
                Color(0xFF808080)
            },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.titleLarge,
                    color =
                        if (isUnlocked) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            Color(0xFF808080)
                        },
                )

                if (!isUnlocked) {
                    Text(
                        text = "完成上一关卡解锁",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF808080),
                    )
                }
            }

            if (isUnlocked) {
                StarsDisplay(stars = stars)
            } else {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF808080),
                )
            }
        }
    }
}

@Composable
fun CircularProgress(
    percentage: Float,
    color: Color,
    size: Dp,
) {
    // Simple circular progress indicator
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        // Background circle
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(size),
        ) {
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = size.toPx() / 2,
            )
        }

        // Progress arc (simplified)
        androidx.compose.material3.CircularProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = 6.dp,
        )
    }
}

@Composable
fun StarsDisplay(
    stars: Int,
    modifier: Modifier = Modifier,
    starColor: Color = com.wordland.ui.theme.AccentOrange,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(3) { index ->
            Icon(
                imageVector =
                    if (index < stars) {
                        Icons.Filled.Star
                    } else {
                        Icons.Outlined.Star
                    },
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
