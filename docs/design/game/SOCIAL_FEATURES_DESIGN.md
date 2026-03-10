# Social Features Design - Wordland KET

**Document Version**: 1.0
**Date**: 2026-02-18
**Author**: Game Designer
**Related**: Task #29 - Social Elements Design (Phase 2)

---

## Executive Summary

Non-competitive social features designed to enhance learning motivation while maintaining a child-friendly, positive environment. The focus is on collective progress rather than individual competition.

**Core Principles**:
1. **Non-Competitive**: No leaderboards, no PVP, no direct comparison
2. **Privacy-First**: Anonymous data, COPPA compliant
3. **Positive Reinforcement**: Celebrate progress, not ranking
4. **No FOMO**: Avoid creating anxiety about missing out

---

## Feature 1: Global Learners Counter

### Purpose

Show children they are part of a larger learning community, reducing isolation and creating a sense of belonging.

### Design Specification

```
┌─────────────────────────────────────────┐
│                                         │
│    🌍 全球学习人数                       │
│                                         │
│         12,458                          │
│      名小朋友正在学习                    │
│                                         │
│    （和你一起探索单词世界！）             │
│                                         │
└─────────────────────────────────────────┘
```

### Display Locations

1. **Home Screen** - Below user profile
2. **Loading Screens** - During app initialization
3. **Achievement Screen** - Bottom of gallery

### Data Strategy

#### Phase 1: Simulated Data (MVP)

```kotlin
package com.wordland.domain.social

/**
 * Simulated global learners counter
 * Provides realistic numbers without requiring backend
 */
object SimulatedGlobalLearners {

    private val baseCount = 12458
    private val dailyGrowthRate = 0.02f  // 2% growth per day
    private val volatility = 50  // Random fluctuation

    /**
     * Get current global learner count
     * @param daysSinceInstall Days since app was first installed
     * @return Simulated learner count
     */
    fun getCurrentCount(daysSinceInstall: Int): Int {
        val growth = (baseCount * dailyGrowthRate * daysSinceInstall).toInt()
        val fluctuation = (Math.random() * volatility).toInt()
        return baseCount + growth + fluctuation
    }

    /**
     * Get recent activity count (learners in last hour)
     */
    fun getRecentActivity(): Int {
        // Simulate 50-200 learners active in last hour
        return 50 + (Math.random() * 150).toInt()
    }

    /**
     * Get today's new learners
     */
    fun getTodayNewLearners(): Int {
        // Simulate 20-100 new learners today
        return 20 + (Math.random() * 80).toInt()
    }
}
```

#### Phase 2: Real Data (Future)

```kotlin
/**
 * Global learners counter backed by real analytics
 * Requires privacy-compliant backend
 */
interface GlobalLearnersRepository {
    suspend fun getTotalLearners(): Result<Int>
    suspend fun getActiveLearners(hours: Int): Result<Int>
    suspend fun getDailyNewLearners(): Result<Int>
}

/**
 * Privacy note: Count only, no personal data exposed
 * - No usernames or identifiable information
 * - Aggregate count only
 * - COPPA compliant for children under 13
 */
```

### Update Frequency

| Context | Update Interval | Rationale |
|---------|----------------|-----------|
| Home Screen | Every 30 seconds | Creates sense of live activity |
| Loading Screen | Cached value | Fast loading |
| Achievement Screen | Once per session | Consistency |

### UI Component

```kotlin
package com.wordland.ui.components.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordland.domain.social.SimulatedGlobalLearners

@Composable
fun GlobalLearnersCard(
    daysSinceInstall: Int,
    modifier: Modifier = Modifier
) {
    var learnerCount by remember { mutableIntStateOf(0) }
    var activeCount by remember { mutableIntStateOf(0) }

    // Update every 30 seconds
    LaunchedEffect(Unit) {
        while (true) {
            learnerCount = SimulatedGlobalLearners.getCurrentCount(daysSinceInstall)
            activeCount = SimulatedGlobalLearners.getRecentActivity()
            kotlinx.coroutines.delay(30000)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🌍",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "全球学习人数",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main count
            Text(
                text = String.format("%,d", learnerCount),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "名小朋友正在学习",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "（和你一起探索单词世界！）",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent activity
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IndicatorDot()
                Text(
                    text = "过去一小时有 $activeCount 位小朋友在学习",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun IndicatorDot() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                color = Color(0xFF4CAF50),
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}
```

