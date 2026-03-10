# Onboarding Design: First 5 Minutes & First 3 Sessions

**Document Version**: 1.0
**Created**: 2026-02-17
**Designer**: game-designer
**Priority**: P0 - User Retention
**Target Audience**: 10-year-old children

---

## Executive Summary

**Current Problem**: New users are thrown directly into learning with zero onboarding. No emotional connection, no "aha!" moment, no reason to care.

**Design Philosophy**: **GAME FUN > LEARNING EFFICIENCY** during onboarding

**Core Hypothesis**: If a 10-year-old doesn't smile or feel excited within the first 2 minutes, they will never come back.

**Target Outcomes**:
- 90% completion rate of onboarding flow
- 70% Day-1 retention (complete first session)
- 40% Day-7 retention (return for second session)

---

## Part 1: The First 5 Minutes Experience

### 1.1 Critical Timeline Analysis

```
┌─────────────────────────────────────────────────────────────────┐
│              FIRST 5 MINUTES - EMOTIONAL JOURNEY               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Minute 0-1:  WOW Moment         ✨ Spark Curiosity             │
│  Minute 1-2:  Agency & Power     🦸 Feel Capable                │
│  Minute 2-3:  First Win          🎉 Immediate Success           │
│  Minute 3-4:  Social Connection  👥 Belonging                   │
│  Minute 4-5:  The Hook           🪝 Reason to Return            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Current State vs. Proposed Flow

**Current Flow** (Boring, Test-like):
```
Open App → Welcome Screen → Tap "Start" → Map Screen → Select Level → Spell Words
```

**Proposed Flow** (Exciting, Game-like):
```
Open App → Animated Intro → Avatar Selection → Mini Tutorial → First "Battle" → Victory Celebration → Unlock Preview
```

---

## Part 2: Detailed Onboarding Flow Design

### 2.1 Screen 0: Cold Open (The "Movie Trailer" Moment)

**Duration**: 15 seconds (skippable after first view)

**Purpose**: Create wonder and establish the world

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                    [FULL SCREEN ANIMATION]                      │
│                                                                 │
│             🌫️🌫️🌫️  FOG CLEARS  🌫️🌫️🌫️                          │
│                                                                 │
│         Slowly reveals a beautiful, mysterious island           │
│         Hidden word shapes glowing in the distance              │
│                                                                 │
│            Text fades in: "欢迎来到 Wordland"                   │
│            "在这里，单词是你的超能力"                            │
│                                                                 │
│            [轻触开始]  [Skip →]                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Design Notes**:
- Minimal animation budget needed (fog fade + text)
- Audio: Mysterious, intriguing tone (not childish)
- Key emotion: **CURIOSITY**

**Implementation Priority**: P1 (can launch without, but highly recommended)

---

### 2.2 Screen 1: Avatar Selection (The "This is ME" Moment)

**Duration**: 30-60 seconds

**Purpose**: Personal investment, ownership

```
┌─────────────────────────────────────────────────────────────────┐
│                     谁是你的向导？                                │
│                  Choose Your Guide                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────┐      ┌─────────┐      ┌─────────┐               │
│   │   🦉    │      │   🐱    │      │   🦊    │               │
│   │  智慧猫 │      │  好奇猫 │      │  勇敢狐 │               │
│   │  Hoot   │      │  Whisker│      │  Brave  │               │
│   │         │      │         │      │         │               │
│   │ "选择我  │      │ "我们一  │      │ "我来保  │               │
│   │  来探索" │      │  起玩!" │      │  护你"  │               │
│   └─────────┘      └─────────┘      └─────────┘               │
│                                                                 │
│              [More guides coming soon!]                        │
│                                                                 │
│                    [Continue →]                                │
└─────────────────────────────────────────────────────────────────┘
```

**Design Notes**:
- 3 options initially (low cognitive load)
- Each avatar has personality, not just appearance
- Avatar appears throughout the app providing encouragement
- NO performance difference (purely cosmetic)
- Key emotion: **OWNERSHIP**

**Implementation Priority**: P0 (essential for personal connection)

**Data Model**:
```kotlin
data class Avatar(
    val id: String,
    val name: String,
    val emoji: String,
    val personality: Personality,
    val encouragementPhrases: List<String>
)

