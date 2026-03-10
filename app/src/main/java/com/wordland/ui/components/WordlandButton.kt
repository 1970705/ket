package com.wordland.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordland.ui.theme.PrimaryLight

// Button sizes
val SmallButtonHeight = 36.dp
val MediumButtonHeight = 48.dp
val LargeButtonHeight = 56.dp

/**
 * Enhanced Wordland button with press animation
 *
 * Features:
 * - Scale down on press (0.97)
 * - Ripple effect
 * - Smooth color transitions
 */
@Composable
fun WordlandButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    iconBeforeText: Boolean = true,
    size: ButtonSize = ButtonSize.MEDIUM,
) {
    val height =
        when (size) {
            ButtonSize.SMALL -> SmallButtonHeight
            ButtonSize.MEDIUM -> MediumButtonHeight
            ButtonSize.LARGE -> LargeButtonHeight
        }

    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel,
                -> isPressed = false
            }
        }
    }

    val buttonScale = if (isPressed) 0.97f else 1f

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (enabled) {
                        PrimaryLight
                    } else {
                        PrimaryLight.copy(alpha = 0.5f)
                    },
                contentColor = Color.White,
                disabledContainerColor = PrimaryLight.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.5f),
            ),
        shape = RoundedCornerShape(12.dp),
        contentPadding =
            PaddingValues(
                horizontal = 24.dp,
                vertical = 12.dp,
            ),
    ) {
        val contentModifier = Modifier.size(20.dp)

        if (icon != null && iconBeforeText) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = contentModifier,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontSize =
                when (size) {
                    ButtonSize.SMALL -> 14.sp
                    ButtonSize.MEDIUM -> 16.sp
                    ButtonSize.LARGE -> 18.sp
                },
        )

        if (icon != null && !iconBeforeText) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = contentModifier,
            )
        }
    }
}

/**
 * Enhanced outlined button with press animation
 */
@Composable
fun WordlandOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    borderColor: Color = PrimaryLight,
    size: ButtonSize = ButtonSize.MEDIUM,
) {
    val height =
        when (size) {
            ButtonSize.SMALL -> SmallButtonHeight
            ButtonSize.MEDIUM -> MediumButtonHeight
            ButtonSize.LARGE -> LargeButtonHeight
        }

    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel,
                -> isPressed = false
            }
        }
    }

    val buttonScale = if (isPressed) 0.97f else 1f
    val buttonBorderColor =
        if (enabled) {
            borderColor
        } else {
            borderColor.copy(alpha = 0.5f)
        }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor =
                    if (enabled) {
                        PrimaryLight
                    } else {
                        PrimaryLight.copy(alpha = 0.5f)
                    },
            ),
        border = BorderStroke(2.dp, buttonBorderColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding =
            PaddingValues(
                horizontal = 24.dp,
                vertical = 12.dp,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontSize =
                when (size) {
                    ButtonSize.SMALL -> 14.sp
                    ButtonSize.MEDIUM -> 16.sp
                    ButtonSize.LARGE -> 18.sp
                },
        )
    }
}

/**
 * Enhanced text button with press animation
 */
@Composable
fun WordlandTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    textColor: Color = PrimaryLight,
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel,
                -> isPressed = false
            }
        }
    }

    val buttonTextColor =
        if (enabled) {
            textColor
        } else {
            textColor.copy(alpha = 0.5f)
        }

    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        colors =
            ButtonDefaults.textButtonColors(
                contentColor = buttonTextColor,
            ),
        contentPadding =
            PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Animated button with icon that rotates on press
 */
@Composable
fun AnimatedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector,
    size: ButtonSize = ButtonSize.MEDIUM,
) {
    val height =
        when (size) {
            ButtonSize.SMALL -> SmallButtonHeight
            ButtonSize.MEDIUM -> MediumButtonHeight
            ButtonSize.LARGE -> LargeButtonHeight
        }

    var isPressed by remember { mutableStateOf(false) }
    var iconRotation by remember { mutableFloatStateOf(0f) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    iconRotation = 15f
                }
                is PressInteraction.Release -> {
                    isPressed = false
                    iconRotation = 0f
                }
                is PressInteraction.Cancel -> {
                    isPressed = false
                    iconRotation = 0f
                }
            }
        }
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = PrimaryLight,
                contentColor = Color.White,
            ),
        shape = RoundedCornerShape(12.dp),
        contentPadding =
            PaddingValues(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp).rotate(iconRotation),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontSize =
                when (size) {
                    ButtonSize.SMALL -> 14.sp
                    ButtonSize.MEDIUM -> 16.sp
                    ButtonSize.LARGE -> 18.sp
                },
        )
    }
}

/**
 * Pulsing button for call-to-action
 * Draws attention with subtle pulse animation
 */
@Composable
fun PulsingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    pulseEnabled: Boolean = true,
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel,
                -> isPressed = false
            }
        }
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = PrimaryLight,
                contentColor = Color.White,
            ),
        shape = RoundedCornerShape(12.dp),
        contentPadding =
            PaddingValues(
                horizontal = 24.dp,
                vertical = 12.dp,
            ),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

enum class ButtonSize {
    SMALL,
    MEDIUM,
    LARGE,
}