### Copywriting Guidelines

**Positive Examples** ✅:
- "名小朋友正在学习"
- "和你一起探索单词世界"
- "欢迎加入学习大家庭"

**Avoid** ❌:
- Don't create urgency ("快加入吧！")
- Don't use competition ("超过别人")
- Don't use FOMO ("别人都在学")

---

## Feature 2: Progress Percentile Display

### Purpose

Show learners how they compare to others in a positive, non-competitive way. The focus is on "you're doing great" rather than "you're better than others."

### Design Specification

```
┌─────────────────────────────────────────┐
│                                         │
│    📊 你的学习进度                       │
│                                         │
│         ━━━━━━━━━━━━━━━━━━━━━━         │
│         ●                               │
│                                         │
│      你超过了 75% 的学习伙伴             │
│                                         │
│      继续加油，你做得很好！              │
│                                         │
└─────────────────────────────────────────┘
```

### Calculation Logic

#### Percentile Formula

```kotlin
package com.wordland.domain.social

/**
 * Calculate user's percentile among all learners
 * Based on words learned (memory strength ≥ 80)
 */
object PercentileCalculator {

    /**
     * Get user's percentile (0-100)
     * @param userWordsMastered Number of words user has mastered
     * @return Percentile value (e.g., 75 means user is top 25%)
     */
    fun calculatePercentile(userWordsMastered: Int): Int {
        // Distribution curve (simulated based on typical learning patterns)
        // Percentile  Meaning
        // 0-20    : 0-5 words
        // 20-40   : 6-10 words
        // 40-60   : 11-20 words
        // 60-80   : 21-40 words
        // 80-100  : 40+ words

        return when {
            userWordsMastered >= 40 -> 80 + minOf(20, (userWordsMastered - 40) / 2)
            userWordsMastered >= 21 -> 60 + ((userWordsMastered - 21) * 20 / 19)
            userWordsMastered >= 11 -> 40 + ((userWordsMastered - 11) * 20 / 9)
            userWordsMastered >= 6 -> 20 + ((userWordsMastered - 6) * 20 / 4)
            userWordsMastered >= 1 -> (userWordsMastered * 20 / 5)
            else -> 0
        }.coerceIn(0, 100)
    }

    /**
     * Get positive message based on percentile
     */
    fun getMessage(percentile: Int): String {
        return when {
            percentile >= 90 -> "太棒了！你是学习先锋！"
            percentile >= 75 -> "你超过了大部分学习伙伴！"
            percentile >= 50 -> "你的进步很大，继续加油！"
            percentile >= 25 -> "稳扎稳打，每天进步一点点！"
            else -> "开始你的学习之旅吧！"
        }
    }

    /**
     * Get encouraging subtitle
     */
    fun getSubtitle(percentile: Int): String {
        return when {
            percentile >= 90 -> "继续保持，你是最棒的！"
            percentile >= 75 -> "再坚持一下，就能达到顶尖水平！"
            percentile >= 50 -> "你已经超过了一半的学习者！"
            percentile >= 25 -> "每一分努力都算数！"
            else -> "万事开头难，加油！"
        }
    }
}
```

### Display Strategy

| Percentile Range | Message Tone | Visual Indicator |
|------------------|--------------|------------------|
| 90-100 | 🌟 Outstanding | Gold theme, stars |
| 75-89 | 🔥 Excellent | Orange theme, fire |
| 50-74 | 💪 Good | Blue theme, upward arrow |
| 25-49 | 🌱 Growing | Green theme, sprout |
| 0-24 | 🎯 Starting | Purple theme, target |

### Anti-Competitive Measures

1. **No exact ranking**: Show percentile ranges, not exact position
2. **Positive framing**: "You're doing great" not "You beat X people"
3. **Progress focus**: Emphasize personal growth over comparison
4. **Opt-out option**: Allow users to hide this feature

### UI Component