enum class Personality {
    WISE,      // "选择我来探索" - Helpful hints
    CURIOUS,   // "我们一起玩!" - Shared discovery
    BRAVE      // "我来保护你" - Confidence building
}
```

---

### 2.3 Screen 2: Interactive Tutorial (The "I Can Do This" Moment)

**Duration**: 60-90 seconds

**Purpose**: Teach mechanics WITHOUT feeling like teaching

**Key Insight**: 10-year-olds hate tutorials. Make it feel like a GAME.

```
┌─────────────────────────────────────────────────────────────────┐
│                      第一个挑战                                   │
│                   Your First Challenge                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   [Avatar appears with animation]                              │
│                                                                 │
│   🦉 Hoot: "看！雾中有个神秘的单词..."                           │
│      "Look! A mysterious word in the fog..."                    │
│                                                                 │
│   ┌─────────────────────────────────────────────────────┐      │
│   │           [ILLUSTRATION: Cat behind fog]            │      │
│   │                 🌫️ 🌫️ 🌫️                          │      │
│   │                                                     │      │
│   │         Hint: 它是一种可爱的动物                    │      │
   │                  ↓                                   │      │
│   │         C _ T                                       │      │
│   │                                                     │      │
│   │         [A][B][C][D][E][F][G][H][I]                │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
│   [Avatar whispers: "Tap the letters to spell CAT"]            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Design Notes**:
- First word is ALWAYS "CAT" (universal, easy, 3 letters)
- Visual hint (illustration) before spelling
- Avatar provides guidance, not instructions
- Gentle hand-holding, not passive watching
- IMMEDIATE success (first word is impossible to fail)
- Key emotion: **COMPETENCE**

**Implementation Priority**: P0 (essential)

**Tutorial Flow**:
1. Show context with visual
2. Highlight first letter needed
3. User taps correct letter → positive feedback
4. Repeat until complete
5. Big celebration on success

---

### 2.4 Screen 3: Victory & First Reward (The "I Won!" Moment)

**Duration**: 15-30 seconds

