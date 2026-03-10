package com.wordland.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.ChestReward
import com.wordland.domain.model.OnboardingUiState
import com.wordland.domain.model.getDisplayName
import com.wordland.domain.model.getEmoji
import com.wordland.ui.components.CelebrationBurst
import com.wordland.ui.components.ConfettiEffect
import kotlinx.coroutines.delay

/**
 * Onboarding Chest Screen - Full Animation Implementation
 *
 * Features:
 * - Animated chest with glow effect
 * - "点击开启" (Click to Open) button
 * - Chest opening animation (shake, scale, fade)
 * - Mystery reveal with particle effects
 * - "太酷了！" continue button
 *
 * Per ONBOARDING_EXPERIENCE_DESIGN.md Section 1.3:
 * - 5-10 second interaction time
 * - Mystery and delight moment
 * - Celebration on reward reveal
 */
@Composable
fun OnboardingChestScreen(
    viewModel: com.wordland.ui.viewmodel.OnboardingViewModel =
        viewModel(
            factory = AppServiceLocator.provideFactory(),
        ),
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Restore state when entering screen
    LaunchedEffect(Unit) {
        val onboardingState = viewModel.onboardingState.value
        if (uiState is com.wordland.domain.model.OnboardingUiState.Idle &&
            onboardingState != null &&
            onboardingState.currentPhase == com.wordland.domain.model.OnboardingPhase.FIRST_CHEST
        ) {
            // Phase is FIRST_CHEST but uiState is Idle, need to open chest
            viewModel.openChest()
        } else if (uiState is com.wordland.domain.model.OnboardingUiState.Idle) {
            // Otherwise, start onboarding to determine correct state
            viewModel.startOnboarding()
        }
    }

    // Chest opening state
    var chestState by remember { mutableStateOf(ChestAnimationState.CLOSED) }
    var showConfetti by remember { mutableStateOf(false) }
    var showReward by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFD700), // Gold
                                        Color(0xFFFFA500), // Orange
                                        MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            ),
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (val state = uiState) {
                    is OnboardingUiState.OpeningChest -> {
                        // Chest opening sequence
                        ChestOpeningSequence(
                            reward = state.reward,
                            chestState = chestState,
                            showConfetti = showConfetti,
                            showReward = showReward,
                            onChestClick = {
                                if (chestState == ChestAnimationState.CLOSED) {
                                    chestState = ChestAnimationState.OPENING
                                }
                            },
                            onAnimationComplete = {
                                chestState = ChestAnimationState.OPENED
                                showConfetti = true
                                showReward = true
                            },
                            onDismiss = onDismiss,
                        )
                    }
                    is OnboardingUiState.Completed -> {
                        // Already completed - show summary
                        CompletedSummary(
                            pet = state.pet,
                            wordsLearned = state.wordsLearned,
                            stars = state.stars,
                            onDismiss = onDismiss,
                        )
                    }
                    else -> {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * Chest animation states
 */
private enum class ChestAnimationState {
    CLOSED, // Initial state, showing closed chest
    OPENING, // Animation in progress
    OPENED, // Fully opened, showing reward
}

/**
 * Chest opening sequence with full animations
 */
@Composable
private fun ChestOpeningSequence(
    reward: ChestReward,
    chestState: ChestAnimationState,
    showConfetti: Boolean,
    showReward: Boolean,
    onChestClick: () -> Unit,
    onAnimationComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Trigger animation sequence when state changes to OPENING
    LaunchedEffect(chestState) {
        if (chestState == ChestAnimationState.OPENING) {
            // Wait for shake and open animations
            delay(1200)
            onAnimationComplete()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Title (fades out when chest opens)
            AnimatedTitle(chestState = chestState)

            // Chest with animations
            AnimatedChest(
                chestState = chestState,
            )

            // Reward reveal (only shown after opening)
            if (showReward) {
                RewardRevealCard(
                    reward = reward,
                    chestState = chestState,
                )
            }

            // Action button
            when (chestState) {
                ChestAnimationState.CLOSED -> {
                    // "点击开启" button with pulse animation
                    PulsingOpenButton(onClick = onChestClick)
                }
                ChestAnimationState.OPENED -> {
                    // "太酷了！" continue button
                    ContinueButton(onClick = onDismiss)
                }
                else -> {
                    // Loading during opening animation
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Confetti overlay (on top of everything)
        if (showConfetti) {
            ConfettiEffect(
                particleCount = 60,
                durationMillis = 2500,
                modifier = Modifier.fillMaxSize(),
            )

            // Center burst effect
            if (showReward) {
                CelebrationBurst(
                    centerX = 500f,
                    centerY = 800f,
                    particleCount = 40,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * Animated title that fades out during opening
 */
@Composable
private fun AnimatedTitle(chestState: ChestAnimationState) {
    val titleAlpha by animateFloatAsState(
        targetValue =
            when (chestState) {
                ChestAnimationState.CLOSED -> 1f
                ChestAnimationState.OPENING -> 0f
                ChestAnimationState.OPENED -> 0f
            },
        animationSpec =
            tween(
                durationMillis = if (chestState == ChestAnimationState.OPENING) 300 else 0,
                easing = FastOutSlowInEasing,
            ),
        label = "title_alpha",
    )

    val titleScale by animateFloatAsState(
        targetValue =
            when (chestState) {
                ChestAnimationState.CLOSED -> 1f
                else -> 0.8f
            },
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 200f,
            ),
        label = "title_scale",
    )

    Text(
        text = "🎁 你获得了神秘奖励！",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier =
            Modifier
                .alpha(titleAlpha)
                .scale(titleScale),
    )
}

/**
 * Animated chest with glow, shake, and opening animations
 */
@Composable
private fun AnimatedChest(chestState: ChestAnimationState) {
    // Glow animation (pulsing when closed)
    val infiniteTransition = rememberInfiniteTransition(label = "chest_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glow_pulse",
    )

    // Shake animation during opening
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(chestState) {
        if (chestState == ChestAnimationState.OPENING) {
            // Shake sequence: left-right-left-right
            shakeAnim.snapTo(-15f)
            delay(100)
            shakeAnim.animateTo(15f, tween(100))
            delay(100)
            shakeAnim.animateTo(-10f, tween(80))
            delay(80)
            shakeAnim.animateTo(10f, tween(80))
            delay(80)
            shakeAnim.animateTo(0f, tween(100))
        }
    }

    // Scale animation during opening
    val chestScale by animateFloatAsState(
        targetValue =
            when (chestState) {
                ChestAnimationState.CLOSED -> 1f
                ChestAnimationState.OPENING -> 1.3f
                ChestAnimationState.OPENED -> 0f // Fade out
            },
        animationSpec =
            when (chestState) {
                ChestAnimationState.OPENING ->
                    spring(
                        dampingRatio = 0.5f,
                        stiffness = 300f,
                    )
                ChestAnimationState.OPENED -> tween(300)
                else -> spring()
            },
        label = "chest_scale",
    )

    // Chest alpha
    val chestAlpha by animateFloatAsState(
        targetValue =
            when (chestState) {
                ChestAnimationState.OPENED -> 0f
                else -> 1f
            },
        animationSpec = tween(300),
        label = "chest_alpha",
    )

    Box(
        modifier =
            Modifier
                .size(180.dp)
                .scale(chestScale)
                .rotate(shakeAnim.value)
                .alpha(chestAlpha),
        contentAlignment = Alignment.Center,
    ) {
        // Outer glow ring
        if (chestState == ChestAnimationState.CLOSED) {
            Box(
                modifier =
                    Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            Color(0xFFFFD700).copy(alpha = glowAlpha * 0.5f),
                                            Color.Transparent,
                                        ),
                                ),
                        ),
            )
        }

        // Chest container
        Box(
            modifier =
                Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFD700), // Gold top
                                        Color(0xFFFF8C00), // Dark orange bottom
                                    ),
                            ),
                    )
                    .then(
                        if (chestState == ChestAnimationState.CLOSED) {
                            Modifier.border(
                                width = 4.dp,
                                color = Color(0xFFFFF8DC).copy(alpha = glowAlpha),
                                shape = CircleShape,
                            )
                        } else {
                            Modifier
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            // Chest emoji
            Text(
                text =
                    when (chestState) {
                        ChestAnimationState.CLOSED -> "🎁"
                        ChestAnimationState.OPENING -> "🎊"
                        ChestAnimationState.OPENED -> "✨"
                    },
                style = MaterialTheme.typography.displayLarge,
                fontSize = 80.sp,
            )
        }

        // Sparkle particles around chest (when closed)
        if (chestState == ChestAnimationState.CLOSED) {
            SparklesAroundChest()
        }
    }
}

/**
 * Sparkle particles around the chest
 */
@Composable
private fun SparklesAroundChest() {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkles")

    val sparkle1Alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(600, delayMillis = 0),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "sparkle1_alpha",
    )

    val sparkle2Alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(600, delayMillis = 200),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "sparkle2_alpha",
    )

    val sparkle3Alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(600, delayMillis = 400),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "sparkle3_alpha",
    )

    // Position sparkles around the chest
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "✨",
            fontSize = 20.sp,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-10).dp, y = (-10).dp)
                    .alpha(sparkle1Alpha),
        )
        Text(
            text = "✨",
            fontSize = 16.sp,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (20).dp)
                    .alpha(sparkle2Alpha),
        )
        Text(
            text = "✨",
            fontSize = 18.sp,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (10).dp, y = (10).dp)
                    .alpha(sparkle3Alpha),
        )
    }
}