```kotlin
package com.wordland.ui.components.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordland.domain.social.PercentileCalculator

@Composable
fun ProgressPercentileCard(
    wordsMastered: Int,
    modifier: Modifier = Modifier
) {
    val percentile = remember(wordsMastered) {
        PercentileCalculator.calculatePercentile(wordsMastered)
    }
    val message = remember(percentile) {
        PercentileCalculator.getMessage(percentile)
    }
    val subtitle = remember(percentile) {
        PercentileCalculator.getSubtitle(percentile)
    }
    val theme = remember(percentile) {
        getPercentileTheme(percentile)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.containerColor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = theme.icon,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "你的学习进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentile / 100f)
                        .height(8.dp)
                        .background(
                            color = theme.progressColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Percentile indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "●",
                    color = theme.progressColor,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "你超过了 $percentile% 的学习伙伴",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Immutable
private data class PercentileTheme(
    val icon: String,
    val containerColor: Color,
    val progressColor: Color,
    val textColor: Color
)

private fun getPercentileTheme(percentile: Int): PercentileTheme {
    return when {
        percentile >= 90 -> PercentileTheme(
            icon = "🌟",
            containerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
            progressColor = Color(0xFFFFD700),
            textColor = Color(0xFFF57F17)
        )
        percentile >= 75 -> PercentileTheme(
            icon = "🔥",
            containerColor = Color(0xFFFF6F00).copy(alpha = 0.2f),
            progressColor = Color(0xFFFF6F00),
            textColor = Color(0xFFE65100)
        )
        percentile >= 50 -> PercentileTheme(
            icon = "💪",
            containerColor = Color(0xFF2196F3).copy(alpha = 0.2f),
            progressColor = Color(0xFF2196F3),
            textColor = Color(0xFF0D47A1)
        )
        percentile >= 25 -> PercentileTheme(
            icon = "🌱",
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
            progressColor = Color(0xFF4CAF50),
            textColor = Color(0xFF1B5E20)
        )
        else -> PercentileTheme(
            icon = "🎯",
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.2f),
            progressColor = Color(0xFF9C27B0),
            textColor = Color(0xFF4A148C)
        )
    }
}
```

---

## Feature 3: Anonymous Daily Leaderboard

### Purpose

Create a sense of community activity without direct competition. Shows "how the community is doing today" rather than "you ranked X today."

### Design Specification

```
┌─────────────────────────────────────────┐
│  📅 今日学习排行榜        今天          │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  🥇 匿名学习者 #AB2F          45词  ││
│  │     今天学习最多的学习伙伴           ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  🥈 匿名学习者 #C8D1          38词  ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  🥉 匿名学习者 #E5A9          32词  ││
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │  🌟 你的排名                 15词   ││
│  │     前 40% | 继续加油！              ││
│  └─────────────────────────────────────┘│
│                                         │
│  ─────────────────────────────────────  │
│  匿名排行榜 | 仅显示今日 | 每天0点重置    │
└─────────────────────────────────────────┘
```

### Anonymity Strategy

**ID Generation**:
```kotlin
package com.wordland.domain.social

/**
 * Generate anonymous ID for leaderboard
 * Not traceable to real user
 */
object AnonymousIdGenerator {

    private val ADJECTIVES = listOf(
        "快乐", "聪明", "勤奋", "勇敢", "好奇",
        "耐心", "专注", "友好", "活泼", "安静"
    )

    private val ANIMALS = listOf(
        "熊猫", "兔子", "松鼠", "海豚", "企鹅",
        "猫咪", "小狗", "小鸟", "海龟", "考拉"
    )

    /**
     * Generate random anonymous ID
     * Format: [Adjective][Animal]#[HexCode]
     * Example: "快乐熊猫#AB2F"
     */
    fun generate(): String {
        val adjective = ADJECTIVES.random()
        val animal = ANIMALS.random()
        val hexCode = (1000..9999).random().toString(16).uppercase()

        return "$adjective$animal#$hexCode"
    }

    /**
     * Generate consistent ID for same user
     * Based on user hash but not reversible
     */
    fun generateForUser(userId: String): String {
        val hash = userId.hashCode().absoluteValue
        val adjectiveIndex = hash % ADJECTIVES.size
        val animalIndex = (hash / ADJECTIVES.size) % ANIMALS.size
        val hexCode = (hash % 9000 + 1000).toString(16).uppercase()

        return "${ADJECTIVES[adjectiveIndex]}${ANIMALS[animalIndex]}#$hexCode"
    }
}
```

### Ranking Logic

