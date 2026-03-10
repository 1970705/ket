package com.wordland.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Game mode selector dialog
 * Allows users to choose between different game modes for a level
 */
@Composable
fun GameModeSelector(
    levelName: String,
    levelId: String,
    islandId: String,
    onDismiss: () -> Unit,
    onStartSpellBattle: (String, String) -> Unit,
    onStartQuickJudge: (String, String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "选择游戏模式",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Level name
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Divider()

                // Game mode options
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Spell Battle Mode
                    GameModeCard(
                        title = "拼写战斗",
                        description = "看中文，拼写英文单词",
                        icon = "⚔️",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            onStartSpellBattle(levelId, islandId)
                            onDismiss()
                        },
                    )

                    // Quick Judge Mode
                    GameModeCard(
                        title = "快速判断",
                        description = "判断翻译是否正确（限时5秒）",
                        icon = "⚡",
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            onStartQuickJudge(levelId, islandId)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

/**
 * Individual game mode card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameModeCard(
    title: String,
    description: String,
    icon: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f),
            ),
        border = BorderStroke(2.dp, color),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Icon
            Text(
                text = icon,
                style = MaterialTheme.typography.displayLarge,
            )

            // Text
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Arrow indicator
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = color,
            )
        }
    }
}