**Purpose**: End on a high, create desire for more

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                   ⭐⭐⭐ 完美! Perfect! ⭐⭐⭐                    │
│                                                                 │
│   ┌─────────────────────────────────────────────────────┐      │
│   │                                                     │      │
│   │         [FOG CLEARS - REVEALS CAT ILLUSTRATION]     │      │
│   │                                                     │      │
│   │              🐱  CAT  🐱                            │      │
│   │                                                     │      │
│   │    You discovered your first word!                 │      │
│   │    你发现了第一个单词！                              │      │
│   │                                                     │      │
│   │    +10 coins    +1 word discovered                 │      │
│   │                                                     │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
│   🦉 Hoot: "太棒了！我发现更多单词了，我们一起去探索吧！"        │
│      "Amazing! I found more words. Let's explore!"              │
│                                                                 │
│                    [Continue →]                                │
└─────────────────────────────────────────────────────────────────┘
```

**Design Notes**:
- Maximum celebration (confetti, sounds, animation)
- Multiple reward types (stars, coins, discovery count)
- Avatar expresses genuine excitement
- Preview of what's next (creates anticipation)
- Key emotion: **ACHIEVEMENT**

**Implementation Priority**: P0 (essential for retention)

---

### 2.5 Screen 4: The Map Preview (The "There's More!" Moment)

**Duration**: 20 seconds

**Purpose**: Show scope without overwhelming

```
┌─────────────────────────────────────────────────────────────────┐
│                     Wordland 世界地图                            │
│                                                                 │
│   ┌─────────────────────────────────────────────────────┐      │
│   │                                                     │      │
│   │   🌫️        👁️           🌫️         📚             │      │
│   │           Look Island   Locked    Locked            │      │
│   │                                                     │      │
│   │              [🚢 Your Ship Here]                    │      │
│   │                                                     │      │
│   │           🌫️   🌫️   🌫️   🌫️                      │      │
│   │        [All fogged - mystery!]                      │      │
│   │                                                     │      │
│   │   1/6 islands discovered                            │      │
│   │                                                     │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
│   🦉 Hoot: "整个世界等你探索！我们可以先从Look Island开始"      │
│      "The whole world awaits! Let's start at Look Island"       │
│                                                                 │
│               [Start Adventure →]    [Preview Map →]           │
└─────────────────────────────────────────────────────────────────┘
```

**Design Notes**:
- Show ONE clear path (no decision paralysis)
- Fogged areas create curiosity
- Player's ship shows position and progress
- "Start Adventure" is highlighted (clear CTA)
- Key emotion: **ANTICIPATION**

**Implementation Priority**: P0 (connects to existing map system)

---

## Part 3: The First 3 Sessions Design

### 3.1 Session Strategy Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    SESSION PROGRESSION                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  SESSION 1: Emotional Connection                                │
│  └── Focus: Fun, success, "I like this"                        │
│  └── Length: 5-10 minutes                                      │
│  └── Content: Onboarding + 3 easy words                        │
│                                                                 │
│  SESSION 2: Building Competence                                │
│  └── Focus: "I'm getting better"                               │
│  └── Length: 10-15 minutes                                     │
│  └── Content: Complete Level 1 (6 words)                       │
│                                                                 │
│  SESSION 3: Social & Long-term Motivation                      │
│  └── Focus: "I want to come back"                              │
│  └── Length: 15-20 minutes                                     │
│  └── Content: Start Level 2 + unlock features                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Session 1: Emotional Hook (5-10 minutes)

**Goal**: User ends session feeling GOOD about the app

**Flow**:
1. Onboarding (Screens 0-4 above): ~3 minutes
2. First 3 "real" words (from Level 1): ~3-5 minutes
3. Session complete screen: ~1 minute

**Session Complete Design**:
```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                     🎉 Today's Victory! 🎉                     │
│                        今日胜利！                               │
│                                                                 │
│   Words discovered: 3                          [🐱][👁️][🌈]   │
│   Best streak: 3                                                      │
│   Stars earned: ⭐⭐⭐⭐⭐⭐⭐⭐⭐                              │
│                                                                 │
│   🦉 Hoot: "你今天很棒！明天我们继续冒险吧！"                    │
│      "Great job today! Let's continue tomorrow!"                │
│                                                                 │
│   ┌─────────────────────────────────────────────────────┐      │
│   │  🔥 Tomorrow: Unlock Level 1 Complete!              │      │
│   │     Complete all 6 words to reveal the...          │      │
│   │     [fogged icon] ???                              │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
│                    [See You Tomorrow!]                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Design Notes**:
- Always end on positive note
- Create anticipation for tomorrow (NOT urgency)
- Avatar becomes "friend" not "teacher"
- Session has clear end (not endless)

**Implementation Priority**: P0

---

### 3.3 Session 2: Building Competence (10-15 minutes)

**Goal**: User feels progress and capability

**Session Start** (Different from first time):
```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│   🦉 Hoot: "欢迎回来！我发现了新的单词..."                     │
│      "Welcome back! I found new words..."                       │
│                                                                 │
│   ┌─────────────────────────────────────────────────────┐      │
│   │  🗺️ Continue Learning                              │      │
│   │                                                     │      │
│   │  Level 1: Look Island                              │      │
│   │  Progress: ████████░░ 3/6 words                    │      │
│   │                                                     │      │
│   │  Next word: [fogged preview]                        │      │
│   │                                                     │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
│           [Continue →]    [Practice Review]                   │
└─────────────────────────────────────────────────────────────────┘
```

**New Features Unlocked**:
- Combo counter (visual feedback)
- Hint system (now available)
- Progress bar (shows advancement)

**Session 2 Complete**:
```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                    🏆 Level 1 Complete! 🏆                    │
│                                                                 │
│   ┌─────────────────────────────────────────────────────┐      │
│   │              [LEVEL COMPLETE ANIMATION]             │      │
│   │                                                     │      │
│   │     ⭐⭐⭐ Perfect! All 6 words mastered!           │      │
│   │                                                     │      │
│   │     [FOG CLEARS - REVEALS: Level 2 location]        │      │
│   │                                                     │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
│   New area unlocked: Color Peninsula 🎨                        │
│                                                                 │
│   🦉 Hoot: "你解锁了新区域！Color Peninsula 有更多秘密..."      │
│                                                                 │
│                    [Continue Adventure →]                      │
└─────────────────────────────────────────────────────────────────┘
```