```kotlin
package com.wordland.domain.social

/**
 * Daily leaderboard entry
 */
@Immutable
data class LeaderboardEntry(
    val anonymousId: String,
    val wordsLearned: Int,
    val isCurrentUser: Boolean = false,
    val rank: Int = 0
)

/**
 * Anonymous leaderboard manager
 */
class AnonymousLeaderboardManager {

    private val dailyEntries = mutableListOf<LeaderboardEntry>()

    /**
     * Get today's leaderboard
     * @param userWordsLearned Current user's word count
     * @return Top 10 entries + user's position
     */
    fun getTodayLeaderboard(userWordsLearned: Int): LeaderboardResult {
        // Simulate other learners (in production, from backend)
        val simulatedEntries = generateSimulatedEntries()

        // Add current user
        val userEntry = LeaderboardEntry(
            anonymousId = AnonymousIdGenerator.generateForUser(getCurrentUserId()),
            wordsLearned = userWordsLearned,
            isCurrentUser = true
        )

        val allEntries = (simulatedEntries + userEntry)
            .sortedByDescending { it.wordsLearned }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }

        val top10 = allEntries.take(10)
        val userPosition = allEntries.find { it.isCurrentUser }

        // Calculate user percentile
        val userPercentile = if (userPosition != null && allEntries.size > 0) {
            ((allEntries.size - userPosition.rank + 1).toFloat() / allEntries.size * 100).toInt()
        } else {
            50
        }

        return LeaderboardResult(
            topEntries = topEntries,
            userEntry = userPosition,
            totalLearners = allEntries.size,
            userPercentile = userPercentile
        )
    }

    /**
     * Generate simulated leaderboard entries
     * Realistic distribution curve
     */
    private fun generateSimulatedEntries(): List<LeaderboardEntry> {
        val entries = mutableListOf<LeaderboardEntry>()

        // Generate 50-100 simulated learners
        val learnerCount = 50 + (Math.random() * 50).toInt()

        repeat(learnerCount) {
            val wordsLearned = generateRealisticWordCount()
            entries.add(
                LeaderboardEntry(
                    anonymousId = AnonymousIdGenerator.generate(),
                    wordsLearned = wordsLearned
                )
            )
        }

        return entries
    }

    /**
     * Generate realistic word count
     * Most users learn 5-20 words/day, fewer learn 30+
     */
    private fun generateRealisticWordCount(): Int {
        val rand = Math.random()
        return when {
            rand < 0.6 -> (5 + Math.random() * 15).toInt()  // 60% learn 5-20 words
            rand < 0.85 -> (20 + Math.random() * 15).toInt()  // 25% learn 20-35 words
            rand < 0.95 -> (35 + Math.random() * 15).toInt()  // 10% learn 35-50 words
            else -> (50 + Math.random() * 20).toInt()  // 5% learn 50+ words
        }
    }

    private fun getCurrentUserId(): String {
        return "user_001"  // From UserRepository
    }
}

@Immutable
data class LeaderboardResult(
    val topEntries: List<LeaderboardEntry>,
    val userEntry: LeaderboardEntry?,
    val totalLearners: Int,
    val userPercentile: Int
)
```

### Anti-Addiction Measures

1. **Daily Reset**: Leaderboard resets at midnight
2. **No Streak Pressure**: Don't show "you lost your streak"
3. **Positive Framing**: "继续加油" not "你落后了"
4. **Hidden by Default**: Requires user to open, not shown on home
5. **Limited Access**: Show maximum 3 times per day

```kotlin
/**
 * Limit leaderboard viewing to prevent obsession
 */
class LeaderboardAccessLimiter {

    private var viewCountToday = 0
    private var lastResetDate = getCurrentDate()

    /**
     * Check if user can view leaderboard
     */
    fun canView(): Boolean {
        resetIfNewDay()
        return viewCountToday < 3
    }

    /**
     * Record a view
     */
    fun recordView() {
        if (canView()) {
            viewCountToday++
        }
    }

    /**
     * Get remaining views
     */
    fun getRemainingViews(): Int {
        resetIfNewDay()
        return maxOf(0, 3 - viewCountToday)
    }

    private fun resetIfNewDay() {
        val today = getCurrentDate()
        if (today != lastResetDate) {
            viewCountToday = 0
            lastResetDate = today
        }
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(System.currentTimeMillis())
    }
}
```

### UI Component

