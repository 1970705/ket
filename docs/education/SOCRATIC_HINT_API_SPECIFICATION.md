# Socratic Hint System - API Specification
## Technical Integration Document

**Author**: Education Specialist
**Date**: 2026-02-18
**Version**: 1.0
**Status**: Ready for Implementation

---

## Overview

This document provides the complete API specification for integrating the Socratic Hint System into Wordland's existing architecture.

---

## Data Models

### SocraticQuestion

```kotlin
package com.wordland.domain.model

data class SocraticQuestion(
    val id: String,
    val targetWord: String,
    val hintLevel: HintLevel,
    val questionType: QuestionType,
    val questionText: String,
    val followUpText: String?,
    val alternativePhrasings: List<String>,
    val associatedContext: String?,
    val difficulty: Int,
    val expectedThinkingTime: Long,
    val createdAt: Long = System.currentTimeMillis()
)

enum class HintLevel {
    LEVEL_1_ACTIVATION,    // Broad, open-ended questions
    LEVEL_2_GUIDANCE,      // Structured, specific guidance
    LEVEL_3_REVELATION     // Near-complete answer
}

enum class QuestionType {
    RECALL,        // Activate prior knowledge
    ASSOCIATION,   // Build connections
    CONTEXTUAL,    // Use situations/scenarios
    ANALYTICAL,    // Word parts/morphology
    SYNTHESIS      // Combine clues
}
```

### SocraticHintResult

```kotlin
package com.wordland.domain.model

data class SocraticHintResult(
    val hintText: String,
    val hintType: HintType,
    val questionType: QuestionType,
    val followUpText: String?,
    val expectedThinkingTime: Long,
    val metadata: SocraticHintMetadata?,
    val timestamp: Long = System.currentTimeMillis()
)

data class SocraticHintMetadata(
    val questionId: String,
    val level: HintLevel,
    val requiresResponse: Boolean,
    val allowsPartialCredit: Boolean = false
)

enum class HintType {
    SOCRATIC,          // Question-based hints
    MORPHOLOGICAL,     // Root/affix based
    TRADITIONAL        // Letter-based (fallback)
}
```

### UserQuestionProgress

```kotlin
package com.wordland.domain.model

@Entity(tableName = "user_question_progress")
@Immutable
data class UserQuestionProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,
    val questionId: String,

    // Tracking
    val timesSeen: Int = 0,
    val lastSeenAt: Long? = null,

    // Response tracking
    val timesResponded: Int = 0,
    val correctResponses: Int = 0,

    // Time tracking
    val averageResponseTime: Long = 0,  // milliseconds
    val fastestResponseTime: Long = Long.MAX_VALUE,

    // Effectiveness
    val ledToCorrectAnswer: Boolean? = null,
    val hintHelpfulnessRating: Int? = null,  // 1-5 user rating

    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## Core APIs

### SocraticHintGenerator

```kotlin
package com.wordland.domain.hint

@Singleton
interface SocraticHintGenerator {
    /**
     * Generate a Socratic hint for the given word
     *
     * @param word The target word
     * @param hintLevel Hint level (1-3)
     * @param userId User ID for personalization
     * @return SocraticHintResult with question and metadata
     */
    suspend fun generateHint(
        word: Word,
        hintLevel: Int,
        userId: String
    ): SocraticHintResult

    /**
     * Generate a dynamic question when database has no pre-written question
     */
    suspend fun generateDynamicQuestion(
        word: Word,
        level: HintLevel,
        userId: String
    ): SocraticHintResult

    /**
     * Calculate expected thinking time based on question characteristics
     */
    fun calculateExpectedTime(question: SocraticQuestion): Long