**Implementation Priority**: P0

---

### 3.4 Session 3: Long-term Motivation (15-20 minutes)

**Goal**: Establish habit, preview long-term journey

**New Features Unlocked**:
- Daily challenge (one special word per day)
- Achievement system (first badge)
- Collection preview (storybook)

**Session 3 Start**:
```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│            📅 Daily Challenge Available! 📅                   │
│                                                                 │
│   ┌─────────────────────────────────────────────────────┐      │
│   │  🔥 Challenge: 3-correct streak                     │      │
│   │     Reward: 🏆 "Getting Started" Badge              │      │
│   │                                                     │      │
│   │  [Accept Challenge]  [Maybe Later]                  │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
│           [Continue Learning →]                                │
└─────────────────────────────────────────────────────────────────┘
```

**First Achievement**:
```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                   🏆 NEW ACHIEVEMENT! 🏆                       │
│                                                                 │
│   ┌─────────────────────────────────────────────────────┐      │
│   │                                                     │      │
│   │              [BADGE ARTWORK]                         │      │
│   │                                                     │      │
│   │         "Early Explorer"                            │      │
│   │         Completed first 10 words!                   │      │
│   │                                                     │      │
│   │         Only 90% of Wordland remains!               │      │
│   │                                                     │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
│   View your achievements: [My Book →]                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Design Notes**:
- First achievement comes EARLY (within Session 3)
- Achievement wording creates curiosity about remaining content
- "My Book" creates collection feeling (not just progress)

**Implementation Priority**: P1 (enhances, but not critical for Day 7)

---

## Part 4: Anti-Friction Design Principles

### 4.1 Reduce Every Barrier

**Principle**: Any friction = lost users

| Barrier | Solution |
|---------|----------|
| "What do I do?" | Always show current objective |
| "I'm stuck" | Always have hint available |
| "This is boring" | Variable interaction, not just tapping |
| "I don't want to learn" | Frame as DISCOVERY, not learning |
| "How long will this take?" | Show session length upfront |
| "I made a mistake" | Gentle error, never punitive |
| "I don't want to come back" | End sessions with anticipation, not exhaustion |

### 4.2 The "2-Minute Rule"

Every session should offer value within 2 minutes of opening:

```
Open App
  ↓
[Within 30 seconds]: Avatar welcomes back + shows progress
  ↓
[Within 60 seconds]: First word is active
  ↓
[Within 120 seconds]: First win/celebration
```

### 4.3 The "No Dead Ends" Promise

User should NEVER feel:
- Confused about what to do next
- Punished for a wrong answer
- Stuck without hints
- Lost in navigation

Every screen has ONE clear primary action.

---

## Part 5: Avatar System Design

### 5.1 Avatar Personalities

```kotlin
enum class AvatarType {
    OWL_HOOT,     // 智慧 - Encouraging, hints
    CAT_WHISKERS, // 好奇 - Enthusiastic, shared discovery
    FOX_BRAVE     // 勇敢 - Confidence-building, protective
}

data class Avatar(
    val type: AvatarType,
    val name: String,
    val emoji: String,
    val personality: Personality,
    val phrases: AvatarPhrases
)

data class AvatarPhrases(
    val welcome: List<String>,
    val correct: List<String>,
    val incorrect: List<String>,
    val complete: List<String>,
    val encourage: List<String>
)

