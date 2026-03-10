# Achievement Screen Designs - Wordland KET

**Document Version**: 1.0
**Date**: 2026-02-18
**Author**: compose-ui-designer
**Related**: ACHIEVEMENT_SYSTEM_DESIGN.md, ACHIEVEMENT_UI_COMPONENTS.md
**Status**: Design Phase

---

## Executive Summary

This document defines the screen layouts for the achievement system in Wordland KET. All screens follow Material 3 navigation patterns and are optimized for children aged 10.

**Screens**:
1. AchievementGalleryScreen - Main achievement list
2. AchievementDetailScreen - Individual achievement details
3. AchievementNotificationQueue - Overlay for multiple unlocks

---

## 1. AchievementGalleryScreen

### Purpose
Main screen showing all achievements with filtering and progress tracking.

### Navigation Route
```
achievements
```

### Composable Signature

```kotlin
@Composable
fun AchievementGalleryScreen(
    viewModel: AchievementViewModel = viewModel(
        factory = AppServiceLocator.provideFactory()
    ),
    onNavigateBack: () -> Unit,
    onAchievementClick: (String) -> Unit
)
```

### Layout Structure

```
┌─────────────────────────────────────────────────────────────────┐
│ ← Achievements                                              [⏱] │ TopAppBar
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  Achievements              7/15 unlocked               47%  │   │ CompletionStatsBar
│  │  ████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ FilterBar
│  [All] [Progress] [Performance] [Combo] [Streak] [Special]     │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │ ✅ 🌟 First Steps            [BRONZE]               ⭐   │   │ AchievementCard
│  │ Complete your first level                                  │   │
│  │ Unlocked: Feb 10, 2026                                     │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │ ✅ 📚 Page Turner            [SILVER]              ⭐⭐  │   │ AchievementCard
│  │ Complete 5 levels                                          │   │
│  │ Unlocked: Feb 12, 2026                                     │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │ 💎 Perfectionist            [GOLD]               ⭐⭐⭐  │   │ AchievementCard
│  │ Complete a level with all 3-star words                     │   │
│  │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 0/1                    │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │ 🏆 Word Hunter               [GOLD]               ⭐⭐⭐  │   │ AchievementCard
│  │ Learn 60 words                                             │   │
│  │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 42/60          70%    │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │ 🔥 Combo Master              [SILVER]              ⭐⭐  │   │ AchievementCard
│  │ Reach a 5x combo                                           │   │
│  │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 3/5            60%    │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │ 🔒 No Hints Hero             [GOLD]               ⭐⭐⭐  │   │ AchievementCard
│  │ Complete level without hints                               │   │
│  │ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 0/6             0%    │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Notes

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.Achievement
import com.wordland.domain.model.AchievementCategory
import com.wordland.domain.model.UserAchievement
import com.wordland.ui.components.achievement.*
import com.wordland.ui.viewmodel.AchievementViewModel
import com.wordland.ui.uistate.AchievementUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementGalleryScreen(
    viewModel: AchievementViewModel = viewModel(
        factory = AppServiceLocator.provideFactory()
    ),
    onNavigateBack: () -> Unit,
    onAchievementClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAchievements()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Sort button (future enhancement)
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Sort,
                            contentDescription = "Sort"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AchievementUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is AchievementUiState.Success -> {
                    AchievementGalleryContent(
                        achievements = state.achievements,
                        userProgress = state.userProgress,
                        completionStats = state.completionStats,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = { viewModel.filterByCategory(it) },
                        onAchievementClick = onAchievementClick
                    )
                }

                is AchievementUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadAchievements() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementGalleryContent(
    achievements: List<Achievement>,
    userProgress: Map<String, UserAchievement>,
    completionStats: CompletionStats,
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit,
    onAchievementClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall progress header
        CompletionStatsBar(
            unlocked = completionStats.unlocked,
            total = completionStats.total
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter bar
        AchievementFilterBar(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Achievement list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Unlocked achievements first
            items(
                items = achievements.filter { achievement ->
                    userProgress[achievement.id]?.isUnlocked == true
                },
                key = { it.id }
            ) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    userProgress = userProgress[achievement.id],
                    onClick = { onAchievementClick(achievement.id) }
                )
            }

            // In-progress achievements
            items(
                items = achievements.filter { achievement ->
                    val progress = userProgress[achievement.id]
                    progress != null && !progress.isUnlocked
                },
                key = { it.id }
            ) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    userProgress = userProgress[achievement.id],
                    onClick = { onAchievementClick(achievement.id) }
                )
            }

            // Locked achievements
            items(
                items = achievements.filter { achievement ->
                    userProgress[achievement.id] == null
                },
                key = { it.id }
            ) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    userProgress = userProgress[achievement.id],
                    onClick = { onAchievementClick(achievement.id) }
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
```