    /**
     * Check if hint should be upgraded to next level
     */
    fun shouldUpgradeHintLevel(
        currentLevel: Int,
        timeElapsed: Long,
        previousAttempts: Int
    ): Boolean
}
```

**Default Implementation**:

```kotlin
@Singleton
class SocraticHintGeneratorImpl @Inject constructor(
    private val questionDatabase: SocraticQuestionDatabase,
    private val userProfileRepository: UserProfileRepository,
    private val questionProgressRepository: UserQuestionProgressRepository
) : SocraticHintGenerator {

    override suspend fun generateHint(
        word: Word,
        hintLevel: Int,
        userId: String
    ): SocraticHintResult {
        val level = when (hintLevel) {
            1 -> HintLevel.LEVEL_1_ACTIVATION
            2 -> HintLevel.LEVEL_2_GUIDANCE
            3 -> HintLevel.LEVEL_3_REVELATION
            else -> HintLevel.LEVEL_1_ACTIVATION
        }

        // Try to get pre-written question
        val question = questionDatabase.getQuestion(word.word, level, userId)

        return if (question != null) {
            // Track question view
            questionProgressRepository.recordView(userId, question.id)

            SocraticHintResult(
                hintText = question.questionText,
                hintType = HintType.SOCRATIC,
                questionType = question.questionType,
                followUpText = question.followUpText,
                expectedThinkingTime = question.expectedThinkingTime,
                metadata = SocraticHintMetadata(
                    questionId = question.id,
                    level = level,
                    requiresResponse = level != HintLevel.LEVEL_3_REVELATION
                )
            )
        } else {
            // Fall back to dynamic generation
            generateDynamicQuestion(word, level, userId)
        }
    }

    override suspend fun generateDynamicQuestion(
        word: Word,
        level: HintLevel,
        userId: String
    ): SocraticHintResult {
        val userProfile = userProfileRepository.getUserProfile(userId)
        val vocabularyLevel = userProfile?.vocabularyLevel ?: 0.5f

        val (questionText, followUpText, questionType) = when (level) {
            HintLevel.LEVEL_1_ACTIVATION -> {
                generateActivationQuestion(word, vocabularyLevel)
            }
            HintLevel.LEVEL_2_GUIDANCE -> {
                generateGuidanceQuestion(word, vocabularyLevel)
            }
            HintLevel.LEVEL_3_REVELATION -> {
                generateRevelationQuestion(word)
            }
        }

        return SocraticHintResult(
            hintText = questionText,
            hintType = HintType.SOCRATIC,
            questionType = questionType,
            followUpText = followUpText,
            expectedThinkingTime = calculateExpectedTimeForType(questionType),
            metadata = SocraticHintMetadata(
                questionId = "dynamic_${word.id}_${level.name}",
                level = level,
                requiresResponse = level != HintLevel.LEVEL_3_REVELATION
            )
        )
    }

    private fun generateActivationQuestion(
        word: Word,
        vocabularyLevel: Float
    ): Triple<String, String?, QuestionType> {
        // Use example sentences first
        val examples = parseExampleSentences(word.exampleSentences)
        if (examples.isNotEmpty()) {
            val example = examples.first()
            return Triple(
                "Think about this: \"${example.sentence}\" What's the word for \"${example.translation}\"?",
                null,
                QuestionType.CONTEXTUAL
            )
        }

        // Use related words
        val related = parseRelatedWords(word.relatedWords)
        if (related.isNotEmpty()) {
            return Triple(
                "It's similar to '${related.first()}'. What word means \"${word.translation}\"?",
                "Starts with: ${word.word.first()}",
                QuestionType.ASSOCIATION
            )
        }

        // Generic activation
        return Triple(
            "What word means \"${word.translation}\"?",
            "It has ${word.word.length} letters",
            QuestionType.RECALL
        )
    }

    private fun generateGuidanceQuestion(
        word: Word,
        vocabularyLevel: Float
    ): Triple<String, String?, QuestionType> {
        val firstLetter = word.word.first()
        val wordLength = word.word.length

        return when {
            wordLength <= 4 -> {
                Triple(
                    "It has $wordLength letters and starts with '$firstLetter'. Meaning: ${word.translation}",
                    "${word.word.first()}_${word.word.drop(1)}",
                    QuestionType.SYNTHESIS
                )
            }
            else -> {
                Triple(
                    "Starts with '${word.word.take(2)}' and means \"${word.translation}\"",
                    "${word.word.take(2)}${"_".repeat(wordLength - 2)}",
                    QuestionType.SYNTHESIS
                )
            }
        }
    }

    private fun generateRevelationQuestion(
        word: Word
    ): Triple<String, String?, QuestionType> {
        val masked = if (word.word.length <= 4) {
            word.word.dropLast(1) + "_"
        } else {
            word.word.dropLast(2) + "__"
        }

        return Triple(
            "The word is: \"$masked\" (meaning: ${word.translation})",
            null,
            QuestionType.SYNTHESIS
        )
    }

    override fun calculateExpectedTime(question: SocraticQuestion): Long {
        val wordCount = question.questionText.split(" ").size
        val readingTime = wordCount * 300L  // 300ms per word for children

        val thinkingTime = when (question.questionType) {
            QuestionType.RECALL -> 2000L
            QuestionType.ASSOCIATION -> 3000L
            QuestionType.CONTEXTUAL -> 4000L
            QuestionType.ANALYTICAL -> 5000L
            QuestionType.SYNTHESIS -> 6000L
        }

        return readingTime + thinkingTime
    }

    private fun calculateExpectedTimeForType(type: QuestionType): Long {
        return when (type) {
            QuestionType.RECALL -> 2500L
            QuestionType.ASSOCIATION -> 3500L
            QuestionType.CONTEXTUAL -> 4500L
            QuestionType.ANALYTICAL -> 5500L
            QuestionType.SYNTHESIS -> 6500L
        }
    }

    override fun shouldUpgradeHintLevel(
        currentLevel: Int,
        timeElapsed: Long,
        previousAttempts: Int
    ): Boolean {
        // Upgrade if:
        // 1. First attempt and > 15 seconds elapsed
        // 2. Multiple attempts (>2)
        return when (currentLevel) {
            1 -> timeElapsed > 15000 || previousAttempts >= 2
            2 -> previousAttempts >= 2
            else -> false
        }
    }
}
```

### SocraticQuestionDatabase

```kotlin
package com.wordland.data.repository

