# Non-Competitive Mechanics Design - Wordland KET

**Document Version**: 1.0
**Date**: 2026-02-18
**Author**: Game Designer
**Related**: Task #29 - Social Elements Design (Phase 2)

---

## Executive Summary

A comprehensive framework for non-competitive game mechanics that prioritize learning, personal growth, and positive reinforcement over competition and ranking.

**Why Non-Competitive?**
- Children aged 6-12 are developing self-esteem
- Competition can create anxiety and discourage struggling learners
- Focus should be on personal progress, not beating others
- Collaborative learning is more effective than competitive

---

## Core Principles

### 1. Personal Progress Focus

**Definition**: Success is measured by individual growth, not comparison to others.

**Implementation**:
```
✅ DO:
- Show progress graphs over time
- Celebrate personal bests
- Highlight improvement milestones
- Compare to past self, not others

❌ DON'T:
- Show rankings against other users
- Display "you beat X people"
- Highlight failures or low ranks
- Create urgency through competition
```

### 2. Positive Reinforcement

**Definition**: All feedback should be encouraging, regardless of performance.

**Implementation**:
```kotlin
/**
 * Positive feedback generator
 * Always finds something to celebrate
 */
object PositiveFeedbackGenerator {

    fun getFeedback(result: LearningResult): FeedbackMessage {
        return when {
            result.isPerfect -> FeedbackMessage(
                title = "完美！",
                message = listOf(
                    "你太棒了！全部正确！",
                    "继续保持！",
                    "你就是学习天才！"
                ).random(),
                confetti = true
            )
            result.hasImprovement -> FeedbackMessage(
                title = "进步啦！",
                message = "比上次做得更好，继续加油！",
                sparkles = true
            )
            result.isCorrect -> FeedbackMessage(
                title = "答对了！",
                message = "很好！再接再厉！",
                checkmark = true
            )
            result.hasEffort -> FeedbackMessage(
                title = "再试一次！",
                message = "努力就会有进步！",
                encouragement = true
            )
            else -> FeedbackMessage(
                title = "加油！",
                message = "每次练习都是进步！",
                neutral = true
            )
        }
    }
}
```

### 3. No FOMO Design

**Definition**: Features should not create anxiety about missing out on rewards or events.

**Implementation**:
```
❌ AVOID:
- "Limited time only! Ends in 2 hours!"
- "Your streak will reset in 1 hour!"
- "Everyone else got the badge!"
- "Don't miss out!"

✅ INSTEAD:
- "Come back anytime to continue learning"
- "Streak saved with 2 grace days"
- "Achievements are always available"
- "Learn at your own pace"
```

### 4. Collaborative Over Competitive

**Definition**: Frame progress as collective achievement rather than individual ranking.

**Implementation**:
```
❌ COMPETITIVE:
"你排名第 25，超过了 75% 的用户"

✅ COLLABORATIVE:
"全球 12,458 名小朋友和你一起学习！"

❌ COMPETITIVE:
"打败你的朋友！"

✅ COLLABORATIVE:
"邀请朋友一起学习！"
```

---

## Mechanics by Category

### Progress Mechanics

#### 1. Personal Growth Graph

```kotlin
/**
 * Personal progress visualization
 * Shows user's progress over time, not comparison to others
 */
@Composable
fun PersonalGrowthGraph(
    progressHistory: List<DailyProgress>,
    modifier: Modifier = Modifier
) {
    // Line chart showing words learned over time
    // Always shows upward trend or flat, never "decline"
    // Focus on cumulative achievement, not daily fluctuation

    Card(modifier = modifier) {
        Column {
            Text("你的学习旅程")

            // Progress chart
            ProgressLineChart(
                data = progressHistory.map { it.totalWords },
                labels = progressHistory.map { it.date }
            )

            // Milestone markers
            MilestoneTimeline(
                milestones = progressHistory.filter { it.hasMilestone }
            )

            // Encouraging message
            Text(
                getMessageForProgress(progressHistory.last().totalWords),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun getMessageForProgress(totalWords: Int): String {
    return when {
        totalWords >= 100 -> "你已经掌握了100个单词！太厉害了！"
        totalWords >= 50 -> "一半啦！继续加油！"
        totalWords >= 20 -> "很好的开始！坚持下去！"
        else -> "万事开头难，你已经迈出了第一步！"
    }
}
```

