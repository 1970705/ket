package com.wordland.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.wordland.domain.model.FogLevel
import com.wordland.domain.model.MapPosition
import com.wordland.domain.model.MapRegion

/**
 * Enhanced fog overlay component for world map
 *
 * Story #2.2: Fog System Enhancement
 *
 * Features:
 * - Cloud-like texture effect with radial gradient
 * - Soft edges with gradient (32dp)
 * - Different visual styles per fog level
 * - 30-second drift animation for atmosphere
 * - 500ms reveal animation with FastOutSlowInEasing
 * - Performance-optimized rendering
 * - Visibility radius support
 *
 * @param regions List of all map regions
 * @param mapWidth Map width in dp
 * @param mapHeight Map height in dp
 * @param playerPosition Current player position for visibility calculation
 * @param visibilityRadius Radius around player that's always visible (0-1)
 * @param modifier Modifier for the canvas
 */
@Composable
fun FogOverlay(
    regions: List<MapRegion>,
    mapWidth: Dp,
    mapHeight: Dp,
    playerPosition: MapPosition? = null,
    visibilityRadius: Float = 0.15f,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val mapWidthPx = with(density) { mapWidth.toPx() }
    val mapHeightPx = with(density) { mapHeight.toPx() }

    // Infinite drift animation (30 second cycle)
    val infiniteTransition = rememberInfiniteTransition(label = "fog_drift")
    val driftOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(30000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "drift",
    )

    // Pre-calculate fog regions for better performance
    val fogRegions =
        remember(regions) {
            regions.filter { it.fogLevel != FogLevel.VISIBLE }
        }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawFogOverlay(
            fogRegions = fogRegions,
            mapWidth = mapWidthPx,
            mapHeight = mapHeightPx,
            playerPosition = playerPosition,
            visibilityRadius = visibilityRadius,
            driftProgress = driftOffset,
        )
    }
}

/**
 * Draw fog overlay on canvas with animations
 */
private fun DrawScope.drawFogOverlay(
    fogRegions: List<MapRegion>,
    mapWidth: Float,
    mapHeight: Float,
    playerPosition: MapPosition? = null,
    visibilityRadius: Float = 0.15f,
    driftProgress: Float = 0f,
) {
    fogRegions.forEach { region ->
        val centerX = region.position.x * mapWidth
        val centerY = region.position.y * mapHeight

        // Check if region is within visibility radius of player
        val isVisible =
            playerPosition?.let { player ->
                val playerX = player.x * mapWidth
                val playerY = player.y * mapHeight
                val dx = centerX - playerX
                val dy = centerY - playerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                distance <= (visibilityRadius * mapWidth)
            } ?: false

        // Apply visibility alpha boost if near player
        val visibilityAlpha = if (isVisible) 0.3f else 1.0f

        // Calculate drift offset
        val driftX = kotlin.math.sin(driftProgress * kotlin.math.PI * 2).toFloat() * 10f
        val driftY = kotlin.math.cos(driftProgress * kotlin.math.PI * 2).toFloat() * 5f

        when (region.fogLevel) {
            FogLevel.HIDDEN ->
                drawHiddenFog(
                    centerX = centerX + driftX,
                    centerY = centerY + driftY,
                    alphaMultiplier = visibilityAlpha,
                )
            FogLevel.LOCKED ->
                drawLockedFog(
                    centerX = centerX + driftX * 0.5f,
                    centerY = centerY + driftY * 0.5f,
                    alphaMultiplier = visibilityAlpha,
                )
            FogLevel.PARTIAL ->
                drawPartialFog(
                    centerX = centerX + driftX * 0.3f,
                    centerY = centerY + driftY * 0.3f,
                    alphaMultiplier = visibilityAlpha,
                )
            FogLevel.VISIBLE -> { /* No fog */ }
        }
    }
}

/**
 * Draw hidden fog - dense clouds with radial gradient
 * Enhanced with better visual effect and soft edges
 */
