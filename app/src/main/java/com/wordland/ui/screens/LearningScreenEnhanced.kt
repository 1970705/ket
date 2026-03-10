@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.Word
import com.wordland.ui.components.*
import com.wordland.ui.components.HintLevel
import com.wordland.ui.uistate.LearningUiState
import com.wordland.ui.viewmodel.LearningViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced Learning Screen with Week 3 features:
 * - Audio playback integration
 * - 3-level hint system
 * - Answer validation with fuzzy matching
 * - Animated feedback
 * - Haptic feedback
 * - Response time tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreenEnhanced(
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
    viewModel: LearningViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var answerText by remember { mutableStateOf("") }
    var showFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(levelId, islandId) {
        viewModel.loadLevel(levelId, islandId)
    }

    // Hint system state
    var currentHintLevel by remember { mutableStateOf(HintLevel.None) }

    // Haptic feedback
    val hapticManager = remember { com.wordland.ui.components.HapticFeedbackManager(context) }

    Scaffold(
        topBar = {
            LearningAppBarEnhanced(
                levelId = levelId,
                onNavigateBack = onNavigateBack,
                onPlayAudio = {
                    // Play audio for current word
                    scope.launch {
                        // TODO: Integrate with AudioAssetManager
                        hapticManager.lightTap()
                    }
                },
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
                    LearningContentEnhanced(
                        question = state.question.translation, // Use translation for display
                        targetWord = state.question.targetWord, // Pass target word separately
                        answerText = answerText,
                        onAnswerChange = {
                            answerText = it
                            hapticManager.lightTap()
                        },
                        onSubmit = {
                            hapticManager.mediumTap()

                            scope.launch {
                                delay(100) // Brief delay for haptic
                                viewModel.submitAnswer(
                                    userAnswer = answerText,
                                    responseTime = 0, // Will be calculated in ViewModel
                                    hintUsed = state.hintShown,
                                )
                                showFeedback = true
                            }
                        },
                        hintLevel = currentHintLevel,
                        onUseHint = {
                            currentHintLevel =
                                when (currentHintLevel) {
                                    HintLevel.None -> HintLevel.Level1
                                    HintLevel.Level1 -> HintLevel.Level2
                                    HintLevel.Level2 -> HintLevel.Level3
                                    HintLevel.Level3 -> HintLevel.Level3 // Already at max hint
                                }
                            hapticManager.lightTap()
                            viewModel.useHint()
                        },
                        currentWord = viewModel.currentWord.collectAsState().value,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.Feedback -> {
                    FeedbackContentEnhanced(
                        result = state.result,
                        stars = state.stars,
                        progress = state.progress,
                        onContinue = {
                            showFeedback = false
                            answerText = ""
                            currentHintLevel = HintLevel.None
                            viewModel.onNextWord()
                            hapticManager.lightTap()
                        },
                        hapticManager = hapticManager,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.LevelComplete -> {
                    LevelCompleteContentEnhanced(
                        stars = state.stars,
                        score = state.score,
                        isNextIslandUnlocked = state.isNextIslandUnlocked,
                        islandMasteryPercentage = state.islandMasteryPercentage,
                        onBack = onNavigateBack,
                        hapticManager = hapticManager,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    hapticManager.errorPattern()
                }

                else -> {}
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            hapticManager.cancel()
        }
    }
}

@Composable
private fun LearningAppBarEnhanced(
    levelId: String,
    onNavigateBack: () -> Unit,
    onPlayAudio: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = getLevelDisplayName(levelId),
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
private fun LearningContentEnhanced(
    question: String,
    targetWord: String,
    answerText: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    hintLevel: HintLevel,
    onUseHint: () -> Unit,
    currentWord: Word?,
    modifier: Modifier = Modifier,
) {
    var hasUserStartedTyping by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Text(
                text = question,
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        // Hint system
        if (currentWord != null && hasUserStartedTyping) {
            HintCard(
                text = "",
                content = "",
                icon = null,
                alpha = 1f,
                scale = 1f,
            )
        }

        // Answer input
        OutlinedTextField(
            value = answerText,
            onValueChange = {
                onAnswerChange(it)
                if (!hasUserStartedTyping && it.isNotEmpty()) {
                    hasUserStartedTyping = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("输入答案...") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!hasUserStartedTyping) {
                // Show hint button before typing starts
                WordlandOutlinedButton(
                    onClick = onUseHint,
                    modifier = Modifier.weight(1f),
                    text = "提示 💡",
                    size = com.wordland.ui.components.ButtonSize.SMALL,
                )
            }

            WordlandButton(
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                text = "提交",
                enabled = answerText.isNotBlank(),
                size = com.wordland.ui.components.ButtonSize.LARGE,
            )
        }
    }
}

@Composable
private fun FeedbackContentEnhanced(
    result: com.wordland.domain.model.LearnWordResult,
    stars: Int,
    progress: Float,
    onContinue: () -> Unit,
    hapticManager: HapticFeedbackManager,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        // Trigger haptic based on result
        if (result.isCorrect) {
            hapticManager.successPattern()
        } else {
            hapticManager.errorPattern()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Animated correct/incorrect indicator
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (result.isCorrect) {
                CorrectAnswerAnimation(
                    onAnimationComplete = {
                        // Animation done
                    },
                )
            } else {
                IncorrectAnswerAnimation(
                    onAnimationComplete = {
                        // Animation done
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback message
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Text(
                text = result.message,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        // Animated memory strength bar
        MemoryStrengthChangeAnimation(
            oldStrength =
                result.newMemoryStrength -
                    if (result.isCorrect) 10 else -15,
            // Approximate
            newStrength = result.newMemoryStrength,
            modifier = Modifier.fillMaxWidth(),
        )

        // Word details
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = result.word.word,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = result.word.translation,
                    style = MaterialTheme.typography.bodyLarge,
                )
                result.word.pronunciation?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Animated star display
        StarEarnedAnimation(
            stars = stars,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        // Continue button
        WordlandButton(
            onClick = {
                hapticManager.lightTap()
                onContinue()
            },
            modifier = Modifier.fillMaxWidth(),
            text = "继续",
            size = com.wordland.ui.components.ButtonSize.LARGE,
        )
    }
}

@Composable
private fun LevelCompleteContentEnhanced(
    stars: Int,
    score: Int,
    isNextIslandUnlocked: Boolean,
    islandMasteryPercentage: Double,
    onBack: () -> Unit,
    hapticManager: HapticFeedbackManager,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        hapticManager.levelComplete()
        if (isNextIslandUnlocked) {
            delay(500)
            hapticManager.islandUnlockCelebration()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Confetti background
        ConfettiEffect(
            particleCount = 150,
            durationMillis = 4000,
        )

        // Content overlay
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "🎉 关卡完成!",
                style = MaterialTheme.typography.displayMedium,
            )

            // Animated stars
            StarEarnedAnimation(
                stars = stars,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            // Score card
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "得分: $score",
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "岛屿掌握度: ${islandMasteryPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mastery progress bar
                    WordlandProgressBar(
                        progress = islandMasteryPercentage.toFloat() / 100f,
                        showPercentage = true,
                    )
                }
            }

            // Unlock notification
            if (isNextIslandUnlocked) {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "🔓",
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Text(
                            text = "新岛屿解锁!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                // Celebration burst
                CelebrationBurst(
                    centerX = 500f,
                    centerY = 500f,
                    particleCount = 80,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Back button
            WordlandButton(
                onClick = {
                    hapticManager.lightTap()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                text = "返回",
                size = com.wordland.ui.components.ButtonSize.LARGE,
            )
        }
    }
}
