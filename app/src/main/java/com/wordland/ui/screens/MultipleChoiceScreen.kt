@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.MultipleChoiceQuestion
import com.wordland.domain.model.SpellBattleQuestion
import com.wordland.ui.components.ComboIndicator
import com.wordland.ui.components.LevelProgressBarEnhanced
import com.wordland.ui.components.StarRatingDisplay
import com.wordland.ui.components.TTSSpeakerButtonEmoji
import com.wordland.ui.uistate.LearningUiState
import com.wordland.ui.viewmodel.LearningViewModel

/**
 * Multiple Choice Screen for Onboarding
 * Designed for 100% success rate with rich visual feedback
 *
 * Features:
 * - 4 options with emoji indicators
 * - Instant visual feedback on selection
 * - Particle effects and animations
 * - Combo indicator for streaks
 * - Progress tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceScreen(
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
    viewModel: LearningViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(levelId, islandId) {
        viewModel.loadLevel(levelId, islandId)
    }

    Scaffold(
        topBar = {
            MultipleChoiceAppBar(
                levelId = levelId,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when (val state = uiState) {
                is LearningUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.Ready -> {
                    MultipleChoiceContent(
                        question = state.question.toMultipleChoiceQuestion(),
                        onAnswerSelected = { selectedAnswer ->
                            viewModel.submitAnswer(
                                userAnswer = selectedAnswer,
                                responseTime = 3000L, // Fixed for multiple choice
                                hintUsed = false,
                            )
                        },
                        comboState = state.comboState,
                        currentWordIndex = state.currentWordIndex,
                        totalWords = state.totalWords,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.Feedback -> {
                    MultipleChoiceFeedbackContent(
                        result = state.result,
                        stars = state.stars,
                        progress = state.progress,
                        comboState = state.comboState,
                        onContinue = {
                            viewModel.onNextWord()
                        },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.LevelComplete -> {
                    LevelCompleteContent(
                        stars = state.stars,
                        score = state.score,
                        isNextIslandUnlocked = state.isNextIslandUnlocked,
                        islandMasteryPercentage = state.islandMasteryPercentage,
                        onBack = onNavigateBack,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun MultipleChoiceAppBar(
    levelId: String,
    onNavigateBack: () -> Unit,
) {
    val displayTitle = remember(levelId) { getLevelDisplayName(levelId) }

    TopAppBar(
        title = {
            Text(
                text = displayTitle,
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
private fun MultipleChoiceContent(
    question: MultipleChoiceQuestion,
    onAnswerSelected: (String) -> Unit,
    comboState: ComboState = ComboState(),
    currentWordIndex: Int = 0,
    totalWords: Int = 6,
    modifier: Modifier = Modifier,
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Progress bar with combo indicator
        LevelProgressBarEnhanced(
            currentWord = currentWordIndex,
            totalWords = totalWords,
            comboState = comboState,
            showCombo = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Question card with emoji and speaker button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Emoji
                question.emoji?.let { emoji ->
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Question (Chinese translation) with speaker button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = question.translation,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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

                // Prompt
                Text(
                    text = "选择正确的英文单词",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Options grid (2x2)
        OptionGrid(
            options = question.options,
            selectedIndex = selectedOption,
            onOptionClick = { index ->
                if (!hasAnswered) {
                    selectedOption = index
                    hasAnswered = true
                    onAnswerSelected(question.options[index])
                }
            },
            enabled = !hasAnswered,
        )
    }
}

@Composable
private fun OptionGrid(
    options: List<String>,
    selectedIndex: Int?,
    onOptionClick: (Int) -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Split into 2 rows of 2 options
        val rows = options.chunked(2)

        rows.forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowOptions.forEach { option ->
                    val index = options.indexOf(option)
                    OptionButton(
                        text = option,
                        selected = selectedIndex == index,
                        enabled = enabled,
                        onClick = { onOptionClick(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue =
            when {
                !enabled && selected -> MaterialTheme.colorScheme.primary
                selected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor",
    )

    val textColor by animateColorAsState(
        targetValue =
            when {
                !enabled && selected -> MaterialTheme.colorScheme.onPrimary
                selected -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
        animationSpec = tween(durationMillis = 300),
        label = "textColor",
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "scale",
    )

    Card(
        modifier =
            modifier
                .height(80.dp)
                .scale(scale)
                .clickable(enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
        border =
            if (selected) {
                BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            },
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (selected) 8.dp else 2.dp,
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MultipleChoiceFeedbackContent(
    result: com.wordland.domain.model.LearnWordResult,
    stars: Int,
    progress: Float,
    comboState: ComboState = ComboState(),
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Success/Error icon
        Text(
            text = if (result.isCorrect) "✅" else "❌",
            style = MaterialTheme.typography.displayLarge,
        )

        // Feedback message
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (result.isCorrect) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                ),
        ) {
            Text(
                text = result.message,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }

        // Combo indicator if active
        if (comboState.isActive) {
            ComboIndicator(
                comboState = comboState,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        // Stars earned
        StarRatingDisplay(
            stars = stars,
            starSize = 40.dp,
        )

        // Word details
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = result.word.word,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = result.word.translation,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        // Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "继续",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

/**
 * Extension function to convert SpellBattleQuestion to MultipleChoiceQuestion
 * This is a temporary bridge until we have proper multiple choice question generation
 */
private fun SpellBattleQuestion.toMultipleChoiceQuestion(): MultipleChoiceQuestion {
    // For now, generate simple distractors
    // In production, these should come from the database or be generated intelligently
    val distractors = generateDistractors(targetWord)

    return MultipleChoiceQuestion(
        wordId = wordId,
        translation = translation,
        targetWord = targetWord,
        options = shuffleOptions(listOf(targetWord) + distractors),
        difficulty = difficulty,
        emoji = getEmojiForWord(targetWord),
    )
}

/**
 * Generate distractor words for multiple choice
 * TODO: Replace with database lookup or intelligent generation
 */
private fun generateDistractors(correctWord: String): List<String> {
    // Simple distractor generation based on word length
    val allWords =
        listOf(
            "dog", "cat", "bird", "fish", "apple", "banana", "orange", "eye",
            "look", "see", "watch", "find", "color", "red", "blue",
        )

    return allWords
        .filter { it != correctWord }
        .shuffled()
        .take(3)
}

/**
 * Shuffle options and ensure correct answer is included
 */
private fun shuffleOptions(options: List<String>): List<String> {
    return options.shuffled()
}

/**
 * Get emoji for word (for visual appeal)
 * TODO: Replace with database mapping
 */
private fun getEmojiForWord(word: String): String? {
    return when (word.lowercase()) {
        "cat" -> "🐱"
        "dog" -> "🐕"
        "bird" -> "🐦"
        "fish" -> "🐟"
        "apple" -> "🍎"
        "banana" -> "🍌"
        "orange" -> "🍊"
        "eye" -> "👁️"
        "look", "see", "watch" -> "👀"
        "color" -> "🎨"
        "red" -> "🔴"
        "blue" -> "🔵"
        "find" -> "🔍"
        else -> null
    }
}