// Example phrases for Owl Hoot
val owlPhrases = AvatarPhrases(
    welcome = listOf(
        "欢迎回来！今天我们探索什么新单词？",
        "你好！我准备好开始新的冒险了！"
    ),
    correct = listOf(
        "正确！你太聪明了！",
        "是的！你记住了！",
        "完美！下一个挑战！"
    ),
    incorrect = listOf(
        "没关系，我们再试试",
        "快想到了，再努力一下",
        "让我给你一个小提示..."
    ),
    complete = listOf(
        "太棒了！你完成了这个关卡！",
        "你是个真正的单词探险家！",
        "我为你感到骄傲！"
    ),
    encourage = listOf(
        "你可以做到的！",
        "相信自己！",
        "一步一步来！"
    )
)
```

### 5.2 Avatar Appearance Schedule

| Context | Avatar Position | Avatar Action |
|---------|----------------|---------------|
| Onboarding | Center screen | Welcome animation |
| Tutorial | Bottom left | Points to UI elements |
| Gameplay | Top right | Reacts to answers |
| Victory | Center | Celebration animation |
| Defeat | Center | Encouragement |
| Session complete | Center | Preview of next session |

---

## Part 6: Technical Implementation Notes

### 6.1 New Screens Required

```kotlin
// New screens
@Composable
fun OnboardingFlow(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel
)

@Composable
fun AvatarSelectionScreen(
    onAvatarSelected: (Avatar) -> Unit
)

@Composable
fun InteractiveTutorialScreen(
    onComplete: () -> Unit,
    viewModel: TutorialViewModel
)

@Composable
fun SessionCompleteScreen(
    sessionResult: SessionResult,
    onContinue: () -> Unit
)
```

### 6.2 State Management

```kotlin
// Domain models
@Immutable
data class OnboardingState(
    val currentStep: OnboardingStep,
    val selectedAvatar: Avatar?,
    val tutorialCompleted: Boolean,
    val isFirstSession: Boolean
)

sealed class OnboardingStep {
    object Welcome : OnboardingStep()
    object AvatarSelection : OnboardingStep()
    object Tutorial : OnboardingStep()
    object FirstVictory : OnboardingStep()
    object MapPreview : OnboardingStep()
    object Complete : OnboardingStep()
}

@Immutable
data class SessionProgress(
    val userId: String,
    val sessionNumber: Int,
    val wordsCompleted: Int,
    val currentStreak: Int,
    val sessionGoal: Int,
    val featuresUnlocked: Set<String>
)
```

### 6.3 Data Persistence

```kotlin
// Track onboarding completion
@Entity(tableName = "user_onboarding")
data class UserOnboardingEntity(
    @PrimaryKey val userId: String,
    val onboardingComplete: Boolean,
    val selectedAvatar: String,
    val completedSessions: Int,
    val lastSessionDate: Long?
)

