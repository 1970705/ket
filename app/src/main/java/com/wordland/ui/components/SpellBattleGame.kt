package com.wordland.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.SpellBattleQuestion

/**
 * Spell Battle Game UI (Performance Optimized)
 *
 * Optimizations:
 * - Stable parameters for composable functions
 * - remember blocks for expensive calculations
 * - key() for LazyColumn items
 * - Minimal recomposition scope
 */
@Composable
fun SpellBattleGame(
    question: SpellBattleQuestion,
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Translation display with speaker button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = question.translation,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.width(8.dp))
            TTSSpeakerButtonEmoji(
                text = question.targetWord,
                ttsController = AppServiceLocator.ttsController,
                modifier = Modifier.size(48.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "用英语拼写这个词",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Answer display boxes (optimized)
        AnswerBoxes(
            targetWord = question.targetWord,
            userAnswer = userAnswer,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Virtual keyboard (optimized)
        VirtualKeyboard(
            onKeyPress = { key ->
                if (key == "BACKSPACE") {
                    onBackspace()
                } else {
                    onAnswerChange(userAnswer + key)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Answer boxes with performance optimizations
 *
 * Key optimizations:
 * - Remember wrong positions calculation
 * - Derived state for character display
 * - Stable keys for item rendering
 */
@Composable
private fun AnswerBoxes(
    targetWord: String,
    userAnswer: String,
    modifier: Modifier = Modifier,
) {
    // Cache wrong positions calculation - only recalculate when inputs change
    val wrongPositions =
        remember(targetWord, userAnswer) {
            val question =
                SpellBattleQuestion(
                    wordId = "",
                    translation = "",
                    targetWord = targetWord,
                    hint = null,
                )
            question.getWrongPositions(userAnswer)
        }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Use key() to help Compose track individual boxes
        for (i in targetWord.indices) {
            key(i) {
                val char =
                    if (i < userAnswer.length) {
                        userAnswer[i].uppercaseChar().toString()
                    } else {
                        ""
                    }

                val isWrong = i in wrongPositions
                val isFilled = i < userAnswer.length

                AnswerBox(
                    char = char,
                    isWrong = isWrong,
                    isFilled = isFilled,
                )
            }
        }
    }
}

/**
 * Individual answer box (extracted for better recomposition)
 */
@Composable
private fun AnswerBox(
    char: String,
    isWrong: Boolean,
    isFilled: Boolean,
) {
    val backgroundColor =
        when {
            isWrong -> MaterialTheme.colorScheme.errorContainer
            isFilled -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

    val borderColor =
        when {
            isWrong -> MaterialTheme.colorScheme.error
            isFilled -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        }

    val textColor =
        when {
            isWrong -> MaterialTheme.colorScheme.error
            isFilled -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Box(
        modifier =
            Modifier
                .size(48.dp)
                .padding(4.dp)
                .background(
                    color = backgroundColor,
                    shape = MaterialTheme.shapes.small,
                )
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = MaterialTheme.shapes.small,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char,
            fontSize = 24.sp,
            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}

/**
 * Virtual keyboard with performance optimizations
 *
 * Key optimizations:
 * - Static keyboard layout (remembered)
 * - Minimal state changes
 * - Efficient event handling
 */
@Composable
private fun VirtualKeyboard(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Remember keyboard layout - doesn't change
    val rows =
        remember {
            listOf(
                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                listOf("Z", "X", "C", "V", "B", "N", "M"),
            )
        }

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            KeyboardRow(
                keys = row,
                onKeyPress = onKeyPress,
                isLastRow = row == rows.last(),
            )
        }
    }
}

/**
 * Individual keyboard row (extracted for better recomposition control)
 */
@Composable
private fun KeyboardRow(
    keys: List<String>,
    onKeyPress: (String) -> Unit,
    isLastRow: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        keys.forEach { key ->
            key(key) {
                KeyboardKey(
                    key = key,
                    onClick = { onKeyPress(key) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Backspace key on last row
        if (isLastRow) {
            Spacer(modifier = Modifier.width(8.dp))
            KeyboardKey(
                key = "⌫",
                onClick = { onKeyPress("BACKSPACE") },
                modifier = Modifier.weight(1.5f),
            )
        }
    }
}

/**
 * Keyboard key with stable parameters
 */
@Composable
private fun KeyboardKey(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Custom clickable component for better performance than Button
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .height(48.dp)
                .padding(2.dp),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxHeight(),
        ) {
            Text(
                text = key,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
            )
        }
    }
}
