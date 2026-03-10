package com.wordland.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordland.ui.components.WordlandButton

/**
 * Onboarding Welcome Screen - First screen of the onboarding flow
 *
 * Features:
 * - Animated dolphin character with floating animation
 * - Welcome message in Chinese
 * - Primary CTA: "开始探险" (Start Adventure)
 * - Secondary CTA: "看看是什么" (Learn More)
 *
 * Per ONBOARDING_EXPERIENCE_DESIGN.md Section 1.1:
 * - Shows: "嗨！我是你的向导小海豚"
 * - Animated boat arrival scene
 * - 0-30 seconds of first experience
 *
 * @param onStartClick Callback when user clicks "Start Adventure"
 * @param onLearnMoreClick Callback when user clicks "Learn More" (optional)
 */
@Composable
fun OnboardingWelcomeScreen(
    onStartClick: () -> Unit,
    onLearnMoreClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
        ) {
            // Background gradient overlay
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.surface,
                                        ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY,
                                ),
                        ),
            )

            // Main content column
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Header section with title
                WelcomeHeader()

                // Center section with dolphin animation
                WelcomeCenterSection()

                // Bottom section with CTAs
                WelcomeActions(
                    onStartClick = onStartClick,
                    onLearnMoreClick = onLearnMoreClick,
                )
            }
        }
    }
}

@Composable
private fun WelcomeHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Main title
        Text(
            text = "欢迎来到 Wordland!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
        )

        // Subtitle with fade-in animation
        val alpha by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, delayMillis = 200),
            label = "subtitle_fade_in",
        )

        Text(
            text = "我们要一起探索单词海洋！",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(alpha * 0.7f),
        )
    }
}

@Composable
private fun WelcomeCenterSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Animated dolphin character
        AnimatedDolphinCharacter()

        // Guide message
        GuideMessage()
    }
}

@Composable
private fun AnimatedDolphinCharacter() {
    val infiniteTransition = rememberInfiniteTransition(label = "dolphin_animation")

    // Floating animation (up and down)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(durationMillis = 1500, easing = { x ->
                        // Smooth easing in and out
                        val y = 2 * x - 1
                        y * y * y
                    }),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "float_offset",
    )

    // Scale pulse animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(durationMillis = 1200, easing = { x ->
                        // Smooth pulse
                        val y = 2 * x - 1
                        y * y * y * y
                    }),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulse_scale",
    )

    // Rotation for wave effect
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(durationMillis = 2000, easing = { x ->
                        // Gentle sway
                        x
                    }),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "sway_rotation",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .offset(y = floatOffset.dp)
                .scale(scale),
    ) {
        // Dolphin emoji in a circle background
        Box(
            modifier =
                Modifier
                    .size(160.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surface,
                                    ),
                                radius = 200f,
                            ),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            // Water wave rings around dolphin
            WaveRings(modifier = Modifier.matchParentSize())

            // Dolphin character
            Text(
                text = "🐬",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 80.sp,
                modifier =
                    Modifier
                        .offset(y = (-10).dp)
                        .scale(scale),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Speech bubble
        SpeechBubble(
            message = "嗨！我是你的向导小海豚",
            modifier =
                Modifier
                    .fillMaxWidth(0.85f)
                    .offset(y = -floatOffset.dp),
        )
    }
}

@Composable
private fun WaveRings(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_rings")

    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2000, delayMillis = 0),
                repeatMode = RepeatMode.Restart,
            ),
        label = "ring1_scale",
    )

    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2000, delayMillis = 666),
                repeatMode = RepeatMode.Restart,
            ),
        label = "ring2_scale",
    )

    val ring3Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2000, delayMillis = 1333),
                repeatMode = RepeatMode.Restart,
            ),
        label = "ring3_scale",
    )

    Box(modifier = modifier) {
        List(3) { index ->
            val scaleValue =
                when (index) {
                    0 -> ring1Scale
                    1 -> ring2Scale
                    else -> ring3Scale
                }
            val alpha = 1f - ((scaleValue - 0.5f) / 0.7f).coerceIn(0f, 1f)

            Box(
                modifier =
                    Modifier
                        .size(120.dp * scaleValue)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.15f),
                        ),
            )
        }
    }
}

@Composable
private fun SpeechBubble(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
        ) {
            Box(
                modifier = Modifier.padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun GuideMessage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "准备好开始你的单词冒险了吗？",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WelcomeActions(
    onStartClick: () -> Unit,
    onLearnMoreClick: (() -> Unit)?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Primary CTA button
        WordlandButton(
            onClick = onStartClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            text = "开始探险",
            size = com.wordland.ui.components.ButtonSize.LARGE,
        )

        // Secondary CTA button (optional)
        if (onLearnMoreClick != null) {
            Button(
                onClick = onLearnMoreClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                    ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(
                    text = "看看是什么",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
