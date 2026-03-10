# Achievement UI Components Design - Wordland KET

**Document Version**: 1.0
**Date**: 2026-02-18
**Author**: compose-ui-designer
**Related**: ACHIEVEMENT_SYSTEM_DESIGN.md, ACHIEVEMENT_SYSTEM_ARCHITECTURE.md
**Status**: Design Phase

---

## Executive Summary

This document defines all UI components for the achievement system in Wordland KET. All components follow Material 3 design principles and are optimized for children aged 10.

**Design Goals**:
1. **Celebratory**: Unlock notifications feel rewarding and exciting
2. **Clear**: Progress is visually obvious and easy to understand
3. **Responsive**: Smooth animations at 60fps
4. **Accessible**: High contrast, tap-friendly targets

---

## Component Architecture

```
ui/components/achievement/
├── AchievementPopup.kt          # Full-screen unlock notification
├── AchievementCard.kt           # List item for achievement gallery
├── AchievementBadge.kt          # Tier badge (Bronze/Silver/Gold/Platinum)
├── AchievementProgress.kt       # Progress bar component
├── AchievementIcon.kt           # Animated icon container
├── AchievementNotification.kt   # Top-bar notification
├── AchievementFilterBar.kt      # Category filter chips
└── CompletionStatsBar.kt        # Overall progress header
```

---

## 1. AchievementPopup (Unlock Notification)

### Purpose
Full-screen celebratory dialog shown when an achievement is unlocked.

### Composable Signature

```kotlin
@Composable
fun AchievementPopup(
    achievement: Achievement,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissDelay: Long = 5000L  // 5 seconds
)
```

### Design Specifications

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                     ┌─────────────────────────┐                 │
│                     │                         │                 │
│                     │       🎉 ACHIEVEMENT      │                 │
│                     │       UNLOCKED! 🎉       │                 │
│                     │                         │                 │
│                     └─────────────────────────┘                 │
│                                                                 │
│                           💎 Perfectionist                      │
│                                                                 │
│              Complete a level with all 3-star words             │
│                                                                 │
│         ╔═══════════════════════════════════════════╗           │
│         ║           ⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐            ║           │
│         ║              ★★★★★★★★★★             ║           │
│         ╚═══════════════════════════════════════════╝           │
│                                                                 │
│                  You earned 30 stars! ⭐                         │
│                                                                 │
│                           ┌─────────┐                           │
│                           │ Awesome! │                           │
│                           └─────────┘                           │
│                                                                 │
│                      Tap anywhere to continue                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Animation Timing

| Phase | Duration | Animation |
|-------|----------|-----------|
| Fade In | 200ms | Alpha 0 → 1 |
| Scale Up | 300ms | Scale 0.5 → 1.0 (spring) |
| Confetti | 800ms | Particle explosion from center |
| Pulse | Continuous | Icon gentle pulse (1.02 ↔ 0.98) |
| Fade Out | 150ms | Alpha 1 → 0 (on dismiss) |

### Implementation Notes

```kotlin
@Composable
fun AchievementPopup(
    achievement: Achievement,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss after 5 seconds
    LaunchedEffect(achievement.id) {
        delay(5000L)
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
        // Confetti particles overlay
        ConfettiOverlay()

        // Main content card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title banner
                AchievementUnlockTitle()

                Spacer(modifier = Modifier.height(8.dp))

                // Achievement icon with pulse animation
                AchievementIcon(
                    icon = achievement.icon,
                    tier = achievement.tier,
                    size = 80.dp
                )

                // Achievement name
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )

                // Achievement description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reward display
                RewardDisplay(reward = achievement.reward)

                Spacer(modifier = Modifier.height(16.dp))

                // Continue button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Awesome!",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dismiss hint
                Text(
                    text = "Tap anywhere to continue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}
```

---

## 2. AchievementCard (List Item)

### Purpose
Individual achievement card for the achievement gallery screen. Shows locked/unlocked state and progress.

### Composable Signature