#### 2. Milestone Celebrations

```kotlin
/**
 * Non-competitive milestone system
 * Celebrates personal achievements at meaningful intervals
 */
object MilestoneDefinitions {

    val MILESTONES = listOf(
        Milestone(
            id = "first_word",
            threshold = 1,
            title = "第一步",
            message = "学会了第一个单词！",
            icon = "🌱",
            animation = AnimationType.SPROUT
        ),
        Milestone(
            id = "ten_words",
            threshold = 10,
            title = "入门",
            message = "学会了10个单词！",
            icon = "📚",
            animation = AnimationType.BOOK_OPEN
        ),
        Milestone(
            id = "fifty_words",
            threshold = 50,
            title = "积累",
            message = "掌握了50个单词！",
            icon = "🌟",
            animation = AnimationType.STAR_BURST
        ),
        Milestone(
            id = "hundred_words",
            threshold = 100,
            title = "成就",
            message = "学会了100个单词！",
            icon = "🏆",
            animation = AnimationType.TROPHY_RISE
        )
    )
}

data class Milestone(
    val id: String,
    val threshold: Int,
    val title: String,
    val message: String,
    val icon: String,
    val animation: AnimationType
)

enum class AnimationType {
    SPROUT, BOOK_OPEN, STAR_BURST, TROPHY_RISE
}
```

### Feedback Mechanics

#### 1. Constructive Error Handling

```kotlin
/**
 * Positive error feedback
 * Frames mistakes as learning opportunities
 */
object ConstructiveFeedback {

    fun getFeedbackForWrongAnswer(
        wrongAnswer: String,
        correctAnswer: String,
        attempts: Int
    ): WrongAnswerFeedback {
        return when {
            attempts == 1 -> WrongAnswerFeedback(
                title = "再想想",
                message = "提示：首字母是 '${correctAnswer[0]}'",
                encouragement = "你可以的！",
                showHint = true
            )
            attempts == 2 -> WrongAnswerFeedback(
                title = "很接近了",
                message = "提示：单词长度是 ${correctAnswer.length} 个字母",
                encouragement = "再试一次！",
                showHint = true
            )
            attempts == 3 -> WrongAnswerFeedback(
                title = "没关系",
                message = "正确答案是：$correctAnswer",
                encouragement = "下次就能记住啦！",
                showAnswer = true
            )
            else -> WrongAnswerFeedback(
                title = "加油",
                message = "多练习几次就能掌握！",
                encouragement = "每个单词都值得学习！",
                showAnswer = false
            )
        }
    }
}

data class WrongAnswerFeedback(
    val title: String,
    val message: String,
    val encouragement: String,
    val showHint: Boolean = false,
    val showAnswer: Boolean = false
)
```

#### 2. Growth Mindset Messaging

```kotlin
/**
 * Growth mindset reinforcement
 * Emphasizes effort and learning over innate ability
 */
object GrowthMindsetMessages {

    val EFFORT_MESSAGES = listOf(
        "努力比天赋更重要！",
        "每次练习都让你更强！",
        "大脑越练越聪明！",
        "坚持就是胜利！",
        "今天比昨天更好！"
    )

    val MISTAKE_MESSAGES = listOf(
        "错误是学习的一部分！",
        "从错误中学习！",
        "每次错误都是进步的机会！",
        "不要怕犯错！"
    )

    val PROGRESS_MESSAGES = listOf(
        "你在不断进步！",
        "看得见你的努力！",
        "你的进步很明显！",
        "继续这样学习下去！"
    )

    fun getRandomEffortMessage(): String = EFFORT_MESSAGES.random()
    fun getRandomMistakeMessage(): String = MISTAKE_MESSAGES.random()
    fun getRandomProgressMessage(): String = PROGRESS_MESSAGES.random()
}
```

### Streak Mechanics (Anti-Burnout)

#### 1. Graceful Streak System

