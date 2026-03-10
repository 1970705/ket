package com.wordland.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Confetti particle data class
 */
data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val rotation: Float = 0f,
    val rotationSpeed: Float = 0f,
    val size: Float = 10f,
)

/**
 * Confetti celebration effect
 * Animates colorful particles falling from top of screen
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 100,
    durationMillis: Int = 3000,
    onComplete: () -> Unit = {},
) {
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var isActive by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Start animation on composition
    LaunchedEffect(Unit) {
        isActive = true
        particles = generateParticles(particleCount)

        // Auto-complete after duration
        delay(durationMillis.toLong())
        isActive = false
        particles = emptyList()
        onComplete()
    }

    // Cleanup on lifecycle destroy
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    isActive = false
                    particles = emptyList()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Animation frame updates
    LaunchedEffect(isActive) {
        if (isActive) {
            while (isActive) {
                delay(16) // ~60 FPS
                particles = updateParticles(particles)
            }
        }
    }

    if (isActive && particles.isNotEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val canvas = drawContext.canvas.nativeCanvas
                canvas.save()
                rotate(
                    degrees = particle.rotation,
                    pivot = Offset(particle.x, particle.y),
                ) {
                    drawRoundRect(
                        color = particle.color,
                        topLeft =
                            Offset(
                                x = particle.x - particle.size / 2,
                                y = particle.y - particle.size / 2,
                            ),
                        size =
                            androidx.compose.ui.geometry.Size(
                                width = particle.size,
                                height = particle.size * 0.6f, // Rectangular confetti
                            ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
                    )
                }
                canvas.restore()
            }
        }
    }
}

/**
 * Generate initial confetti particles
 */
private fun generateParticles(count: Int): List<ConfettiParticle> {
    val colors =
        listOf(
            Color(0xFFFF6B6B), // Red
            Color(0xFF4ECDC4), // Teal
            Color(0xFFFFD93D), // Yellow
            Color(0xFF6BCB77), // Green
            Color(0xFF4D96FF), // Blue
            Color(0xFFFF9FF3), // Pink
            Color(0xFFA8E6CF), // Light blue
        )

    return List(count) {
        val startX = Random.nextFloat() * 1000f // Screen width approximation
        ConfettiParticle(
            x = startX,
            y = -50f, // Start above screen
            velocityX = Random.nextFloat() * 4f - 2f, // -2 to 2
            velocityY = Random.nextFloat() * 3f + 2f, // 2 to 5 (downward)
            color = colors.random(),
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = Random.nextFloat() * 10f - 5f, // -5 to 5
            size = Random.nextFloat() * 8f + 6f, // 6 to 14
        )
    }
}

/**
 * Update particle positions for animation frame
 */
private fun updateParticles(particles: List<ConfettiParticle>): List<ConfettiParticle> {
    val screenHeight = 2000f // Approximate max height

    return particles.map { particle ->
        val newX = particle.x + particle.velocityX
        val newY = particle.y + particle.velocityY
        val newRotation = particle.rotation + particle.rotationSpeed

        // Remove if off screen
        if (newY > screenHeight) {
            return@map null
        }

        particle.copy(
            x = newX,
            y = newY,
            rotation = newRotation,
        )
    }.filterNotNull()
}

/**
 * Simple celebration burst effect
 * Smaller confetti explosion from center point
 */
@Composable
fun CelebrationBurst(
    modifier: Modifier = Modifier,
    centerX: Float = 500f,
    centerY: Float = 500f,
    particleCount: Int = 50,
) {
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var isActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isActive = true
        particles = generateBurstParticles(centerX, centerY, particleCount)

        // Auto-complete after 2 seconds
        delay(2000)
        isActive = false
        particles = emptyList()
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            while (isActive) {
                delay(16)
                particles = updateBurstParticles(particles)
            }
        }
    }

    if (isActive && particles.isNotEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {
            particles.forEach { particle ->
                drawRoundRect(
                    color = particle.color,
                    topLeft = Offset(particle.x, particle.y),
                    size = androidx.compose.ui.geometry.Size(particle.size, particle.size * 0.6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
                )
            }
        }
    }
}

/**
 * Generate particles for burst effect (from center outward)
 */
private fun generateBurstParticles(
    centerX: Float,
    centerY: Float,
    count: Int,
): List<ConfettiParticle> {
    val colors =
        listOf(
            Color(0xFFFF6B6B),
            Color(0xFF4ECDC4),
            Color(0xFFFFD93D),
            Color(0xFF6BCB77),
            Color(0xFFFF9FF3),
        )

    val angleStep = 360f / count

    return List(count) { index ->
        val angle = (index * angleStep) * (Math.PI / 180f)
        val speed = Random.nextFloat() * 5f + 3f // 3 to 8

        ConfettiParticle(
            x = centerX,
            y = centerY,
            velocityX = (cos(angle).toFloat() * speed),
            velocityY = (sin(angle).toFloat() * speed),
            color = colors.random(),
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = Random.nextFloat() * 15f - 7.5f,
            size = Random.nextFloat() * 6f + 4f, // Smaller particles
        )
    }
}

/**
 * Update burst particles with gravity
 */
private fun updateBurstParticles(particles: List<ConfettiParticle>): List<ConfettiParticle> {
    val gravity = 0.2f
    val screenWidth = 1000f
    val screenHeight = 2000f

    return particles.map { particle ->
        val newVelocityY = particle.velocityY + gravity
        val newX = particle.x + particle.velocityX
        val newY = particle.y + newVelocityY
        val newRotation = particle.rotation + particle.rotationSpeed

        // Fade out logic (alpha based on position)
        if (newX < 0 || newX > screenWidth || newY > screenHeight) {
            return@map null
        }

        particle.copy(
            x = newX,
            y = newY,
            velocityY = newVelocityY,
            rotation = newRotation,
        )
    }.filterNotNull()
}