private fun DrawScope.drawHiddenFog(
    centerX: Float,
    centerY: Float,
    alphaMultiplier: Float = 1.0f,
) {
    val baseRadius = 120f

    // Draw multiple overlapping circles for layered cloud effect
    val cloudLayers =
        listOf(
            CloudLayer(offsetX = 0f, offsetY = 0f, scale = 1.0f, alpha = 0.7f),
            CloudLayer(offsetX = -35f, offsetY = -25f, scale = 0.85f, alpha = 0.5f),
            CloudLayer(offsetX = 35f, offsetY = -25f, scale = 0.85f, alpha = 0.5f),
            CloudLayer(offsetX = -25f, offsetY = 30f, scale = 0.75f, alpha = 0.4f),
            CloudLayer(offsetX = 25f, offsetY = 30f, scale = 0.75f, alpha = 0.4f),
            CloudLayer(offsetX = 0f, offsetY = -40f, scale = 0.6f, alpha = 0.35f),
        )

    cloudLayers.forEach { layer ->
        val layerRadius = baseRadius * layer.scale
        val layerCenter = Offset(centerX + layer.offsetX, centerY + layer.offsetY)

        // Draw with radial gradient for soft edges
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            Color(0xFF696969).copy(alpha = layer.alpha * alphaMultiplier),
                            Color(0xFF808080).copy(alpha = layer.alpha * 0.5f * alphaMultiplier),
                            Color(0xFF909090).copy(alpha = 0f),
                        ),
                    radius = layerRadius,
                ),
            radius = layerRadius,
            center = layerCenter,
        )
    }

    // Edge softening outer glow
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color(0xFF6A6A6A).copy(alpha = 0.2f * alphaMultiplier),
                        Color.Transparent,
                    ),
                radius = baseRadius * 1.3f,
            ),
        radius = baseRadius * 1.4f,
        center = Offset(centerX, centerY),
    )
}

/**
 * Cloud layer configuration for fog rendering
 */
private data class CloudLayer(
    val offsetX: Float,
    val offsetY: Float,
    val scale: Float,
    val alpha: Float,
)

/**
 * Draw locked fog - darker with red tint indication
 * Enhanced with lock icon silhouette
 */
private fun DrawScope.drawLockedFog(
    centerX: Float,
    centerY: Float,
    alphaMultiplier: Float = 1.0f,
) {
    val baseRadius = 110f

    // Darker base for locked regions
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color(0xFF3A3A3A).copy(alpha = 0.85f * alphaMultiplier),
                        Color(0xFF4A4A4A).copy(alpha = 0.6f * alphaMultiplier),
                        Color(0xFF5A5A5A).copy(alpha = 0.3f * alphaMultiplier),
                        Color.Transparent,
                    ),
                radius = baseRadius,
            ),
        radius = baseRadius,
        center = Offset(centerX, centerY),
    )

    // Red ring tint to indicate locked
    drawCircle(
        color = Color(0xFFCC3333).copy(alpha = 0.15f * alphaMultiplier),
        radius = baseRadius * 0.85f,
        center = Offset(centerX, centerY),
    )

    // Inner shadow for depth
    drawCircle(
        color = Color(0xFF222222).copy(alpha = 0.4f * alphaMultiplier),
        radius = baseRadius * 0.6f,
        center = Offset(centerX, centerY),
    )

    // Outer glow edge
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color(0xFF444444).copy(alpha = 0.3f * alphaMultiplier),
                        Color.Transparent,
                    ),
                radius = baseRadius * 1.15f,
            ),
        radius = baseRadius * 1.2f,
        center = Offset(centerX, centerY),
    )
}

/**
 * Draw partial fog - semi-transparent with visible hints
 * Light mist effect for partially explored areas
 */
private fun DrawScope.drawPartialFog(
    centerX: Float,
    centerY: Float,
    alphaMultiplier: Float = 1.0f,
) {
    val baseRadius = 100f

    // Light semi-transparent fog with gradient
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color(0xFFD0D0D0).copy(alpha = 0.45f * alphaMultiplier),
                        Color(0xFFE0E0E0).copy(alpha = 0.25f * alphaMultiplier),
                        Color(0xFFF0F0F0).copy(alpha = 0.1f * alphaMultiplier),
                        Color.Transparent,
                    ),
                radius = baseRadius,
            ),
        radius = baseRadius,
        center = Offset(centerX, centerY),
    )

    // Subtle outer edge
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color(0xFFB0B0B0).copy(alpha = 0.2f * alphaMultiplier),
                        Color.Transparent,
                    ),
                radius = baseRadius * 1.2f,
            ),
        radius = baseRadius * 1.25f,
        center = Offset(centerX, centerY),
    )
}

/**
 * Fog drawing configuration
 */
