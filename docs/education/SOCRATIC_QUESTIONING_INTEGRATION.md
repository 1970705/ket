# Socratic Questioning Integration
## Guided Discovery for Wordland Vocabulary Learning

**Author**: Education Specialist
**Date**: 2026-02-18
**Status**: Design Phase
**Target Audience**: 10-year-old KET learners

---

## Executive Summary

This document outlines the integration of Socratic questioning methods into Wordland's hint system, replacing direct letter reveals with guided discovery. Research shows that information learned through active recall and guided reasoning creates stronger memory traces than passive reception.

---

## Table of Contents

1. [Educational Philosophy](#educational-philosophy)
2. [Socratic Method Framework](#socratic-method-framework)
3. [Question Hierarchy Design](#question-hierarchy-design)
4. [Integration with Current Hint System](#integration-with-current-hint-system)
5. [Implementation Guide](#implementation-guide)
6. [Examples & Scripts](#examples--scripts)

---

## Educational Philosophy

### Why Socratic Questioning?

#### Cognitive Science Principles

**The Testing Effect** (Roediger & Karpicke, 2006):
- Active retrieval strengthens memory more than restudy
- Socratic questioning IS retrieval practice

**Generation Effect** (Slamecka & Graf, 1978):
- Self-generated information remembered 2x better
- Questions prompt student to generate answers

**Depth of Processing** (Craik & Lockhart, 1972):
- Deeper processing = stronger encoding
- Socratic method forces semantic processing

#### Benefits for Age 10 Learners

| Traditional Direct Teaching | Socratic Questioning |
|----------------------------|----------------------|
| Passive receipt | Active generation |
| Surface-level processing | Deep semantic processing |
| Weak memory trace | Strong memory trace |
| Dependent on teacher | Builds independence |
| Single correct answer | Multiple pathways |

---

## Socratic Method Framework

### The "Art of Questioning" in Vocabulary Learning

#### Core Principles

1. **Guide, Don't Tell**
   - Bad: "The word is 'visible'"
   - Good: "What do we call things that can be seen?"

2. **Build on Prior Knowledge**
   - Connect to words student already knows
   - Use familiar contexts

3. **Sequence from Simple to Complex**
   - Start with obvious clues
   - Progress to subtle connections

4. **Provide "Thinking Time"**
   - Allow 5-10 seconds before re-prompting
   - Reduce anxiety

5. **Validate Partial Knowledge**
   - "You're on the right track!"
   - "That's part of it..."

### Question Types

#### Type 1: Recall Questions (Activating Prior Knowledge)

**Purpose**: Access related information in long-term memory.

**Templates**:
- "What word do we use for [related concept]?"
- "Think about [context]... what word comes to mind?"
- "You learned [related word] last week. How might this new word be related?"

**Example**:
> Target: "observe"
> Question: "What do we do with our eyes when we want to learn something new?"
> → "We look at it carefully"
> → "Yes! And the word for 'looking carefully' is 'observe'"

#### Type 2: Association Questions (Building Connections)

**Purpose**: Connect new word to known concepts.

**Templates**:
- "What's another word for [synonym]?"
- "What's the opposite of [antonym]?"
- "If you [action], what are you [doing]?"

**Example**:
> Target: "bright"
> Question: "Think about the sun. What word describes how the sun shines?"
> → "Shiny?"
> → "Close! What about 'bright'? Can you say 'The sun is bright'?"

#### Type 3: Contextual Questions (Using Situations)

**Purpose**: Ground word in meaningful scenario.

**Templates**:
- "Imagine you're [situation]. What would you [do/see/feel]?"
- "When might you need to [action]?"
- "Picture a [scene]. What word describes this?"

**Example**:
> Target: "stare"
> Question: "Imagine you see something amazing, like a rainbow. You look at it without blinking. What's another word for looking like that?"
> → "Watch?"
> → "Similar, but 'stare' means looking for a long time without stopping"

#### Type 4: Analytical Questions (Breaking Down Words)

**Purpose**: Use morphological awareness.

**Templates**:
- "This word has a part you know: [morpheme]. What does that mean?"
- "If [prefix] means [meaning], what might the whole word mean?"

**Example**:
> Target: "invisible"
> Question: "You learned 'vision' means 'seeing'. What if you add 'in-' which means 'not'?"
> → "Not seeing?"
> → "Yes! 'Invisible' means you can't see it"

#### Type 5: Synthesis Questions (Putting It Together)

**Purpose**: Combine clues to reach answer.

**Templates**:
- "You said [clue 1] and [clue 2]. Put those together..."
- "Think about [clue]. Now add [morpheme]. What do you get?"

**Example**:
> Target: "appearance"
> Question: "When something 'appears', what does it do?"
> → "Shows up?"
> → "Yes! And what do we call the way something looks when it shows up?"
> → "Appearance!"

---

## Question Hierarchy Design

### Three-Level Hint System

**Replace current letter-based hints with Socratic questions**:

#### Level 1: Activation Hint (The Nudge)

**Goal**: Activate relevant prior knowledge.

**Characteristics**:
- Broad, open-ended
- No direct answer given
- Success if student recalls word independently

**Examples by Word Type**:

| Word Type | Level 1 Question |
|-----------|-----------------|
| **Observation verbs** | "What do we do with our eyes to learn about something?" |
| **Colors** | "Think of the sky on a clear day. What color is it?" |
| **Materials** | "What material comes from trees and we use it to write?" |
| **Tools** | "What tool do you use to hit a nail into wood?" |
| **Actions** | "Show me the action of [word]. What's it called?" |

#### Level 2: Guidance Hint (The Signpost)

**Goal**: Provide structured scaffolding.

**Characteristics**:
- More specific than Level 1
- May include partial information
- Points toward answer without giving it

**Examples by Word Type**:

| Word Type | Level 2 Question |
|-----------|-----------------|
| **Observation verbs** | "It starts with S and rhymes with 'hair'. Think about looking closely..." |
| **Colors** | "It's the color of grass and leaves. It starts with G..." |
| **Materials** | "It's P-A-___ and we write on it. What material?" |
| **Tools** | "You use it to build things. It has a heavy head. H-A-___" |
| **Actions** | "First letter: [letter]. It means [synonym]. Think: [context clue]..." |

#### Level 3: Revelation Hint (The Reveal)

**Goal**: Ensure student can proceed.

**Characteristics**:
- Almost complete answer
- Maintains some thinking requirement
- Used only when necessary

**Examples by Word Type**:

| Word Type | Level 3 Question |
|-----------|-----------------|
| **Observation verbs** | "O-__-__-R-__-E. It means 'looking at something carefully'" |
| **Colors** | "G-R-___-__-N. The color of leaves" |
| **Materials** | "P-A-P-__-R. We write on it" |
| **Tools** | "H-A-__-__-E-R. You hit nails with it" |
| **Actions** | "[Full word with vowels masked]. It means [definition]" |

### Adaptive Hint Selection

**Algorithm**: Choose hint level based on student profile.

```kotlin
object SocraticHintSelector {

    fun selectHintLevel(
        word: Word,
        userProfile: UserProfile,
        previousAttempts: Int,
        timeElapsed: Long
    ): HintLevel {
        // If this is first attempt, start with Level 1
        if (previousAttempts == 0) {
            return HintLevel.LEVEL_1_ACTIVATION
        }

        // If student struggled before, skip to Level 2
        if (previousAttempts >= 1 && timeElapsed > 10000) {
            return HintLevel.LEVEL_2_GUIDANCE
        }

        // If multiple failures, use Level 3
        if (previousAttempts >= 2) {
            return HintLevel.LEVEL_3_REVELATION
        }

        // If student has high vocabulary, stay at Level 1
        if (userProfile.vocabularyLevel > 0.8f && previousAttempts < 2) {
            return HintLevel.LEVEL_1_ACTIVATION
        }

        // Default: Level 2
        return HintLevel.LEVEL_2_GUIDANCE
    }
}
```

---

## Integration with Current Hint System

### Current System Analysis

**File**: `domain/hint/HintGenerator.kt`

Current implementation:
```kotlin
fun generateHint(word: String, level: Int): String {
    return when (level) {
        1 -> "首字母: ${word.first()}"  // First letter
        2 -> "前半部分: ${word.take(3)}${"_".repeat(word.length - 3)}"  // First half
        3 -> "完整单词（元音隐藏）: ${maskVowels(word)}"  // Vowels masked
        else -> ""
    }
}
```

**Problems**:
- Purely orthographic (spelling-based)
- No semantic processing
- Doesn't leverage morphological data
- Creates dependency on letter hints

### Enhanced Hint System Architecture

**New Hierarchy**:
```
HintSystem
├── SocraticHintGenerator (NEW - Primary)
│   ├── ActivationQuestionGenerator
│   ├── GuidanceQuestionGenerator
│   └── RevelationQuestionGenerator
├── MorphologicalHintGenerator (NEW - Secondary)
│   └── Uses root/affix knowledge
└── TraditionalHintGenerator (EXISTING - Fallback)
    └── Letter-based hints
```

### Decision Tree for Hint Selection

```kotlin
object EnhancedHintSystem {

    suspend fun generateHint(
        word: Word,
        userId: String,
        hintLevel: Int,
        attemptNumber: Int
    ): HintResult {
        // 1. Check if user has learned morphemes in this word
        val morphemes = getLearnedMorphemes(userId, word)

        if (morphemes.isNotEmpty()) {
            // Use morphological hint (higher cognitive processing)
            return MorphologicalHintGenerator.generate(
                word, morphemes, hintLevel
            )
        }

        // 2. Use Socratic questioning (guided discovery)
        if (hintLevel <= 2) {
            return SocraticHintGenerator.generate(
                word, hintLevel, userId
            )
        }

        // 3. Fall back to traditional hints (only when necessary)
        return TraditionalHintGenerator.generate(
            word.word, hintLevel
        )
    }
}
```

---

## Implementation Guide

### Phase 1: Question Database (Week 1)

**File**: `data/seed/SocraticQuestionDatabase.kt`

```kotlin
object SocraticQuestionDatabase {

    data class SocraticQuestion(
        val id: String,
        val targetWord: String,
        val hintLevel: HintLevel,
        val questionType: QuestionType,
        val questionText: String,
        val followUpText: String?,  // If student gets close
        val alternativePhrasings: List<String>,  // For variety
        val associatedContext: String?,  // "picture", "sentence", etc.
        val difficulty: Int  // 1-5
    )

    enum class HintLevel {
        LEVEL_1_ACTIVATION,    // Broad, open-ended
        LEVEL_2_GUIDANCE,      // Structured, specific
        LEVEL_3_REVELATION     // Near-complete answer
    }

    enum class QuestionType {
        RECALL,        // Prior knowledge
        ASSOCIATION,   // Connections
        CONTEXTUAL,    // Situations
        ANALYTICAL,    // Word parts
        SYNTHESIS      // Combining clues
    }

    private val questions = listOf(
        // Look Island - Level 1
        SocraticQuestion(
            id = "q_look_001_1",
            targetWord = "look",
            hintLevel = HintLevel.LEVEL_1_ACTIVATION,
            questionType = QuestionType.ASSOCIATION,
            questionText = "What do we do with our eyes to see something?",
            followUpText = "Yes! And that word starts with L...",
            alternativePhrasings = listOf(
                "When you want to see something, what action do you do?",
                "Show me: [eye icon]. What's the word for this action?"
            ),
            associatedContext = "eye_action",
            difficulty = 1
        ),
        SocraticQuestion(
            id = "q_see_001_1",
            targetWord = "see",
            hintLevel = HintLevel.LEVEL_1_ACTIVATION,
            questionType = QuestionType.RECALL,
            questionText = "Think about your eyes. What happens when light enters them?",
            followUpText = "That's right! And 'see' starts with S...",
            alternativePhrasings = listOf(
                "What's the opposite of 'look away'?",
                "I ___ with my eyes. What's the missing word?"
            ),
            associatedContext = "sensory",
            difficulty = 1
        ),
        SocraticQuestion(
            id = "q_watch_001_1",
            targetWord = "watch",
            hintLevel = HintLevel.LEVEL_2_GUIDANCE,
            questionType = QuestionType.CONTEXTUAL,
            questionText = "Imagine you're looking at something for a long time, like a TV show or a game. What's the word?",
            followUpText = "It rhymes with 'match' and starts with W...",
            alternativePhrasings = listOf(
                "What do you do with a movie? You ___ it.",
                "Not just 'look', but for a longer time..."
            ),
            associatedContext = "duration",
            difficulty = 1
        ),

        // Look Island - Level 2 (Colors)
        SocraticQuestion(
            id = "q_red_002_1",
            targetWord = "red",
            hintLevel = HintLevel.LEVEL_1_ACTIVATION,
            questionType = QuestionType.ASSOCIATION,
            questionText = "Think of a fire truck or a stop sign. What color are they?",
            followUpText = "Yes! It's a bright, warm color. R-__-",
            alternativePhrasings = listOf(
                "What color is an apple?",
                "The color of blood and fire..."
            ),
            associatedContext = "color",
            difficulty = 1
        ),
        SocraticQuestion(
            id = "q_blue_002_1",
            targetWord = "blue",
            hintLevel = HintLevel.LEVEL_1_ACTIVATION,
            questionType = QuestionType.CONTEXTUAL,
            questionText = "Look up at the sky on a clear day. What color is it?",
            followUpText = "Or think of the ocean. What color?",
            alternativePhrasings = listOf(
                "What color are jeans?",
                "It's the color of sadness and the sky..."
            ),
            associatedContext = "color",
            difficulty = 1
        ),

        // Look Island - Level 3 (Advanced)
        SocraticQuestion(
            id = "q_stare_003_1",
            targetWord = "stare",
            hintLevel = HintLevel.LEVEL_1_ACTIVATION,
            questionType = QuestionType.CONTEXTUAL,
            questionText = "Imagine looking at something amazing, like a rainbow. You look at it for a long time without blinking. What's the word?",
            followUpText = "It's like 'look' but stronger and longer. S-___-",
            alternativePhrasings = listOf(
                "What's another word for 'looking for a long time'?",
                "When you look intensely at something..."
            ),
            associatedContext = "gaze",
            difficulty = 2
        ),
        SocraticQuestion(
            id = "q_observe_003_1",
            targetWord = "observe",
            hintLevel = HintLevel.LEVEL_2_GUIDANCE,
            questionType = QuestionType.ASSOCIATION,
            questionText = "Scientists watch things carefully to learn. What word means 'watching carefully to learn'?",
            followUpText = "It starts with O and means more than just 'look'",
            alternativePhrasings = listOf(
                "What do scientists do in an experiment?",
                "It's like 'look' but more careful and serious..."
            ),
            associatedContext = "scientific",
            difficulty = 3
        ),
        SocraticQuestion(
            id = "q_appear_003_1",
            targetWord = "appear",
            hintLevel = HintLevel.LEVEL_2_GUIDANCE,
            questionType = QuestionType.CONTEXTUAL,
            questionText = "Close your eyes. Now open them. I ___! What happened?",
            followUpText = "I came into sight. A-___-___-",
            alternativePhrasings = listOf(
                "The opposite of 'disappear'. What is it?",
                "When something comes into view, it..."
            ),
            associatedContext = "emergence",
            difficulty = 3
        ),

        // Make Lake examples
        SocraticQuestion(
            id = "q_make_001_1",
            targetWord = "make",
            hintLevel = HintLevel.LEVEL_1_ACTIVATION,
            questionType = QuestionType.CONTEXTUAL,
            questionText = "What do we call it when we create something with our hands?",
            followUpText = "You ___ a cake. You ___ art. What's the word?",
            alternativePhrasings = listOf(
                "To create, to build, to ___",
                "What's the opposite of 'break'?"
            ),
            associatedContext = "creation",
            difficulty = 1
        ),
        SocraticQuestion(
            id = "q_build_001_1",
            targetWord = "build",
            hintLevel = HintLevel.LEVEL_2_GUIDANCE,
            questionType = QuestionType.CONTEXTUAL,
            questionText = "Think about construction workers. They make houses and buildings. What's the word?",
            followUpText = "It starts with B. To ___ a house.",
            alternativePhrasings = listOf(
                "What do we do with bricks and wood?",
                "Construct, erect, ___"
            ),
            associatedContext = "construction",
            difficulty = 1
        ),
        // ... 250+ more questions
    )

    fun getQuestion(
        targetWord: String,
        hintLevel: HintLevel,
        userId: String? = null
    ): SocraticQuestion? {
        // Filter by word and level
        val matching = questions.filter {
            it.targetWord.equals(targetWord, ignoreCase = true) &&
            it.hintLevel == hintLevel
        }

        // If user provided, avoid recently asked questions
        val available = if (userId != null) {
            val recent = getRecentlyAskedQuestions(userId)
            matching.filterNot { it.id in recent }
        } else {
            matching
        }

        // Return random from available (for variety)
        return available.randomOrNull()
    }

    private fun getRecentlyAskedQuestions(userId: String): List<String> {
        // Implement question tracking per user
        return emptyList()  // Placeholder
    }
}
```

### Phase 2: Socratic Hint Generator (Week 2)

**File**: `domain/hint/SocraticHintGenerator.kt`

```kotlin
@Singleton
class SocraticHintGenerator @Inject constructor(
    private val questionDatabase: SocraticQuestionDatabase,
    private val userProfileRepository: UserProfileRepository
) {

    suspend fun generate(
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

        // Get question from database
        val question = questionDatabase.getQuestion(word.word, level, userId)

        return if (question != null) {
            SocraticHintResult(
                hintText = question.questionText,
                hintType = HintType.SOCRATIC,
                questionType = question.questionType,
                followUpText = question.followUpText,
                expectedThinkingTime = calculateExpectedTime(question),
                metadata = SocraticHintMetadata(
                    questionId = question.id,
                    level = level,
                    requiresResponse = true  // Student should think/answer
                )
            )
        } else {
            // Fallback to generated question
            generateDynamicQuestion(word, level, userId)
        }
    }

    private fun calculateExpectedTime(question: SocraticQuestion): Long {
        // Base time + reading time + thinking time
        val wordCount = question.questionText.split(" ").size
        val readingTime = wordCount * 300L  // 300ms per word (child)
        val thinkingTime = when (question.questionType) {
            QuestionType.RECALL -> 2000L
            QuestionType.ASSOCIATION -> 3000L
            QuestionType.CONTEXTUAL -> 4000L
            QuestionType.ANALYTICAL -> 5000L
            QuestionType.SYNTHESIS -> 6000L
        }
        return readingTime + thinkingTime
    }

    private suspend fun generateDynamicQuestion(
        word: Word,
        level: HintLevel,
        userId: String
    ): SocraticHintResult {
        // Generate question on-the-fly if not in database
        val userProfile = userProfileRepository.getUserProfile(userId)
        val vocabularyLevel = userProfile?.vocabularyLevel ?: 0.5f

        return when (level) {
            HintLevel.LEVEL_1_ACTIVATION -> {
                val question = generateActivationQuestion(word, vocabularyLevel)
                SocraticHintResult(
                    hintText = question,
                    hintType = HintType.SOCRATIC,
                    questionType = QuestionType.ASSOCIATION,
                    expectedThinkingTime = 3000L
                )
            }
            HintLevel.LEVEL_2_GUIDANCE -> {
                val question = generateGuidanceQuestion(word, vocabularyLevel)
                SocraticHintResult(
                    hintText = question,
                    hintType = HintType.SOCRATIC,
                    questionType = QuestionType.CONTEXTUAL,
                    expectedThinkingTime = 5000L
                )
            }
            HintLevel.LEVEL_3_REVELATION -> {
                val question = generateRevelationQuestion(word)
                SocraticHintResult(
                    hintText = question,
                    hintType = HintType.SOCRATIC,
                    questionType = QuestionType.SYNTHESIS,
                    expectedThinkingTime = 2000L
                )
            }
        }
    }

    private fun generateActivationQuestion(word: Word, vocabularyLevel: Float): String {
        // Use example sentences and related words
        val examples = parseExampleSentences(word.exampleSentences)
        if (examples.isNotEmpty()) {
            val firstExample = examples.first()
            return "Think about this: \"${firstExample.sentence}\" What's the word for \"${firstExample.translation}\"?"
        }

        // Fall back to related words
        val related = parseRelatedWords(word.relatedWords)
        if (related.isNotEmpty()) {
            val similarWord = related.first()
            return "It's similar to '$similarWord'. What word means \"${word.translation}\"?"
        }

        return "What word means \"${word.translation}\"?"
    }

    private fun generateGuidanceQuestion(word: Word, vocabularyLevel: Float): String {
        val firstLetter = word.word.first()
        val wordLength = word.word.length

        return when {
            wordLength <= 4 -> {
                "It has $wordLength letters and starts with '$firstLetter'. Meaning: ${word.translation}"
            }
            wordLength <= 6 -> {
                "Starts with '${word.word.take(2)}' and has $wordLength letters. Think about: ${word.translation}"
            }
            else -> {
                "First letter: $firstLetter. It means \"${word.translation}\". ${word.word.take(3)}___"
            }
        }
    }

    private fun generateRevelationQuestion(word: Word): String {
        // Reveal most of the word, mask 1-2 letters
        val masked = if (word.word.length <= 4) {
            word.word.dropLast(1) + "_"
        } else {
            word.word.dropLast(2) + "__"
        }
        return "The word is: \"$masked\" (meaning: ${word.translation})"
    }
}

data class SocraticHintResult(
    val hintText: String,
    val hintType: HintType,
    val questionType: QuestionType,
    val followUpText: String? = null,
    val expectedThinkingTime: Long,
    val metadata: SocraticHintMetadata? = null
)

data class SocraticHintMetadata(
    val questionId: String,
    val level: HintLevel,
    val requiresResponse: Boolean
)

enum class HintType {
    SOCRATIC,          // Question-based
    MORPHOLOGICAL,     // Word parts
    TRADITIONAL        // Letter-based
}
```

### Phase 3: UI Integration (Week 3)

**File**: `ui/components/SocraticHintCard.kt`

```kotlin
@Composable
fun SocraticHintCard(
    hintResult: SocraticHintResult,
    onResponse: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFollowUp by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(true) }

    // Auto-show follow-up after thinking time
    LaunchedEffect(hintResult.expectedThinkingTime) {
        delay(hintResult.expectedThinkingTime)
        isThinking = false
        if (hintResult.followUpText != null) {
            delay(3000)  // 3 more seconds
            showFollowUp = true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (hintResult.questionType) {
                QuestionType.RECALL -> Color(0xFFE3F2FD)  // Light blue
                QuestionType.ASSOCIATION -> Color(0xFFFFF3E0)  // Light orange
                QuestionType.CONTEXTUAL -> Color(0xFFE8F5E9)  // Light green
                QuestionType.ANALYTICAL -> Color(0xFFF3E5F5)  // Light purple
                QuestionType.SYNTHESIS -> Color(0xFFFFEBEE)  // Light red
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Thinking indicator
            if (isThinking) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Think about it...", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main question
            Text(
                text = hintResult.hintText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            // Follow-up text (if any)
            if (showFollowUp && hintResult.followUpText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hintResult.followUpText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Response input
            if (hintResult.metadata?.requiresResponse == true) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Handle input */ },
                    label = { Text("Type your answer...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Skip")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { /* Submit */ }) {
                        Text("Check")
                    }
                }
            } else {
                // Just a dismiss button for revelation hints
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Got it!")
                    }
                }
            }
        }
    }
}
```

### Phase 4: ViewModel Integration (Week 4)

**File**: `ui/viewmodel/LearningViewModel.kt` (Modifications)

```kotlin
@HiltViewModel
class LearningViewModel @Inject constructor(
    // ... existing dependencies
    private val socraticHintGenerator: SocraticHintGenerator,
    private val hintUsageTracker: HintUsageTracker
) : ViewModel() {

    fun useSocraticHint() {
        viewModelScope.launch {
            val currentState = _uiState.value as? LearningUiState.Ready ?: return@launch

            // Generate Socratic hint
            val hintResult = socraticHintGenerator.generate(
                word = currentState.question,
                hintLevel = currentState.hintLevel + 1,
                userId = userId
            )

            // Track hint usage
            hintUsageTracker.trackUsage(
                userId = userId,
                wordId = currentState.question.wordId,
                hintType = hintResult.hintType,
                hintLevel = currentState.hintLevel + 1
            )

            _uiState.value = currentState.copy(
                hintText = hintResult.hintText,
                hintLevel = currentState.hintLevel + 1,
                hintType = hintResult.hintType,
                hintMetadata = hintResult.metadata
            )
        }
    }

    fun submitHintResponse(response: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? LearningUiState.Ready ?: return@launch

            // Check if response is correct or close
            val isCorrect = checkHintResponse(
                response = response,
                targetWord = currentState.question.targetWord
            )

            if (isCorrect) {
                // User figured it out from hint!
                _uiState.value = currentState.copy(
                    hintText = "Correct! You got it: ${currentState.question.targetWord}",
                    hintLevel = 3  // Max level
                )
            } else {
                // Provide feedback or next hint
                val feedback = generateHintFeedback(response, currentState.question)
                _uiState.value = currentState.copy(
                    hintText = feedback,
                    hintMetadata = currentState.hintMetadata?.copy(
                        requiresResponse = true
                    )
                )
            }
        }
    }

    private fun checkHintResponse(response: String, targetWord: String): Boolean {
        return response.equals(targetWord, ignoreCase = true)
    }

    private fun generateHintFeedback(response: String, question: SpellBattleQuestion): String {
        // Analyze response for partial correctness
        return when {
            response.isEmpty() -> "Take your time. Think about the question..."
            response.first() == question.targetWord.first() -> {
                "Good start! It begins with '${response.first()}'. What comes next?"
            }
            response.length > 2 -> {
                "You're on the right track! Keep going..."
            }
            else -> {
                "Not quite. Try again or ask for another hint."
            }
        }
    }
}
```

---

## Examples & Scripts

### Example 1: Teaching "Observe" (Look Island, Level 3)

**Context**: Student has been exposed to "look", "see", "watch"

**Student**: [Looks at translation: "观察"] "I don't know this one."

**Level 1 Hint**:
> "Think about scientists. When they do experiments, they need to watch carefully. What word means 'watching carefully to learn'?"

**Student**: [Thinking...] "Is it 'inspect'?"

**System**: [Shows follow-up]
> "Close! Inspect means 'look into something closely'. This word is more about watching to learn. It starts with O..."

**Student**: "Observe!"

**System**: "Yes! Observe means 'to watch carefully and learn from what you see'. Great job!"

---

### Example 2: Teaching "Build" (Make Lake, Level 1)

**Context**: Student knows "make", "create"

**Student**: [Types "m-a-k-e"] [System shows: Incorrect]

**Level 1 Hint**:
> "Imagine you're a construction worker. You use bricks and wood to make a house. What's the word?"

**Student**: "Make?"

**System**: [Follow-up]
> "Making is part of it, but this word is more specific. It starts with B and rhymes with 'filled'..."

**Student**: "Build!"

**System**: "Perfect! To build means to construct something by putting parts together."

---

### Example 3: Teaching "Invisible" (Look Island, Level 5)

**Context**: Student knows "visible", has learned prefix "in-"

**Student**: [Struggling, 15 seconds elapsed]

**Level 2 Hint**:
> "You learned that 'visible' means 'can be seen'. What if we add 'in-' which means 'not'?"

**Student**: "Invisible?"

**System**: "Yes! Can you use it in a sentence?"

**Student**: "The air is invisible."

**System**: "Excellent example! Air is invisible because we can't see it."

---

## Assessment Strategy

### Measuring Socratic Effectiveness

#### Metrics

1. **Independence Rate**:
   - How often students answer after Level 1 hint only
   - Target: 60% independence

2. **Response Latency**:
   - Time between hint and answer
   - Should decrease as student learns pattern

3. **Follow-Up Engagement**:
   - How often students use follow-up text
   - Indicates scaffolding effectiveness

4. **Long-Term Retention**:
   - Compare words learned via Socratic vs. traditional hints
   - Expected: 25% better retention

#### A/B Testing Design

**Group A**: Control (traditional letter hints)
**Group B**: Experimental (Socratic hints)

**Measure**:
- Immediate accuracy
- 7-day retention
- 30-day retention
- Student satisfaction survey

**Expected Results**:
```
7-day retention:
- Group A: 52%
- Group B: 65% (+25%)

30-day retention:
- Group A: 38%
- Group B: 47% (+24%)

Student satisfaction:
- Group A: 3.2/5
- Group B: 4.5/5 (+40%)
```

---

## Expected Outcomes

### Cognitive Benefits

| Outcome | Traditional Hints | Socratic Hints | Improvement |
|---------|------------------|----------------|-------------|
| Immediate recall | 78% | 76% | -2% (minimal) |
| 7-day retention | 52% | 65% | +25% |
| 30-day retention | 38% | 47% | +24% |
| Transfer to new words | 22% | 35% | +59% |
| Metacognitive awareness | Low | High | +200% |

### Behavioral Changes

1. **Reduced Hint Dependency**: Students use fewer hints over time
2. **Increased Confidence**: More willing to attempt unknown words
3. **Better Questioning**: Students start asking their own questions
4. **Deeper Processing**: Longer response times initially, then faster

---

## Appendix

### A. Question Templates by Word Category

**Observation Verbs**:
- Level 1: "What do we do with our eyes to [action]?"
- Level 2: "It's like 'look' but [difference]. Starts with [letter]..."
- Level 3: "[Partial word]. It means [definition]."

**Colors**:
- Level 1: "Think of [object]. What color is it?"
- Level 2: "It's the color of [associated thing]. [Letter]-___"
- Level 3: "[Partial word]. The color of [context]."

**Materials**:
- Level 1: "What material do we use to [action]?"
- Level 2: "It comes from [source] and we use it for [purpose]. [Letters]-___"
- Level 3: "[Partial word]. We [action] with it."

**Tools**:
- Level 1: "What tool helps you [action]?"
- Level 2: "It has [part] and is used for [purpose]. [Letter]-___-___-"
- Level 3: "[Partial word]. You [action] with it."

**Actions**:
- Level 1: "Show me: [gesture]. What's this action called?"
- Level 2: "It means [synonym] and starts with [letter]. Think about [context]..."
- Level 3: "[Partial word]. To [definition]."

### B. Sample Dialogue: Full Socratic Sequence

**Target Word**: "examine" (Make Lake, Level 10)

**System**: [Shows Chinese: "检查，细看"]

**Student**: "I don't know this one."

**System** [Level 1]:
> "Think about a doctor. When you go for a check-up, what does the doctor do to your body?"

**Student**: "Checks it?"

**System** [Follow-up]:
> "Yes! But there's a more specific word. It starts with E and means 'look at closely to learn about it'."

**Student**: "Explore?"

**System** [Level 2]:
> "Close! Explore is about going to new places. This word is about looking closely at something. E-___-___-___-..."
> [Hint: Think about 'test' + 'eye' sounds]

**Student**: "Examine!"

**System**:
> "Perfect! Examine means 'to look at something carefully and closely, like a doctor examining a patient.' Can you use it in a sentence?"

**Student**: "The teacher will examine my homework."

**System**:
> "Excellent! You understand the word perfectly."

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Next Review**: 2026-03-18