@Singleton
interface SocraticQuestionDatabase {
    /**
     * Get a question for the target word and hint level
     *
     * @param targetWord The word being learned
     * @param hintLevel The hint level (1-3)
     * @param userId User ID (to avoid repeating questions)
     * @return SocraticQuestion or null if none found
     */
    suspend fun getQuestion(
        targetWord: String,
        hintLevel: HintLevel,
        userId: String?
    ): SocraticQuestion?

    /**
     * Get all questions for a specific word
     */
    suspend fun getQuestionsForWord(targetWord: String): List<SocraticQuestion>

    /**
     * Get recently asked questions for a user
     */
    suspend fun getRecentlyAskedQuestions(
        userId: String,
        limit: Int = 20
    ): List<String>

    /**
     * Add a new question to the database
     */
    suspend fun addQuestion(question: SocraticQuestion)

    /**
     * Batch import questions
     */
    suspend fun importQuestions(questions: List<SocraticQuestion>)
}
```

**Room Implementation**:

```kotlin
@Singleton
class SocraticQuestionDatabaseImpl @Inject constructor(
    private val database: WordDatabase
) : SocraticQuestionDatabase {

    private val dao get() = database.socraticQuestionDao()

    override suspend fun getQuestion(
        targetWord: String,
        hintLevel: HintLevel,
        userId: String?
    ): SocraticQuestion? {
        // Get all matching questions
        val matching = dao.getByWordAndLevel(targetWord, hintLevel.name)

        // Filter out recently asked if user provided
        val available = if (userId != null) {
            val recent = getRecentlyAskedQuestions(userId, limit = 10)
            matching.filterNot { it.id in recent }
        } else {
            matching
        }

        // Return random from available
        return available.randomOrNull()
    }

    override suspend fun getQuestionsForWord(targetWord: String): List<SocraticQuestion> {
        return dao.getByWord(targetWord)
    }

    override suspend fun getRecentlyAskedQuestions(userId: String, limit: Int): List<String> {
        return dao.getRecentQuestionIds(userId, limit)
    }

    override suspend fun addQuestion(question: SocraticQuestion) {
        dao.insert(question)
    }

    override suspend fun importQuestions(questions: List<SocraticQuestion>) {
        dao.insertAll(questions)
    }
}
```

### EnhancedHintSystem

```kotlin
package com.wordland.domain.hint

