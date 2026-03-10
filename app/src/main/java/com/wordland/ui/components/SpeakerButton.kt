package com.wordland.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordland.data.assets.AudioAssetManager
import com.wordland.media.TTSController
import kotlinx.coroutines.launch

/**
 * Speaker button for playing word pronunciation audio
 *
 * Features:
 * - 40dp size as specified
 * - Toggle play/pause icon
 * - Loading indicator while playing
 * - Material Design 3 IconButton
 */
@Composable
fun SpeakerButton(
    wordId: String,
    audioManager: AudioAssetManager,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            if (!isPlaying) {
                coroutineScope.launch {
                    isPlaying = true
                    audioManager.playWordAudio(wordId) {
                        isPlaying = false
                    }
                }
            } else {
                audioManager.stopAudio()
                isPlaying = false
            }
        },
        modifier = modifier.size(40.dp),
    ) {
        if (isPlaying) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Text(
                text = "\uD83D\uDD0A", // Speaker emoji
                style = MaterialTheme.typography.titleLarge,
                fontSize = 24.sp,
            )
        }
    }
}

/**
 * Simple speaker button without audio manager dependency
 * Use this when you want to handle audio playback externally
 */
@Composable
fun SimpleSpeakerButton(
    isPlaying: Boolean = false,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onPlayClick,
        modifier = modifier.size(40.dp),
        enabled = enabled,
    ) {
        if (isPlaying) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Text(
                text = "\uD83D\uDD0A", // Speaker emoji
                style = MaterialTheme.typography.titleLarge,
                fontSize = 24.sp,
                modifier = Modifier.alpha(if (enabled) 1f else 0.38f),
            )
        }
    }
}

/**
 * Enhanced TTS Speaker button with better visibility and larger size.
 *
 * NOTE: TTS is currently disabled due to device compatibility issues.
 * The button is shown but displays a message when clicked.
 * TODO: Implement with pre-recorded audio files in P1.
 *
 * @param text The text to speak when clicked (currently unused)
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param onStart Callback when speech starts (currently unused)
 * @param onComplete Callback when speech completes (currently unused)
 */
@Composable
fun TTSSpeakerButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onStart: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var showMessage by remember { mutableStateOf(false) }

    // Enhanced button with background card
    Surface(
        onClick = {
            // Show message that TTS is not available
            showMessage = true
        },
        modifier = modifier.size(56.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        border =
            BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
        shadowElevation = 2.dp,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "🔊",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 32.sp,
                modifier = Modifier.alpha(0.5f),
            )
        }
    }

    // Show info snackbar when clicked
    if (showMessage) {
        androidx.compose.material3.Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                androidx.compose.material3.TextButton(onClick = { showMessage = false }) {
                    Text("知道了")
                }
            },
        ) {
            Text("语音功能即将推出，敬请期待！")
        }
    }
}

/**
 * TTS Speaker button with emoji icon instead of Material Icons
 *
 * @param text The text to speak when clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param ttsController Optional shared TTSController
 */
@Composable
fun TTSSpeakerButtonEmoji(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    ttsController: TTSController? = null,
    onStart: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSpeaking by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }

    // Create or use provided TTS controller
    val controller =
        remember(ttsController) {
            ttsController ?: TTSController(context)
        }

    // Keep callbacks updated
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnComplete by rememberUpdatedState(onComplete)

    // Observe TTS state
    LaunchedEffect(controller) {
        controller.isReady.collect { ready ->
            isReady = ready
        }
    }

    LaunchedEffect(controller) {
        controller.isPlaying.collect { playing ->
            isSpeaking = playing
        }
    }

    // Clean up TTS when composable is disposed
    DisposableEffect(controller, ttsController) {
        onDispose {
            if (ttsController == null) {
                controller.stop()
            }
        }
    }

    val canSpeak = !isSpeaking && text.isNotBlank() && enabled && isReady

    IconButton(
        onClick = {
            if (canSpeak) {
                coroutineScope.launch {
                    currentOnStart?.invoke()
                    controller.onSpeakingCompleteListener = {
                        currentOnComplete?.invoke()
                    }
                    controller.speak(text)
                }
            }
        },
        modifier = modifier.size(40.dp),
        enabled = enabled && isReady && !isSpeaking,
    ) {
        if (isSpeaking) {
            // Pulsing animation while playing
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = LinearEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
                    ),
                label = "alpha",
            )

            CircularProgressIndicator(
                modifier =
                    Modifier
                        .size(24.dp)
                        .alpha(alpha),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Text(
                text = "\uD83D\uDD0A", // Speaker emoji
                style = MaterialTheme.typography.titleLarge,
                fontSize = 24.sp,
                modifier = Modifier.alpha(if (enabled && isReady) 1f else 0.38f),
            )
        }
    }
}