---

## 2. AchievementDetailScreen

### Purpose
Detailed view of a single achievement with progress and tips.

### Navigation Route
```
achievements/{achievementId}
```

### Composable Signature

```kotlin
@Composable
fun AchievementDetailScreen(
    achievementId: String,
    viewModel: AchievementViewModel = viewModel(
        factory = AppServiceLocator.provideFactory()
    ),
    onNavigateBack: () -> Unit
)
```

### Layout Structure

```
┌─────────────────────────────────────────────────────────────────┐
│ ← Word Hunter                                                  │ TopAppBar
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│              🏆                                                   │ Icon
│                                                                 │
│           Word Hunter                [PLATINUM]              ⭐⭐⭐⭐ │ Tier badge
│                                                                 │
│           Learn 60 words                                         │ Description
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  Progress: 42/60 words (70%)                                │ │ Progress section
│  │  ╔═══════════════════════════════════════════════════════╗  │ │
│  │  ║████████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░║  │ │
│  │  ╚═══════════════════════════════════════════════════════╝  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  📊 How to unlock                                          │ │ How to unlock
│  │  • Master 60 unique words                                   │ │
│  │  • Memory strength must be ≥ 80                             │ │
│  │  • Currently: 42/60 words mastered                          │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  🎁 Reward                                                 │ │ Reward section
│  │  • 100 stars ⭐                                            │ │
│  │  • Badge: "Word Hunter" 🏆                                   │ │
│  │  • Shows on your profile                                   │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  📈 Your progress                                          │ │ Progress stats
│  │  • Started: Feb 10, 2026                                    │ │
│  │  • Average: 3.5 words/day                                  │ │
│  │  • Estimated: 5 days to complete                            │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  💡 Tips                                                   │ │ Tips section
│  │  • Review words regularly to increase memory strength      │ │
│  │  • Focus on one level at a time                            │ │
│  │  • Play every day to maintain your learning streak         │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│                                    ┌─────────────────────────┐   │ Share button
│                                    │   Share Achievement     │   │
│                                    └─────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Notes

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.Achievement
import com.wordland.domain.model.AchievementTier
import com.wordland.ui.components.achievement.*
import com.wordland.ui.viewmodel.AchievementViewModel
import com.wordland.ui.uistate.AchievementUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementDetailScreen(
    achievementId: String,
    viewModel: AchievementViewModel = viewModel(
        factory = AppServiceLocator.provideFactory()
    ),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(achievementId) {
        viewModel.loadAchievementDetail(achievementId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievement Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is AchievementUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .wrapContentSize(Alignment.Center)
                )
            }

            is AchievementUiState.DetailSuccess -> {
                AchievementDetailContent(
                    achievement = state.achievement,
                    userProgress = state.userProgress,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is AchievementUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadAchievementDetail(achievementId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .wrapContentSize(Alignment.Center)
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun AchievementDetailContent(
    achievement: Achievement,
    userProgress: UserAchievement?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isUnlocked = userProgress?.isUnlocked == true
    val progress = userProgress?.progress ?: 0
    val target = userProgress?.target ?: achievement.requirement.targetValue
    val progressPercent = if (target > 0) progress.toFloat() / target else 0f

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Achievement icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = getTierColor(achievement.tier).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            val iconAlpha = if (isUnlocked) 1f else 0.3f
            Text(
                text = if (isUnlocked) achievement.icon else "🔒",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .alpha(iconAlpha)
            )
        }

        // Achievement name with tier badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            AchievementBadge(tier = achievement.tier)
        }

        // Description
        Text(
            text = achievement.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        // Progress section
        if (!isUnlocked) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Progress: $progress/$target words (${(progressPercent * 100).toInt()}%)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = MaterialTheme.colorScheme.primary,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        } else {
            // Unlocked date
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✅",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Column {
                        Text(
                            text = "Achievement Unlocked!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        userProgress?.unlockedAt?.let { timestamp ->
                            Text(
                                text = "on ${formatDate(timestamp)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // How to unlock section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📊 How to unlock",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                when (val requirement = achievement.requirement) {
                    is com.wordland.domain.model.AchievementRequirement.CompleteLevels -> {
                        RequirementItem(
                            icon = "📚",
                            text = "Complete ${requirement.count} levels"
                        )
                        RequirementItem(
                            icon = "⭐",
                            text = "Minimum ${requirement.minStars} stars each"
                        )
                    }
                    is com.wordland.domain.model.AchievementRequirement.MasterWords -> {
                        RequirementItem(
                            icon = "📖",
                            text = "Master ${requirement.count} words"
                        )
                        RequirementItem(
                            icon = "🧠",
                            text = "Memory strength ≥ ${requirement.memoryStrengthThreshold}"
                        )
                    }
                    is com.wordland.domain.model.AchievementRequirement.ComboMilestone -> {
                        RequirementItem(
                            icon = "🔥",
                            text = "Reach a ${requirement.comboCount}x combo"
                        )
                    }
                    is com.wordland.domain.model.AchievementRequirement.StreakDays -> {
                        RequirementItem(
                            icon = "📅",
                            text = "Practice for ${requirement.days} days in a row"
                        )
                        RequirementItem(
                            icon = "✏️",
                            text = "At least ${requirement.minWordsPerDay} word per day"
                        )
                    }
                    else -> {
                        RequirementItem(
                            icon = "🎯",
                            text = "Meet the achievement criteria"
                        )
                    }
                }
            }
        }

        // Reward section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎁 Reward",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                RewardDisplay(reward = achievement.reward)
            }
        }

        // Progress stats (if in progress)
        if (!isUnlocked && userProgress != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📈 Your progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Divider()

                    // Calculate estimated completion
                    val daysElapsed = if (userProgress.lastUpdated > 0) {
                        (System.currentTimeMillis() - userProgress.lastUpdated) / (24 * 60 * 60 * 1000)
                    } else 0

                    val wordsPerDay = if (daysElapsed > 0) {
                        progress.toFloat() / daysElapsed.toFloat()
                    } else 0f

                    val remainingWords = target - progress
                    val estimatedDays = if (wordsPerDay > 0) {
                        (remainingWords / wordsPerDay).toInt()
                    } else 0

                    StatRow("Started", formatDate(userProgress.lastUpdated))
                    StatRow("Average", String.format("%.1f words/day", wordsPerDay))
                    StatRow("Estimated", "~$estimatedDays days to complete")
                }
            }
        }

        // Tips section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                getTipsForAchievement(achievement).forEach { tip ->
                    Text(
                        text = "• $tip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Share button
        Button(
            onClick = { /* TODO: Share achievement */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Share,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share Achievement",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RequirementItem(
    icon: String,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    return sdf.format(Date(timestamp))
}

private fun getTierColor(tier: AchievementTier): Color {
    return when (tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32)
        AchievementTier.SILVER -> Color(0xFFC0C0C0)
        AchievementTier.GOLD -> Color(0xFFFFD700)
        AchievementTier.PLATINUM -> Color(0xFFE5E4E2)
    }
}

private fun getTipsForAchievement(achievement: Achievement): List<String> {
    return when (achievement.id) {
        "word_hunter", "word_collector_50", "word_collector_100" -> listOf(
            "Review words regularly to increase memory strength",
            "Focus on one level at a time",
            "Play every day to maintain your learning streak"
        )
        "perfectionist" -> listOf(
            "Take your time on each question",
            "Think carefully before answering",
            "Use hints only when really needed"
        )
        "combo_master", "unstoppable" -> listOf(
            "Maintain focus during your session",
            "Answer at a steady pace",
            "Avoid rushing through questions"
        )
        "dedicated_student", "week_warrior" -> listOf(
            "Set a daily learning goal",
            "Try to practice at the same time each day",
            "Even 5 minutes counts toward your streak!"
        )
        else -> listOf(
            "Keep practicing to improve your skills",
            "Consistency is key to mastery"
        )
    }
}
```

