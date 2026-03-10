# Achievement UI Components Design

**Version**: 1.0
**Date**: 2026-02-18
**Author**: android-architect
**Status**: Design Phase

---

## Overview

本文档定义成就系统的UI组件架构，包括Compose组件设计、动画规范和用户交互流程。

---

## UI Architecture

### Component Hierarchy

```
AchievementScreen (Root)
├── AchievementTopBar
│   ├── Title: "Achievements"
│   ├── ProgressIndicator
│   └── FilterChipRow
├── LazyColumn
│   ├── AchievementCategoryHeader
│   ├── AchievementList (grouped by category)
│   │   └── AchievementCard
│   │       ├── Icon
│   │       ├── Title & Description
│   │       ├── ProgressBar
│   │       ├── ProgressText
│   │       └── StatusIndicator
│   └── AchievementDetailDialog (tappable)
└── AchievementNotificationOverlay
    └── AchievementUnlockedDialog
```

---

## Core Compose Components

### 1. AchievementScreen

```kotlin
// File: ui/screens/AchievementScreen.kt
package com.wordland.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordland.domain.model.achievement.AchievementCategory
import com.wordland.ui.viewmodel.AchievementViewModel

@Composable
fun AchievementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }

    Scaffold(
        topBar = {
            AchievementTopBar(
                onNavigateBack = onNavigateBack,
                selectedCategory = selectedCategory,
                onCategoryFilter = { category ->
                    selectedCategory = if (selectedCategory == category) null else category
                    viewModel.filterByCategory(selectedCategory)
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is AchievementUiState.Loading -> {
                AchievementLoadingIndicator()
            }
            is AchievementUiState.Ready -> {
                AchievementList(
                    achievements = state.achievements,
                    statistics = state.statistics,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is AchievementUiState.Error -> {
                AchievementErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadAchievements() }
                )
            }
        }
    }
}
```

### 2. AchievementCard

```kotlin
// File: ui/components/achievement/AchievementCard.kt
package com.wordland.ui.components.achievement

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordland.domain.model.achievement.AchievementWithProgress

@Composable
fun AchievementCard(
    achievementWithProgress: AchievementWithProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val achievement = achievementWithProgress.achievement
    val progress = achievementWithProgress.progress
    val isUnlocked = achievementWithProgress.isUnlocked

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isUnlocked -> MaterialTheme.colorScheme.primaryContainer
                achievement.category == AchievementCategory.PROGRESS -> MaterialTheme.colorScheme.surface
                achievement.category == AchievementCategory.PERFORMANCE -> MaterialTheme.colorScheme.secondaryContainer
                achievement.category == AchievementCategory.COMBO -> Color(0xFFFFCC80).copy(alpha = 0.3f)
                achievement.category == AchievementCategory.STREAK -> Color(0xFF81D4FA).copy(alpha = 0.3f)
                achievement.category == AchievementCategory.SPECIAL -> Color(0xFFCFB53B).copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            AchievementIcon(
                icon = achievement.icon,
                isUnlocked = isUnlocked,
                category = achievement.category
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUnlocked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (isUnlocked) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Unlocked",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                // Progress Bar
                if (!isUnlocked) {
                    AchievementProgressBar(
                        progress = progress,
                        target = achievementWithProgress.target,
                        category = achievement.category
                    )
                } else {
                    Text(
                        text = "Unlocked!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementIcon(
    icon: String,
    isUnlocked: Boolean,
    category: AchievementCategory,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when (category) {
                    AchievementCategory.PROGRESS -> Color(0xFF4CAF50)
                    AchievementCategory.PERFORMANCE -> Color(0xFFFFC107)
                    AchievementCategory.COMBO -> Color(0xFFFF5722)
                    AchievementCategory.STREAK -> Color(0xFF2196F3)
                    AchievementCategory.SPECIAL -> Color(0xFF9C27B0)
                }.copy(alpha = if (isUnlocked) 1f else 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
private fun AchievementProgressBar(
    progress: Int,
    target: Int,
    category: AchievementCategory,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$progress / $target",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { progress.toFloat() / target.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when (category) {
                AchievementCategory.PROGRESS -> Color(0xFF4CAF50)
                AchievementCategory.PERFORMANCE -> Color(0xFFFFC107)
                AchievementCategory.COMBO -> Color(0xFFFF5722)
                AchievementCategory.STREAK -> Color(0xFF2196F3)
                AchievementCategory.SPECIAL -> Color(0xFF9C27B0)
            }
        )
    }
}
```

