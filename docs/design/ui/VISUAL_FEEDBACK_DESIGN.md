# Visual Feedback Design Specification

**Document Version**: 1.0
**Created**: 2026-02-20
**Designer**: game-designer
**Priority**: P0 - Sprint 1 Epic #1 Dependency

---

## 📋 Table of Contents

1. [Overview](#1-overview)
2. [Animation Duration Standards](#2-animation-duration-standards)
3. [Easing Curves & Functions](#3-easing-curves--functions)
4. [Multi-Animation Coordination](#4-multi-animation-coordination)
5. [Visual Feedback Hierarchy](#5-visual-feedback-hierarchy)
6. [Star Rating Differential Feedback](#6-star-rating-differential-feedback)
7. [Error Feedback Design](#7-error-feedback-design)
8. [Cognitive Load Control](#8-cognitive-load-control)
9. [Code Examples](#9-code-examples)
10. [Acceptance Criteria](#10-acceptance-criteria)

---

## 1. Overview

### 1.1 Design Goals

Visual feedback in Wordland serves three primary purposes:

1. **Immediate Confirmation**: User actions get instant response (<100ms)
2. **Emotional Engagement**: Feedback creates delight and motivation
3. **Learning Reinforcement**: Visual cues strengthen memory formation

### 1.2 Design Principles

| Principle | Description | Example |
|-----------|-------------|---------|
| **Clarity** | Feedback meaning is unambiguous | ✅ Green flash = correct, ❌ Red shake = wrong |
| **Consistency** | Same action always produces same feedback | Correct answer always shows green checkmark |
| **Moderation** | Not overwhelming, especially for children | Celebrations are exciting, not chaotic |
| **Progressiveness** | Feedback intensity matches achievement | 3-star gets more celebration than 1-star |

### 1.3 Target Audience Considerations

**Age 6-12 (Core: 10 years)**:
- Shorter attention spans → Keep feedback quick (≤500ms)
- Sensory sensitivity → Provide intensity reduction option
- ADHD prevalence (~10%) → Avoid excessive concurrent animations
- Need for encouragement → Positive framing even in failure

---

## 2. Animation Duration Standards

### 2.1 Duration Categories

| Category | Duration | Use Cases | Examples |
|----------|----------|-----------|----------|
| **Instant** | 50-100ms | Color flashes, button presses | Letter color change, tap feedback |
| **Quick** | 150-250ms | Icon animations, small transitions | Checkmark appearance, combo icon |
| **Standard** | 300-400ms | UI element transitions | Progress bar fill, card slide |
| **Extended** | 500-800ms | Celebrations, complex sequences | Star reveal, level complete |
| **Cinematic** | 1000-1500ms | Major milestones | Achievement unlock, island master |

### 2.2 Duration by Context

```kotlin
/**
 * Animation duration constants
 */
object AnimationDuration {
    // Instant feedback (color, touch)
    const val INSTANT = 75ms      // Button press, color flash
    const val QUICK_FLASH = 100ms // Success/error color reveal

    // Quick feedback (icons, small elements)
    const val ICON_POP = 200ms           // Checkmark, X mark
    const val LETTER_APPEAR = 150ms      // Letter fly-in
    const val COMBO_PULSE = 250ms        // Combo indicator pulse

    // Standard feedback (transitions)
    const val PROGRESS_FILL = 350ms      // Progress bar
    const val CARD_SLIDE = 300ms         // Card entry/exit
    const val BUTTON_SCALE = 200ms       // Button press effect

    // Extended feedback (celebrations)
    const val STAR_REVEAL = 400ms        // Per star (staggered)
    const val CONFETTI = 800ms           // Full confetti burst
    const val PET_CELEBRATE = 600ms      // Pet animation

    // Cinematic (milestones)
    const val LEVEL_COMPLETE = 1200ms    // Full level complete sequence
    const val ACHIEVEMENT_UNLOCK = 1500ms // Achievement popup
}
```

### 2.3 Timing Rules

**Rule 1: Response Time**
- User action → Feedback start: ≤50ms
- Perceived instant: ≤100ms
- Acceptable delay: ≤150ms

**Rule 2: Staggered Sequences**
- Star 1 → delay 100ms → Star 2 → delay 100ms → Star 3
- Total sequence time: ~600ms (3 stars × 200ms each)

**Rule 3: Concurrent Limit**
- Max 3 animations playing simultaneously
- Priority system decides which to show

---

## 3. Easing Curves & Functions

### 3.1 Curve Definitions

```kotlin
/**
 * Easing curves for Wordland animations
 */
object WordlandEasing {
    /**
     * EaseOutQuad - Fast start, slow end
     * Best for: Elements entering screen, pop-in effects
     * Formula: 1 - (1 - x)²
     */
    val FastOutSlowIn: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    /**
     * EaseInCubic - Slow start, fast end
     * Best for: Elements exiting screen, dismiss animations
     * Formula: x³
     */
    val SlowInFastOut: Easing = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)

    /**
     * EaseOutElastic - Bouncy, overshoots target
     * Best for: Attention-grabbing elements, celebrations
     * Amplitude: 1.2, Period: 0.5
     */
    val Bounce: Easing = CubicBezierEasing(0.68f, -0.6f, 0.32f, 1.6f)

    /**
     * EaseInOut - Smooth start and end
     * Best for: Position changes, movement
     * Formula: x < 0.5 ? 2x² : 1 - (-2x + 2)² / 2
     */
    val Smooth: Easing = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)

    /**
     * Linear - Constant speed
     * Best for: Progress bars, loading indicators
     */
    val Linear: Easing = Easing.Linear
}
```

### 3.2 Easing Usage Matrix

| Animation Type | Easing | Reason |
|---------------|--------|--------|
| Letter fly-in | `FastOutSlowIn` | Snappy entry, gentle landing |
| Star reveal | `Bounce` | Celebratory, draws attention |
| Error shake | `Linear` | Consistent shake speed |
| Progress fill | `Smooth` | Natural, predictable |
| Card slide in | `FastOutSlowIn` | Quick entry, smooth stop |
| Card slide out | `SlowInFastOut` | Quick exit, efficient |
| Button press | `Bounce` (subtle) | Tactile feel |
| Confetti | `FastOutSlowIn` | Explosive start, graceful fall |

### 3.3 Visual Curve Representation

```
FastOutSlowIn (EaseOutQuad):
Velocity
  │
  │╱
  │╲
  │ ╲___
  │
  └──────> Time

Bounce:
Position
  │         ___
  │       ╱    ╲
  │   ___╱      ╲___
  │ ╱
  │
  └──────> Time
```

---

## 4. Multi-Animation Coordination

### 4.1 Priority System

Animations have priorities (1=highest, 5=lowest):

| Priority | Animation | Behavior |
|----------|-----------|----------|
| **P1** | Error feedback, correct answer | Always plays, interrupts others |
| **P2** | Star reveal | Plays, delays P3-P5 |
| **P3** | Combo indicator | Plays if slot available |
| **P4** | Pet reaction | Plays if slot available |
| **P5** | Ambient effects | Skipped if higher priority active |

### 4.2 Exclusivity Rules

**Mutually Exclusive Groups** (only one can play):

| Group | Animations | Conflict Resolution |
|-------|-----------|-------------------|
| **Answer Feedback** | Correct flash, Error shake | Most recent wins |
| **Celebration** | 3-star, 2-star, 1-star complete | Highest star rating wins |
| **Screen Effects** | Confetti, Shake, Pulse | Most intense wins |

### 4.3 Concurrent Rules

**Safe to Play Together**:
- Correct answer color ✅ + Checkmark ✅ + Combo update ✅
- Star reveal ✅ + Pet celebration ✅ + Confetti ✅
- Button press ✅ + Sound ✅ + Haptic ✅

**Unsafe Combinations** (coordinate with delays):
- Error shake + Success celebration → Add 200ms delay
- Screen transition + Input feedback → Complete transition first
- Full-screen celebration + Another celebration → Queue second

### 4.4 Coordinator Implementation

```kotlin
/**
 * Manages animation coordination
 */
class AnimationCoordinator {
    private val activeAnimations = mutableMapOf<AnimationGroup, Animation>()
    private val animationQueue = ArrayDeque<Animation>()

    fun requestAnimation(animation: Animation): Boolean {
        val group = animation.group

        // Check exclusivity
        if (activeAnimations[group]?.isPlaying == true) {
            if (group.exclusive) {
                // Same group exclusive animation playing
                if (animation.priority > activeAnimations[group]?.priority ?: 0) {
                    // Interrupt and replace
                    activeAnimations[group]?.cancel()
                    return playAnimation(animation)
                }
                return false // Lower priority, skip
            }
        }

        // Check concurrent limit (max 3)
        if (activeAnimations.values.count { it.isPlaying } >= 3) {
            if (animation.priority >= 3) {
                // High priority, find lowest to interrupt
                val lowest = activeAnimations.values
                    .filter { it.isPlaying }
                    .minByOrNull { it.priority }
                lowest?.cancel()
            } else {
                // Queue for later
                animationQueue.add(animation)
                return false
            }
        }

        return playAnimation(animation)
    }

    private fun playAnimation(animation: Animation): Boolean {
        activeAnimations[animation.group] = animation
        animation.onComplete = {
            activeAnimations.remove(animation.group)
            playNextQueued()
        }
        animation.start()
        return true
    }

    private fun playNextQueued() {
        val next = animationQueue.removeFirstOrNull() ?: return
        requestAnimation(next)
    }
}

enum class AnimationGroup(val exclusive: Boolean) {
    ANSWER_FEEDBACK(exclusive = true),
    CELEBRATION(exclusive = true),
    SCREEN_EFFECT(exclusive = true),
    PET_REACTION(exclusive = false),
    UI_TRANSITION(exclusive = false)
}

data class Animation(
    val group: AnimationGroup,
    val priority: Int,
    val duration: Duration,
    var onComplete: (() -> Unit)? = null,
    var isPlaying: Boolean = false,
    fun start() {},
    fun cancel() {}
)
```

### 4.5 Scenario Examples

**Scenario 1: Correct Answer with Combo**
```
Timeline: 0ms    100ms  200ms  300ms  400ms
          |      |      |      |      |
Correct: ████████                          (P1, green flash)
Check:         ██████████                  (P2, scale in)
Combo:                ████████████████████ (P3, pulse update)
Pet:                        ████████████████ (P4, happy bounce)
```

**Scenario 2: Error During Success**
```
Timeline: 0ms    100ms  200ms  300ms  400ms  500ms  600ms
          |      |      |      |      |      |      |
Correct: ████████████████████████
          (User submits wrong letter)
Error:               ████████                    (P1, shake, interrupts)
          (Animation resumes at 400ms)
Correct:                     ████████████████████
```

---

## 5. Visual Feedback Hierarchy

### 5.1 Three-Layer System

```
┌─────────────────────────────────────────────┐
│         Layer 3: CELEBRATION (Milestone)     │
│  Full-screen effects, major achievements    │
│  Trigger: Level complete, 3-star, achievement│
│  Duration: 800-1500ms                       │
├─────────────────────────────────────────────┤
│         Layer 2: DETAILED (Information)      │
│  Icons, text, progress, star rating         │
│  Trigger: Answer confirmed, combo update    │
│  Duration: 200-500ms                        │
├─────────────────────────────────────────────┤
│         Layer 1: IMMEDIATE (Recognition)     │
│  Color changes, haptic, sound               │
│  Trigger: Every user action                 │
│  Duration: 50-150ms                         │
└─────────────────────────────────────────────┘
```

### 5.2 Layer Matrix

| Layer | Visual | Auditory | Haptic | Duration | Frequency |
|-------|--------|----------|--------|----------|-----------|
| **1: Immediate** | Color flash, border highlight | Short beep/click | Tap vibration | 50-150ms | Every action |
| **2: Detailed** | Icons, text, stars, progress | Word pronunciation | Success pulse | 200-500ms | Per answer |
| **3: Celebration** | Full screen, particles, pet | Music, jingles | Celebration pattern | 800-1500ms | Milestones |

### 5.3 Feedback by Action

| User Action | Layer 1 | Layer 2 | Layer 3 (Condition) |
|-------------|---------|---------|---------------------|
| **Tap letter key** | Button press flash | Letter appears in slot | - |
| **Submit correct** | Green flash + haptic | Checkmark + score update | 3-star: confetti |
| **Submit wrong** | Red flash + shake | Gentle "try again" | - |
| **Use hint** | Blue flash | Hint text appears | - |
| **Combo +1** | Flame pulse | Combo count update | 5+: screen shake |
| **Level complete** | Success flash | Stars + summary | All stars: pet dance |

### 5.4 Intensity Control

```kotlin
/**
 * Feedback intensity based on context
 */
enum class FeedbackIntensity {
    SUBTLE,    // For repeated actions, sensitive mode
    NORMAL,    // Default feedback
    HIGH,      // For achievements, milestones
    EXTREME    // For major accomplishments (rare)
}

data class FeedbackConfig(
    val visual: VisualIntensity = VisualIntensity.NORMAL,
    val audio: AudioIntensity = AudioIntensity.NORMAL,
    val haptic: HapticIntensity = HapticIntensity.NORMAL
)

fun getIntensityForContext(context: FeedbackContext): FeedbackIntensity {
    return when {
        context.isRepeatedAction -> FeedbackIntensity.SUBTLE
        context.isMajorAchievement -> FeedbackIntensity.HIGH
        context.sensitivityModeEnabled -> FeedbackIntensity.SUBTLE
        else -> FeedbackIntensity.NORMAL
    }
}
```

---

## 6. Star Rating Differential Feedback

### 6.1 Star-Level Celebrations

| Stars | Visual | Audio | Duration | Pet Reaction |
|-------|--------|-------|----------|---------------|
| **⭐⭐⭐ 3-star** | Full confetti, 3 stars bounce | Victory fanfare | 1200ms | Ecstatic jump + spin |
| **⭐⭐ 2-star** | Mini confetti, 2 stars pop | Cheer chime | 800ms | Proud nod + thumbs up |
| **⭐ 1-star** | Single star fade in | Gentle ding | 500ms | Encouraging smile |
| **✗ 0-star** | Gray X, soft glow | Understanding hum | 400ms | Encouraging nod |

### 6.2 Star Reveal Animation

```
3-Star Reveal Timeline:

    0ms      150ms    300ms    450ms    600ms    750ms    900ms
     │        │        │        │        │        │        │
     │        ⭐       ⭐⭐      ⭐⭐⭐
     │         │        │         │
     │         │        │         └─ Final bounce (all 3)
     │         │        └─ 2nd star appears with bounce
     │         └─ 1st star appears with bounce
     └─ Start

Duration per star: 200ms (staggered by 100ms)
Total sequence: ~600ms before final celebration
```

### 6.3 Level Complete by Stars

**3-Star (Perfect)**:
```
┌─────────────────────────────────────────┐
│                                         │
│           ⭐ ⭐ ⭐                       │
│       Perfect! 太棒了!                  │
│                                         │
│    🎊🎊🎊 CONFETTI 🎊🎊🎊               │
│                                         │
│   🐱 Happy cat spin + jump              │
│                                         │
│   Score: 450  Combo: 8                 │
│                                         │
│         [继续学习]                      │
│                                         │
└─────────────────────────────────────────┘
```

**2-Star (Good)**:
```
┌─────────────────────────────────────────┐
│                                         │
│           ⭐ ⭐ ⚪                       │
│      Great Job! 做得好!                 │
│                                         │
│    ✨✨ Mini sparkles                    │
│                                         │
│   🐱 Cat nods and smiles                │
│                                         │
│   Score: 320  Combo: 4                 │
│                                         │
│         [继续学习]                      │
│                                         │
└─────────────────────────────────────────┘
```

**1-Star (Passed)**:
```
┌─────────────────────────────────────────┐
│                                         │
│           ⭐ ⚪ ⚪                       │
│      Good Try! 继续努力!                │
│                                         │
│   🐱 Cat encouraging smile              │
│                                         │
│   Score: 180  Combo: 1                 │
│                                         │
│         [继续学习] [重试]               │
│                                         │
└─────────────────────────────────────────┘
```

### 6.4 Progressive Celebration Intensity

```kotlin
/**
 * Celebration parameters by star rating
 */
data class CelebrationConfig(
    val confettiCount: Int,
    val confettiSpread: Float,
    val petAnimation: PetReaction,
    val message: String,
    val messageDuration: Duration,
    val showRetryButton: Boolean
)

fun getCelebrationConfig(stars: Int): CelebrationConfig {
    return when (stars) {
        3 -> CelebrationConfig(
            confettiCount = 50,
            confettiSpread = 1.0f,
            petAnimation = PetReaction.ECSTATIC,
            message = "Perfect! 太棒了!",
            messageDuration = 1200.ms,
            showRetryButton = false
        )
        2 -> CelebrationConfig(
            confettiCount = 20,
            confettiSpread = 0.6f,
            petAnimation = PetReaction.PROUD,
            message = "Great Job! 做得好!",
            messageDuration = 800.ms,
            showRetryButton = false
        )
        1 -> CelebrationConfig(
            confettiCount = 0,
            confettiSpread = 0f,
            petAnimation = PetReaction.ENCOURAGING,
            message = "Good Try! 继续努力!",
            messageDuration = 500.ms,
            showRetryButton = true
        )
        else -> CelebrationConfig(
            confettiCount = 0,
            confettiSpread = 0f,
            petAnimation = PetReaction.GENTLE,
            message = "Let's try again! 再试一次!",
            messageDuration = 400.ms,
            showRetryButton = true
        )
    }
}
```

---

## 7. Error Feedback Design

### 7.1 Progressive Error Feedback

| Error Type | Visual | Audio | Message | Follow-up Action |
|------------|--------|-------|---------|------------------|
| **First error** | Gentle red flash | Soft "hm" | "Almost there!" (几乎对了!) | Show hint option |
| **Repeated error** (same word) | Red shake | Gentle buzz | "Try again!" (再试一次!) | Auto-show hint |
| **Multiple errors** (3+) | Red pulse | None | "Need help?" (需要帮助?) | Strong hint suggestion |
| **Guessing detected** | Orange flash + ? | Inquisitive sound | "Take your time" (慢慢来) | Slow down pacing |

### 7.2 Error Avoidance & Frustration Prevention

```kotlin
/**
 * Error feedback with frustration detection
 */
class ErrorFeedbackHandler {
    private val errorHistory = mutableMapOf<String, MutableList<Long>>()
    private val frustrationThreshold = 3 // errors before intervention

    fun handleError(wordId: String, attempt: String): ErrorFeedback {
        val now = System.currentTimeMillis()
        val history = errorHistory.getOrPut(wordId) { mutableListOf() }
        history.add(now)

        // Clean old errors (older than 5 minutes)
        val fiveMinutesAgo = now - 300_000
        history.removeAll { it < fiveMinutesAgo }

        val consecutiveErrors = history.size
        val timeSinceLastError = if (history.size > 1) {
            now - history[history.size - 2]
        } else 0L

        return when {
            // Detect guessing (rapid-fire errors)
            timeSinceLastError < 2000 && consecutiveErrors >= 2 -> ErrorFeedback(
                visual = ErrorVisual.GUESSING_WARNING,
                audio = AudioSound.GENTLE_CHIME,
                message = "Take your time... 慢慢来",
                showHint = true,
                autoHintLevel = 1
            )

            // Frustration detected (many errors)
            consecutiveErrors >= frustrationThreshold -> ErrorFeedback(
                visual = ErrorVisual.SUPPORTIVE,
                audio = AudioSound.ENCOURAGING,
                message = "Let me help you! 我来帮你!",
                showHint = true,
                autoHintLevel = 2
            )

            // Repeated error
            consecutiveErrors >= 2 -> ErrorFeedback(
                visual = ErrorVisual.GENTLE_SHAKE,
                audio = AudioSound.TRY_AGAIN,
                message = "Try again! 再试一次!",
                showHint = true,
                autoHintLevel = 1
            )

            // First error
            else -> ErrorFeedback(
                visual = ErrorVisual.COLOR_FLASH,
                audio = AudioSound.GENTLE,
                message = "Almost! 差一点!",
                showHint = false,
                autoHintLevel = 0
            )
        }
    }
}

data class ErrorFeedback(
    val visual: ErrorVisual,
    val audio: AudioSound,
    val message: String,
    val showHint: Boolean,
    val autoHintLevel: Int
)

enum class ErrorVisual {
    COLOR_FLASH,      // Simple red tint
    GENTLE_SHAKE,     // Side-to-side shake
    SUPPORTIVE,        // Warm color, no shake
    GUESSING_WARNING   // Orange with question mark
}
```

### 7.3 Error Animation Timings

```
First Error:
├─ Red flash (100ms)
├─ Shake animation (200ms)
├─ Message fade in (150ms)
└─ Message duration (1500ms)
Total: ~1950ms

Repeated Error:
├─ Red flash (100ms)
├─ Shake animation (200ms)
├─ Message fade in (150ms)
├─ Hint suggestion auto-shows (300ms)
└─ Message duration (2000ms)
Total: ~2750ms

Guessing Warning:
├─ Orange flash (150ms)
├─ Question mark pulse (300ms)
├─ Supportive message (2000ms)
└─ Hint auto-reveals (500ms)
Total: ~2950ms
```

### 7.4 Shame-Free Error Messaging

| Negative (Avoid) | Positive (Use) |
|------------------|----------------|
| "Wrong!" ❌ | "Almost there!" ✅ |
| "You failed" ❌ | "Not quite, try again" ✅ |
| "That's bad" ❌ | "Let's try a different way" ✅ |
| "You're wrong again" ❌ | "Learning takes practice!" ✅ |

---

## 8. Cognitive Load Control

### 8.1 Concurrent Animation Limits

```kotlin
/**
 * Cognitive load management
 */
object CognitiveLoadLimits {
    // Maximum concurrent animations by sensitivity mode
    const val MAX_CONCURRENT_NORMAL = 3
    const val MAX_CONCURRENT_REDUCED = 2
    const val MAX_CONCURRENT_MINIMAL = 1

    // Maximum animation intensity
    val MAX_PARTICLE_COUNT_NORMAL = 50
    val MAX_PARTICLE_COUNT_REDUCED = 20
    val MAX_PARTICLE_COUNT_MINIMAL = 0

    // Animation speed multiplier
    const val SPEED_MULTIPLIER_NORMAL = 1.0f
    const val SPEED_MULTIPLIER_REDUCED = 0.8f
    const val SPEED_MULTIPLIER_MINIMAL = 0.5f
}
```

### 8.2 Sensitivity Modes

| Mode | Concurrent | Particles | Speed | Target Users |
|------|-----------|-----------|-------|--------------|
| **Normal** | 3 | Full (50) | 100% | Default |
| **Reduced** | 2 | Limited (20) | 80% | Sensory sensitive |
| **Minimal** | 1 | None (0) | 50% | ADHD, anxiety |
| **Custom** | User setting | User setting | User setting | Personalized |

### 8.3 ADHD-Friendly Design

**Reduce Overstimulation**:
- Fewer simultaneous animations (max 1-2)
- No sudden flashes (use fades instead)
- Consistent, predictable feedback
- Option to disable confetti/particles

**Maintain Engagement**:
- Keep core feedback (color, checkmark)
- Preserve celebration messages
- Maintain pet reactions (slower, calmer)
- Audio feedback becomes more important

### 8.4 Mode Switching UI

```
┌─────────────────────────────────────────┐
│  ⚙️ Settings                            │
├─────────────────────────────────────────┤
│  🎨 Animation Style                     │
│                                         │
│  ○ Normal (recommended)                 │
│    Full animations and effects          │
│                                         │
│  ○ Reduced                              │
│    Fewer effects, slower animations     │
│                                         │
│  ● Minimal (ADHD friendly)              │
│    Simple feedback, no particles        │
│                                         │
│  ─────────────────────────────          │
│                                         │
│  🔔 Sound Effects      [ON ─────●]      │
│  📳 Haptic Feedback    [ON ─────●]      │
│  🎊 Confetti           [ON ─────○]      │
│                                         │
└─────────────────────────────────────────┘
```

---

## 9. Code Examples

### 9.1 Compose Animation Implementation

```kotlin
/**
 * Letter fly-in animation
 */
@Composable
fun LetterBox(
    letter: Char?,
    isRevealed: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "letter_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearEasing
        ),
        label = "letter_alpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
            }
            .border(
                width = 2.dp,
                color = if (isRevealed) Color.Blue else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isRevealed) {
            Text(
                text = letter?.toString() ?: "",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

### 9.2 Star Reveal Animation

```kotlin
/**
 * Staggered star reveal animation
 */
@Composable
fun StarRatingDisplay(
    stars: Int,
    modifier: Modifier = Modifier
) {
    val visibleStars = remember { mutableStateListOf(false, false, false) }

    LaunchedEffect(stars) {
        // Reset all
        repeat(3) { visibleStars[it] = false }
        // Staggered reveal
        repeat(stars) { index ->
            delay(100L * (index + 1)) // 100ms, 200ms, 300ms
            visibleStars[index] = true
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val scale by animateFloatAsState(
                targetValue = if (visibleStars[index]) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = 300f
                ),
                label = "star_$index"
            )

            val rotation by animateFloatAsState(
                targetValue = if (visibleStars[index]) 0f else -180f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                ),
                label = "star_rotation_$index"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.rotationZ = rotation
                    }
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (index < stars) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
```

### 9.3 Feedback Coordinator Usage

```kotlin
/**
 * Usage example of AnimationCoordinator
 */
@Composable
fun FeedbackContainer(
    feedbackState: FeedbackState,
    modifier: Modifier = Modifier
) {
    val coordinator = remember { AnimationCoordinator() }

    LaunchedEffect(feedbackState) {
        when (feedbackState) {
            is FeedbackState.Correct -> {
                coordinator.requestAnimation(
                    Animation(
                        group = AnimationGroup.ANSWER_FEEDBACK,
                        priority = 1,
                        duration = 100.ms
                    ) { /* Play correct animation */ }
                )

                if (feedbackState.stars == 3) {
                    delay(200)
                    coordinator.requestAnimation(
                        Animation(
                            group = AnimationGroup.CELEBRATION,
                            priority = 2,
                            duration = 1200.ms
                        ) { /* Play celebration */ }
                    )
                }
            }
            is FeedbackState.Incorrect -> {
                coordinator.requestAnimation(
                    Animation(
                        group = AnimationGroup.ANSWER_FEEDBACK,
                        priority = 1,
                        duration = 200.ms
                    ) { /* Play error animation */ }
                )
            }
        }
    }

    // UI content based on feedbackState
}
```

### 9.4 Color Flash Effect

```kotlin
/**
 * Instant color flash feedback
 */
@Composable
fun FlashFeedback(
    flashColor: Color,
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (trigger) 0.3f else 0f,
        animationSpec = tween(
            durationMillis = 75,
            easing = LinearEasing
        ),
        label = "flash_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(flashColor.copy(alpha = alpha))
    )
}
```

---

## 10. Acceptance Criteria

### 10.1 Performance Criteria

| Criterion | Target | Measurement Method |
|-----------|--------|-------------------|
| **Frame rate** | ≥60fps | Android GPU Profiler |
| **Response time** | ≤50ms | Performance monitoring |
| **Animation smoothness** | No jank | Visual inspection + profiler |
| **Memory impact** | <20MB increase | Heap dump analysis |
| **Battery impact** | <5% increase / hour | Battery Historian |

### 10.2 Functional Criteria

- [ ] All animations use specified easing curves
- [ ] Priority system prevents animation conflicts
- [ ] Concurrent animation limit respected (max 3)
- [ ] Star rating produces differentiated feedback
- [ ] Error feedback progressive (first vs repeated)
- [ ] Sensitivity modes functional (normal/reduced/minimal)
- [ ] Pet reactions synchronized with feedback

### 10.3 User Experience Criteria

| Metric | Target | Test Method |
|--------|--------|-------------|
| **Clarity** | 100% users understand feedback | User testing |
| **Delight** | ≥80% positive feedback on celebrations | Survey |
| **No overwhelm** | ≤5% report "too much" | Sensory questionnaire |
| **ADHD friendly** | ≥90% completion in minimal mode | Usability study |

### 10.4 Testing Checklist

**Unit Tests**:
- [ ] Easing functions produce correct values
- [ ] Animation coordinator respects priorities
- [ ] Concurrent limit enforced

**Integration Tests**:
- [ ] Feedback triggered on answer submission
- [ ] Multiple feedback events coordinated correctly
- [ ] Sensitivity mode changes affect all animations

**Visual Tests**:
- [ ] Screenshot tests for all star levels
- [ ] Animation timing verified
- [ ] No flickering or artifacts

**User Tests**:
- [ ] 10 children (age 6-12) complete test levels
- [ ] Feedback confusion rate measured
- [ ] Delight/engagement surveyed

---

## Appendix A: Quick Reference

### Animation Duration Quick Reference

```
Instant:  75ms   - Button press, color flash
Quick:    150ms  - Letter fly-in, icon pop
Standard: 300ms  - Card slide, progress fill
Extended: 500ms  - Star reveal, pet reaction
Cinematic: 1200ms - Level complete, achievement
```

### Easing Quick Reference

```
FastOutSlowIn - Enter, pop-in, appear
SlowInFastOut - Exit, dismiss, disappear
Bounce        - Celebration, attention, buttons
Smooth        - Movement, position changes
Linear        - Progress, loading, consistent
```

### Color Quick Reference

```kotlin
val CORRECT = Color(0xFF4CAF50)  // Green - Success
val INCORRECT = Color(0xFFF44336) // Red - Error (soft)
val HINT = Color(0xFF2196F3)      // Blue - Information
val WARNING = Color(0xFFFF9800)   // Orange - Caution
val GUESSING = Color(0xFFFF9800)  // Orange - Take time
```

---

**Document Status**: ✅ Complete
**Next Steps**: Implement Epic #1 (Visual Feedback Enhancement)
**Estimated Implementation**: 5-7 development days