// Track session progress
@Entity(tableName = "session_progress")
data class SessionProgressEntity(
    @PrimaryKey val userId_sessionNumber: String,
    val userId: String,
    val sessionNumber: Int,
    val wordsCompleted: Int,
    val featuresUnlocked: String, // JSON
    val completedAt: Long?
)
```

---

## Part 7: Success Metrics

### 7.1 Onboarding Completion

| Metric | Target | Measurement |
|--------|--------|-------------|
| Complete onboarding flow | >90% | Analytics event |
| Avatar selection time | <30s | Time analytics |
| Tutorial completion | >95% | Screen flow |
| First word success rate | 100% | By design (CAT) |

### 7.2 Session Retention

| Metric | Target | Measurement |
|--------|--------|-------------|
| Day 0 (Session 1 complete) | >90% | Session completion |
| Day 1 return | >70% | 24h return |
| Day 7 return | >40% | 7d return |
| Session 2 start | >70% | Continued after Day 1 |

### 7.3 Engagement Signals

| Signal | Target | Measurement |
|--------|--------|-------------|
| Avatar interaction | >50% tap rate | Tap events |
| Hint usage | <30% (but available) | Feature usage |
| Session completion | >80% | Session end reached |
| Feature unlock engagement | >60% | New feature clicks |

---

## Part 8: Implementation Roadmap

### Phase 1: Core Onboarding (Week 1-2)

**Priority**: P0

| Feature | Effort | Owner | Dependencies |
|---------|--------|-------|--------------|
| Avatar selection screen | 4h | compose-ui | Avatar data model |
| Interactive tutorial (CAT) | 6h | compose-ui | Tutorial viewmodel |
| First victory screen | 3h | compose-ui | Animation library |
| Session complete screen | 3h | compose-ui | None |
| Onboarding state tracking | 2h | android-engineer | Database migration |
| Avatar phrases (3 avatars) | 2h | education-specialist | Avatar system |

**Total**: ~20 hours

**Deliverable**: Complete onboarding flow from app open to first session

### Phase 2: Session Management (Week 2-3)

**Priority**: P0

| Feature | Effort | Owner | Dependencies |
|---------|--------|-------|--------------|
| Session progress tracking | 4h | android-engineer | Database schema |
| Session 2/3 start screens | 3h | compose-ui | Session data |
| Feature unlock system | 4h | android-engineer | None |
| Daily challenge preview | 3h | compose-ui | Challenge system |
| Achievement trigger | 2h | android-engineer | Achievement model |

**Total**: ~16 hours

**Deliverable**: Complete session flow (1-3)

### Phase 3: Polish & Enhance (Week 3-4)

**Priority**: P1

| Feature | Effort | Owner | Dependencies |
|---------|--------|-------|--------------|
| Avatar animations | 8h | compose-ui | Animation library |
| Celebration effects (confetti) | 4h | compose-ui | Particle system |
| Sound effects | 6h | audio (if available) | Audio assets |
| Onboarding skip option | 2h | compose-ui | Settings |
| A/B testing setup | 4h | android-engineer | Analytics |

**Total**: ~24 hours

**Deliverable**: Polished onboarding with all enhancements

---

## Part 9: A/B Testing Variants

### Variant A (Control): Minimal Onboarding

- Skip directly to avatar selection
- No cold open animation
- Tutorial is optional
- Focus on speed to content

### Variant B (Proposed): Emotional Onboarding

- Full cold open
- Required avatar selection
- Interactive tutorial with guaranteed success
- Focus on emotional connection

**Hypothesis**: Variant B will have higher Day 7 retention despite longer onboarding

**Success**: Variant B shows >10% improvement in Day 7 retention

---

## Part 10: Key Design Decisions Rationale

### Decision 1: Why Avatar Selection First?

**Rationale**: Before learning anything, user needs to feel "this is for me." Avatar creates personal investment.

**Alternative Considered**: Direct to gameplay

**Why Rejected**: Without personal connection, gameplay feels like a test, not an adventure.

### Decision 2: Why Guaranteed First Win (CAT)?

**Rationale**: First experience must be success. "CAT" with illustration hint ensures 100% success rate.

**Alternative Considered**: Random first word from Level 1

**Why Rejected**: Random could be too hard, causing early frustration and abandonment.

### Decision 3: Why Show Map Preview Before Content?

**Rationale**: Creates anticipation ("there's more to discover") not overwhelm ("I have to do all this").

**Alternative Considered**: Hide map until after Level 1 complete

**Why Rejected**: Without preview, user doesn't understand the journey they're on.

### Decision 4: Why End Sessions Explicitly?

**Rationale**: Clear ending creates natural stopping point and anticipation for next session.

**Alternative Considered**: Endless play until user quits

**Why Rejected**: Endless play causes exhaustion, not anticipation. No clear "victory" moment.

### Decision 5: Why No "Learning" Language During Onboarding?

**Rationale**: "Learning" sounds like work. "Discovering" sounds like adventure.

**Alternative Considered**: Use educational language

**Why Rejected**: 10-year-olds associate "learning" with school (work). Games should feel like play.

---

## Appendix: Avatar Illustration Briefs

### Owl Hoot (智慧猫头鹰)

- **Visual**: Large eyes, small reading glasses, friendly expression
- **Colors**: Purple/Blue (wisdom colors)
- **Personality**: Patient, encouraging, helpful
- **Animation**: Nods approval, flaps wings on victory, tilts head when thinking

### Cat Whiskers (好奇猫咪)

- **Visual**: Big eyes, whiskers, playful pose
- **Colors**: Orange (energy, fun)
- **Personality**: Excited, curious, friendly
- **Animation**: Bounces excitedly, paws at words, purrs on success

### Fox Brave (勇敢小狐狸)

- **Visual**: Alert ears, confident stance, friendly smile
- **Colors**: Red/Orange (bravery, energy)
- **Personality**: Protective, encouraging, confident
- **Animation**: Thumps chest on victory, determined stance, high fives

---

**Document Status**: Ready for Review
**Next Steps**: Team approval → Implementation Phase 1
**Contact**: game-designer for questions
