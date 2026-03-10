package com.wordland.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordland.media.TTSController

/**
 * TTS Speaker Button with emoji animation
 *
 * A circular button that plays TTS pronunciation when clicked.
 * Shows visual feedback during playback with animated emoji.
 *
 * @param text The text to speak
 * @param ttsController The TTSController instance
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled (checks TTS readiness)
 * @param onSpeakStart Optional callback when speaking starts
 * @param onSpeakComplete Optional callback when speaking completes
 */
@Composable
fun TTSSpeakerButtonEmoji(
    text: String,
    ttsController: TTSController,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSpeakStart: (() -> Unit)? = null,
    onSpeakComplete: (() -> Unit)? = null,
) {
    // Track whether TTS is ready and speaking
    var isReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var buttonScale by remember { mutableStateOf(1f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Collect TTS state
    LaunchedEffect(Unit) {
        ttsController.isReady.collect { ready ->
            isReady = ready
        }
    }

    LaunchedEffect(Unit) {
        ttsController.isPlaying.collect { playing ->
            isSpeaking = playing
            // Add subtle scale animation when speaking
            if (playing) {
                buttonScale = 1.1f
                onSpeakStart?.invoke()
            } else {
                buttonScale = 1f
                onSpeakComplete?.invoke()
            }
        }
    }

    // Collect error message
    LaunchedEffect(Unit) {
        ttsController.errorMessage.collect { error ->
            errorMessage = error
        }
    }

    // Animated speaker emoji
    val speakerEmoji =
        when {
            !isReady -> "🔇" // Muted when not ready
            isSpeaking -> "🔊" // Speaking with sound waves
            else -> "🔈" // Ready to speak
        }

    val buttonEnabled = enabled && isReady && !isSpeaking

    Box(
        modifier =
            modifier
                .size(48.dp)
                .scale(buttonScale)
                .then(
                    if (buttonEnabled) {
                        Modifier.clickable {
                            // Speak the text
                            val success = ttsController.speak(text)
                            if (!success) {
                                // If speaking failed, reset scale
                                buttonScale = 1f
                            }
                        }
                    } else {
                        Modifier
                    },
                )
                .alpha(if (buttonEnabled) 1f else 0.5f)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                )
                .border(
                    width = if (buttonEnabled) 2.dp else 1.dp,
                    color =
                        if (buttonEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = speakerEmoji,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

/**
 * Compact TTS Speaker Button (smaller version for inline use)
 *
 * @param text The text to speak
 * @param ttsController The TTSController instance
 * @param modifier Modifier for the button
 */
@Composable
fun TTSSpeakerButtonCompact(
    text: String,
    ttsController: TTSController,
    modifier: Modifier = Modifier,
) {
    var isReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ttsController.isReady.collect { isReady = it }
    }

    LaunchedEffect(Unit) {
        ttsController.isPlaying.collect { isSpeaking = it }
    }

    val emoji =
        when {
            !isReady -> "🔇"
            isSpeaking -> "🔊"
            else -> "🔈"
        }

    val buttonEnabled = isReady && !isSpeaking

    Box(
        modifier =
            modifier
                .size(36.dp)
                .then(
                    if (buttonEnabled) {
                        Modifier.clickable { ttsController.speak(text) }
                    } else {
                        Modifier
                    },
                )
                .alpha(if (buttonEnabled) 1f else 0.4f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp,
        )
    }
}
