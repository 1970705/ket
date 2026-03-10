@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.AnimationQuality
import com.wordland.domain.model.FeedbackType
import com.wordland.domain.model.SpellBattleQuestion
import com.wordland.ui.components.ComboIndicator
import com.wordland.ui.components.CompactPetAnimation
import com.wordland.ui.components.HintCard
import com.wordland.ui.components.LevelProgressBarEnhanced
import com.wordland.ui.components.MemoryStrengthBar
import com.wordland.ui.components.SpellBattleGame
import com.wordland.ui.components.StarRatingDisplay
import com.wordland.ui.components.TTSSpeakerButtonEmoji
import com.wordland.ui.components.WordCompletionFeedback
import com.wordland.ui.components.WordlandButton
import com.wordland.ui.uistate.LearningUiState
import com.wordland.ui.viewmodel.LearningViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
    onViewStarBreakdown: (
        stars: Int,
        accuracy: Int,
        hintsUsed: Int,
        timeTaken: Int,
        errorCount: Int,
    ) -> Unit = { _, _, _, _, _ -> },
    viewModel: LearningViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()
    val petAnimationState by viewModel.petAnimationState.collectAsState()
    var answerText by remember { mutableStateOf("") }

    // Track question start time for response time calculation
    // This is critical for Combo system to work correctly (anti-guessing logic)
    var questionStartTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(levelId, islandId) {
        viewModel.loadLevel(levelId, islandId)
    }

    // Record start time when a new question is shown
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LearningUiState.Ready -> {
                if (questionStartTime == 0L) {
                    questionStartTime = System.currentTimeMillis()
                }
            }
            is LearningUiState.Feedback, is LearningUiState.LevelComplete -> {
                // Reset for next question
                questionStartTime = 0L
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            LearningAppBar(
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
            // Pet animation in top-right corner (non-intrusive)
            if (selectedPet != null) {
                CompactPetAnimation(
                    pet = selectedPet!!,
                    animationState = petAnimationState,
                    size = 48.sp,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                )
            }
            when (val state = uiState) {
                is LearningUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.Ready -> {
                    LearningContent(
                        question = state.question,
                        answerText = answerText,
                        onAnswerChange = { answerText = it },
                        onSubmit = {
                            // Calculate actual response time in milliseconds
                            // This is critical for Combo system anti-guessing logic
                            val responseTime =
                                if (questionStartTime > 0) {
                                    System.currentTimeMillis() - questionStartTime
                                } else {
                                    0L
                                }
                            viewModel.submitAnswer(
                                userAnswer = answerText,
                                responseTime = responseTime,
                                hintUsed = state.hintShown,
                            )
                        },
                        hintText = state.hintText,
                        hintLevel = state.hintLevel,
                        hintsRemaining = state.hintsRemaining,
                        hintPenaltyApplied = state.hintPenaltyApplied,
                        onUseHint = { viewModel.useHint() },
                        comboState = state.comboState,
                        currentWordIndex = state.currentWordIndex,
                        totalWords = state.totalWords,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is LearningUiState.Feedback -> {
                    FeedbackContent(
                        result = state.result,
                        stars = state.stars,
                        progress = state.progress,
                        comboState = state.comboState,
                        onContinue = {
                            answerText = ""
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
                        maxCombo = state.maxCombo,
                        onBack = onNavigateBack,
                        onViewStarBreakdown = {
                            onViewStarBreakdown(
                                state.stars,
                                state.accuracy,
                                state.hintsUsed,
                                state.timeTaken,
                                state.errorCount,
                            )
                        },
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
private fun LearningAppBar(
    levelId: String,
    onNavigateBack: () -> Unit,
) {
    // Memoize level display name to avoid recalculation on recomposition
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
private fun LearningContent(
    question: SpellBattleQuestion,
    answerText: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    hintText: String?,
    hintLevel: Int,
    hintsRemaining: Int,
    hintPenaltyApplied: Boolean,
    onUseHint: () -> Unit,
    comboState: com.wordland.domain.model.ComboState = com.wordland.domain.model.ComboState(),
    currentWordIndex: Int = 0,
    totalWords: Int = 6,
    modifier: Modifier = Modifier,
) {
    // Memoize backspace callback to avoid recreating lambda on recomposition
    val onBackspace =
        remember(answerText, onAnswerChange) {
            {
                // Handle backspace - remove last character
                if (answerText.isNotEmpty()) {
                    onAnswerChange(answerText.dropLast(1))
                }
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .padding(
                    bottom =
                        WindowInsets.systemBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp), // Reduced from 16.dp to 12.dp
    ) {
        // Enhanced progress bar with combo indicator and motivational messages
        LevelProgressBarEnhanced(
            currentWord = currentWordIndex,
            totalWords = totalWords,
            comboState = comboState,
            showCombo = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Enhanced hint card with progressive hints
        HintCard(
            hintText = hintText,
            hintLevel = hintLevel,
            hintsRemaining = hintsRemaining,
            hintPenaltyApplied = hintPenaltyApplied,
            onUseHint = onUseHint,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Word translation with TTS pronunciation button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = question.translation,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(8.dp))

            TTSSpeakerButtonEmoji(
                text = question.targetWord,
                ttsController = AppServiceLocator.ttsController,
                modifier = Modifier.size(48.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Spell Battle Game
        SpellBattleGame(
            question = question,
            userAnswer = answerText,
            onAnswerChange = onAnswerChange,
            onBackspace = onBackspace,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit button (full width since hint is in the card)
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = answerText.isNotEmpty(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "提交答案",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun FeedbackContent(
    result: com.wordland.domain.model.LearnWordResult,
    stars: Int,
    progress: Float,
    comboState: com.wordland.domain.model.ComboState = com.wordland.domain.model.ComboState(),
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Determine feedback type based on result
    val feedbackType =
        remember(result.isCorrect, stars) {
            when {
                !result.isCorrect -> FeedbackType.Incorrect
                stars == 3 -> FeedbackType.PerfectThreeStar
                stars == 2 -> FeedbackType.GoodTwoStar
                else -> FeedbackType.CorrectOneStar
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Visual feedback animation
        WordCompletionFeedback(
            feedbackType = feedbackType,
            quality = AnimationQuality.Medium,
            modifier = Modifier.fillMaxWidth(),
        )

        // Combo indicator if active
        if (comboState.isActive) {
            ComboIndicator(
                comboState = comboState,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

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
            )
        }

        // Memory strength display
        MemoryStrengthBar(
            strength = result.newMemoryStrength,
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

        // Stars earned
        StarRatingDisplay(
            stars = stars,
            starSize = 32.dp,
        )

        WordlandButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            text = "继续",
            size = com.wordland.ui.components.ButtonSize.LARGE,
        )
    }
}

@Composable
internal fun LevelCompleteContent(
    stars: Int,
    score: Int,
    isNextIslandUnlocked: Boolean,
    islandMasteryPercentage: Double,
    maxCombo: Int = 0,
    onBack: () -> Unit,
    onViewStarBreakdown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Animation state for star reveal
    var animationPlayed by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Animate each star appearing with a delay
    // Ensure at least one animation for edge cases
    val starAnimations =
        remember(stars) {
            List(stars.coerceAtLeast(1)) { index ->
                Animatable(0f)
            }
        }

    // Trigger star animations
    LaunchedEffect(stars) {
        animationPlayed = true
        // Show confetti effect for perfect performance
        if (stars == 3) {
            showConfetti = true
        }
        starAnimations.forEachIndexed { index, animatable ->
            delay(index * 250L) // 250ms delay between each star
            animatable.animateTo(
                targetValue = 1f,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
            )
        }
    }

    // Scale animation for the entire card
    val cardScale by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0.8f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "card_scale",
    )

    // Rotate animation for perfect stars (3 stars only)
    val starRotation by animateFloatAsState(
        targetValue = if (animationPlayed && stars == 3) 360f else 0f,
        animationSpec =
            tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing,
            ),
        label = "star_rotation",
    )

    // Performance message and emoji based on stars
    val performanceData =
        remember(stars) {
            when (stars) {
                3 -> Triple("🌟", "完美表现！", "全部正确，太棒了！")
                2 -> Triple("👍", "做得不错！", "继续努力，争取满分！")
                1 -> Triple("💪", "继续加油！", "多多练习会更好！")
                else -> Triple("🎯", "再试一次！", "相信你能做得更好！")
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Title with scale animation
        Text(
            text = "🎉 关卡完成!",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.scale(cardScale),
        )

        // Animated star rating with enhanced effects
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val isEarned = index < stars
                val scale =
                    if (isEarned && index < starAnimations.size) {
                        starAnimations[index].value
                    } else {
                        0.5f
                    }
                val alpha = if (isEarned) 1f else 0.2f
                val rotation = if (isEarned && stars == 3) starRotation else 0f

                // Glow effect container for earned stars
                Box(
                    modifier =
                        Modifier
                            .size(60.dp)
                            .scale(scale.coerceIn(0.5f, 1.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    // Outer glow for earned stars
                    if (isEarned) {
                        Box(
                            modifier =
                                Modifier
                                    .size(68.dp)
                                    .alpha(alpha * 0.4f)
                                    .background(
                                        color =
                                            MaterialTheme.colorScheme.tertiary.copy(
                                                alpha = 0.5f,
                                            ),
                                        shape = RoundedCornerShape(34.dp),
                                    ),
                        )
                    }

                    // Star icon
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = if (isEarned) "获得星星" else "未获得",
                        tint =
                            if (isEarned) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        modifier =
                            Modifier
                                .size(52.dp)
                                .scale(scale)
                                .alpha(alpha)
                                .then(
                                    if (isEarned && stars == 3) {
                                        Modifier.rotate(rotation)
                                    } else {
                                        Modifier
                                    },
                                ),
                    )
                }
            }
        }

        // Performance message card with slide-in animation
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .scale(cardScale),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        when (stars) {
                            3 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                            2 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                ),
            border =
                BorderStroke(
                    width = 2.dp,
                    color =
                        when (stars) {
                            3 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                            2 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Emoji and main message
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = performanceData.first,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = performanceData.second,
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color =
                            when (stars) {
                                3 -> MaterialTheme.colorScheme.tertiary
                                2 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }

                // Subtitle message
                Text(
                    text = performanceData.third,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Divider
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Score
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "$score",
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "得分",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Max Combo (if > 1)
                    if (maxCombo > 1) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "🔥 $maxCombo",
                                style =
                                    MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = "最大连击",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Island Mastery
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "${islandMasteryPercentage.toInt()}%",
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            color =
                                when {
                                    islandMasteryPercentage >= 80 ->
                                        MaterialTheme.colorScheme.tertiary
                                    islandMasteryPercentage >= 50 ->
                                        MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.primary
                                },
                        )
                        Text(
                            text = "掌握度",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Next island unlock message
        if (isNextIslandUnlocked) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                border =
                    BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                modifier = Modifier.scale(cardScale),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🔓",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "新岛屿解锁!",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }
        }

        // View Star Breakdown button
        WordlandButton(
            onClick = onViewStarBreakdown,
            modifier = Modifier.fillMaxWidth(),
            text = "查看星级详情",
            size = com.wordland.ui.components.ButtonSize.MEDIUM,
        )

        WordlandButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            text = "继续探险",
            size = com.wordland.ui.components.ButtonSize.LARGE,
        )
    }

    // Confetti effect for perfect performance (3 stars)
    if (showConfetti) {
        com.wordland.ui.components.ConfettiEffect(modifier = Modifier.fillMaxSize())
    }
}

@Composable
internal fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

fun getLevelDisplayName(levelId: String): String {
    return levelId.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
}