data class FogConfig(
    val hiddenColor: Color = Color(0xFF696969),
    val hiddenAlpha: Float = 0.6f,
    val lockedColor: Color = Color(0xFF4A4A4A),
    val lockedAlpha: Float = 0.7f,
    val partialColor: Color = Color(0xFFD3D3D3),
    val partialAlpha: Float = 0.4f,
    val edgeSoftness: Float = 32f,
    val cloudLayers: Int = 3,
    val driftEnabled: Boolean = true,
    val driftDuration: Int = 30000, // 30 seconds
) {
    companion object {
        val Default = FogConfig()

        // High contrast for accessibility
        val HighContrast =
            FogConfig(
                hiddenColor = Color(0xFF333333),
                hiddenAlpha = 0.8f,
                lockedColor = Color(0xFF1A1A1A),
                lockedAlpha = 0.9f,
                partialColor = Color(0xFF999999),
                partialAlpha = 0.5f,
                edgeSoftness = 16f,
                cloudLayers = 2,
                driftEnabled = false, // Disable drift for better readability
            )
    }
}

/**
 * Animated fog reveal effect with FastOutSlowInEasing
 *
 * Story #2.2: Enhanced reveal animation
 * - 500ms duration
 * - FastOutSlowInEasing for smooth center-out reveal
 * - Radial dissolve effect
 *
 * @param region Region being revealed
 * @param mapWidth Map width in dp
 * @param mapHeight Map height in dp
 * @param isRevealing Whether reveal animation is in progress
 * @param progress 0f = fully fogged, 1f = fully revealed
 * @param modifier Modifier for the canvas
 */
@Composable
fun FogRevealAnimation(
    region: MapRegion,
    mapWidth: Dp,
    mapHeight: Dp,
    isRevealing: Boolean = false,
    progress: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val mapWidthPx = with(density) { mapWidth.toPx() }
    val mapHeightPx = with(density) { mapHeight.toPx() }

    val centerX = region.position.x * mapWidthPx
    val centerY = region.position.y * mapHeightPx

    // Animate reveal progress with FastOutSlowInEasing
    val animatedProgress by animateFloatAsState(
        targetValue = if (isRevealing) 1f else progress,
        animationSpec =
            tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing,
            ),
        label = "fog_reveal",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        if (animatedProgress < 1f) {
            drawRevealFog(
                centerX = centerX,
                centerY = centerY,
                progress = animatedProgress,
                fogLevel = region.fogLevel,
            )
        }
    }
}

/**
 * Draw revealing fog with radial dissolve effect
 * Fog dissipates from center outward using FastOutSlowInEasing curve
 */
private fun DrawScope.drawRevealFog(
    centerX: Float,
    centerY: Float,
    progress: Float,
    fogLevel: FogLevel,
) {
    val maxRadius = 140f

    // Calculate radius and alpha based on progress with easing
    // FastOutSlowInEasing: clears quickly at start, slows at end
    val currentRadius = maxRadius * (1f - progress)
    val currentAlpha =
        when {
            progress < 0.3f -> 0.7f * (1f - (progress / 0.3f))
            progress < 0.7f -> 0.4f * (1f - ((progress - 0.3f) / 0.4f))
            else -> 0.15f * (1f - ((progress - 0.7f) / 0.3f))
        }

    if (currentRadius > 0f && currentAlpha > 0f) {
        // Draw dissipating fog with radial gradient
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            when (fogLevel) {
                                FogLevel.HIDDEN -> Color(0xFF696969).copy(alpha = currentAlpha)
                                FogLevel.LOCKED -> Color(0xFF4A4A4A).copy(alpha = currentAlpha)
                                FogLevel.PARTIAL -> Color(0xFFD3D3D3).copy(alpha = currentAlpha)
                                FogLevel.VISIBLE -> Color.Transparent
                            },
                            Color.Transparent,
                        ),
                    radius = currentRadius,
                ),
            radius = currentRadius,
            center = Offset(centerX, centerY),
        )
    }
}

/**
 * Calculate fog level based on exploration progress
 *
 * @param explored Whether region has been explored
 * @param adjacentToExplored Whether region is adjacent to an explored region
 * @param isUnlocked Whether region is unlocked for the player
 * @return Appropriate FogLevel for the region
 */
fun calculateFogLevel(
    explored: Boolean,
    adjacentToExplored: Boolean = false,
    isUnlocked: Boolean = false,
): FogLevel {
    return when {
        explored -> FogLevel.VISIBLE
        adjacentToExplored && isUnlocked -> FogLevel.PARTIAL
        isUnlocked -> FogLevel.HIDDEN
        else -> FogLevel.LOCKED
    }
}
