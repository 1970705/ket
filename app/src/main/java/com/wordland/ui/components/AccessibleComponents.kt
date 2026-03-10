package com.wordland.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wordland.ui.theme.AppShapes

/**
 * Accessible card component with proper semantic support
 *
 * Features:
 * - Minimum touch target size (48dp)
 * - Proper semantic role for screen readers
 * - Content description support
 * - Consistent styling
 * - Accessibility compliance
 *
 * @param onClick Optional click callback
 * @param modifier Compose modifier
 * @param enabled Whether the card is enabled
 * @param shape Card shape
 * @param border Optional border
 * @param colors Card colors
 * @param contentDescription Content description for screen readers
 * @param interactionSource Interaction source
 * @param content Card content
 *
 * @since 1.6 (Epic #8.4)
 */
@Composable
fun AccessibleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: CornerBasedShape = AppShapes.CardShape,
    border: BorderStroke? = null,
    colors: CardColors = CardDefaults.cardColors(),
    contentDescription: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier =
        if (onClick != null) {
            modifier
                .clickable(
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.Button,
                    interactionSource = interactionSource,
                    indication = rememberRipple(bounded = true, radius = 200.dp),
                )
                .semantics {
                    if (contentDescription != null) {
                        this.contentDescription = contentDescription
                    }
                }
        } else {
            modifier
        }

    Card(
        modifier = cardModifier,
        shape = shape,
        border = border,
        colors = colors,
    ) {
        val contentPadding =
            PaddingValues(
                horizontal = 20.dp,
                vertical = 20.dp,
            )

        Column(
            modifier = Modifier.padding(contentPadding),
        ) {
            content()
        }
    }
}

/**
 * Accessible surface variant with elevation
 *
 * @param modifier Compose modifier
 * @param tonalElevation Tonal elevation
 * @param shadowElevation Shadow elevation
 * @param shape Surface shape
 * @param color Surface color
 * @param contentColor Content color
 * @param content Surface content
 */
@Composable
fun AccessibleSurface(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 1.dp,
    shadowElevation: Dp = 0.dp,
    shape: Shape = AppShapes.CardShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        content()
    }
}

/**
 * Interactive list item with proper accessibility
 *
 * Features:
 * - Minimum touch target (48dp height)
 * - Proper semantic structure
 * - Click feedback
 * - Content description support
 *
 * @param text Primary text
 * @param secondaryText Optional secondary text
 * @param onClick Click callback
 * @param modifier Compose modifier
 * @param enabled Whether enabled
 * @param leading Optional leading composable
 * @param trailing Optional trailing composable
 * @param contentDescription Content description for screen readers
 */
@Composable
fun AccessibleListItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    contentDescription: String? = null,
) {
    val itemModifier =
        modifier
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
            )
            .semantics {
                this.contentDescription =
                    contentDescription
                        ?: text +
                        if (secondaryText != null) {
                            ", $secondaryText"
                        } else {
                            ""
                        }
            }

    androidx.compose.material3.ListItem(
        modifier = itemModifier,
        headlineContent = {
            androidx.compose.material3.Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent =
            secondaryText?.let {
                {
                    androidx.compose.material3.Text(
                        text = secondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        leadingContent = leading,
        trailingContent = trailing,
        colors =
            androidx.compose.material3.ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
    )
}

/**
 * Icon button with minimum touch target size (48dp)
 *
 * @param onClick Click callback
 * @param modifier Compose modifier
 * @param enabled Whether enabled
 * @param contentDescription Content description for screen readers
 * @param content Icon content
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String?,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .clickable(
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.Button,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false, radius = 24.dp),
                )
                .also {
                    if (contentDescription != null) {
                        it.semantics {
                            this.contentDescription = contentDescription
                        }
                    } else {
                        it
                    }
                },
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        content()
    }
}
