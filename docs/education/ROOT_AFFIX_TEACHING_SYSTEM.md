# Root & Affix Teaching System (RATS)
## Morphological Awareness for KET Vocabulary Learning

**Author**: Education Specialist
**Date**: 2026-02-18
**Status**: Design Phase
**Target Audience**: 10-year-old KET learners

---

## Executive Summary

The Root & Affix Teaching System (RATS) leverages Wordland's existing morphological data (root/prefix/suffix fields) to develop students' morphological awareness—the ability to recognize and use word parts to unlock meaning. Research shows morphological awareness predicts vocabulary growth better than phonological awareness, especially for ages 8-12.

---

## Table of Contents

1. [Educational Rationale](#educational-rationale)
2. [Current Content Analysis](#current-content-analysis)
3. [Teaching Methodology](#teaching-methodology)
4. [Game Integration Design](#game-integration-design)
5. [Progressive Introduction Plan](#progressive-introduction-plan)
6. [Implementation Guide](#implementation-guide)
7. [Assessment Strategy](#assessment-strategy)

---

## Educational Rationale

### Why Morphological Awareness Matters

#### Research Findings

| Study | Finding | Age Group |
|-------|---------|-----------|
| Carlisle (2000) | MA predicts reading comprehension, r = .65 | Grades 4-5 |
| Nagy et al. (2006) | MA accounts for 32% of vocabulary variance | Ages 10-12 |
| Anglin (1993) | Children learn 8-10 words/day from morphology | Elementary |

**Key Insight**: Teaching 20 roots + 20 affixes unlocks 1000+ words.

#### Cognitive Benefits

1. **Economy of Learning**: One root teaches multiple words
   - Example: "spect" (look) → inspect, respect, spectacle, perspective

2. **Transferability**: Skills apply to unfamiliar words
   - Student sees "transport" → "trans-" (across) + "port" (carry) = "carry across"

3. **Metalinguistic Awareness**: Understands how language works
   - Develops analytical thinking about word formation

4. **Confidence Building**: Toolkit for decoding difficult words
   - Reduces anxiety about "big words"

### Developmental Appropriateneness (Age 10)

**Piaget's Concrete Operational Stage**:
- Can think logically about concrete objects
- Word parts are "concrete" building blocks
- Classification skills developing

**Implications for Design**:
- Visual, manipulable components (blocks, puzzles)
- Pattern recognition activities
- Concrete examples before abstract rules

---

## Current Content Analysis

### Existing Morphological Data

Wordland's Word model includes:
```kotlin
val root: String?        // e.g., "spect", "port", "ject"
val prefix: String?      // e.g., "trans-", "re-", "pre-"
val suffix: String?      // e.g., "-tion", "-able", "-ly"
```

### Content Audit

#### Look Island Morphological Content

| Word | Root | Prefix | Suffix | Family Members in Content |
|------|------|--------|--------|---------------------------|
| color | col | - | -or | - |
| red | reh | - | - | - |
| blue | blwe | - | - | - |
| bright | bher | - | - | - |
| observe | serv | ob- | - | - |
| appear | par | ap- | - | - |
| visible | - | - | -ible | - |
| witness | wit | - | -ness | - |
| examine | - | - | -ine | - |
| emerge | merg | e- | - | - |
| notice | nor | - | - | - |

**Assessment**: 10/30 words (33%) have morphological data, but no systematic families.

#### Make Lake Morphological Content

| Word | Root | Prefix | Suffix | Teaching Opportunities |
|------|------|--------|--------|----------------------|
| create | - | - | - | - |
| produce | - | - | - | - |
| manufacture | - | - | -ure | vs. factory, fracture |
| invention | - | in- | -tion | vs. invent |
| innovation | - | in- | -ation | vs. innovate |
| craftsmanship | - | - | -ship | Abstract suffix |

**Assessment**: Richer morphological content, especially in later levels.

### Recommended Content Enhancement

Add 50 high-frequency roots/affixes across islands:

**Top 20 Roots for KET**:
1. spect (look) - Look Island enhancement
2. port (carry) - Move Valley potential
3. ject (throw) - Make Lake enhancement
4. struct (build) - Make Lake natural fit
5. tract (pull) - Move Valley
6. form (shape) - Make Lake
7. vis (see) - Look Island enhancement
8. dict (say) - Say Mountain
9. scrib/write - Say Mountain
10. graph (write) - Say Mountain
11. aud (hear) - Listen Valley
12. phon (sound) - Listen Valley
13. tract (pull) - Move Valley
14. struct (build) - Make Lake
15. fac (make) - Make Lake
16. cred (believe) - Think Forest
17. cogn (know) - Think Forest
18. ped (foot) - Move Valley
19. man (hand) - Make Lake
20. gest (carry) - Move Valley

**Top 20 Affixes for KET**:

Prefixes:
1. un- (not)
2. re- (again)
3. pre- (before)
4. dis- (apart)
5. mis- (wrong)
6. trans- (across)
7. con- (with)
8. in- (not/into)
9. sub- (under)
10. over- (too much)

Suffixes:
1. -tion (action)
2. -ment (result)
3. -ful (full of)
4. -less (without)
5. -able (can)
6. -ly (manner)
7. -er (person)
8. -ness (quality)
9. -ive (quality)
11. -or (person)

---

## Teaching Methodology

### Learning Sequence

#### Phase 1: Discovery (Pattern Recognition)

**Goal**: Notice that words share parts.

**Activity**: Word Sort
- Present 6 words: inspect, respect, spectator, spectacle, prospect, aspect
- Ask: "What do you notice about these words?"
- Guide to finding "spect" in all of them
- Reveal: "Spect means 'to look'"

**Game Integration**: "Word Detective" mini-game
```
[Screen shows 4 words with a common root]
→ Student selects the matching letters
→ Reveals the root meaning
→ Unlocks root badge
```

#### Phase 2: Explicit Instruction (Rule Learning)

**Goal**: Learn systematic meanings.

**Format**: Root/Affix Card
```
┌─────────────────────┐
│      TRANS-         │
│                     │
│   [→ → →]          │
│                     │
│   Meaning: ACROSS   │
│                     │
│   Examples:         │
│   • transport       │
│   • translate       │
│   • transform       │
└─────────────────────┘
```

**Teaching Script**:
> "When you see TRANS- at the start of a word, it means 'across' or 'from one side to another.' Look at TRANSPORT: TRANS (across) + PORT (carry). It means to carry across!"

#### Phase 3: Guided Practice (Application)

**Goal**: Use morphology to decode new words.

**Activity**: Word Building
```
Given: spect (look), re- (back), in- (into)

Build a word that means "look back into"
→ inspect

Build a word that means "look again"
→ respect
```

**Game Integration**: "Word Builder" mode
```
[Root + Affix slots]
┌─────┐  ┌─────┐  ┌──────┐
│     │  │ VIS- │  │      │
│     │ + │      │ + │ -IBLE │
│     │  │      │  │      │
└─────┘  └─────┘  └──────┘

Meaning: Can be seen
Word: visible
```

#### Phase 4: Independent Application (Transfer)

**Goal**: Apply to unfamiliar words.

**Challenge**: "Morphology Mystery"
```
New word: "interject"
Question: What does this mean?
→ INTER (between) + JECT (throw) = "throw between"
```

### Socratic Questioning Framework

#### Instead of Direct Letter Reveals

**Current Hint System**:
> "First letter: V"

**Morphological Hint System**:
> Level 1: "Think about what you can see with your eyes. This word means 'can be seen.'"
> Level 2: "It starts with a prefix that means 'can' or 'able to'..."
> Level 3: "V-I-S-I-B-L-E (visible)"

**Benefits**:
- Teaches meaning, not just spelling
- Connects to prior knowledge
- Develops analytical thinking

---

## Game Integration Design

### New Game Mode: "Word Builder"

**Objective**: Build words from roots and affixes to match meanings.

**Gameplay**:
1. Present a meaning: "carry across"
2. Show root bank: port, spect, ject, form
3. Show prefix bank: trans-, re-, pre-, dis-
4. Player combines: TRANS + PORT = transport
5. Success: Unlock word family

**Progression**:
- Level 1: 2 parts (prefix + root)
- Level 2: 3 parts (prefix + root + suffix)
- Level 3: Multiple choices (select correct prefix)

### Enhanced Hint System Integration

**Replace Letter Hints with Morphological Hints**:

```kotlin
/**
 * Morphological hint generator
 */
object MorphologicalHintGenerator {

    fun generateHint(
        word: Word,
        hintLevel: Int,
        userLearnedRoots: List<String>
    ): String {
        return when (hintLevel) {
            1 -> generateMeaningClue(word, userLearnedRoots)
            2 -> generateComponentClue(word, userLearnedRoots)
            3 -> generatePartialSpelling(word)
            else -> ""
        }
    }

    private fun generateMeaningClue(word: Word, learnedRoots: List<String>): String {
        // If user has learned the root, use it
        if (word.root != null && word.root in learnedRoots) {
            val rootMeaning = getRootMeaning(word.root)
            return "This word contains '$root' which means '$rootMeaning'"
        }

        // Otherwise, give contextual clue
        return getExampleSentenceHint(word)
    }

    private fun generateComponentClue(word: Word, learnedRoots: List<String>): String {
        val parts = mutableListOf<String>()

        if (word.prefix != null && word.prefix in learnedRoots) {
            parts.add("prefix '${word.prefix}'")
        }
        if (word.root != null && word.root in learnedRoots) {
            parts.add("root '${word.root}'")
        }
        if (word.suffix != null && word.suffix in learnedRoots) {
            parts.add("suffix '${word.suffix}'")
        }

        return if (parts.isNotEmpty()) {
            "Combine: ${parts.joinToString(" + ")}"
        } else {
            "First letter: ${word.word.first()}"
        }
    }

    private fun generatePartialSpelling(word: Word): String {
        // Reveal first half as fallback
        val half = (word.word.length + 1) / 2
        return word.word.take(half) + "_".repeat(word.word.length - half)
    }
}
```

### UI Component: "Word Explorer"

**Screen Design**:
```
┌──────────────────────────────────────┐
│         WORD EXPLORER                │
├──────────────────────────────────────┤
│                                      │
│        VISIBLE                       │
│       ────────                       │
│                                      │
│  ┌─────────┐  ┌─────────┐  ┌──────┐ │
│  │  VIS-   │  │  -IBLE  │  │      │ │
│  │ (see)   │  │ (able)  │  │      │ │
│  └─────────┘  └─────────┘  └──────┘ │
│       │            │                 │
│       └────────────┴─────────┐       │
│                             ▼       │
│                      "Can be seen"   │
│                                      │
│  Family:                             │
│  • vision, visit, visual             │
│  • capable, durable, flexible        │
│                                      │
│  [See Examples] [Practice Family]    │
└──────────────────────────────────────┘
```

---

## Progressive Introduction Plan

### Scope & Sequence

**Principle**: Introduce most productive roots/affixes first.

**Month 1: Visual Roots (Look Island Integration)**
- Week 1: spect (look)
- Week 2: vis (see)
- Week 3: vid/vis (see - reinforce)
- Week 4: -ive (suffix: quality)

**Month 2: Movement Roots (Move Valley)**
- Week 5: port (carry)
- Week 6: tract (pull/drag)
- Week 7: ject (throw)
- Week 8: trans- (across)

**Month 3: Creation Roots (Make Lake)**
- Week 9: struct (build)
- Week 10: form (shape)
- Week 11: fac (make/do)
- Week 12: -tion (suffix: action)

**Month 4: Communication Roots (Say Mountain)**
- Week 13: dict (say)
- Week 14: scrib/script (write)
- Week 15: graph (write/draw)
- Week 16: -ment (suffix: result)

**Month 5: Sound Roots (Listen Valley)**
- Week 17: aud (hear)
- Week 18: phon (sound)
- Week 19: voc (voice/call)
- Week 20: -able (suffix: capable)

### Mastery Criteria

**Root/Affix Mastery =**:
1. Can identify root in at least 5 words
2. Can explain root meaning in own words
3. Can apply root to decode new word
4. Achieves 80% accuracy in Word Builder
5. Uses root in Morphological Hint successfully

---

## Implementation Guide

### Phase 1: Content Enhancement (2 weeks)

**File**: `data/seed/MorphologicalDatabase.kt`

```kotlin
/**
 * Root and affix definitions with examples
 */
object MorphologicalDatabase {

    data class Morpheme(
        val id: String,           // e.g., "root_spect"
        val type: MorphemeType,   // ROOT, PREFIX, SUFFIX
        val text: String,         // "spect" or "trans-" or "-tion"
        val meaning: String,      // "to look" or "across" or "action"
        val origin: String?,      // "Latin" for interest
        val examples: List<String>, // ["inspect", "respect", ...]
        val frequency: Int,       // 1-100 (how common)
        val islandId: String?,    // Primary island for teaching
        val unlockLevel: Int,     // When to introduce (1-10)
        val relatedMorphemes: List<String> // IDs of related morphemes
    )

    enum class MorphemeType { ROOT, PREFIX, SUFFIX }

    private val allMorphemes = listOf(
        Morpheme(
            id = "root_spect",
            type = MorphemeType.ROOT,
            text = "spect",
            meaning = "to look or see",
            origin = "Latin",
            examples = listOf("inspect", "respect", "spectator", "spectacle", "prospect", "aspect"),
            frequency = 85,
            islandId = "look_island",
            unlockLevel = 3,
            relatedMorphemes = listOf("root_vis", "root_vid")
        ),
        Morpheme(
            id = "prefix_trans",
            type = MorphemeType.PREFIX,
            text = "trans-",
            meaning = "across, through, beyond",
            origin = "Latin",
            examples = listOf("transport", "translate", "transform", "transparent", "transplant"),
            frequency = 78,
            islandId = "move_valley",
            unlockLevel = 4,
            relatedMorphemes = listOf("root_port", "root_form")
        ),
        // ... 48 more morphemes
    )

    fun getMorphemeById(id: String): Morpheme? = allMorphemes.find { it.id == id }

    fun getMorphemesByIsland(islandId: String): List<Morpheme> {
        return allMorphemes.filter { it.islandId == islandId }
    }

    fun getMorphemesForLevel(unlockLevel: Int): List<Morpheme> {
        return allMorphemes.filter { it.unlockLevel == unlockLevel }
    }

    fun searchMorphemesContaining(text: String): List<Morpheme> {
        return allMorphemes.filter {
            it.text.equals(text, ignoreCase = true) ||
            text.contains(it.text, ignoreCase = true)
        }
    }
}
```

### Phase 2: Data Model Extension (1 week)

**File**: `domain/model/UserMorphemeProgress.kt`

```kotlin
@Entity(tableName = "user_morpheme_progress")
@Immutable
data class UserMorphemeProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,
    val morphemeId: String,  // FK to MorphologicalDatabase

    // Learning status
    val status: MorphemeLearningStatus,  // NEW, LEARNING, MASTERED
    val introductionDate: Long = System.currentTimeMillis(),
    val masteryDate: Long? = null,

    // Practice metrics
    val timesRecognized: Int = 0,
    val timesApplied: Int = 0,
    val correctApplications: Int = 0,

    // Word family mastery
    val wordsSeen: Int = 0,
    val wordsMastered: Int = 0,

    val lastPracticeDate: Long? = null,
    val nextReviewDate: Long? = null
)

enum class MorphemeLearningStatus {
    NEW,        // Not yet introduced
    LEARNING,   // Introduced but not mastered
    MASTERED    // Can apply independently
}
```

### Phase 3: Use Case Implementation (2 weeks)

**File**: `domain/usecase/IntroduceMorphemeUseCase.kt`

```kotlin
@Singleton
class IntroduceMorphemeUseCase @Inject constructor(
    private val morphemeRepository: MorphemeRepository,
    private val progressRepository: UserMorphemeProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        morphemeId: String
    ): IntroduceMorphemeResult {
        // Get morpheme definition
        val morpheme = morphemeRepository.getMorphemeById(morphemeId)
            ?: return IntroduceMorphemeResult.Error("Morpheme not found")

        // Check if user already knows related morphemes
        val relatedProgress = morpheme.relatedMorphemes.mapNotNull {
            progressRepository.getUserProgress(userId, it)
        }

        // Create introduction sequence
        val introduction = createIntroductionSequence(morpheme, relatedProgress)

        return IntroduceMorphemeResult.Success(
            morpheme = morpheme,
            sequence = introduction,
            canProceed = true
        )
    }

    private fun createIntroductionSequence(
        morpheme: MorphemeDatabase.Morpheme,
        relatedProgress: List<UserMorphemeProgress>
    ): IntroductionSequence {
        return IntroductionSequence(
            steps = listOf(
                IntroductionStep.Discovery(morpheme.examples),
                IntroductionStep.ExplicitInstruction(morpheme),
                IntroductionStep.GuidedPractice(morpheme),
                IntroductionStep.IndependentApplication(morpheme)
            )
        )
    }
}
```

**File**: `domain/usecase/GetMorphologicalHintUseCase.kt`

```kotlin
@Singleton
class GetMorphologicalHintUseCase @Inject constructor(
    private val morphemeRepository: MorphemeRepository,
    private val progressRepository: UserMorphemeProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        word: Word,
        hintLevel: Int
    ): MorphologicalHint {
        // Find morphemes in word
        val morphemes = findMorphemesInWord(word)

        // Filter to those user has learned
        val learnedMorphemes = morphemes.filter { morpheme ->
            val progress = progressRepository.getUserProgress(userId, morpheme.id)
            progress?.status == MorphemeLearningStatus.LEARNING ||
            progress?.status == MorphemeLearningStatus.MASTERED
        }

        return if (learnedMorphemes.isNotEmpty()) {
            // Use morphological hint
            generateMorphologicalHint(word, learnedMorphemes, hintLevel)
        } else {
            // Fall back to traditional hint
            MorphologicalHint.TraditionalHint(
                text = TraditionalHintGenerator.generateHint(word.word, hintLevel)
            )
        }
    }

    private suspend fun findMorphemesInWord(word: Word): List<MorphemeDatabase.Morpheme> {
        val found = mutableListOf<MorphemeDatabase.Morpheme>()

        // Check prefix
        if (word.prefix != null) {
            morphemeRepository.searchMorphemesContaining(word.prefix)
                .let { found.addAll(it) }
        }

        // Check root
        if (word.root != null) {
            morphemeRepository.searchMorphemesContaining(word.root)
                .let { found.addAll(it) }
        }

        // Check suffix
        if (word.suffix != null) {
            morphemeRepository.searchMorphemesContaining(word.suffix)
                .let { found.addAll(it) }
        }

        return found
    }
}
```

### Phase 4: UI Implementation (3 weeks)

**Screen**: `ui/screens/WordBuilderScreen.kt`

```kotlin
@Composable
fun WordBuilderScreen(
    level: Int,
    onWordBuilt: (String) -> Unit,
    viewModel: WordBuilderViewModel = viewModel(factory = AppServiceLocator.provideFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is WordBuilderUiState.Ready -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Target meaning
                MeaningCard(state.targetMeaning)

                // Component slots
                ComponentSlots(
                    prefixSlot = state.prefixSlot,
                    rootSlot = state.rootSlot,
                    suffixSlot = state.suffixSlot,
                    onPrefixSelected = { viewModel.selectPrefix(it) },
                    onRootSelected = { viewModel.selectRoot(it) },
                    onSuffixSelected = { viewModel.selectSuffix(it) }
                )

                // Component bank
                ComponentBank(
                    prefixes = state.availablePrefixes,
                    roots = state.availableRoots,
                    suffixes = state.availableSuffixes,
                    learnedMorphemes = state.learnedMorphemes
                )

                // Build button
                Button(
                    onClick = { viewModel.buildWord() },
                    enabled = state.canBuild
                ) {
                    Text("Build Word")
                }
            }
        }
    }
}
```

**Screen**: `ui/screens/WordExplorerScreen.kt`

```kotlin
@Composable
fun WordExplorerScreen(
    wordId: String,
    onBack: () -> Unit
) {
    val viewModel: WordExplorerViewModel = viewModel(factory = AppServiceLocator.provideFactory())
    val explorerState by viewModel.explorerState.collectAsState()

    val word = explorerState.word
    val morphemes = explorerState.morphemes
    val wordFamily = explorerState.wordFamily

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Word header
        Text(text = word.word, style = MaterialTheme.typography.displayLarge)

        // Morphology breakdown
        MorphologyBreakdown(
            prefix = morphemes.find { it.type == PREFIX },
            root = morphemes.find { it.type == ROOT },
            suffix = morphemes.find { it.type == SUFFIX }
        )

        // Meaning
        Text(text = word.translation, style = MaterialTheme.typography.bodyLarge)

        // Word family
        WordFamilySection(
            familyWords = wordFamily,
            onWordClick = { /* Navigate to word */ }
        )

        // Examples
        ExampleSentences(word.exampleSentences)
    }
}
```

---

## Assessment Strategy

### Formative Assessment

#### In-Game Checks

1. **Recognition Check**:
   - "Which of these words contains 'spect'?"
   - Options: respect, expect, inspect, suspect
   - All correct! → spect mastery +10%

2. **Application Check**:
   - "If 'port' means 'carry' and 'trans-' means 'across', what does 'transport' mean?"
   - Multiple choice + open response

3. **Production Check**:
   - "Build a word using 'struct' that means 'the process of building'"
   - User combines: con + struct + ion = construction

#### Progress Tracking

```kotlin
data class MorphemeAssessment(
    val morphemeId: String,
    val userId: String,

    // Recognition (Can identify in words)
    val recognitionScore: Float,  // 0-1

    // Comprehension (Understands meaning)
    val comprehensionScore: Float,  // 0-1

    // Application (Can use to decode)
    val applicationScore: Float,  // 0-1

    // Overall mastery
    val masteryLevel: MasteryLevel,  // BEGINNING, DEVELOPING, PROFICIENT, MASTERY

    val lastAssessed: Long,
    val nextAssessmentDue: Long
)

enum class MasteryLevel {
    BEGINNING,    // 0-25%: Can't recognize
    DEVELOPING,   // 26-50%: Recognizes, doesn't understand
    PROFICIENT,   // 51-75%: Understands, limited application
    MASTERY       // 76-100%: Independent application
}
```

### Summative Assessment

#### End-of-Island Challenge

**Format**: "Morphology Master Challenge"
```
You've learned 5 roots on Look Island:
• spect (look)
• vis (see)
• vid (see)
• stare (gaze)
• view (see)

Challenge: Decode these new words:
1. supervision → super (above) + vis (see) + ion (action) = "watching from above"
2. invisible → in (not) + vis (see) + ible (able) = "cannot be seen"
3. television → tele (far) + vis (see) + ion (action) = "seeing from far away"

Score: ___ / 3
Mastery Badge: ___
```

---

## Expected Outcomes

### Quantitative Metrics

| Metric | Baseline | 3-Month Target | 6-Month Target |
|--------|----------|----------------|----------------|
| Morphemes mastered | 0 | 15 | 30 |
| Word families unlocked | 0 | 45 | 120 |
| New word decoding accuracy | 25% | 45% | 65% |
| Etymology interest | - | +40% survey | +60% survey |

### Qualitative Outcomes

1. **Metalinguistic Awareness**: Student can explain word formation
2. **Transfer Skills**: Applies morphology to unfamiliar words
3. **Confidence**: Reduced anxiety about "big words"
4. **Curiosity**: Asks "Where does this word come from?"

### Long-Term Impact

- **Vocabulary Growth Rate**: +2 words/day (from morphological analysis)
- **Reading Comprehension**: +15% (morphological awareness contribution)
- **Spelling Accuracy**: +10% (pattern recognition)

---

## Appendix

### A. Sample Lesson Plan: "Spect" (Look Island, Level 3)

**Duration**: 10 minutes (mini-lesson within gameplay)

**Learning Objectives**:
1. Identify root "spect" in words
2. Explain meaning of "spect" (to look/see)
3. Decode new words containing "spect"

**Materials**:
- Word cards: inspect, respect, spectacle, spectator, aspect, prospect
- Visual: Eye with "spect" written inside
- Audio: Pronunciation of each word

**Procedure**:

1. **Hook (1 min)**:
   - Show: "spectator" from Look Island
   - Question: "What does a spectator do?"
   - Answer: "Watches, looks at something"

2. **Discovery (3 min)**:
   - Display 6 word cards
   - Student highlights common letters
   - Reveals: "S-P-E-C-T"
   - "This is called a 'root' - the base of a word"

3. **Explicit Instruction (2 min)**:
   - "Spect comes from Latin and means 'to look or see'"
   - Show animation: EYES + SPECT = INSPECT (look into)
   - Student traces each word: IN + SPECT, RE + SPECT, etc.

4. **Guided Practice (3 min)**:
   - Activity: "Match the word to its meaning"
     - inspect ↔ look into closely
     - respect ↔ look up to someone
     - spectacle ↔ something amazing to see
   - Student drags words to meanings

5. **Independent Application (1 min)**:
   - Challenge: "What does 'prospect' mean?"
   - Hint: pro- = forward
   - Student combines: forward + look = "looking forward"

**Assessment**:
- Can student define "spect"? ✓
- Can student find "spect" in new word? ✓
- Can student use "spect" to decode meaning? ✓

**Next Steps**:
- Introduce related root "vis" (see) next level
- Practice with "Word Builder" mini-game

### B. Morpheme Database Schema

```sql
CREATE TABLE morphemes (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,  -- 'ROOT', 'PREFIX', 'SUFFIX'
    text TEXT NOT NULL,  -- The morpheme itself
    meaning TEXT NOT NULL,
    origin TEXT,
    examples TEXT,  -- JSON array
    frequency INTEGER,
    island_id TEXT,
    unlock_level INTEGER,
    related_morphemes TEXT  -- JSON array
);

CREATE TABLE user_morpheme_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    morpheme_id TEXT NOT NULL,
    status TEXT NOT NULL,
    introduction_date INTEGER,
    mastery_date INTEGER,
    times_recognized INTEGER DEFAULT 0,
    times_applied INTEGER DEFAULT 0,
    correct_applications INTEGER DEFAULT 0,
    words_seen INTEGER DEFAULT 0,
    words_mastered INTEGER DEFAULT 0,
    last_practice_date INTEGER,
    next_review_date INTEGER,
    FOREIGN KEY (morpheme_id) REFERENCES morphemes(id),
    UNIQUE(user_id, morpheme_id)
);
```

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Next Review**: 2026-03-18