```kotlin
/**
 * Child-friendly streak system with grace days
 * Prevents anxiety from "losing" streaks
 */
class GracefulStreakManager {

    companion object {
        private const val GRACE_DAYS = 2  // 2 free days
        private const val MAX_FREEZE = 5  // Can accumulate 5 frozen days
    }

    data class StreakState(
        val currentStreak: Int,
        val longestStreak: Int,
        val frozenDaysUsed: Int,
        val freezeDaysAvailable: Int,
        val lastActiveDate: Long
    )

    /**
     * Calculate new streak after absence
     * Uses grace days before breaking streak
     */
    fun calculateStreak(
        previousState: StreakState,
        currentDate: Long = System.currentTimeMillis()
    ): StreakState {
        val daysSinceLastActive = daysBetween(previousState.lastActiveDate, currentDate)

        return when {
            // Consecutive day
            daysSinceLastActive == 1L -> {
                previousState.copy(
                    currentStreak = previousState.currentStreak + 1,
                    longestStreak = maxOf(
                        previousState.longestStreak,
                        previousState.currentStreak + 1
                    ),
                    frozenDaysUsed = 0,  // Reset grace days
                    lastActiveDate = currentDate
                )
            }
            // Within grace period
            daysSinceLastActive <= (GRACE_DAYS - previousState.frozenDaysUsed + 1) -> {
                previousState.copy(
                    frozenDaysUsed = previousState.frozenDaysUsed + daysSinceLastActive.toInt() - 1,
                    lastActiveDate = currentDate
                )
            }
            // Streak broken - but no shame!
            else -> {
                previousState.copy(
                    currentStreak = 1,
                    frozenDaysUsed = 0,
                    lastActiveDate = currentDate
                )
            }
        }
    }

    /**
     * Get streak message - always positive
     */
    fun getStreakMessage(streak: Int): String {
        return when {
            streak >= 30 -> "连续学习${streak}天！你是学习大师！"
            streak >= 14 -> "连续学习${streak}天！太棒了！"
            streak >= 7 -> "连续学习${streak}天！保持下去！"
            streak >= 3 -> "连续学习${streak}天！很好的开始！"
            streak == 1 -> "开始新的学习旅程！"
            else -> "欢迎回来！"
        }
    }

    /**
     * Check if streak freeze is available
     */
    fun canUseFreeze(state: StreakState): Boolean {
        return state.freezeDaysAvailable > 0
    }

    /**
     * Use a freeze day (item from shop)
     */
    fun useFreeze(state: StreakState): StreakState {
        require(canUseFreeze(state)) { "No freeze days available" }
        return state.copy(
            freezeDaysAvailable = state.freezeDaysAvailable - 1,
            frozenDaysUsed = maxOf(0, state.frozenDaysUsed - 1)
        )
    }

    private fun daysBetween(from: Long, to: Long): Long {
        val diff = to - from
        return diff / (24 * 60 * 60 * 1000)
    }
}
```

### Reward Mechanics

#### 1. Effort-Based Rewards

```kotlin
/**
 * Reward calculation based on effort, not just results
 */
class EffortBasedRewardSystem {

    /**
     * Calculate stars earned for a session
     * Factors in: accuracy, time spent, effort, improvement
     */
    fun calculateStars(session: LearningSession): Int {
        val accuracyScore = calculateAccuracyScore(session)
        val effortScore = calculateEffortScore(session)
        val improvementScore = calculateImprovementScore(session)

        val totalScore = accuracyScore + effortScore + improvementScore

        return when {
            totalScore >= 90 -> 3
            totalScore >= 60 -> 2
            totalScore >= 30 -> 1
            else -> 1  // Always give at least 1 star for trying
        }
    }

    private fun calculateAccuracyScore(session: LearningSession): Int {
        return (session.correctAnswers.toFloat() / session.totalAnswers * 40).toInt()
    }

    private fun calculateEffortScore(session: LearningSession): Int {
        // Reward for time spent thinking (not guessing)
        val avgThinkTime = session.totalTime / session.totalAnswers
        val adequateTimeRatio = session.answersWithAdequateTime.toFloat() / session.totalAnswers

        return (adequateTimeRatio * 30).toInt()
    }

    private fun calculateImprovementScore(session: LearningSession): Int {
        // Reward for improvement over previous attempts
        return session.improvementBonus.coerceAtMost(30)
    }
}

data class LearningSession(
    val correctAnswers: Int,
    val totalAnswers: Int,
    val totalTime: Long,
    val answersWithAdequateTime: Int,
    val improvementBonus: Int
)
```