### 3. AchievementUnlockedDialog

```kotlin
// File: ui/components/achievement/AchievementUnlockedDialog.kt
package com.wordland.ui.components.achievement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wordland.domain.model.achievement.Achievement
import kotlinx.coroutines.delay

@Composable
fun AchievementUnlockedDialog(
    achievement: Achievement,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss after 5 seconds
    LaunchedEffect(Unit) {
        delay(5000)
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "🎉 ACHIEVEMENT UNLOCKED! 🎉",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = achievement.icon,
                        style = MaterialTheme.typography.displayLarge
                    )
                }

                // Achievement Name
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Reward
                AchievementRewardDisplay(reward = achievement.reward)

                // Continue Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Awesome!")
                }
            }
        }
    }
}

@Composable
private fun AchievementRewardDisplay(reward: AchievementReward) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp, 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Stars,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = reward.getDisplayText(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
```

### 4. AchievementNotificationOverlay

```kotlin
// File: ui/components/achievement/AchievementNotificationOverlay.kt
package com.wordland.ui.components.achievement

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.wordland.domain.model.achievement.AchievementUnlockEvent

@Composable
fun AchievementNotificationOverlay(
    events: List<AchievementUnlockEvent>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (events.isNotEmpty()) {
        // Show only the most recent
        val latestEvent = events.first()

        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(100f),
            contentAlignment = Alignment.Center
        ) {
            // Semi-transparent backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss)
            )

            // Dialog
            AchievementUnlockedDialog(
                achievement = latestEvent.achievement,
                onDismiss = onDismiss
            )
        }
    }
}
```

---

## Animation Specifications

### Unlock Animation Sequence

```kotlin
// File: ui/components/achievement/AchievementAnimations.kt
package com.wordland.ui.components.achievement

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color

/**
 * Achievement unlock celebration animation
 * - Icon scales up from 0 to 1.2x then settles at 1.0x
 * - Confetti particles explode from center
 * - Stars burst from behind icon
 */
@Composable
fun AchievementUnlockAnimation(
    content: @Composable () -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 200f
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}

/**
 * Confetti particle effect
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier
) {
    val particles = remember { List(30) { Particle.random() } }

    var trigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { trigger = true }

    particles.forEach { particle ->
        val animatedOffsetY by animateFloatAsState(
            targetValue = if (trigger) particle.endY else 0f,
            animationSpec = tween(
                durationMillis = particle.duration,
                easing = LinearEasing
            ),
            label = "offsetY_${particle.id}"
        )

        val animatedOffsetX by animateFloatAsState(
            targetValue = if (trigger) particle.endX else 0f,
            animationSpec = tween(
                durationMillis = particle.duration,
                easing = LinearEasing
            ),
            label = "offsetX_${particle.id}"
        )

        val animatedAlpha by animateFloatAsState(
            targetValue = if (trigger) 0f else 1f,
            animationSpec = tween(
                durationMillis = particle.duration,
                delayMillis = particle.duration - 200
            ),
            label = "alpha_${particle.id}"
        )

        Box(
            modifier = modifier
                .offset { IntOffset(x.toInt(), y.toInt()) }
                .size(particle.size.dp)
                .background(particle.color)
                .alpha(animatedAlpha)
        )
    }
}

data class Particle(
    val id: Int,
    val size: Float,
    val color: Color,
    val angle: Float,
    val velocity: Float,
    val duration: Int
) {
    val endX: Float = (kotlin.math.cos(angle) * velocity * duration).toFloat()
    val endY: Float = (kotlin.math.sin(angle) * velocity * duration).toFloat()

    companion object {
        fun random(): Particle {
            val angle = (0..360).random() * (kotlin.math.PI / 180f)
            val velocity = 2f + Math.random().toFloat() * 3f
            val duration = 800 + (0..400).random()

            return Particle(
                id = (0..1000).random(),
                size = 4f + Math.random().toFloat() * 8f,
                color = Color(
                    red = (0..255).random(),
                    green = (0..255).random(),
                    blue = (0..255).random()
                ),
                angle = angle,
                velocity = velocity,
                duration = duration
            )
        }
    }
}
```

