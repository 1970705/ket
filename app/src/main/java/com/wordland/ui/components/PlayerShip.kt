package com.wordland.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordland.domain.model.MapPosition
import kotlin.math.abs

/**
 * Player ship component for world map
 *
 * Story #2.3: Player Ship Display
 *
 * Features:
 * - Shows player's current position on world map
 * - Animated movement between regions (300ms, EaseInOut)
 * - Arrival/departure animations
 * - Pulse effect on position change
 * - "You are here" label
 * - Only visible in World View
 *
 * @param currentRegionId ID of the region the player is currently in
 * @param targetPosition Target position on the map (normalized 0-1)
 * @param mapWidth Map width in dp
 * @param mapHeight Map height in dp
 * @param showLabel Whether to show "You are here" label
 * @param modifier Modifier for the ship container
 */
@Composable
fun PlayerShip(
    currentRegionId: String,
    targetPosition: MapPosition,
    mapWidth: Dp,
    mapHeight: Dp,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    // Current animated position
    var currentPosition by remember { mutableStateOf(targetPosition) }

    // Animate to target position when it changes
    val animatedX by animateFloatAsState(
        targetValue = targetPosition.x,
        animationSpec =
            tween(
                durationMillis = 300,
                easing = EaseInOut,
            ),
        label = "ship_x",
    )

    val animatedY by animateFloatAsState(
        targetValue = targetPosition.y,
        animationSpec =
            tween(
                durationMillis = 300,
                easing = EaseInOut,
            ),
        label = "ship_y",
    )

    // Pulse animation on position change
    var pulseScale by remember { mutableFloatStateOf(1f) }
    var showPulse by remember { mutableStateOf(false) }

    LaunchedEffect(targetPosition) {
        if (currentPosition != targetPosition) {
            showPulse = true
            pulseScale = 1.3f

            // Pulse animation
            kotlinx.coroutines.delay(50)
            pulseScale = 1.0f
            kotlinx.coroutines.delay(150)
            showPulse = false

            currentPosition = targetPosition
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (showPulse) pulseScale else 1f,
        animationSpec =
            tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing,
            ),
        label = "pulse_scale",
    )

    // Calculate pixel position
    val mapWidthPx = with(density) { mapWidth.toPx() }
    val mapHeightPx = with(density) { mapHeight.toPx() }
    val xPx = animatedX * mapWidthPx
    val yPx = animatedY * mapHeightPx
    val shipSize = 32.dp
    val shipSizePx = with(density) { shipSize.toPx() }

    Box(
        modifier =
            modifier
                .offset {
                    IntOffset(
                        x = (xPx - shipSizePx / 2).toInt(),
                        y = (yPx - shipSizePx / 2).toInt(),
                    )
                }
                .size(shipSize)
                .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        // Ship emoji with shadow
        Text(
            text = "🚢",
            fontSize = 28.sp,
            modifier =
                Modifier
                    .size(shipSize),
        )

        // "You are here" label
        if (showLabel) {
            Text(
                text = "You are here",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier =
                    Modifier
                        .offset(y = 22.dp)
                        .alpha(0.9f),
            )
        }
    }
}

/**
 * Simple player ship marker (without label)
 * For use in compact map displays
 *
 * @param position Ship position (normalized 0-1)
 * @param mapWidth Map width in dp
 * @param mapHeight Map height in dp
 * @param modifier Modifier for the ship
 */
@Composable
fun PlayerShipMarker(
    position: MapPosition,
    mapWidth: Dp,
    mapHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val mapWidthPx = with(density) { mapWidth.toPx() }
    val mapHeightPx = with(density) { mapHeight.toPx() }
    val xPx = position.x * mapWidthPx
    val yPx = position.y * mapHeightPx
    val markerSize = 24.dp
    val markerSizePx = with(density) { markerSize.toPx() }

    Box(
        modifier =
            modifier
                .offset {
                    IntOffset(
                        x = (xPx - markerSizePx / 2).toInt(),
                        y = (yPx - markerSizePx / 2).toInt(),
                    )
                }
                .size(markerSize),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "🚢",
            fontSize = 20.sp,
            modifier = Modifier.size(markerSize),
        )
    }
}

/**
 * Canvas-based ship icon for custom drawing
 * More performant for many ships
 */
fun DrawScope.drawShipIcon(
    centerX: Float,
    centerY: Float,
    size: Float = 24f,
    color: Color = Color(0xFF2196F3),
) {
    // Ship body (hull)
    val hullPath =
        androidx.compose.ui.graphics.Path().apply {
            moveTo(centerX, centerY - size / 2)
            lineTo(centerX + size / 2, centerY)
            lineTo(centerX + size / 3, centerY + size / 3)
            lineTo(centerX - size / 3, centerY + size / 3)
            lineTo(centerX - size / 2, centerY)
            close()
        }

    // Draw hull with shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.2f),
        radius = size / 2 + 4f,
        center = androidx.compose.ui.geometry.Offset(centerX + 2f, centerY + 2f),
    )

    // Draw hull
    drawPath(
        path = hullPath,
        color = color,
    )

    // Draw mast
    drawLine(
        color = color.copy(alpha = 0.8f),
        start = androidx.compose.ui.geometry.Offset(centerX, centerY - size / 2),
        end = androidx.compose.ui.geometry.Offset(centerX, centerY - size * 0.8f),
        strokeWidth = 2f,
    )

    // Draw sail
    val sailPath =
        androidx.compose.ui.graphics.Path().apply {
            moveTo(centerX + 1f, centerY - size * 0.8f)
            lineTo(centerX + size * 0.4f, centerY - size * 0.4f)
            lineTo(centerX + 1f, centerY - size * 0.4f)
            close()
        }

    drawPath(
        path = sailPath,
        color = Color.White,
    )
}

/**
 * Ship state for tracking position and animation
 *
 * @param position Current position (normalized 0-1)
 * @param targetPosition Destination for animation
 * @param isMoving Whether ship is currently moving
 * @param currentRegionId ID of region ship is at
 */
data class ShipState(
    val position: MapPosition = MapPosition(0.5f, 0.5f),
    val targetPosition: MapPosition = MapPosition(0.5f, 0.5f),
    val isMoving: Boolean = false,
    val currentRegionId: String? = null,
) {
    /**
     * Check if ship has reached its destination
     */
    fun hasArrived(): Boolean {
        return !isMoving &&
            abs(position.x - targetPosition.x) < 0.01f &&
            abs(position.y - targetPosition.y) < 0.01f
    }

    /**
     * Update to a new region
     */
    fun moveToRegion(
        regionId: String,
        newPosition: MapPosition,
    ): ShipState {
        return copy(
            targetPosition = newPosition,
            isMoving = position != newPosition,
            currentRegionId = regionId,
        )
    }

    /**
     * Complete movement animation
     */
    fun completeMovement(): ShipState {
        return copy(
            position = targetPosition,
            isMoving = false,
        )
    }

    companion object {
        /**
         * Initial ship state at center of map
         */
        val Initial =
            ShipState(
                position = MapPosition(0.5f, 0.5f),
                targetPosition = MapPosition(0.5f, 0.5f),
                isMoving = false,
                currentRegionId = null,
            )
    }
}
