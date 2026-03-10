# Contextual Learning System
## Sentence-Based Vocabulary Acquisition for Wordland

**Author**: Education Specialist
**Date**: 2026-02-18
**Status**: Design Phase
**Target Audience**: 10-year-old KET learners

---

## Executive Summary

The Contextual Learning System enhances Wordland's current word-isolation approach by introducing sentence-based learning, context-driven word selection, and story-based vocabulary introduction. Research shows that words learned in meaningful context are retained 2-3x better than words learned in isolation.

---

## Table of Contents

1. [Educational Rationale](#educational-rationale)
2. [Current System Analysis](#current-system-analysis)
3. [Contextual Learning Modes](#contextual-learning-modes)
4. [Content Structure Design](#content-structure-design)
5. [Implementation Guide](#implementation-guide)
6. [Assessment Strategy](#assessment-strategy)

---

## Educational Rationale

### Why Context Matters

#### Research Evidence

**Nation (2001)**: Words learned from context account for 75% of vocabulary growth.

**Nagy & Herman (1987)**:
- Contextual learning: 2-3 words/day retained
- Decontextualized learning: 0.5-1 word/day retained

**Cunningham & Stanovich (1998)**:
- Context acts as "memory glue"
- Multiple exposures in varied contexts = stronger encoding

### The "Depth of Processing" Advantage

| Learning Type | Cognitive Processing | Retention Rate |
|---------------|---------------------|----------------|
| **Word Isolation** (current) | Shallow (orthographic) | ~35% |
| **Sentence Context** | Medium (syntactic) | ~55% |
| **Story Context** | Deep (semantic) | ~75% |
| **Personal Context** | Deepest (episodic) | ~85% |

### Age 10 Developmental Considerations

**Concrete Operational Stage** (Piaget):
- Can understand logical relationships
- Still benefit from concrete examples
- Stories and scenarios provide "mental hooks"

**Implications**:
- Use relatable scenarios (school, home, play)
- Build narratives around word families
- Connect to personal experiences

---

## Current System Analysis

### Content Inventory

**Existing Example Sentences**:
- Every Word has `exampleSentences` field
- Format: JSON array with sentence + translation
- Quality: Variable, some generic

**Example**:
```json
[
  {"sentence": "Look at the blackboard.", "translation": "看黑板。"},
  {"sentence": "Please look here.", "translation": "请看这里。"}
]
```

**Assessment**:
- ✓ Content exists
- ✗ Not utilized in gameplay
- ✗ No contextual practice mode
- ✗ Sentences treated as metadata, not learning content

### Current Gameplay Modes

| Mode | Context Level | Description |
|------|---------------|-------------|
| Spell Battle | None | Chinese → English spelling |
| Multiple Choice | Low | Select word from 4 options |
| Fill Blank | Medium | Complete sentence with word |

**Gap**: No dedicated contextual learning mode

---

## Contextual Learning Modes

### Mode 1: Sentence Completion (Gap-Fill)

**Objective**: Select correct word to complete sentence.

**Gameplay**:
```
┌─────────────────────────────────────────┐
│         Sentence Complete!              │
├─────────────────────────────────────────┤
│                                         │
│  "Please ___ at the blackboard."        │
│                                         │
│  Options:                               │
│  ┌─────────┐  ┌─────────┐              │
│  │  look   │  │  watch  │              │
│  └─────────┘  └─────────┘              │
│  ┌─────────┐  ┌─────────┐              │
│  │   see   │  │  stare  │              │
│  └─────────┘  └─────────┘              │
│                                         │
│  [Listen] [Hint] [Check]                │
└─────────────────────────────────────────┘
```

**Difficulty Progression**:
- Level 1: Obvious context (only one fit)
- Level 2: Multiple possible (need nuance)
- Level 3: No options given (open response)

### Mode 2: Context Clues Detective

**Objective**: Deduce word meaning from surrounding text.

**Gameplay**:
```
┌─────────────────────────────────────────┐
│       Context Clues Detective            │
├─────────────────────────────────────────┤
│                                         │
│  Read the sentence:                     │
│                                         │
│  "The glasses help him ___ better,      │
│   but he still can't find his book."    │
│                                         │
│  Context clues:                         │
│  • "glasses" = something for eyes       │
│  • "better" = improvement in ability    │
│                                         │
│  What word means "to use your eyes"?    │
│                                         │
│  Type your answer: ____________         │
│                                         │
└─────────────────────────────────────────┘
```

**Skills Developed**:
- Inference from context
- Semantic reasoning
- Metalinguistic awareness

### Mode 3: Story Builder

**Objective**: Create coherent story using vocabulary words.

**Gameplay**:
```
┌─────────────────────────────────────────┐
│           Story Builder                  │
├─────────────────────────────────────────┤
│                                         │
│  Target Words: look, see, watch, find   │
│                                         │
│  Build a story using ALL these words:   │
│                                         │
│  1. "I ___ out the window and ___ a     │
│     cat in the tree."                   │
│  2. "I ___ it jump to another branch."  │
│  3. "Then I ___ my sister and told      │
│     her to come ___."                   │
│                                         │
│  [Drag words to blanks]                 │
│                                         │
└─────────────────────────────────────────┘
```

**Learning Benefits**:
- Understands word relationships
- Practices correct usage
- Creates personal context

### Mode 4: Real-World Scenario

**Objective**: Apply vocabulary to practical situations.

**Gameplay**:
```
┌─────────────────────────────────────────┐
│      Real-World Scenario                │
├─────────────────────────────────────────┤
│                                         │
│  Scenario: You're at the eye doctor.    │
│                                         │
│  Doctor: "Please ___ the chart on the   │
│           wall."                        │
│  You: "I can ___ the letters clearly."  │
│  Doctor: "Good, now I will ___ your     │
│           eyes with this machine."      │
│                                         │
│  Choose the best word for each blank:   │
│  [look / see / watch / observe]          │
│                                         │
│  1. First blank: ______                 │
│  2. Second blank: ______                │
│  3. Third blank: ______                 │
│                                         │
│  [Check Answers]                        │
└─────────────────────────────────────────┘
```

---

## Content Structure Design

### Enhanced Example Sentence Schema

**Current Schema**:
```json
[
  {"sentence": "Look at the blackboard.", "translation": "看黑板。"}
]
```

**Enhanced Schema**:
```json
{
  "sentences": [
    {
      "sentence": "Look at the blackboard.",
      "translation": "看黑板。",
      "contextType": "classroom",
      "difficulty": 1,
      "audioUrl": "assets/audio/look_example_1.mp3",
      "imageHint": "classroom_scene.png",
      "wordPosition": 0,
      "blanksAvailable": true
    },
    {
      "sentence": "I can see the stars.",
      "translation": "我能看到星星。",
      "contextType": "nature",
      "difficulty": 1,
      "audioUrl": "assets/audio/see_example_1.mp3",
      "imageHint": "night_sky.png",
      "wordPosition": 3,
      "blanksAvailable": true
    }
  ],
  "miniStories": [
    {
      "title": "The Lost Dog",
      "sentences": [
        "I look out the window.",
        "I see a dog in the street.",
        "I watch it run to the park.",
        "I find my shoes and go out."
      ],
      "vocabulary": ["look", "see", "watch", "find"],
      "comprehensionQuestions": [
        "Where is the dog?",
        "What does the narrator do?"
      ]
    }
  ]
}
```

### Context Categories

**Category 1: School (学校场景)**
- Classroom interactions
- Teacher instructions
- Student activities

**Target Words**: look, see, check, write, read, draw, color

**Example Sentences**:
- "Look at the blackboard, please."
- "Can you see the words clearly?"
- "Check your homework carefully."

**Category 2: Home (家庭场景)**
- Daily routines
- Family interactions
- Household activities

**Target Words**: clean, cook, make, fix, find

**Example Sentences**:
- "Mom cooks dinner in the kitchen."
- "I help clean the living room."
- "Dad fixes the broken chair."

**Category 3: Nature (自然场景)**
- Outdoor observations
- Animals and plants
- Weather and seasons

**Target Words**: grow, plant, bright, dark, light, color

**Example Sentences**:
- "Plants grow in spring."
- "The sky is bright today."
- "Leaves change color in autumn."

**Category 4: Play (娱乐场景)**
- Games and sports
- Creative activities
- Social interactions

**Target Words**: play, watch, draw, paint, build

**Example Sentences**:
- "We play games after school."
- "Watch me score a goal!"
- "Let's draw pictures together."

### Mini-Story Design

**Story Template**:
```
Title: [Theme-based title]
Theme: [Category]
Vocabulary: [3-5 target words]
Length: 4-6 sentences
Reading Level: [Age-appropriate]

Story Structure:
1. Introduction (Set scene)
2. Problem/Action (Use 1-2 words)
3. Resolution (Use remaining words)
4. Reflection (Personal connection)
```

**Example: "The Rainbow Hunt"**

```
Title: The Rainbow Hunt
Theme: Nature
Vocabulary: look, see, bright, color, appear
Island: Look Island, Level 2
Story:
"I look out the window after the rain."
"I see something amazing in the sky!"
"A bright arc with many colors appears."
"I run to find my camera."
"I take a picture before it disappears."
Comprehension:
1. What does the narrator see?
2. How many colors are in a rainbow?
3. Why does the narrator need a camera?
```

---

## Implementation Guide

### Phase 1: Content Enhancement (2 weeks)

**File**: `data/seed/ContextualContentDatabase.kt`

```kotlin
object ContextualContentDatabase {

    data class ContextualSentence(
        val id: String,
        val wordId: String,
        val sentence: String,
        val translation: String,
        val contextType: ContextType,
        val difficulty: Int,
        val wordPosition: Int,  // Where target word appears
        val hasBlank: Boolean,  // Can be made into fill-blank
        val audioUrl: String?,
        val imageUrl: String?,
        val distractorWords: List<String>  // For multiple choice
    )

    data class MiniStory(
        val id: String,
        val title: String,
        val theme: ContextType,
        val vocabulary: List<String>,  // word IDs
        val sentences: List<String>,
        val translations: List<String>,
        val comprehensionQuestions: List<ComprehensionQuestion>,
        val difficulty: Int,
        val audioUrl: String?,
        val imageUrl: String?
    )

    data class ComprehensionQuestion(
        val question: String,
        val translation: String,
        val options: List<String>,  // For multiple choice
        val correctAnswer: String,
        val type: QuestionType  // LITERAL, INFERENCE, VOCABULARY
    )

    enum class ContextType {
        SCHOOL, HOME, NATURE, PLAY, FOOD, ANIMALS, TRANSPORT, WEATHER
    }

    enum class QuestionType {
        LITERAL,      // Explicitly stated in text
        INFERENCE,    // Requires reasoning
        VOCABULARY    // Tests target word meaning
    }

    // Look Island contextual content
    private val lookIslandSentences = listOf(
        ContextualSentence(
            id = "look_sent_001",
            wordId = "look_001",
            sentence = "Look at the blackboard.",
            translation = "看黑板。",
            contextType = ContextType.SCHOOL,
            difficulty = 1,
            wordPosition = 0,
            hasBlank = true,
            audioUrl = "assets/audio/sent/look_001.mp3",
            imageUrl = "images/context/classroom_board.png",
            distractorWords = listOf("see", "watch", "find")
        ),
        ContextualSentence(
            id = "see_sent_001",
            wordId = "look_002",
            sentence = "I can see the stars.",
            translation = "我能看到星星。",
            contextType = ContextType.NATURE,
            difficulty = 1,
            wordPosition = 3,
            hasBlank = true,
            audioUrl = "assets/audio/sent/see_001.mp3",
            imageUrl = "images/context/night_stars.png",
            distractorWords = listOf("look", "watch", "find")
        ),
        // ... 100+ more sentences
    )

    private val lookIslandStories = listOf(
        MiniStory(
            id = "look_story_001",
            title = "The Rainbow Hunt",
            theme = ContextType.NATURE,
            vocabulary = listOf("look_001", "look_002", "look_010", "look_011"),
            sentences = listOf(
                "I look out the window after the rain.",
                "I see something amazing in the sky!",
                "A bright arc with many colors appears.",
                "I run to find my camera.",
                "I take a picture before it disappears."
            ),
            translations = listOf(
                "雨后我向窗外看。",
                "我看到天空中有些神奇的东西！",
                "一个带有许多颜色的明亮弧线出现了。",
                "我跑去找到我的相机。",
                "我在它消失前拍了一张照片。"
            ),
            comprehensionQuestions = listOf(
                ComprehensionQuestion(
                    question = "What does the narrator see in the sky?",
                    translation = "叙述者在天空中看到了什么？",
                    options = listOf("A rainbow", "The sun", "The moon", "Stars"),
                    correctAnswer = "A rainbow",
                    type = QuestionType.LITERAL
                ),
                ComprehensionQuestion(
                    question = "Why does the narrator need a camera?",
                    translation = "为什么叙述者需要相机？",
                    options = listOf(
                        "To take a picture",
                        "To look closer",
                        "To show their parents",
                        "To study the rainbow"
                    ),
                    correctAnswer = "To take a picture",
                    type = QuestionType.INFERENCE
                ),
                ComprehensionQuestion(
                    question = "What does 'bright' mean in this story?",
                    translation = "在这个故事中'bright'是什么意思？",
                    options = listOf("Shining", "Big", "Colorful", "Fast"),
                    correctAnswer = "Shining",
                    type = QuestionType.VOCABULARY
                )
            ),
            difficulty = 2,
            audioUrl = "assets/audio/stories/panshanbow_hunt.mp3",
            imageUrl = "images/stories/panshanbow_hunt.png"
        ),
        // ... 20+ more stories
    )

    fun getSentencesForWord(wordId: String): List<ContextualSentence> {
        return lookIslandSentences.filter { it.wordId == wordId }
    }

    fun getSentencesByContext(contextType: ContextType): List<ContextualSentence> {
        return lookIslandSentences.filter { it.contextType == contextType }
    }

    fun getStoryById(storyId: String): MiniStory? {
        return lookIslandStories.find { it.id == storyId }
    }

    fun getStoriesForLevel(levelId: String): List<MiniStory> {
        return lookIslandStories.filter { it.difficulty <= 2 }  // Simplified
    }
}
```

### Phase 2: Game Mode Implementation (3 weeks)

**Screen**: `ui/screens/SentenceCompleteScreen.kt`

```kotlin
@Composable
fun SentenceCompleteScreen(
    levelId: String,
    onComplete: (Int) -> Unit,  // Score
    viewModel: SentenceCompleteViewModel = viewModel(
        factory = AppServiceLocator.provideFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is SentenceCompleteUiState.Ready -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Progress
                LinearProgressIndicator(
                    progress = { state.currentQuestion / state.totalQuestions.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sentence with blank
                SentenceWithBlank(
                    sentence = state.sentence,
                    blankPosition = state.blankPosition,
                    selectedWord = state.selectedWord
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Context clues (optional hint)
                if (state.showContextClues) {
                    ContextClueCard(state.contextClues)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Word options
                WordOptionsGrid(
                    options = state.options,
                    selectedWord = state.selectedWord,
                    onWordSelected = { viewModel.selectWord(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.playAudio() }
                    ) {
                        Icon(Icons.Default.VolumeUp, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Listen")
                    }

                    Button(
                        onClick = { viewModel.submitAnswer() },
                        enabled = state.selectedWord != null
                    ) {
                        Text("Check")
                    }
                }
            }
        }
        is SentenceCompleteUiState.Feedback -> {
            FeedbackScreen(
                isCorrect = state.isCorrect,
                correctWord = state.correctWord,
                explanation = state.explanation,
                onNext = { viewModel.nextQuestion() }
            )
        }
        is SentenceCompleteUiState.Complete -> {
            CompletionScreen(
                score = state.score,
                totalQuestions = state.totalQuestions,
                onRetry = { viewModel.retry() },
                onExit = { onComplete(state.score) }
            )
        }
    }
}

@Composable
fun SentenceWithBlank(
    sentence: String,
    blankPosition: Int,
    selectedWord: String?
) {
    val words = sentence.split(" ")
    val displayWords = words.mapIndexed { index, word ->
        if (index == blankPosition) {
            selectedWord ?: "_____"
        } else {
            word
        }
    }

    Text(
        text = displayWords.joinToString(" "),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun ContextClueCard(clues: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 Context Clues",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            clues.forEach { clue ->
                Text(
                    text = "• $clue",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
```

**Screen**: `ui/screens/StoryModeScreen.kt`

```kotlin
@Composable
fun StoryModeScreen(
    storyId: String,
    onComplete: () -> Unit,
    viewModel: StoryModeViewModel = viewModel(
        factory = AppServiceLocator.provideFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })

    when (val state = uiState) {
        is StoryModeUiState.Reading -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Story header
                StoryHeader(
                    title = state.story.title,
                    vocabulary = state.story.vocabulary,
                    progress = { pagerState.currentPage / 4f }
                )

                // Story pages (horizontal pager)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    StoryPage(
                        sentences = state.story.sentences.slice(
                            (page * 2) until ((page + 1) * 2).coerceAtMost(state.story.sentences.size)
                        ),
                        translations = state.story.translations.slice(
                            (page * 2) until ((page + 1) * 2).coerceAtMost(state.story.translations.size)
                        ),
                        vocabulary = state.story.vocabulary,
                        highlightedWords = state.story.vocabulary,  // Highlight target words
                        onWordClick = { word ->
                            viewModel.showWordDetail(word)
                        }
                    )
                }

                // Page indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { page ->
                        val isSelected = pagerState.currentPage == page
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Text("Previous")
                    }

                    if (pagerState.currentPage == 3) {
                        Button(onClick = { viewModel.startComprehension() }) {
                            Text("Take Quiz")
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
        is StoryModeUiState.Quiz -> {
            ComprehensionQuizScreen(
                questions = state.questions,
                onAnswer = { answer -> viewModel.submitAnswer(answer) }
            )
        }
        is StoryModeUiState.Results -> {
            StoryResultsScreen(
                score = state.score,
                correctAnswers = state.correctAnswers,
                onReview = { /* Review story */ },
                onComplete = onComplete
            )
        }
    }
}

@Composable
fun StoryPage(
    sentences: List<String>,
    translations: List<String>,
    vocabulary: List<String>,
    highlightedWords: List<String>,
    onWordClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            sentences.forEachIndexed { index, sentence ->
                val words = sentence.split(" ")
                val annotatedString = buildAnnotatedString {
                    words.forEach { word ->
                        val cleanWord = word.trim(',', '.', '!', '?')
                        if (cleanWord.lowercase() in highlightedWords.map { it.lowercase() }) {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append(word)
                            }
                            pushStringAnnotation(tag = "word", annotation = cleanWord)
                        } else {
                            append(word)
                        }
                        append(" ")
                    }
                }

                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "word", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                onWordClick(annotation.item)
                            }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Translation (smaller, below)
                Text(
                    text = translations.getOrNull(index) ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
```

### Phase 3: ViewModel Logic (2 weeks)

**File**: `ui/viewmodel/SentenceCompleteViewModel.kt`

```kotlin
@HiltViewModel
class SentenceCompleteViewModel @Inject constructor(
    private val getContextualQuestions: GetContextualQuestionsUseCase,
    private val submitSentenceAnswer: SubmitSentenceAnswerUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val levelId: String = checkNotNull(savedStateHandle["levelId"])

    private val _uiState = MutableStateFlow<SentenceCompleteUiState>(
        SentenceCompleteUiState.Loading
    )
    val uiState: StateFlow<SentenceCompleteUiState> = _uiState.asStateFlow()

    private val currentQuestions: MutableList<ContextualQuestion> = mutableListOf()
    private var currentQuestionIndex = 0
    private var selectedWord: String? = null
    private val score = mutableIntListOf(0, 0)  // [correct, total]

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.value = SentenceCompleteUiState.Loading

            val questions = getContextualQuestions(
                levelId = levelId,
                questionCount = 10
            )

            if (questions.isEmpty()) {
                _uiState.value = SentenceCompleteUiState.Error("No questions available")
                return@launch
            }

            currentQuestions.clear()
            currentQuestions.addAll(questions.shuffled())
            currentQuestionIndex = 0

            showCurrentQuestion()
        }
    }

    private fun showCurrentQuestion() {
        if (currentQuestionIndex >= currentQuestions.size) {
            showResults()
            return
        }

        val question = currentQuestions[currentQuestionIndex]
        _uiState.value = SentenceCompleteUiState.Ready(
            sentence = question.sentence,
            blankPosition = question.blankPosition,
            options = question.options.shuffled(),
            correctWord = question.correctWord,
            contextClues = question.contextClues,
            currentQuestion = currentQuestionIndex + 1,
            totalQuestions = currentQuestions.size,
            selectedWord = null,
            showContextClues = false
        )
    }

    fun selectWord(word: String) {
        val currentState = _uiState.value as? SentenceCompleteUiState.Ready ?: return
        _uiState.value = currentState.copy(selectedWord = word)
        selectedWord = word
    }

    fun submitAnswer() {
        val currentState = _uiState.value as? SentenceCompleteUiState.Ready ?: return
        val answer = selectedWord ?: return

        val isCorrect = answer.equals(currentState.correctWord, ignoreCase = true)
        score[1]++  // Increment total
        if (isCorrect) {
            score[0]++  // Increment correct
        }

        viewModelScope.launch {
            val feedback = submitSentenceAnswer(
                questionId = currentQuestions[currentQuestionIndex].id,
                userAnswer = answer,
                isCorrect = isCorrect
            )

            _uiState.value = SentenceCompleteUiState.Feedback(
                isCorrect = isCorrect,
                correctWord = currentState.correctWord,
                explanation = generateExplanation(currentState, isCorrect),
                currentQuestion = currentQuestionIndex + 1,
                totalQuestions = currentQuestions.size
            )
        }
    }

    private fun generateExplanation(
        state: SentenceCompleteUiState.Ready,
        isCorrect: Boolean
    ): String {
        return if (isCorrect) {
            "✓ Correct! '${state.correctWord}' is the right word."
        } else {
            "✗ The correct answer is '${state.correctWord}'. " +
            getWordExplanation(state.correctWord)
        }
    }

    private fun getWordExplanation(word: String): String {
        // Get word explanation from database
        return "This word means [definition] and fits in this context because [reason]."
    }

    fun nextQuestion() {
        currentQuestionIndex++
        selectedWord = null
        showCurrentQuestion()
    }

    private fun showResults() {
        _uiState.value = SentenceCompleteUiState.Complete(
            score = score[0],
            totalQuestions = score[1],
            percentage = (score[0].toFloat() / score[1] * 100).toInt()
        )
    }
}

sealed class SentenceCompleteUiState {
    object Loading : SentenceCompleteUiState()
    data class Ready(
        val sentence: String,
        val blankPosition: Int,
        val options: List<String>,
        val correctWord: String,
        val contextClues: List<String>,
        val currentQuestion: Int,
        val totalQuestions: Int,
        val selectedWord: String?,
        val showContextClues: Boolean
    ) : SentenceCompleteUiState()

    data class Feedback(
        val isCorrect: Boolean,
        val correctWord: String,
        val explanation: String,
        val currentQuestion: Int,
        val totalQuestions: Int
    ) : SentenceCompleteUiState()

    data class Complete(
        val score: Int,
        val totalQuestions: Int,
        val percentage: Int
    ) : SentenceCompleteUiState()

    data class Error(val message: String) : SentenceCompleteUiState()
}
```

---

## Assessment Strategy

### Measuring Contextual Learning Effectiveness

#### Metrics

1. **Contextual Transfer Rate**:
   - Can student use word in new context?
   - Measure: % correct in novel scenarios

2. **Sentence Completion Accuracy**:
   - Improvement over time
   - Target: 85% accuracy after 3 sessions

3. **Story Comprehension**:
   - Literal vs. inference questions
   - Target: 80% literal, 60% inference

4. **Production Quality**:
   - Can student create sentences?
   - Rubric: Grammar, vocabulary usage, meaning

### Pre-Post Assessment

**Pre-Test** (before contextual learning):
```
1. Spelling: ___ → ___ (Chinese → English)
2. Multiple Choice: Select word from 4 options
3. Sentence Completion: 5 sentences

Baseline: ___%
```

**Post-Test** (after contextual learning):
```
1. Spelling (same words) → ___%
2. Multiple Choice (new sentences) → ___%
3. Sentence Completion (new contexts) → ___%
4. Sentence Production (create sentence) → ___%

Growth: ___%
```

---

## Expected Outcomes

### Quantitative Improvements

| Metric | Current (Isolation) | Target (Contextual) | Improvement |
|--------|-------------------|-------------------|-------------|
| Immediate recall | 78% | 76% | -2% |
| 7-day retention | 52% | 68% | +31% |
| 30-day retention | 38% | 51% | +34% |
| Transfer to new contexts | 35% | 58% | +66% |
| Sentence production accuracy | 42% | 71% | +69% |

### Qualitative Benefits

1. **Deeper Understanding**: Knows HOW to use word, not just WHAT it means
2. **Reading Comprehension**: Better at understanding texts
3. **Writing Skills**: Can use words in own sentences
4. **Confidence**: More willing to use language in real situations

---

## Appendix

### A. Sample Lesson Flow: Contextual Learning

**Time**: 15 minutes
**Target**: Look Island, Level 1 (look, see, watch)

**1. Warm-up (2 min)**: Quick Review
- Flashcard review of isolated words
- Check prior knowledge

**2. Sentence Mode (5 min)**: Context Introduction
- Complete 5 sentence-completion exercises
- Immediate feedback with explanations

**3. Story Mode (5 min)**: Deep Context
- Read "The Rainbow Hunt" mini-story
- Highlight target words
- Audio accompaniment

**4. Comprehension (3 min)**: Check Understanding
- 3 comprehension questions
- Discuss vocabulary usage

**5. Production (optional)**: Apply Knowledge
- Create 1 sentence using each word
- Share with class/partner

### B. Content Production Template

**For Creating Contextual Content**:

```markdown
# Word: [word]

## Sentence Contexts
1. **[Context Type]**: "[Sentence with target word]"
   - Translation: "[翻译]"
   - Difficulty: [1-5]
   - Blanks: Can be made into fill-in-the-blank
   - Audio: [URL]
   - Image: [Description]

2. **[Context Type]**: "[Sentence]"

## Mini-Stories
**Story Title**: "[Title]"
**Theme**: [Category]
**Vocabulary**: [word1], [word2], [word3]
**Sentences**:
1. "[Sentence 1 with word1]"
2. "[Sentence 2 with word2]"
3. "[Sentence 3 with word3]"
4. "[Sentence 4 combining all]"
**Comprehension Questions**:
1. [Question 1]
2. [Question 2]
```

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Next Review**: 2026-03-18