@Singleton
class EnhancedHintSystem @Inject constructor(
    private val socraticHintGenerator: SocraticHintGenerator,
    private val morphologicalHintGenerator: MorphologicalHintGenerator,
    private val traditionalHintGenerator: TraditionalHintGenerator,
    private val morphemeRepository: MorphemeRepository,
    private val progressRepository: UserMorphemeProgressRepository
) {

    suspend fun generateHint(
        word: Word,
        userId: String,
        hintLevel: Int,
        attemptNumber: Int
    ): HintResult {
        // Priority 1: Check if user has learned morphemes in this word
        val morphemes = word.root?.let { listOfNotNull(
            morphemeRepository.getMorpheme(it),
            word.prefix?.let { morphemeRepository.getMorpheme(it) },
            word.suffix?.let { morphemeRepository.getMorpheme(it) }
        ) } ?: emptyList()

        val learnedMorphemes = morphemes.filter { morpheme ->
            val progress = progressRepository.getUserProgress(userId, morpheme.id)
            progress?.status == MorphemeLearningStatus.LEARNING ||
            progress?.status == MorphemeLearningStatus.MASTERED
        }

        if (learnedMorphemes.isNotEmpty()) {
            // Use morphological hint (highest cognitive processing)
            return morphologicalHintGenerator.generate(
                word, learnedMorphemes, hintLevel
            )
        }

        // Priority 2: Use Socratic questioning for levels 1-2
        if (hintLevel <= 2) {
            return socraticHintGenerator.generateHint(
                word = word,
                hintLevel = hintLevel,
                userId = userId
            ).let { socraticResult ->
                HintResult.Socratic(socraticResult)
            }
        }

        // Priority 3: Fall back to traditional hints
        return traditionalHintGenerator.generate(
            word.word, hintLevel
        ).let { traditionalResult ->
            HintResult.Traditional(traditionalResult)
        }
    }
}