---

## 3. AchievementNotificationQueue

### Purpose
Overlay notification queue for multiple achievement unlocks during gameplay.

### Composable Signature

```kotlin
@Composable
fun AchievementNotificationQueue(
    notifications: List<Achievement>,
    onDismiss: (String) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

### Layout Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                         (Gameplay)                              │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ 🎉 Perfectionist unlocked!                          [×] [→] │ ← Most recent
│  └───────────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ 🎉 Combo Master unlocked!                           [×] [→] │
│  └───────────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ 🎉 First Steps unlocked!                            [×] [→] │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Notes

```kotlin
@Composable
fun AchievementNotificationQueue(
    notifications: List<Achievement>,
    onDismiss: (String) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleNotifications = notifications.take(3) // Max 3 visible

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        visibleNotifications.forEach { achievement ->
            AchievementNotification(
                achievement = achievement,
                onDismiss = { onDismiss(achievement.id) },
                onClick = { onClick(achievement.id) }
            )
        }

        if (notifications.size > 3) {
            // "View all" button
            TextButton(onClick = { /* Navigate to achievements */ }) {
                Text(
                    text = "View all ${notifications.size} notifications",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

---

## 4. ViewModel State Management

### UI State Definition

```kotlin
sealed class AchievementUiState {
    object Loading : AchievementUiState()

    data class Success(
        val achievements: List<Achievement>,
        val userProgress: Map<String, UserAchievement>,
        val completionStats: CompletionStats,
        val selectedCategory: AchievementCategory? = null
    ) : AchievementUiState()

    data class DetailSuccess(
        val achievement: Achievement,
        val userProgress: UserAchievement?
    ) : AchievementUiState()

    data class Error(
        val message: String
    ) : AchievementUiState()

    data class ShowUnlock(
        val achievement: Achievement
    ) : AchievementUiState()
}
```

---

## 5. Navigation Integration

### Add to SetupNavGraph.kt

```kotlin
// Achievement Gallery Screen
composable(route = "achievements") {
    AchievementGalleryScreen(
        onNavigateBack = { navController.popBackStack() },
        onAchievementClick = { achievementId ->
            navController.navigate("achievements/$achievementId")
        }
    )
}

// Achievement Detail Screen
composable(
    route = "achievements/{achievementId}",
    arguments = listOf(
        navArgument("achievementId") { type = NavType.StringType }
    )
) { backStackEntry ->
    val achievementId = backStackEntry.arguments?.getString("achievementId") ?: ""
    AchievementDetailScreen(
        achievementId = achievementId,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

---

## 6. Responsive Layout Guidelines

### Screen Size Adaptations

| Screen Width | Layout Changes |
|--------------|----------------|
| < 360dp (small phones) | Single column, smaller icons, reduced padding |
| 360dp - 600dp (phones) | Standard layout as specified |
| 600dp - 840dp (foldables) | Larger icons, more spacing |
| > 840dp (tablets) | Two-column achievement list |

### Landscape Adaptations

- Hide top app bar title
- Move stats to side panel
- Two-column achievement layout

---

## 7. Dark Mode Support

All components use Material 3 color schemes which automatically adapt:

```kotlin
// Example: Achievement card background
colors = CardDefaults.cardColors(
    containerColor = if (isUnlocked) {
        MaterialTheme.colorScheme.primaryContainer  // Adapts to dark mode
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
)
```

---

## 8. Accessibility Features

- Screen reader descriptions for all interactive elements
- Minimum touch target size of 48dp
- High contrast ratios (WCAG AA compliant)
- Scalable text support
- Keyboard navigation support

---

## 9. Animation Performance Targets

| Animation | Target FPS | Max Duration |
|-----------|-----------|--------------|
| Popup entry | 60fps | 300ms |
| Confetti | 60fps | 1500ms |
| Card tap | 60fps | 150ms |
| Progress bar | 60fps | 500ms |
| Filter select | 60fps | 200ms |

---

## 10. Testing Checklist

### AchievementGalleryScreen
- [ ] Loading state displays correctly
- [ ] Error state shows retry button
- [ ] Filter chips filter achievements correctly
- [ ] Completion stats calculate correctly
- [ ] Card tap navigates to detail
- [ ] Back button returns to previous screen

### AchievementDetailScreen
- [ ] All achievement types display correctly
- [ ] Progress shows for in-progress achievements
- [ ] Unlocked date shows for completed
- [ ] Tips display per achievement type
- [ ] Share button is visible
- [ ] Scroll works for long content

### AchievementNotificationQueue
- [ ] Multiple notifications stack correctly
- [ ] Dismiss button removes individual notification
- [ ] Click navigates to detail
- [ ] Max 3 notifications visible
- [ ] "View all" button appears when >3

---

## 11. Future Enhancements

### Sorting Options
- By progress (nearest completion first)
- By difficulty (easiest first)
- By recent unlock
- By category

### Search
- Search achievement names
- Search descriptions
- Filter by completion status

### Social Features
- Share achievement image
- Compare progress with friends
- Leaderboards for achievement points

---

**Next Steps**:
1. Implement AchievementViewModel
2. Create AchievementRepository
3. Add integration tests
4. Verify on real device

**Document Version**: 1.0
**Last Updated**: 2026-02-18