```kotlin
package com.wordland.ui.components.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordland.domain.social.AnonymousLeaderboardManager
import com.wordland.domain.social.LeaderboardEntry
import com.wordland.domain.social.LeaderboardAccessLimiter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnonymousLeaderboardSheet(
    onDismiss: () -> Unit,
    userWordsLearned: Int,
    modifier: Modifier = Modifier
) {
    val accessLimiter = remember { LeaderboardAccessLimiter() }

    // Check if user can view
    if (!accessLimiter.canView()) {
        AccessLimitDialog(
            remainingViews = 0,
            onDismiss = onDismiss
        )
        return
    }

    // Record view
    LaunchedEffect(Unit) {
        accessLimiter.recordView()
    }

    // Get leaderboard data
    val leaderboardResult = remember(userWordsLearned) {
        AnonymousLeaderboardManager().getTodayLeaderboard(userWordsLearned)
    }

    // Remaining views
    val remainingViews = remember {
        accessLimiter.getRemainingViews()
    }

    // Sheet content
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📅 今日学习排行榜",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "今天",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Remaining views warning
        if (remainingViews < 3) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "今日剩余查看次数: $remainingViews/3",
                    modifier = Modifier.padding(8.dp, 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Leaderboard entries
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leaderboardResult.topEntries) { entry ->
                LeaderboardEntryItem(entry = entry)
            }

            // User's position (if not in top 10)
            item {
                leaderboardResult.userEntry?.let { userEntry ->
                    if (userEntry.rank > 10) {
                        LeaderboardEntryItem(
                            entry = userEntry,
                            showUserIndicator = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Footer
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "匿名排行榜 | 仅显示今日 | 每天0点重置",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Close button
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("关闭")
        }
    }
}

@Composable
private fun LeaderboardEntryItem(
    entry: LeaderboardEntry,
    showUserIndicator: Boolean = false
) {
    val backgroundColor = if (entry.isCurrentUser || showUserIndicator) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        when (entry.rank) {
            1 -> Color(0xFFFFD700).copy(alpha = 0.1f)  // Gold
            2 -> Color(0xFFC0C0C0).copy(alpha = 0.1f)  // Silver
            3 -> Color(0xFFCD7F32).copy(alpha = 0.1f)  // Bronze
            else -> MaterialTheme.colorScheme.surface
        }
    }

    val rankIcon = when (entry.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#${entry.rank}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Text(
                    text = rankIcon,
                    style = MaterialTheme.typography.titleLarge
                )

                // Anonymous ID
                Column {
                    Text(
                        text = entry.anonymousId,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal
                    )

                    if (entry.rank == 1) {
                        Text(
                            text = "今天学习最多的学习伙伴",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Word count
            Text(
                text = "${entry.wordsLearned}词",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // User indicator
        if (showUserIndicator) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "前 ${entry.userPercentile ?: 50}% | 继续加油！",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AccessLimitDialog(
    remainingViews: Int,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌟",
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "今天的查看次数用完啦",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "明天再来看看吧！学习更重要哦~",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("知道了")
                }
            }
        }
    }
}
```

---

## Integration Points

### Home Screen

```kotlin
// In HomeScreen.kt
@Composable
fun HomeScreen(
    // ... existing parameters
    daysSinceInstall: Int,
    wordsMastered: Int
) {
    LazyColumn {
        // ... existing content ...

        // Social features section (bottom of home)
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlobalLearnersCard(daysSinceInstall = daysSinceInstall)
                ProgressPercentileCard(wordsMastered = wordsMastered)
            }
        }
    }
}
```

### Profile Screen

```kotlin
// In ProgressScreen.kt
@Composable
fun ProgressScreen(
    // ... existing parameters
    onShowLeaderboard: () -> Unit
) {
    Column {
        // ... existing content ...

        // Leaderboard button
        Button(
            onClick = onShowLeaderboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("查看今日排行榜")
        }
    }
}
```

---

## Privacy & COPPA Compliance

### Data Protection

1. **No Personal Information**: Anonymous IDs only
2. **No Location Data**: No geographic tracking
3. **No Direct Messaging**: No communication between users
4. **Aggregate Data Only**: Counts and percentiles, not individual identities
5. **Parental Controls**: Option to disable all social features

### Parent Notification

```kotlin
/**
 * Parental control settings for social features
 */
data class SocialFeatureSettings(
    val showGlobalLearners: Boolean = true,
    val showPercentile: Boolean = true,
    val showLeaderboard: Boolean = true,
    val requireParentalConsent: Boolean = true
)
```

---

## Success Metrics

| Feature | Metric | Target |
|---------|--------|--------|
| Global Learners | Display views | >80% of users |
| Progress Percentile | Positive sentiment | >90% feedback |
| Leaderboard | Daily views (avg) | 1-2 per user |
| Leaderboard | Time spent | <2 minutes/session |

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Next Document**: NON_COMPETITIVE_MECHANICS.md
