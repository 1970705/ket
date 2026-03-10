@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
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
import com.wordland.domain.model.FillBlankQuestion
import com.wordland.domain.model.SpellBattleQuestion
import com.wordland.ui.components.ComboIndicator
import com.wordland.ui.components.LevelProgressBarEnhanced
import com.wordland.ui.components.StarRatingDisplay
import com.wordland.ui.components.TTSSpeakerButtonEmoji
import com.wordland.ui.uistate.LearningUiState
import com.wordland.ui.viewmodel.LearningViewModel

/**
 * Fill Blank Screen for Onboarding
 *
 * Features:
 * - First letter pre-filled (non-editable hint)
 * - Letter pool with scrambled letters + distractors
 * - Instant visual feedback on letter selection
 * - Progress bar with combo indicator
 * - Material Design 3 styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillBlankScreen(
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
    viewModel: LearningViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Track answer state for fill blank game
    var currentAnswer by remember { mutableStateOf("") }
    var usedLetterIndices by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(levelId, islandId) {
        viewModel.loadLevel(levelId, islandId)
    }

    // Reset answer when moving to next word
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LearningUiState.Ready -> {
                currentAnswer = ""
                usedLetterIndices = emptySet()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            FillBlankAppBar(
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
                    val fillBlankQuestion =
                        remember(state.question.wordId) {
                            state.question.toFillBlankQuestion()
                        }

                    FillBlankContent(
                        question = fillBlankQuestion,
                        currentAnswer = currentAnswer,
                        usedLetterIndices = usedLetterIndices,
                        onLetterSelected = { letter, index ->
                            currentAnswer += letter
                            usedLetterIndices += index
                        },
                        onBackspace = {
                            if (currentAnswer.isNotEmpty()) {
                                currentAnswer = currentAnswer.dropLast(1)
                                // Remove the last index from usedLetterIndices
                                val lastIndex = usedLetterIndices.last()
                                usedLetterIndices = usedLetterIndices - lastIndex
                            }
                        },
                        onSubmit = {
                            viewModel.submitAnswer(
                                userAnswer =
                                    fillBlankQuestion.targetWord.substring(0, currentAnswer.length) +
                                        fillBlankQuestion.targetWord.substring(currentAnswer.length),
                                responseTime = 3000L,
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
                    FillBlankFeedbackContent(
                        result = state.result,
                        stars = state.stars,
                        progress = state.progress,
                        comboState = state.comboState,
                        onContinue = {
                            currentAnswer = ""
                            usedLetterIndices = emptySet()
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
private fun FillBlankAppBar(
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
private fun FillBlankContent(
    question: FillBlankQuestion,
    currentAnswer: String,
    usedLetterIndices: Set<Int>,
    onLetterSelected: (Char, Int) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    comboState: ComboState = ComboState(),
    currentWordIndex: Int = 0,
    totalWords: Int = 6,
    modifier: Modifier = Modifier,
) {
    val isComplete = currentAnswer.length >= question.targetWord.length - 1

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

        // Question card
        QuestionCard(
            translation = question.translation,
            targetWord = question.targetWord,
            firstLetter = question.firstLetterHint,
            emoji = question.emoji,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Answer box with letter slots
        AnswerBox(
            targetWord = question.targetWord,
            firstLetter = question.firstLetterHint,
            currentAnswer = currentAnswer,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Letter pool grid
        LetterPool(
            letters = question.letterPool,
            usedIndices = usedLetterIndices,
            onLetterClick = onLetterSelected,
            isEnabled = !isComplete,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Backspace button
            OutlinedButton(
                onClick = onBackspace,
                modifier = Modifier.weight(1f),
                enabled = currentAnswer.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("删除")
            }

            // Submit button
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                enabled = isComplete,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("提交", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun QuestionCard(
    translation: String,
    targetWord: String,
    firstLetter: String,
    emoji: String?,
) {
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
            emoji?.let { emoji ->
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.displayLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Translation with speaker button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = translation,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.width(8.dp))

                TTSSpeakerButtonEmoji(
                    text = targetWord,
                    ttsController = AppServiceLocator.ttsController,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

@Composable
private fun AnswerBox(
    targetWord: String,
    firstLetter: String,
    currentAnswer: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border =
            BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            // First letter (pre-filled, non-editable)
            LetterSlot(
                letter = firstLetter.uppercase(),
                isFilled = true,
                isHint = true,
            )

            Spacer(modifier = Modifier.width(8.dp))

            // User answer slots
            val totalSlots = targetWord.length - 1
            repeat(totalSlots) { index ->
                val isFilled = index < currentAnswer.length
                val letter = if (isFilled) currentAnswer[index].toString() else ""

                LetterSlot(
                    letter = letter,
                    isFilled = isFilled,
                    isHint = false,
                )

                if (index < totalSlots - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LetterSlot(
    letter: String,
    isFilled: Boolean,
    isHint: Boolean,
) {
    val backgroundColor by animateColorAsState(
        targetValue =
            when {
                isHint -> MaterialTheme.colorScheme.primaryContainer
                isFilled -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor",
    )

    val borderColor by animateColorAsState(
        targetValue =
            when {
                isHint -> MaterialTheme.colorScheme.primary
                isFilled -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            },
        animationSpec = tween(durationMillis = 300),
        label = "borderColor",
    )

    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1.05f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "scale",
    )

    Box(
        modifier =
            Modifier
                .size(48.dp)
                .scale(scale)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(8.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isFilled) letter.uppercase() else "",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (isHint) FontWeight.Bold else FontWeight.Normal,
            color =
                when {
                    isHint -> MaterialTheme.colorScheme.onPrimaryContainer
                    isFilled -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

@Composable
private fun LetterPool(
    letters: List<Char>,
    usedIndices: Set<Int>,
    onLetterClick: (Char, Int) -> Unit,
    isEnabled: Boolean,
) {
    val (rows, cols) =
        when {
            letters.size <= 6 -> 2 to 3
            letters.size <= 12 -> 3 to 4
            else -> 4 to 4
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Text(
            text = "选择缺失的字母",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Letter grid
        val chunks = letters.chunked(cols)
        chunks.forEachIndexed { chunkIndex, rowLetters ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rowLetters.forEachIndexed { rowIndex, letter ->
                    val index = chunkIndex * cols + rowIndex
                    val isUsed = index in usedIndices

                    LetterButton(
                        letter = letter,
                        isUsed = isUsed,
                        isEnabled = isEnabled && !isUsed,
                        onClick = { onLetterClick(letter, index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterButton(
    letter: Char,
    isUsed: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue =
            when {
                !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                isUsed -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primaryContainer
            },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor",
    )

    val textColor by animateColorAsState(
        targetValue =
            when {
                !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                isUsed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            },
        animationSpec = tween(durationMillis = 300),
        label = "textColor",
    )

    val scale by animateFloatAsState(
        targetValue = if (isUsed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "scale",
    )

    Box(
        modifier =
            Modifier
                .size(56.dp)
                .scale(scale)
                .background(
                    color = backgroundColor,
                    shape = CircleShape,
                )
                .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter.uppercaseChar().toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

@Composable
private fun FillBlankFeedbackContent(
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
 * Extension function to convert SpellBattleQuestion to FillBlankQuestion
 * This is a temporary bridge until we have proper fill blank question generation
 */
private fun SpellBattleQuestion.toFillBlankQuestion(): FillBlankQuestion {
    val targetWordUpper = targetWord.trim().uppercase()
    val firstLetter = targetWordUpper.first().toString()
    val remainingLetters = targetWordUpper.drop(1).toList()

    // Generate distractors (2 random letters not in the word)
    val allLetters = ('A'..'Z').toList()
    val targetLetters = targetWordUpper.toSet()
    val distractors =
        allLetters
            .filter { it !in targetLetters }
            .shuffled()
            .take(2)

    // Create letter pool: correct letters + distractors, shuffled
    val letterPool = (remainingLetters + distractors).shuffled()

    return FillBlankQuestion(
        wordId = wordId,
        translation = translation,
        targetWord = targetWordUpper,
        firstLetterHint = firstLetter,
        letterPool = letterPool,
        difficulty = difficulty,
        emoji = getEmojiForWord(targetWord),
    )
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