sealed class HintResult {
    data class Socratic(val result: SocraticHintResult) : HintResult()
    data class Morphological(val result: MorphologicalHintResult) : HintResult()
    data class Traditional(val result: TraditionalHintResult) : HintResult()
}
```

---

## ViewModel Integration

### LearningViewModel Extensions

```kotlin
@HiltViewModel
class LearningViewModel @Inject constructor(
    // ... existing dependencies
    private val enhancedHintSystem: EnhancedHintSystem,
    private val questionProgressRepository: UserQuestionProgressRepository,
    private val hintResponseAnalyzer: HintResponseAnalyzer
) : ViewModel() {

    // ... existing code

    fun useSocraticHint() {
        viewModelScope.launch {
            val currentState = _uiState.value as? LearningUiState.Ready
                ?: return@launch

            // Determine appropriate hint level
            val hintLevel = if (currentState.hintLevel == 0) {
                1
            } else {
                currentState.hintLevel + 1
            }

            // Generate hint using enhanced system
            val hintResult = enhancedHintSystem.generateHint(
                word = currentState.question.targetWord.toWord(),
                userId = userId,
                hintLevel = hintLevel,
                attemptNumber = currentState.incorrectAttempts
            )

            // Update UI state based on hint type
            _uiState.value = when (hintResult) {
                is HintResult.Socratic -> {
                    currentState.copy(
                        hintText = hintResult.result.hintText,
                        hintLevel = hintLevel,
                        hintType = HintType.SOCRATIC,
                        hintMetadata = hintResult.result.metadata,
                        showHintCard = true,
                        hintThinkingTime = hintResult.result.expectedThinkingTime
                    )
                }
                is HintResult.Morphological -> {
                    currentState.copy(
                        hintText = hintResult.result.hintText,
                        hintLevel = hintLevel,
                        hintType = HintType.MORPHOLOGICAL,
                        hintMetadata = hintResult.result.metadata,
                        showHintCard = true
                    )
                }
                is HintResult.Traditional -> {
                    currentState.copy(
                        hintText = hintResult.result.hintText,
                        hintLevel = hintLevel,
                        hintType = HintType.TRADITIONAL,
                        showHintCard = true
                    )
                }
            }

            // Track hint usage
            trackHintUsage(hintResult, hintLevel)
        }
    }

    private fun trackHintUsage(hintResult: HintResult, level: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value as? LearningUiState.Ready ?: return@launch

            when (hintResult) {
                is HintResult.Socratic -> {
                    hintResult.result.metadata?.let { metadata ->
                        questionProgressRepository.recordView(
                            userId = userId,
                            questionId = metadata.questionId
                        )
                    }
                }
                else -> {
                    // Track traditional hints
                    hintUsageTracker.trackTraditionalHint(
                        userId = userId,
                        wordId = currentState.question.wordId,
                        level = level
                    )
                }
            }
        }
    }

    fun submitHintResponse(response: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? LearningUiState.Ready
                ?: return@launch

            val metadata = currentState.hintMetadata
                ?: return@launch

            // Analyze response
            val analysis = hintResponseAnalyzer.analyzeResponse(
                response = response,
                targetWord = currentState.question.targetWord,
                metadata = metadata
            )

            // Record response
            questionProgressRepository.recordResponse(
                userId = userId,
                questionId = metadata.questionId,
                response = response,
                isCorrect = analysis.isCorrect,
                responseTime = analysis.responseTime
            )

            // Provide feedback
            when {
                analysis.isCorrect -> {
                    _uiState.value = currentState.copy(
                        hintText = "✓ Correct! Great job!",
                        hintFeedbackType = HintFeedbackType.CORRECT,
                        hintMetadata = metadata.copy(
                            requiresResponse = false
                        )
                    )
                }
                analysis.isClose -> {
                    _uiState.value = currentState.copy(
                        hintText = analysis.feedback ?: "Close! Try again...",
                        hintFeedbackType = HintFeedbackType.CLOSE,
                        showHintCard = true
                    )
                }
                else -> {
                    _uiState.value = currentState.copy(
                        hintText = analysis.feedback ?: "Not quite. Would you like another hint?",
                        hintFeedbackType = HintFeedbackType.INCORRECT,
                        showHintCard = true
                    )
                }
            }
        }
    }

    fun dismissHint() {
        val currentState = _uiState.value as? LearningUiState.Ready ?: return
        _uiState.value = currentState.copy(
            showHintCard = false,
            hintText = null
        )
    }

    fun requestFollowUp() {
        val currentState = _uiState.value as? LearningUiState.Ready ?: return

        // Get follow-up text if available
        val followUpText = when (val hintType = currentState.hintType) {
            HintType.SOCRATIC -> {
                // This would be stored from the original hint result
                // For now, we'll need to regenerate or store it
                currentState.hintFollowUpText
            }
            else -> null
        }

        if (followUpText != null) {
            _uiState.value = currentState.copy(
                hintText = followUpText,
                showHintCard = true
            )
        }
    }
}
```

---

## DAO Definitions

### SocraticQuestionDao

```kotlin
@Dao
interface SocraticQuestionDao {
    @Query("SELECT * FROM socratic_questions WHERE targetWord = :targetWord AND hintLevel = :hintLevel")
    suspend fun getByWordAndLevel(targetWord: String, hintLevel: String): List<SocraticQuestion>

    @Query("SELECT * FROM socratic_questions WHERE targetWord = :targetWord")
    suspend fun getByWord(targetWord: String): List<SocraticQuestion>

    @Query("""
        SELECT DISTINCT questionId
        FROM user_question_progress
        WHERE userId = :userId
        ORDER BY lastSeenAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentQuestionIds(userId: String, limit: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: SocraticQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<SocraticQuestion>)

    @Query("DELETE FROM socratic_questions")
    suspend fun clearAll()
}
```

### UserQuestionProgressDao

```kotlin
@Dao
interface UserQuestionProgressDao {
    @Query("""
        SELECT * FROM user_question_progress
        WHERE userId = :userId AND questionId = :questionId
    """)
    suspend fun getProgress(userId: String, questionId: String): UserQuestionProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserQuestionProgress)

    @Update
    suspend fun update(progress: UserQuestionProgress)

    @Query("""
        UPDATE user_question_progress
        SET timesSeen = timesSeen + 1,
            lastSeenAt = :timestamp
        WHERE userId = :userId AND questionId = :questionId
    """)
    suspend fun recordView(userId: String, questionId: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_question_progress
        SET timesResponded = timesResponded + 1,
            correctResponses = correctResponses + :isCorrectInt,
            averageResponseTime = (
                (averageResponseTime * timesResponded + :responseTime) / (timesResponded + 1)
            ),
            fastestResponseTime = MIN(fastestResponseTime, :responseTime)
        WHERE userId = :userId AND questionId = :questionId
    """)
    suspend fun recordResponse(
        userId: String,
        questionId: String,
        responseTime: Long,
        isCorrectInt: Int  // 1 for correct, 0 for incorrect
    )