---

## Material 3 Compliance

### Color Scheme

```kotlin
// Achievement category colors (Material 3 compliant)
object AchievementColors {
    val progress = Color(0xFF4CAF50)    // Green
    val performance = Color(0xFFFFC107) // Amber
    val combo = Color(0xFFFF5722)       // Orange Red
    val streak = Color(0xFF2196F3)       // Blue
    val special = Color(0xFF9C27B0)      // Purple
}
```

### Typography

| Element | Style | Size | Weight |
|---------|-------|------|--------|
| Card Title | titleMedium | 16sp | SemiBold |
| Description | bodySmall | 12sp | Normal |
| Progress | labelSmall | 11sp | Medium |
| Dialog Title | headlineSmall | 24sp | Bold |
| Dialog Body | bodyMedium | 14sp | Normal |

### Spacing

| Element | Padding |
|---------|---------|
| Card inner | 16.dp |
| Icon size | 56.dp |
| Progress height | 8.dp |
| Dialog padding | 24.dp |
| Filter chip gap | 8.dp |

---

## Integration with LearningViewModel

```kotlin
// In LearningScreen.kt
@Composable
fun LearningScreen(
    // ... existing parameters ...
    achievementViewModel: AchievementViewModel = hiltViewModel()
) {
    // Collect unlock events
    val unlockEvents by achievementViewModel.unlockEvents
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Show notification overlay if there are unlock events
    AchievementNotificationOverlay(
        events = unlockEvents,
        onDismiss = { achievementViewModel.dismissNotification() }
    )

    // ... rest of LearningScreen UI ...
}
```

---

## Testing UI Components

### Compose UI Tests

```kotlin
// test/.../AchievementCardTest.kt
class AchievementCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun achievementCard_displaysProgress() {
        // Given
        val achievement = AchievementWithProgress(
            achievement = testAchievement,
            userProgress = UserAchievement(
                userId = "user1",
                achievementId = "test",
                progress = 42,
                target = 60
            )
        )

        // When
        composeTestRule.setContent {
            AchievementCard(
                achievementWithProgress = achievement,
                onClick = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("42 / 60")
            .assertIsDisplayed()
    }

    @Test
    fun achievementCard_showsUnlockedStatus() {
        // Given
        val achievement = AchievementWithProgress(
            achievement = testAchievement,
            userProgress = UserAchievement(
                userId = "user1",
                achievementId = "test",
                progress = 60,
                target = 60,
                isUnlocked = true
            )
        )

        // When
        composeTestRule.setContent {
            AchievementCard(
                achievementWithProgress = achievement,
                onClick = {}
            )
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Unlocked")
            .assertIsDisplayed()
    }
}
```

---

## Performance Considerations

1. **Lazy Loading**: Use `LazyColumn` with `key()` for stable list items
2. **Image Loading**: Achievement icons are emoji (no network calls)
3. **Animation**: Use `animateFloatAsState` for smooth, performant animations
4. **Recomposition**: Minimize by using `@Immutable` on data classes

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Next Review**: After Task #16 completion
