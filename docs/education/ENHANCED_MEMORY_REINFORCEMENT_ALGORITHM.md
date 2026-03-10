# Enhanced Memory Reinforcement Algorithm (EMRA)
## Wordland KET Vocabulary Learning System

**Author**: Education Specialist
**Date**: 2026-02-18
**Status**: Design Phase
**Target Audience**: 10-year-old KET learners

---

## Executive Summary

The Enhanced Memory Reinforcement Algorithm (EMRA) addresses critical gaps in Wordland's current memory system by introducing personalized forgetting curves, dynamic response time baselines, and optimized retrieval practice schedules. This document provides the complete technical specification for implementation.

---

## Table of Contents

1. [Problem Analysis](#problem-analysis)
2. [Educational Foundations](#educational-foundations)
3. [Algorithm Architecture](#algorithm-architecture)
4. [Component Specifications](#component-specifications)
5. [Implementation Guide](#implementation-guide)
6. [Testing Strategy](#testing-strategy)
7. [Expected Outcomes](#expected-outcomes)

---

## Problem Analysis

### Current System Limitations

| Issue | Current Implementation | Impact |
|-------|----------------------|--------|
| **Generic Forgetting Rate** | Fixed 0.5 baseline for all users | Doesn't account for individual memory differences |
| **Fixed Response Times** | 2-6 seconds based only on word difficulty | Ignores personal typing speed and processing time |
| **Session-Only Learning** | No within-session retrieval practice | Misses opportunity for same-session consolidation |
| **Static Difficulty** | Word difficulty 1-5 fixed per word | Doesn't adapt to individual learner's trajectory |

### Research-Based Improvements Needed

1. **Individual Forgetting Curves**: Ebbinghaus curves vary significantly between individuals
2. **Personal Baselines**: Children's typing speeds vary 3x (500ms to 1500ms per letter)
3. **Retrieval Practice**: Same-session review improves long-term retention by 40%
4. **Adaptive Difficulty**: Dynamic adjustment maintains optimal challenge zone (Vygotsky's ZPD)

---

## Educational Foundations

### Theoretical Framework

#### 1. Spaced Repetition Theory (Ebbinghaus, 1885)

**Forgetting Curve Equation**:
```
R(t) = e^(-t/S)
```
- R(t) = Retention at time t
- S = Memory stability (individual-specific)
- t = Time since last review

**Key Insight**: Each learner has a unique "S" value that changes with each word.

#### 2. Testing Effect (Roediger & Karpicke, 2006)

**Finding**: Retrieval practice enhances long-term retention more than restudy.

**Application**:
- Within-session reviews at expanding intervals
- Active recall before spaced review
- Varied question formats

#### 3. Desirable Difficulty (Bjork, 1994)

**Principle**: Learning is most effective when moderately difficult.

**Implementation**:
- Adjust response time thresholds dynamically
- Balance success rate at 70-80%
- Adaptive challenge based on performance

#### 4. Zone of Proximal Development (Vygotsky, 1978)

**Concept**: Learning occurs just beyond current capability.

**Algorithm Application**:
- If success rate > 85%: Increase difficulty
- If success rate < 65%: Decrease difficulty
- Target: 75% success rate

---

## Algorithm Architecture

### System Overview

```
EMRA System
├── Personal Forgetting Curve (PFC) Module
│   ├── Individual Baseline Calculator
│   ├── Forgetting Rate Estimator
│   └── Memory Stability Tracker
├── Dynamic Response Time (DRT) Module
│   ├── Personal Baseline Tracker
│   ├── Word-Specific Multiplier
│   └── Context Adjuster
├── Retrieval Practice Scheduler (RPS)
│   ├── Within-Session Planner
│   ├── Cross-Session Planner
│   └── Priority Calculator
└── Adaptive Difficulty Manager (ADM)
    ├── Performance Analyzer
    ├── Challenge Adjuster
    └── Zone Tracker
```

### Data Flow

```
User Response
    ↓
[DRT Module] → Calculate Quality Score
    ↓
[PFC Module] → Update Memory Stability
    ↓
[ADM] → Adjust Difficulty
    ↓
[RPS] → Schedule Next Review
    ↓
Update UserWordProgress
```

---

## Component Specifications

### Component 1: Personal Forgetting Curve (PFC) Module

#### Purpose
Track and predict individual forgetting patterns for each word.

#### Data Model Extensions

```kotlin
/**
 * Enhanced progress with personalized forgetting curve
 */
data class PersonalForgettingData(
    val wordId: String,
    val userId: String,

    // Individual forgetting parameters
    val personalStability: Float = 50f,        // S value: hours until 50% retention
    val personalDecayRate: Float = 0.5f,       // Individual decay rate
    val stabilityHistory: List<Float> = emptyList(), // Track S over time

    // Learning velocity
    val acquisitionSpeed: Float = 1.0f,        // How fast user acquires new words
    val consolidationRate: Float = 1.0f,       // How fast memory stabilizes

    // Review effectiveness
    val lastReviewEffectiveness: Float = 1.0f, // 0-2, 1 = neutral
    val averageReviewEffectiveness: Float = 1.0f,

    // Timestamps
    val firstLearnTime: Long? = null,
    val lastStabilityUpdate: Long = System.currentTimeMillis()
)
```

#### Algorithm: Personal Stability Calculation

```kotlin
object PersonalForgettingCurveAlgorithm {

    /**
     * Calculate personal stability (S) after a review
     *
     * S_new = S_old × EF × ReviewEffectiveness
     *
     * Where EF is SM-2 ease factor
     */
    fun calculateNewStability(
        oldStability: Float,
        easeFactor: Float,
        quality: Int,
        timeSinceLastReview: Long
    ): Float {
        // Base stability change from SM-2
        val stabilityMultiplier = when {
            quality >= 5 -> 2.0f  // Perfect: Double stability
            quality >= 4 -> 1.5f  // Good: 50% increase
            quality >= 3 -> 1.0f  // OK: Maintain
            quality >= 2 -> 0.5f  // Poor: Halve stability
            else -> 0.25f         // Failed: Quarter stability
        }

        // Time-based decay check
        val hoursElapsed = timeSinceLastReview / (1000f * 60f * 60f)
        val predictedRetention = calculateRetention(oldStability, hoursElapsed)

        // If actual performance beats prediction, increase stability more
        val performanceBonus = if (quality >= 3 && predictedRetention < 0.5f) {
            1.3f // User remembered better than expected
        } else if (quality < 3 && predictedRetention > 0.7f) {
            0.7f // User forgot more than expected
        } else {
            1.0f
        }

        val newStability = oldStability * stabilityMultiplier * performanceBonus

        // Constrain to reasonable bounds (1 hour to 6 months)
        return newStability.coerceIn(1f, 4320f)
    }

    /**
     * Calculate predicted retention at time t
     * R(t) = e^(-t/S)
     */
    fun calculateRetention(stability: Float, hoursElapsed: Float): Float {
        return exp(-hoursElapsed / stability)
    }

    /**
     * Calculate personalized next review time
     * Target: Review when retention drops to 70-80%
     */
    fun calculateNextReviewTime(
        stability: Float,
        targetRetention: Float = 0.75f
    ): Long {
        // Solve for t: R(t) = targetRetention
        // t = -S × ln(targetRetention)
        val hoursUntilReview = -stability * ln(targetRetention)

        // Add small random buffer (±10%) to avoid clustering
        val buffer = Random.nextFloat(-0.1f, 0.1f)
        val adjustedHours = hoursUntilReview * (1 + buffer)

        return System.currentTimeMillis() + (adjustedHours * 3600 * 1000).toLong()
    }

    /**
     * Initialize stability for a new word
     * Based on user's historical acquisition speed
     */
    fun initializeStability(userAcquisitionSpeed: Float): Float {
        return 4f * userAcquisitionSpeed // Start at 4 hours, adjusted by speed
    }
}
```

#### Integration with SM-2

```kotlin
/**
 * Combined SM-2 + Personal Forgetting Curve review processor
 */
fun processReviewWithPersonalCurve(
    currentProgress: UserWordProgress,
    personalData: PersonalForgettingData,
    isCorrect: Boolean,
    responseTimeMs: Long,
    hintsUsed: Int,
    wordDifficulty: Int
): EnhancedReviewResult {
    // 1. Get SM-2 quality score
    val sm2Quality = Sm2Algorithm.calculateQuality(
        isCorrect, responseTimeMs, hintsUsed, wordDifficulty, isChild = true
    )

    // 2. Calculate time since last review
    val timeSinceReview = if (currentProgress.lastReviewTime != null) {
        System.currentTimeMillis() - currentProgress.lastReviewTime
    } else {
        0L
    }

    // 3. Update personal stability
    val newStability = PersonalForgettingCurveAlgorithm.calculateNewStability(
        oldStability = personalData.personalStability,
        easeFactor = currentProgress.easeFactor,
        quality = sm2Quality,
        timeSinceLastReview = timeSinceReview
    )

    // 4. Calculate next review time
    val nextReviewTime = PersonalForgettingCurveAlgorithm.calculateNextReviewTime(
        stability = newStability,
        targetRetention = 0.75f
    )

    // 5. Update SM-2 parameters (for compatibility)
    val sm2Result = Sm2Algorithm.processReview(
        currentProgress = Sm2Progress(
            easeFactor = currentProgress.easeFactor,
            repetitions = currentProgress.sm2Repetitions,
            intervalDays = currentProgress.sm2Interval,
            totalReviews = currentProgress.totalReviews,
            successfulReviews = currentProgress.successfulReviews,
            forgettingRate = personalData.personalDecayRate
        ),
        isCorrect = isCorrect,
        responseTimeMs = responseTimeMs,
        hintsUsed = hintsUsed,
        wordDifficulty = wordDifficulty,
        isChild = true
    )

    return EnhancedReviewResult(
        quality = sm2Quality,
        newStability = newStability,
        nextReviewTime = nextReviewTime,
        sm2Result = sm2Result,
        personalForgettingData = personalData.copy(
            personalStability = newStability,
            lastStabilityUpdate = System.currentTimeMillis(),
            stabilityHistory = personalData.stabilityHistory + newStability
        )
    )
}
```

### Component 2: Dynamic Response Time (DRT) Module

#### Purpose
Calculate personalized expected response times based on user history.

#### Algorithm: Personal Baseline Calculation

```kotlin
object DynamicResponseTimeAlgorithm {

    /**
     * Personal baseline data per user
     */
    data class PersonalBaseline(
        val userId: String,
        val baseTypingSpeed: Float,        // ms per letter (personalized)
        val baseProcessingTime: Float,      // ms overhead (thinking time)
        val wordMultipliers: Map<String, Float> = emptyMap(), // word-specific adjustments
        val lastCalibrated: Long = System.currentTimeMillis()
    )

    /**
     * Calculate expected response time for a specific user and word
     *
     * Expected = BaseProcessing + (WordLength × BaseTypingSpeed) × WordMultiplier × ContextAdjustment
     */
    fun calculateExpectedTime(
        baseline: PersonalBaseline,
        word: String,
        wordDifficulty: Int,
        wordId: String,
        isNewWord: Boolean
    ): Long {
        val wordLength = word.length

        // Get word-specific multiplier (default to 1.0)
        val wordMultiplier = baseline.wordMultipliers.getOrDefault(wordId, 1.0f)

        // Context adjustments
        val contextAdjustment = when {
            isNewWord -> 1.5f        // New words take longer
            wordDifficulty == 5 -> 1.3f  // Hard words take longer
            wordDifficulty == 1 -> 0.8f  // Easy words faster
            else -> 1.0f
        }

        val expectedTime = baseline.baseProcessingTime +
            (wordLength * baseline.baseTypingSpeed * wordMultiplier * contextAdjustment)

        return expectedTime.toLong()
    }

    /**
     * Update personal baseline after each response
     * Uses exponential moving average for smooth adaptation
     */
    fun updateBaseline(
        currentBaseline: PersonalBaseline,
        wordId: String,
        wordLength: Int,
        actualTime: Long,
        expectedTime: Long
    ): PersonalBaseline {
        val ratio = actualTime.toFloat() / expectedTime.toFloat()

        // Adjust word-specific multiplier
        val currentWordMultiplier = currentBaseline.wordMultipliers.getOrDefault(wordId, 1.0f)
        val alphaWord = 0.2f // Learning rate for word-specific adjustments
        val newWordMultiplier = currentWordMultiplier * (1 - alphaWord) + ratio * alphaWord

        // Adjust global baseline if ratio is consistently off
        val alphaGlobal = 0.05f // Slower adaptation for global baseline
        val newTypingSpeed = currentBaseline.baseTypingSpeed *
            (1 - alphaGlobal * (ratio - 1f))

        return currentBaseline.copy(
            baseTypingSpeed = newTypingSpeed.coerceIn(100f, 2000f), // 100ms-2s per letter
            wordMultipliers = currentBaseline.wordMultipliers + (wordId to newWordMultiplier),
            lastCalibrated = System.currentTimeMillis()
        )
    }

    /**
     * Initialize baseline from first 5 responses
     */
    fun initializeBaseline(firstResponses: List<Long>, wordLengths: List<Int>): PersonalBaseline {
        require(firstResponses.size >= 5) { "Need at least 5 responses" }

        // Estimate typing speed from linear regression
        val avgTimePerLetter = firstResponses.zip(wordLengths)
            .map { (time, length) -> time.toFloat() / length }
            .average()
            .toFloat()

        // Estimate base processing time (intercept)
        val estimatedProcessingTime = firstResponses
            .zip(wordLengths)
            .map { (time, length) -> time - (length * avgTimePerLetter) }
            .average()
            .toFloat()
            .coerceAtLeast(500f) // Minimum 500ms processing time

        return PersonalBaseline(
            userId = "user_001",
            baseTypingSpeed = avgTimePerLetter,
            baseProcessingTime = estimatedProcessingTime
        )
    }

    /**
     * Detect guessing based on personal baseline
     */
    fun detectGuessing(
        actualTime: Long,
        expectedTime: Long,
        personalBaseline: PersonalBaseline
    ): Boolean {
        // Guessing if response is faster than 50% of expected time
        // But account for user's natural speed variance
        val threshold = expectedTime * 0.5f
        return actualTime < threshold && actualTime < 2000L // At least 2 seconds
    }
}
```

### Component 3: Retrieval Practice Scheduler (RPS)

#### Purpose
Optimize review timing both within sessions and across sessions.

#### Algorithm: Within-Session Retrieval Schedule

```kotlin
object RetrievalPracticeScheduler {

    /**
     * Schedule for words within current learning session
     */
    data class WithinSessionPlan(
        val wordId: String,
        val initialPosition: Int,
        val reviews: List<Int>  // Positions to review (e.g., [3, 6, 12])
    )

    /**
     * Generate expanding retrieval schedule within session
     *
     * Based: Landauer & Bjork (1978) - expanding intervals
     * Pattern: 1, 3, 6, 12, 24 (positions in session)
     */
    fun generateWithinSessionSchedule(
        totalWordsInSession: Int,
        wordIndex: Int,
        predictedDifficulty: Float
    ): WithinSessionPlan {
        val baseIntervals = listOf(3, 6, 12, 24)

        // Adjust intervals based on predicted difficulty
        val adjustedIntervals = when {
            predictedDifficulty > 0.7f -> {
                // Hard words: More frequent reviews
                listOf(2, 4, 8, 16, 30)
            }
            predictedDifficulty < 0.3f -> {
                // Easy words: Fewer reviews
                listOf(5, 15, 30)
            }
            else -> baseIntervals
        }

        // Filter out positions beyond session length
        val validReviews = adjustedIntervals
            .map { wordIndex + it }
            .filter { it < totalWordsInSession }

        return WithinSessionPlan(
            wordId = "",
            initialPosition = wordIndex,
            reviews = validReviews
        )
    }

    /**
     * Determine which words to review at current session position
     */
    fun getWordsForReview(
        currentPosition: Int,
        sessionPlans: List<WithinSessionPlan>
    ): List<String> {
        return sessionPlans
            .filter { plan ->
                plan.reviews.contains(currentPosition)
            }
            .map { it.wordId }
    }

    /**
     * Schedule cross-session reviews using personal forgetting curve
     */
    fun generateCrossSessionSchedule(
        personalData: PersonalForgettingData,
        currentProgress: UserWordProgress
    ): Long {
        return PersonalForgettingCurveAlgorithm.calculateNextReviewTime(
            stability = personalData.personalStability,
            targetRetention = 0.75f
        )
    }

    /**
     * Calculate review priority for word selection
     * Higher priority = should review sooner
     */
    fun calculateReviewPriority(
        personalData: PersonalForgettingData,
        currentProgress: UserWordProgress,
        currentTime: Long = System.currentTimeMillis()
    ): Float {
        val hoursSinceLastReview = if (currentProgress.lastReviewTime != null) {
            (currentTime - currentProgress.lastReviewTime) / (1000f * 60f * 60f)
        } else {
            1000f // Never reviewed = high priority
        }

        // Calculate current retention
        val currentRetention = PersonalForgettingCurveAlgorithm.calculateRetention(
            stability = personalData.personalStability,
            hoursElapsed = hoursSinceLastReview
        )

        // Priority is inverse of retention (lower retention = higher priority)
        val retentionPriority = (1f - currentRetention) * 100f

        // Add urgency if overdue
        val urgency = if (currentProgress.nextReviewTime != null) {
            val hoursUntilDue = (currentProgress.nextReviewTime - currentTime) / (1000f * 60f * 60f)
            if (hoursUntilDue < 0) {
                abs(hoursUntilDue) * 10f // 10 points per hour overdue
            } else {
                0f
            }
        } else {
            50f // Never scheduled = high priority
        }

        return retentionPriority + urgency
    }
}
```

### Component 4: Adaptive Difficulty Manager (ADM)

#### Purpose
Maintain optimal challenge level (Zone of Proximal Development).

#### Algorithm: Success Rate Tracking

```kotlin
object AdaptiveDifficultyManager {

    data class DifficultyState(
        val userId: String,
        val recentSuccessRate: Float,     // Rolling average
        val recentAverageTime: Float,     // Rolling average
        val currentDifficultyModifier: Float = 1.0f, // 0.5 - 2.0
        val inOptimalZone: Boolean = true,
        val lastAdjustment: Long = System.currentTimeMillis()
    )

    /**
     * Calculate rolling success rate (last 20 attempts)
     */
    fun calculateRollingSuccessRate(
        recentResults: List<Boolean>
    ): Float {
        if (recentResults.isEmpty()) return 0.75f // Default to 75%

        val weights = listOf(
            0.05f, 0.05f, 0.05f, 0.05f, 0.05f,  // Oldest 5: 5% each
            0.05f, 0.05f, 0.05f, 0.05f, 0.05f,
            0.08f, 0.08f, 0.08f, 0.08f, 0.08f,  // Middle 5: 8% each
            0.1f, 0.1f, 0.1f, 0.1f, 0.1f         // Newest 5: 10% each
        )

        val weightedSuccess = recentResults.takeLast(20)
            .reversed()
            .zip(weights)
            .sumOf { (result, weight) -> if (result) weight.toDouble() else 0.0 }

        return weightedSuccess.toFloat().coerceIn(0f, 1f)
    }

    /**
     * Determine if user is in optimal challenge zone
     * Target: 70-80% success rate
     */
    fun isInOptimalZone(successRate: Float): Boolean {
        return successRate in 0.70f..0.80f
    }

    /**
     * Calculate difficulty adjustment
     */
    fun calculateDifficultyAdjustment(
        currentState: DifficultyState,
        recentSuccessRate: Float,
        recentAverageTime: Float,
        expectedAverageTime: Float
    ): DifficultyAdjustment {
        val timeRatio = recentAverageTime / expectedAverageTime

        return when {
            // Too easy: High success rate AND fast responses
            recentSuccessRate > 0.85f && timeRatio < 0.8f -> {
                DifficultyAdjustment(
                    newModifier = (currentState.currentDifficultyModifier * 1.1f)
                        .coerceAtMost(2.0f),
                    action = AdjustmentAction.INCREASE,
                    reason = "Performance excellent, increasing challenge"
                )
            }
            // Too hard: Low success rate AND slow responses
            recentSuccessRate < 0.65f || timeRatio > 1.5f -> {
                DifficultyAdjustment(
                    newModifier = (currentState.currentDifficultyModifier * 0.9f)
                        .coerceAtLeast(0.5f),
                    action = AdjustmentAction.DECREASE,
                    reason = "Struggling detected, reducing difficulty"
                )
            }
            // Optimal zone
            else -> {
                DifficultyAdjustment(
                    newModifier = currentState.currentDifficultyModifier,
                    action = AdjustmentAction.MAINTAIN,
                    reason = "Performance in optimal zone"
                )
            }
        }
    }

    /**
     * Apply difficulty modifier to word selection
     */
    fun selectWordsWithDifficulty(
        allWords: List<Word>,
        difficultyModifier: Float,
        targetCount: Int
    ): List<Word> {
        // Adjust target difficulty
        val targetDifficulty = when {
            difficultyModifier > 1.2f -> 4  // Harder
            difficultyModifier > 1.0f -> 3
            difficultyModifier < 0.8f -> 1  // Easier
            difficultyModifier < 1.0f -> 2
            else -> 2  // Default
        }

        // Select words around target difficulty
        val words = allWords.filter { it.difficulty == targetDifficulty }

        return if (words.size >= targetCount) {
            words.shuffled().take(targetCount)
        } else {
            // Not enough words at exact level, expand range
            val range = 1
            allWords.filter { it.difficulty in (targetDifficulty - range)..(targetDifficulty + range) }
                .shuffled()
                .take(targetCount)
        }
    }
}

data class DifficultyAdjustment(
    val newModifier: Float,
    val action: AdjustmentAction,
    val reason: String
)

enum class AdjustmentAction {
    INCREASE,    // Make harder
    DECREASE,    // Make easier
    MAINTAIN     // Keep same
}
```

---

## Implementation Guide

### Phase 1: Data Model Extension (Week 1)

**File**: `domain/model/PersonalForgettingData.kt`

```kotlin
@Entity(tableName = "personal_forgetting_data")
@Immutable
data class PersonalForgettingData(
    @PrimaryKey
    val userId: String,
    val wordId: String,
    val personalStability: Float = 50f,
    val personalDecayRate: Float = 0.5f,
    val stabilityHistory: String, // JSON serialized list
    val acquisitionSpeed: Float = 1.0f,
    val consolidationRate: Float = 1.0f,
    val lastReviewEffectiveness: Float = 1.0f,
    val firstLearnTime: Long? = null,
    val lastStabilityUpdate: Long = System.currentTimeMillis()
)
```

**File**: `domain/model/PersonalBaseline.kt`

```kotlin
@Entity(tableName = "personal_baseline")
@Immutable
data class PersonalBaseline(
    @PrimaryKey
    val userId: String,
    val baseTypingSpeed: Float = 500f, // ms per letter
    val baseProcessingTime: Float = 2000f,
    val wordMultipliers: String, // JSON serialized map
    val lastCalibrated: Long = System.currentTimeMillis()
)
```

### Phase 2: Algorithm Implementation (Week 2)

**File**: `domain/algorithm/PersonalForgettingCurveAlgorithm.kt`
**File**: `domain/algorithm/DynamicResponseTimeAlgorithm.kt`
**File**: `domain/algorithm/RetrievalPracticeScheduler.kt`
**File**: `domain/algorithm/AdaptiveDifficultyManager.kt`

### Phase 3: Repository & Use Case Updates (Week 3)

**File**: `data/repository/PersonalForgettingRepository.kt`

```kotlin
@Singleton
class PersonalForgettingRepository @Inject constructor(
    private val database: WordDatabase
) {
    suspend fun getPersonalData(userId: String, wordId: String): PersonalForgettingData?
    suspend fun updatePersonalData(data: PersonalForgettingData)
    suspend fun initializePersonalData(userId: String, wordId: String)
}
```

**File**: `domain/usecase/ProcessReviewWithPersonalCurveUseCase.kt`

```kotlin
@Singleton
class ProcessReviewWithPersonalCurveUseCase @Inject constructor(
    private val forgettingRepository: PersonalForgettingRepository,
    private val baselineRepository: PersonalBaselineRepository
) {
    suspend operator fun invoke(
        userId: String,
        wordId: String,
        isCorrect: Boolean,
        responseTimeMs: Long,
        hintsUsed: Int,
        wordDifficulty: Int
    ): EnhancedReviewResult
}
```

### Phase 4: UI Integration (Week 4)

**Files to modify**:
- `ui/viewmodel/LearningViewModel.kt`: Add EMRA processing
- `ui/screens/LearningScreen.kt`: Display personalized stats
- `ui/screens/ReviewScreen.kt`: Use priority-based word selection

---

## Testing Strategy

### Unit Tests

**File**: `app/src/test/java/com/wordland/domain/algorithm/PersonalForgettingCurveAlgorithmTest.kt`

```kotlin
class PersonalForgettingCurveAlgorithmTest {
    @Test
    fun `calculateNewStability doubles on perfect recall`() {
        val oldStability = 10f
        val newStability = PersonalForgettingCurveAlgorithm.calculateNewStability(
            oldStability, 2.5f, 5, 3600000L
        )
        assertEquals(20f, newStability, 0.1f)
    }

    @Test
    fun `calculateRetention follows exponential decay`() {
        val retention = PersonalForgettingCurveAlgorithm.calculateRetention(10f, 10f)
        // R(10) = e^(-10/10) = e^(-1) ≈ 0.368
        assertEquals(0.368f, retention, 0.01f)
    }

    @Test
    fun `calculateNextReviewTime targets 75% retention`() {
        val nextReview = PersonalForgettingCurveAlgorithm.calculateNextReviewTime(10f, 0.75f)
        val hoursUntilReview = (nextReview - System.currentTimeMillis()) / (1000 * 60 * 60f)
        // t = -S × ln(0.75) = -10 × (-0.288) ≈ 2.88 hours
        assertEquals(2.88f, hoursUntilReview, 0.1f)
    }
}
```

**File**: `DynamicResponseTimeAlgorithmTest.kt`

```kotlin
class DynamicResponseTimeAlgorithmTest {
    @Test
    fun `calculateExpectedTime accounts for word length`() {
        val baseline = PersonalBaseline("user_001", 500f, 2000f)
        val expected5 = DynamicResponseTimeAlgorithm.calculateExpectedTime(
            baseline, "hello", 2, "word_1", false
        )
        val expected6 = DynamicResponseTimeAlgorithm.calculateExpectedTime(
            baseline, "hello!", 2, "word_2", false
        )
        assertTrue(expected6 > expected5)
    }

    @Test
    fun `updateBaseline adapts to consistent patterns`() {
        var baseline = PersonalBaseline("user_001", 500f, 2000f)
        // Simulate consistently slower responses
        repeat(10) {
            baseline = DynamicResponseTimeAlgorithm.updateBaseline(
                baseline, "word", 5, 5000L, 3000L
            )
        }
        assertTrue(baseline.baseTypingSpeed > 500f)
    }
}
```

### Integration Tests

**File**: `app/src/test/java/com/wordland/integration/EMRAIntegrationTest.kt`

```kotlin
@ExperimentalCoroutinesApi
class EMRAIntegrationTest {
    @Test
    fun `complete review cycle updates all components`() = runTest {
        // Setup
        val useCase = ProcessReviewWithPersonalCurveUseCase(...)
        val userId = "test_user"
        val wordId = "test_word"

        // First review (learning)
        var result = useCase(
            userId, wordId, isCorrect = true,
            responseTimeMs = 3000L, hintsUsed = 0, wordDifficulty = 2
        )

        // Verify stability increased
        assertTrue(result.personalForgettingData.personalStability > 50f)

        // Second review (after 1 hour)
        advanceTimeBy(3_600_000L)
        result = useCase(
            userId, wordId, isCorrect = true,
            responseTimeMs = 2500L, hintsUsed = 0, wordDifficulty = 2
        )

        // Verify stability increased again
        assertTrue(result.personalForgettingData.personalStability > result2.personalForgettingData.personalStability)
    }
}
```

### A/B Testing Plan

**Variant A**: Current system (control)
**Variant B**: EMRA with all components

**Metrics to track**:
- Long-term retention (30-day)
- Words mastered per hour
- User engagement (session length)
- Frustration indicators (abandon rate)

---

## Expected Outcomes

### Quantitative Improvements

| Metric | Current | Target (3 months) |
|--------|---------|-------------------|
| 30-day retention | ~45% | 65% (+44%) |
| Words learned per hour | 8 | 10 (+25%) |
| Review accuracy | 72% | 78% (+8%) |
| Session completion rate | 68% | 80% (+18%) |

### Qualitative Improvements

1. **Personalized Learning**: Each user gets optimized review timing
2. **Reduced Frustration**: Difficulty adapts to individual ability
3. **Increased Engagement**: Optimal challenge maintains interest
4. **Better Long-Term Memory**: Personalized forgetting curves maximize retention

### Monitoring Plan

**Weekly metrics**:
- Average personal stability across users
- Typing speed calibration accuracy
- Review priority distribution
- Optimal zone achievement rate

**Monthly analysis**:
- Retention curve by decile
- Difficulty modifier drift
- Cross-session review compliance

---

## Appendix

### A. Mathematical Derivations

#### Forgetting Curve Derivation

Starting from Ebbinghaus:
```
R(t) = e^(-t/S)
```

Solving for t when R(t) = 0.75:
```
0.75 = e^(-t/S)
ln(0.75) = -t/S
t = -S × ln(0.75)
t ≈ 0.288 × S
```

#### Response Time Baseline

Linear model:
```
T = T0 + L × S
```
- T = Total response time
- T0 = Base processing time
- L = Word length
- S = Typing speed per letter

Using exponential moving average for adaptation:
```
S_new = S_old × (1 - α) + S_measured × α
```

### B. Reference Implementation

See accompanying files:
- `PersonalForgettingCurveAlgorithm.kt`
- `DynamicResponseTimeAlgorithm.kt`
- `RetrievalPracticeScheduler.kt`
- `AdaptiveDifficultyManager.kt`

### C. Pseudocode for Complete EMRA Flow

```
FUNCTION ProcessUserResponse(userId, wordId, response, time):
    // 1. Get current state
    progress = GetUserProgress(userId, wordId)
    personal = GetPersonalData(userId, wordId)
    baseline = GetPersonalBaseline(userId)

    // 2. Calculate expected time
    expectedTime = DynamicResponseTimeAlgorithm.calculateExpectedTime(
        baseline, response.word, response.difficulty, wordId, response.isNewWord
    )

    // 3. Detect guessing
    isGuessing = DynamicResponseTimeAlgorithm.detectGuessing(
        time, expectedTime, baseline
    )

    // 4. Calculate quality score
    quality = Sm2Algorithm.calculateQuality(
        response.isCorrect, time, response.hintsUsed, response.difficulty, true
    )

    // 5. Update personal forgetting curve
    newStability = PersonalForgettingCurveAlgorithm.calculateNewStability(
        personal.personalStability, progress.easeFactor, quality, progress.timeSinceLastReview
    )

    // 6. Update personal baseline
    newBaseline = DynamicResponseTimeAlgorithm.updateBaseline(
        baseline, wordId, response.word.length, time, expectedTime
    )

    // 7. Calculate next review time
    nextReview = PersonalForgettingCurveAlgorithm.calculateNextReviewTime(newStability)

    // 8. Update adaptive difficulty
    successRate = AdaptiveDifficultyManager.calculateRollingSuccessRate(
        GetUserRecentResults(userId, 20)
    )
    difficultyAdjustment = AdaptiveDifficultyManager.calculateDifficultyAdjustment(
        currentDifficultyState, successRate, averageTime, expectedTime
    )

    // 9. Schedule within-session reviews
    sessionPlan = RetrievalPracticeScheduler.generateWithinSessionSchedule(
        totalWords, currentWordIndex, predictedDifficulty
    )

    // 10. Persist all updates
    SaveUserProgress(progress with updated values)
    SavePersonalData(personal with newStability)
    SavePersonalBaseline(newBaseline)
    SaveDifficultyState(difficultyState with adjustment)

    RETURN EnhancedReviewResult(quality, newStability, nextReview, ...)
END FUNCTION
```

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Next Review**: 2026-03-18
