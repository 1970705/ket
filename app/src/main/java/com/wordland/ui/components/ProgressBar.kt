package com.wordland.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wordland.ui.theme.PrimaryLight

@Composable
fun WordlandProgressBar(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    color: Color = PrimaryLight,
    backgroundColor: Color = Color(0xFFCCCCCC).copy(alpha = 0.3f),
    height: Dp = 12.dp,
    showPercentage: Boolean = false,
    label: String? = null,
) {
    Column(
        modifier = modifier,
    ) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (showPercentage) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(height / 2),
                    ),
            color = color,
        )
    }
}

@Composable
fun MemoryStrengthBar(
    strength: Int, // 0 to 100
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "记忆强度",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "$strength/100",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryLight,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = strength.coerceIn(0, 100) / 100f,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        color = Color(0xFFCCCCCC).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                    ),
            color =
                when {
                    strength >= 80 -> Color(0xFF4CAF50) // Green
                    strength >= 50 -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFF44336) // Red
                },
        )
    }
}

@Composable
fun LevelProgressIndicator(
    currentLevel: Int,
    totalLevels: Int,
    modifier: Modifier = Modifier,
) {
    val progress = currentLevel.toFloat() / totalLevels.toFloat()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "关卡 $currentLevel / $totalLevels",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        WordlandProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            showPercentage = true,
        )
    }
}