/**
 * Pulsing "点击开启" button
 */
@Composable
private fun PulsingOpenButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "button_pulse")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "button_scale",
    )

    com.wordland.ui.components.WordlandButton(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .scale(buttonScale),
        text = "点击开启",
        size = com.wordland.ui.components.ButtonSize.LARGE,
    )
}

/**
 * Continue button after reward reveal
 */
@Composable
private fun ContinueButton(onClick: () -> Unit) {
    val buttonScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 200f,
            ),
        label = "continue_button_scale",
    )

    com.wordland.ui.components.WordlandButton(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(buttonScale),
        text = "太酷了！",
        size = com.wordland.ui.components.ButtonSize.LARGE,
    )
}

/**
 * Reward reveal card with animation
 */
@Composable
private fun RewardRevealCard(
    reward: ChestReward,
    chestState: ChestAnimationState,
) {
    val cardAlpha by animateFloatAsState(
        targetValue = if (chestState == ChestAnimationState.OPENED) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 400,
                delayMillis = 200,
                easing = FastOutSlowInEasing,
            ),
        label = "card_alpha",
    )

    val cardScale by animateFloatAsState(
        targetValue = if (chestState == ChestAnimationState.OPENED) 1f else 0.5f,
        animationSpec =
            spring(
                dampingRatio = 0.7f,
                stiffness = 250f,
            ),
        label = "card_scale",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .scale(cardScale)
                .alpha(cardAlpha),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // "恭喜获得！" title
            Text(
                text = "恭喜获得！",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            // Reward emoji with glow background
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        ),
                                ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                val emoji =
                    when (reward) {
                        is ChestReward.PetEmoji -> reward.emoji
                        is ChestReward.CelebrationEffect -> "✨"
                        is ChestReward.RarePetStyle -> reward.emoji
                    }
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 64.sp,
                )
            }

            // Reward name
            Text(
                text = reward.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            // Reward description
            Text(
                text = reward.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Completed summary for users who already finished onboarding
 */
@Composable
private fun CompletedSummary(
    pet: com.wordland.domain.model.PetType,
    wordsLearned: Int,
    stars: Int,
    onDismiss: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "🎉 完成训练！",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "你的伙伴: ${pet.getDisplayName()} ${pet.getEmoji()}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "学习单词: $wordsLearned",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "获得星星: $stars",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        com.wordland.ui.components.WordlandButton(
            onClick = onDismiss,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            text = "开始冒险",
        )
    }
}