#### 2. Non-Exclusive Rewards

```kotlin
/**
 * Non-exclusive reward system
 * Missing an event doesn't mean missing out forever
 */
object NonExclusiveRewards {

    /**
     * All rewards are permanently available
     * No "limited time" exclusives
     */
    val ALL_REWARDS = listOf(
        Reward(
            id = "book_avatar",
            name = "书本头像",
            description = "爱学习的书本",
            icon = "📖",
            unlockMethod = UnlockMethod.ACHIEVEMENT,
            requirement = "Learn 50 words",
            isExclusive = false,
            isPermanent = true
        ),
        Reward(
            id = "star_background",
            name = "星空背景",
            description = "美丽的星空主题",
            icon = "🌌",
            unlockMethod = UnlockMethod.SHOP,
            cost = 100,
            isExclusive = false,
            isPermanent = true
        )
        // ... all rewards follow this pattern
    )

    /**
     * Shop always has all items
     * No rotating stock
     */
    fun getShopItems(): List<Reward> {
        return ALL_REWARDS.filter { it.unlockMethod == UnlockMethod.SHOP }
    }
}

data class Reward(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val unlockMethod: UnlockMethod,
    val requirement: String? = null,
    val cost: Int? = null,
    val isExclusive: Boolean = false,
    val isPermanent: Boolean = true
)

enum class UnlockMethod {
    ACHIEVEMENT,  // Unlock by completing achievement
    SHOP,         // Purchase with stars
    MILESTONE,    // Unlock at learning milestone
    AUTOMATIC     // Unlock automatically for all users
}
```

---

## UI Patterns

### Pattern 1: Progress Without Comparison

```kotlin
/**
 * Progress display that shows personal growth only
 * No comparison to other users
 */
@Composable
fun PersonalProgressCard(
    userProgress: UserProgress,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title - focus on self, not others
            Text(
                text = "你的学习进度",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress circle - personal goal based
            val progress = userProgress.wordsLearned.toFloat() / userProgress.personalGoal
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(120.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message - encouraging, not comparative
            Text(
                text = "${userProgress.wordsLearned} / ${userProgress.personalGoal} 词",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = getEncouragingMessage(userProgress),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Personal milestones - not competitive ranks
            Spacer(modifier = Modifier.height(16.dp))

            MilestoneTimeline(
                milestones = userProgress.personalMilestones,
                currentProgress = userProgress.wordsLearned
            )
        }
    }
}

private fun getEncouragingMessage(progress: UserProgress): String {
    val percentage = (progress.wordsLearned.toFloat() / progress.personalGoal * 100).toInt()
    return when {
        percentage >= 100 -> "你完成了目标！太棒了！"
        percentage >= 75 -> "快要完成目标了！"
        percentage >= 50 -> "已经完成一半了！"
        percentage >= 25 -> "很好的开始！"
        else -> "开始你的学习旅程！"
    }
}
```

### Pattern 2: Celebration Without Comparison

```kotlin
/**
 * Celebration screen that focuses on achievement, not ranking
 */
@Composable
fun LevelCompleteCelebration(
    result: LevelResult,
    onNextLevel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celebration icon
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title - positive, not ranking
        Text(
            text = "关卡完成！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle - focus on effort
        Text(
            text = "你完成了这一关的学习！",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stars - earned, not compared
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                Text(
                    text = if (index < result.stars) "⭐" else "☆",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats - personal, not comparative
        StatRow(
            icon = "✓",
            label = "正确",
            value = "${result.correctAnswers}/${result.totalAnswers}"
        )

        StatRow(
            icon = "💪",
            label = "连胜",
            value = "${result.maxCombo}x"
        )

        StatRow(
            icon = "⏱️",
            label = "用时",
            value = formatTime(result.totalTime)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Encouraging message
        Text(
            text = getEncouragingCompletionMessage(result),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Continue button
        Button(
            onClick = onNextLevel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("继续学习")
        }
    }
}

private fun getEncouragingCompletionMessage(result: LevelResult): String {
    return when {
        result.stars == 3 -> "完美表现！你太棒了！"
        result.stars == 2 -> "做得很好！继续加油！"
        result.hasImprovement -> "比上次有进步！"
        result.showedEffort -> "努力学习会有收获！"
        else -> "每次学习都是进步！"
    }
}

@Composable
private fun StatRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(icon)
            Text(label)
        }
        Text(value, fontWeight = FontWeight.Bold)
    }
}
```