```kotlin
@Composable
fun AchievementCard(
    achievement: Achievement,
    userProgress: UserAchievement?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### Design Specifications

**Unlocked State**:
```
┌─────────────────────────────────────────────────────────────┐
│  ✅ 🏆 Word Hunter           [PLATINUM]              ⭐⭐⭐⭐  │
│                                                             │
│  Learn 60 words                                            │
│                                                             │
│  Unlocked: Feb 15, 2026                                     │
│  Reward: 100 stars + Badge                                  │
└─────────────────────────────────────────────────────────────┘
```

**In Progress State**:
```
┌─────────────────────────────────────────────────────────────┐
│  🏆 Word Hunter               [GOLD]                   ⭐⭐⭐  │
│                                                             │
│  Learn 60 words                                            │
│                                                             │
│  ████████████████████░░░░░░░░  42/60  (70%)                 │
└─────────────────────────────────────────────────────────────┘
```

**Locked State**:
```
┌─────────────────────────────────────────────────────────────┐
│  🔒 Word Hunter              [GOLD]                   ⭐⭐⭐  │
│                                                             │
│  ???                                                        │
│                                                             │
│  ░░░░░░░░░░░░░░░░░░░░░░  0/60                             │
└─────────────────────────────────────────────────────────────┘
```

### Implementation Notes

```kotlin
@Composable
fun AchievementCard(
    achievement: Achievement,
    userProgress: UserAchievement?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = userProgress?.isUnlocked == true
    val isLocked = userProgress == null
    val progress = userProgress?.progress ?: 0
    val target = userProgress?.target ?: achievement.requirement.targetValue
    val progressPercent = if (target > 0) progress.toFloat() / target else 0f

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isUnlocked -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isUnlocked) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (isLocked) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            getTierColor(achievement.tier).copy(alpha = 0.2f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val iconAlpha = if (isLocked) 0.3f else 1f
                Text(
                    text = if (isLocked) "🔒" else achievement.icon,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.alpha(iconAlpha)
                )

                // Unlocked checkmark
                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .align(Alignment.BottomEnd)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLocked) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    // Tier badge
                    AchievementBadge(tier = achievement.tier)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Description or locked message
                Text(
                    text = when {
                        isLocked -> "???"
                        isUnlocked -> achievement.description
                        else -> achievement.description
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (isLocked) 0.5f else 0.7f
                    )
                )

                // Progress bar (for in-progress)
                if (!isUnlocked && !isLocked) {
                    Spacer(modifier = Modifier.height(8.dp))

                    AchievementProgressBar(
                        progress = progressPercent,
                        current = progress,
                        target = target
                    )
                }

                // Unlocked date
                if (isUnlocked && userProgress?.unlockedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlocked: ${formatDate(userProgress.unlockedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Stars indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(achievement.tier.stars) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (isUnlocked || !isLocked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
```

---

## 3. AchievementBadge (Tier Indicator)

### Purpose
Small badge indicating achievement tier (Bronze/Silver/Gold/Platinum).

### Composable Signature

```kotlin
@Composable
fun AchievementBadge(
    tier: AchievementTier,
    modifier: Modifier = Modifier
)
```

### Design Specifications

| Tier | Color | Label |
|------|-------|-------|
| Bronze | `#CD7F32` | Bronze |
| Silver | `#C0C0C0` | Silver |
| Gold | `#FFD700` | Gold |
| Platinum | `#E5E4E2` | Platinum |

### Implementation Notes

```kotlin
@Composable
fun AchievementBadge(
    tier: AchievementTier,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32) to "Bronze"
        AchievementTier.SILVER -> Color(0xFFC0C0C0) to "Silver"
        AchievementTier.GOLD -> Color(0xFFFFD700) to "Gold"
        AchievementTier.PLATINUM -> Color(0xFFE5E4E2) to "Platinum"
    }

    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}
```

---

## 4. AchievementProgressBar

### Purpose
Visual progress indicator for achievement progress.

### Composable Signature

```kotlin
@Composable
fun AchievementProgressBar(
    progress: Float,  // 0.0 to 1.0
    current: Int,
    target: Int,
    modifier: Modifier = Modifier
)
```

### Design Specifications

```
████████████████████░░░░░  42/60  (70%)

|<- 100% ->|
```

### Implementation Notes

```kotlin
@Composable
fun AchievementProgressBar(
    progress: Float,
    current: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            color = MaterialTheme.colorScheme.primary,
            drawStopIndicator = { _, _ -> }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$current/$target",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

---

## 5. AchievementNotification (Top Bar)

### Purpose
Non-intrusive top notification for achievement unlocks. Less disruptive than full popup.

### Composable Signature

```kotlin
@Composable
fun AchievementNotification(
    achievement: Achievement,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### Design Specifications

```
┌─────────────────────────────────────────────────────────────────┐
│ 🎉 Perfectionist unlocked!                              [×] [→] │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Notes

```kotlin
@Composable
fun AchievementNotification(
    achievement: Achievement,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${achievement.name} unlocked!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
}
```

---

## 6. AchievementFilterBar

### Purpose
Category filter chips for achievement gallery.

### Composable Signature

```kotlin
@Composable
fun AchievementFilterBar(
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit,
    modifier: Modifier = Modifier
)
```

### Design Specifications

```
[All] [Progress] [Performance] [Combo] [Streak] [Special]
 ↑ selected
```

### Implementation Notes

```kotlin
@Composable
fun AchievementFilterBar(
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        null to "All",
        AchievementCategory.PROGRESS to "Progress",
        AchievementCategory.PERFORMANCE to "Performance",
        AchievementCategory.COMBO to "Combo",
        AchievementCategory.STREAK to "Streak",
        AchievementCategory.SPECIAL to "Special"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (category, label) ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(label) },
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )
        }
    }
}
```

---

## 7. CompletionStatsBar

### Purpose
Header showing overall achievement completion stats.

### Composable Signature

```kotlin
@Composable
fun CompletionStatsBar(
    unlocked: Int,
    total: Int,
    modifier: Modifier = Modifier
)
```

### Design Specifications

```
┌─────────────────────────────────────────────────────────────────┐
│  Achievements                     7/15 unlocked              47%   │
│  ████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░    │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Notes

```kotlin
@Composable
fun CompletionStatsBar(
    unlocked: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) unlocked.toFloat() / total else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$unlocked/$total unlocked",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(percentage * 100).toInt()}% complete",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )
        }
    }
}
```

---

## 8. ConfettiOverlay

### Purpose
Particle effect for achievement unlock celebration.

### Composable Signature

```kotlin
@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier
)
```

### Implementation Notes

```kotlin
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")

    // Create multiple particles with different animations
    val particles = remember { listOf(1..30).map { it } }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        particles.forEach { index ->
            val offsetX by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000 + (index * 50),
                    easing = LinearEasing
                ),
                label = "offset_$index"
            )

            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000 + (index * 50),
                    easing = FastOutSlowInEasing
                ),
                label = "offsetY_$index"
            )

            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = LinearEasing
                ),
                label = "rotation_$index"
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 1500
                    0f at 0 using LinearEasing
                    1f at 300 using LinearEasing
                    1f at 800 using LinearEasing
                    0f at 1500 using LinearEasing
                },
                label = "alpha_$index"
            )

            val (size, color) = when (index % 4) {
                0 -> 12.dp to MaterialTheme.colorScheme.primary
                1 -> 10.dp to MaterialTheme.colorScheme.secondary
                2 -> 14.dp to MaterialTheme.colorScheme.tertiary
                else -> 8.dp to Color(0xFFFFD700) // Gold
            }

            Box(
                modifier = Modifier
                    .offset(
                        x = LocalDensity.current.run { (offsetX * 200).toDp() },
                        y = LocalDensity.current.run { (offsetY * 400).toDp() }
                    )
                    .rotate(rotation)
                    .size(size)
                    .alpha(alpha)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
        }
    }
}
```

---

## 9. RewardDisplay

### Purpose
Display reward information in achievement popup.

### Composable Signature

```kotlin
@Composable
fun RewardDisplay(
    reward: AchievementReward,
    modifier: Modifier = Modifier
)
```

### Implementation Notes

```kotlin
@Composable
fun RewardDisplay(
    reward: AchievementReward,
    modifier: Modifier = Modifier
) {
    when (reward) {
        is AchievementReward.Stars -> {
            Surface(
                modifier = modifier,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "You earned ${reward.amount} stars!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        is AchievementReward.Title -> {
            Surface(
                modifier = modifier,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Title: ${reward.displayName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        is AchievementReward.Badge -> {
            Surface(
                modifier = modifier,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reward.iconName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Badge: ${reward.displayName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        is AchievementReward.PetUnlock -> {
            Surface(
                modifier = modifier,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reward.petIcon,
                        style = MaterialTheme.typography.displaySmall
                    )
                    Column {
                        Text(
                            text = "Pet Unlocked!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = reward.petName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        is AchievementReward.Multiple -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reward.rewards.forEach { subReward ->
                    RewardDisplay(reward = subReward)
                }
            }
        }
    }
}
```

---

## 10. Animation Summary

| Animation | Duration | Easing | Trigger |
|-----------|----------|--------|---------|
| Popup Fade In | 200ms | EaseOut | On show |
| Popup Scale | 300ms | Spring (0.4f, 400f) | On show |
| Icon Pulse | Continuous (800ms loop) | Linear | Idle |
| Confetti | 1500ms | Linear/Decay | On show |
| Card Tap | 150ms | Spring | On click |
| Progress Bar | 500ms | EaseOut | On value change |
| Filter Chip | 200ms | EaseInOut | On select |

---

## Color Palette

| Usage | Color |
|-------|-------|
| Bronze Tier | `#CD7F32` |
| Silver Tier | `#C0C0C0` |
| Gold Tier | `#FFD700` |
| Platinum Tier | `#E5E4E2` |
| Progress Bar | MaterialTheme.colorScheme.primary |
| Progress Track | MaterialTheme.colorScheme.surfaceVariant |
| Confetti Primary | MaterialTheme.colorScheme.primary |
| Confetti Secondary | MaterialTheme.colorScheme.secondary |
| Confetti Gold | `#FFD700` |

---

## Accessibility

| Component | Support |
|-----------|---------|
| Screen Reader | All actions labeled |
| Touch Target | Min 48dp |
| Color Contrast | WCAG AA compliant |
| Font Scaling | Supports text scale |
| Talkback | Content descriptions |

---

## Testing Checklist

- [ ] AchievementPopup displays correctly with mock data
- [ ] Auto-dismiss works after 5 seconds
- [ ] Confetti animation plays smoothly
- [ ] AchievementCard shows correct states (locked/in-progress/unlocked)
- [ ] Progress bar calculates percentages correctly
- [ ] Filter bar filters achievements by category
- [ ] All animations maintain 60fps
- [ ] Dark mode colors are correct
- [ ] Text scaling works properly
- [ ] Touch targets meet minimum size requirements

---

**Next Steps**:
1. Create AchievementScreen.kt (full gallery view)
2. Integrate with AchievementViewModel
3. Add instrumentation tests
4. Verify on real device

**Document Version**: 1.0
**Last Updated**: 2026-02-18