    @Query("""
        SELECT * FROM user_question_progress
        WHERE userId = :userId
        ORDER BY lastSeenAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentProgress(userId: String, limit: Int = 50): List<UserQuestionProgress>
}
```

---

## UI Component Specifications

### SocraticHintCard Props

```kotlin
@Composable
fun SocraticHintCard(
    // Required
    hintText: String,
    hintType: HintType,
    questionType: QuestionType,

    // Optional
    followUpText: String? = null,
    expectedThinkingTime: Long? = null,
    requiresResponse: Boolean = false,

    // Callbacks
    onResponse: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
    onFollowUpRequest: () -> Unit = {},

    // Styling
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    showThinkingIndicator: Boolean = true
)
```

---

## Database Schema

### socratic_questions Table

```sql
CREATE TABLE socratic_questions (
    id TEXT PRIMARY KEY,
    targetWord TEXT NOT NULL,
    hintLevel TEXT NOT NULL,  -- 'LEVEL_1_ACTIVATION', 'LEVEL_2_GUIDANCE', 'LEVEL_3_REVELATION'
    questionType TEXT NOT NULL,
    questionText TEXT NOT NULL,
    followUpText TEXT,
    alternativePhrasings TEXT,  -- JSON array
    associatedContext TEXT,
    difficulty INTEGER NOT NULL,
    expectedThinkingTime INTEGER NOT NULL,
    createdAt INTEGER NOT NULL
);

CREATE INDEX idx_socratic_questions_word ON socratic_questions(targetWord);
CREATE INDEX idx_socratic_questions_level ON socratic_questions(hintLevel);
```

### user_question_progress Table

```sql
CREATE TABLE user_question_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId TEXT NOT NULL,
    questionId TEXT NOT NULL,
    timesSeen INTEGER DEFAULT 0,
    lastSeenAt INTEGER,
    timesResponded INTEGER DEFAULT 0,
    correctResponses INTEGER DEFAULT 0,
    averageResponseTime INTEGER DEFAULT 0,
    fastestResponseTime INTEGER,
    ledToCorrectAnswer INTEGER,
    hintHelpfulnessRating INTEGER,
    updatedAt INTEGER NOT NULL,
    UNIQUE(userId, questionId)
);

CREATE INDEX idx_user_question_progress_user ON user_question_progress(userId);
CREATE INDEX idx_user_question_progress_last_seen ON user_question_progress(lastSeenAt);
```

---

## Testing Interfaces

### SocraticHintGeneratorTest

```kotlin
interface SocraticHintGeneratorTest {
    @Test
    fun `generateHint returns SocraticHintResult with correct level`()

    @Test
    fun `generateHint prioritizes pre-written questions over dynamic`()

    @Test
    fun `generateDynamicQuestion creates appropriate activation question`()

    @Test
    fun `calculateExpectedTime accounts for word count and type`()

    @Test
    fun `shouldUpgradeHintLevel returns true after 15 seconds at level 1`()
}
```

---

## Migration Path

### Phase 1: Data Layer (Week 1)
1. Create database schema
2. Implement DAOs
3. Seed initial question database (50 questions)

### Phase 2: Domain Layer (Week 2)
1. Implement SocraticHintGenerator
2. Implement SocraticQuestionDatabase
3. Implement EnhancedHintSystem router

### Phase 3: Integration (Week 3)
1. Update LearningViewModel
2. Create HintResponseAnalyzer
3. Implement question progress tracking

### Phase 4: UI (Week 4)
1. Implement SocraticHintCard
2. Update LearningScreen
3. Add hint response input UI

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