---

## Anti-Addiction Measures

### 1. Session Reminders

```kotlin
/**
 * Gentle reminders to take breaks
 * Not punitive, but caring
 */
class SessionReminderManager {

    private val reminderIntervals = listOf(
        20 to "你学习了20分钟，休息一下吧！",
        40 to "40分钟了！眼睛和大脑都需要休息~",
        60 to "学习一小时啦！去活动一下吧！"
    )

    fun shouldShowReminder(sessionMinutes: Int): String? {
        return reminderIntervals
            .filter { it.first == sessionMinutes }
            .map { it.second }
            .firstOrNull()
    }

    @Composable
    fun SessionReminderDialog(
        message: String,
        onContinue: () -> Unit,
        onTakeBreak: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onContinue,
            title = { Text("💤 休息时间") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = onTakeBreak) {
                    Text("休息一下")
                }
            },
            dismissButton = {
                TextButton(onClick = onContinue) {
                    Text("再学一会")
                }
            }
        )
    }
}
```

### 2. No Streak Pressure

```kotlin
/**
 * Remove anxiety from streak system
 */
class NoPressureStreakSystem {

    /**
     * Get streak message - never shaming
     */
    fun getStreakStatusMessage(streak: Int, daysSinceLastActive: Int): String {
        return when {
            streak == 0 -> "开始新的学习旅程！"
            daysSinceLastActive <= 2 -> "你的连续天数还在！继续加油！"
            daysSinceLastActive <= 7 -> "有一段时间没学习了，欢迎回来！"
            else -> "好久不见！开始新的学习吧！"
        }
    }

    /**
     * Show grace days remaining, not "streak will expire"
     */
    fun getGracePeriodMessage(graceDaysRemaining: Int): String {
        return when {
            graceDaysRemaining >= 2 -> "有2天宽限期，不用担心！"
            graceDaysRemaining == 1 -> "还有1天宽限期。"
            else -> "今天学习可以保持连续天数！"
        }
    }
}
```

---

## Copywriting Guidelines

### Positive Language Patterns

| Avoid ❌ | Use ✅ Instead |
|----------|----------------|
| "你落后了" | "继续加油" |
| "快点学习" | "学习要开始了" |
| "别人都..." | "你可以..." |
| "不要错过" | "随时可以参加" |
| "只剩X次" | "今天已查看X次" |
| "你输了" | "再试一次" |
| "你排名X" | "你的进度" |
| "打败对手" | "超越自己" |

### Encouraging Phrases

**For Effort**:
- "很好的尝试！"
- "你很努力！"
- "坚持下去！"
- "每次练习都有帮助！"

**For Progress**:
- "你在进步！"
- "比上次更好！"
- "看得见的成长！"
- "继续这样下去！"

**For Mistakes**:
- "没关系！"
- "错误是学习的一部分！"
- "再试一次！"
- "从错误中学习！"

---

## Implementation Checklist

### Design Review
- [ ] No competitive ranking elements
- [ ] All feedback is positive or constructive
- [ ] No FOMO mechanics
- [ ] Progress is personal, not comparative
- [ ] Streak system has grace days
- [ ] Rewards are non-exclusive
- [ ] Session reminders are gentle
- [ ] Language is encouraging

### Testing
- [ ] User testing with children (focus groups)
- [ ] Parent feedback on social features
- [ ] Expert review (child psychologist)
- [ ] A/B testing for messaging

---

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Positive sentiment | >90% | User feedback |
| Session anxiety | <5% | Parent reports |
| Return rate (after break) | >70% | Analytics |
| Feature opt-out rate | <10% | Settings data |
| Daily active users | Stable | Retention analysis |

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Related**: SOCIAL_FEATURES_DESIGN.md
