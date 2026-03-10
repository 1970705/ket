package com.wordland.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordland.ui.theme.AppShapes
import com.wordland.ui.theme.spacingMedium

/**
 * Loading state component
 *
 * Features:
 * - Centered circular progress indicator
 * - Optional loading message
 * - Child-friendly animation
 * - Accessibility support
 *
 * @param message Optional loading message
 * @param modifier Compose modifier
 *
 * @since 1.6 (Epic #8.4)
 */
@Composable
fun LoadingState(
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(0.8f) }
    val infiniteTransition = rememberInfiniteTransition(label = "loading_scale")

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1000,
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "loading_scale",
    )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(spacingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier =
                Modifier
                    .size(48.dp)
                    .scale(animatedScale),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(spacingMedium))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Error state component
 *
 * Features:
 * - Centered error icon
 * - Error message
 * - Optional retry action
 * - Child-friendly design
 * - Accessibility support
 *
 * @param message Error message to display
 * @param onRetry Optional retry callback
 * @param modifier Compose modifier
 *
 * @since 1.6 (Epic #8.4)
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(spacingMedium)
                .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Error icon with subtle shake animation
        val infiniteTransition = rememberInfiniteTransition(label = "error_shake")

        val shakeOffset by infiniteTransition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "error_shake",
        )

        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "错误",
            modifier =
                Modifier
                    .size(64.dp)
                    .padding(horizontal = 4.dp),
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(spacingMedium))

        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "请稍后再试",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = onRetry,
                shape = AppShapes.ButtonShape,
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}

/**
 * Empty state component
 *
 * Features:
 * - Centered empty state icon
 * - Title and description
 * - Optional action
 * - Child-friendly design
 * - Accessibility support
 *
 * @param title Empty state title
 * @param description Empty state description
 * @param icon Optional icon to display
 * @param actionText Optional action button text
 * @param onAction Optional action callback
 * @param modifier Compose modifier
 *
 * @since 1.6 (Epic #8.4)
 */
@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.Filled.MailOutline,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(0f) }
    val targetScale = 1f

    LaunchedEffect(Unit) {
        scale = targetScale
    }

    val animatedScale by
        animateFloatAsState(
            targetValue = scale,
            animationSpec =
                spring(
                    dampingRatio = 0.6f,
                    stiffness = 200f,
                ),
            label = "empty_scale",
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(spacingMedium)
                .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier =
                Modifier
                    .size(72.dp)
                    .scale(animatedScale),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(spacingMedium))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = onAction,
                shape = AppShapes.ButtonShape,
            ) {
                Text(actionText)
            }
        }
    }
}

/**
 * Success state component
 *
 * Features:
 * - Centered success icon
 * - Success message
 * - Optional continue action
 * - Child-friendly design
 * - Accessibility support
 *
 * @param message Success message to display
 * @param actionText Optional action button text
 * @param onAction Optional action callback
 * @param modifier Compose modifier
 *
 * @since 1.6 (Epic #8.4)
 */
@Composable
fun SuccessState(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "success_pulse")

    LaunchedEffect(Unit) {
        scale = 1f
    }

    val animatedScale by
        animateFloatAsState(
            targetValue = scale,
            animationSpec =
                spring(
                    dampingRatio = 0.5f,
                    stiffness = 200f,
                ),
            label = "success_scale",
        )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "success_pulse",
    )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(spacingMedium)
                .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Success icon
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "成功",
                modifier =
                    Modifier
                        .size(64.dp)
                        .scale(animatedScale * pulseScale),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(spacingMedium))

        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = onAction,
                shape = AppShapes.ButtonShape,
            ) {
                Text(actionText)
            }
        }
    }
}

/**
 * Network error state
 *
 * Specialized error state for network issues
 */
@Composable
fun NetworkErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ErrorState(
        message = "网络连接失败",
        onRetry = onRetry,
        modifier = modifier,
    )
}

/**
 * No data state
 *
 * Specialized empty state for no data available
 */
@Composable
fun NoDataState(
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = "暂无数据",
        description = "还没有任何内容哦",
        icon = Icons.Filled.MailOutline,
        actionText = if (onRefresh != null) "刷新" else null,
        onAction = onRefresh,
        modifier = modifier,
    )
}

/**
 * Coming soon state
 *
 * Specialized empty state for upcoming features
 */
@Composable
fun ComingSoonState(
    featureName: String,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = "即将推出",
        description = "$featureName 功能正在开发中，敬请期待！",
        icon = Icons.Filled.Favorite,
        modifier = modifier,
    )
}
