# Socratic Hint System Design

**Author**: Education Specialist
**Date**: 2026-02-18
**Status**: Design Complete - Ready for Implementation
**Priority**: P1 (High)
**Task**: #13 Integration

---

## 1. Executive Summary

The Socratic Hint System transforms Wordland's hint mechanism from simple letter reveals into an **active learning tool** that guides students toward answers through **progressive questioning** and **morphological awareness**. This design aligns with **Task #13** (Enhance Hint System) and integrates with the existing `HintGenerator`, `HintManager`, and `UseHintUseCaseEnhanced` components.

### Key Benefits

- **Active recall**: Students think through the answer rather than receiving it passively
- **Metacognitive development**: Learners build problem-solving strategies for vocabulary
- **Morphological awareness**: Introduces roots/affixes using existing data fields
- **Long-term retention**: Socratic methods produce deeper learning than direct answers

---

## 2. Educational Philosophy

### 2.1 Socratic Method in Vocabulary Learning

The Socratic method uses **guided questions** to lead learners to discover answers themselves. In vocabulary acquisition:

1. **Activate prior knowledge**: Connect to known words
2. **Guide observation**: Highlight word structure clues
3. **Scaffold thinking**: Progressive hints without giving the answer
4. **Build metacognition**: Teach students "how to think" about words

### 2.2 Contrast with Current System

| Aspect | Current System | Socratic System |
|--------|---------------|-----------------|
| Level 1 | First letter ("首字母: A") | Morphological clue (root/prefix/suffix) |
| Level 2 | First half ("ap___") | Socratic question |
| Level 3 | Vowels masked ("_ppl_") | Partial letter reveal |
| Pedagogy | Passive reception | Active construction |
| Learning | Surface-level | Deep processing |

---

## 3. Three-Level Hint Architecture

### 3.1 Level 1: Morphological Clue

**Purpose**: Activate word structure analysis skills

**Implementation**: Leverage existing `Word.root`, `Word.prefix`, `Word.suffix` fields

**Examples**:

```
Word: "observe" (root: "serv", prefix: "ob")
Hint 1: "这个词包含词根 'serv' (服务/看) 和前缀 'ob' (向)"

Word: "witness" (root: "wit", suffix: "ness")
Hint 1: "这个词包含词根 'wit' (知道/看见) 和后缀 '-ness' (状态)"

Word: "color" (root: "col", suffix: "or")
Hint 1: "这个词包含词根 'col' (覆盖/色)"

Word: "visible" (no root data)
Hint 1: Fallback to current system ("首字母: V")
```

**Fallback Strategy**:
- If morphological data is missing → Use first letter hint (current behavior)
- If word is ≤3 letters → Skip to Level 2

**KET-Specific Rationale**:
- KET vocabulary includes many transparent words (e.g., *observe*, *appear*, *visible*)
- Early exposure to morphology builds foundation for PET/FCE levels
- Even simple awareness of word parts improves spelling accuracy

### 3.2 Level 2: Socratic Question

**Purpose**: Guide thinking through definition, context, or word relationships

**Question Types**:

1. **Definition-based** (most common):
   ```
   Translation: "观察"
   Question: "What word means 'to watch or study something carefully'?"
   Chinese: "哪个词表示'仔细观看或研究'？"
   ```

2. **Context-based** (using `exampleSentences`):
   ```
   Translation: "瞥见"
   Example: "I caught a _____ of the bird."
   Question: "Complete this sentence: 'I caught a _____ of the bird.'"
   ```

3. **Association-based** (using `relatedWords`):
   ```
   Translation: "盯着看"
   Related: ["gaze", "look", "watch"]
   Question: "Similar to 'gaze' but with a silent 't' at the end"
   ```

4. **Contrast-based** (for confusable words):
   ```
   Translation: "看见"
   Contrast: "Not 'watch' (持续观看) but the result (看见)"
   Question: "Is this 'watch' (持续观看) or 'see' (看见)?"
   ```

**Socratic Question Templates** (by `partOfSpeech`):

```kotlin
// Verbs (动作词)
"What verb means '{translation}'?"
"Which action word describes '{action_description}'?"

// Nouns (名词)
"What noun means '{translation}'?"
"What object/thing represents '{concept}'?"

// Adjectives (形容词)
"What adjective describes '{quality}'?"
```

**Fallback**:
- If no example sentence exists → Use definition question
- If no related words exist → Use definition question

### 3.3 Level 3: Partial Letter Reveal

**Purpose**: Final scaffolding while maintaining active recall

**Implementation**: Use existing `generateLevel3Hint()` (vowels masked)

**Rationale**: By Level 3, the student has:
1. Analyzed word structure (Level 1)
2. Thought through meaning/usage (Level 3)
3. Needs minimal confirmation to complete recall

**Examples**:
```
"observe" → "_bs_rv_" (vowels masked)
"witness" → "w_tn_ss" (vowels masked)
```

---

## 4. Integration with Existing System

### 4.1 Architecture Diagram

```
User taps hint button
        ↓
LearningViewModel.useHint()
        ↓
UseHintUseCaseEnhanced.invoke()
        ↓
HintManager.canUseHint() → Check limits
        ↓
HintManager.useHint() → Increment level
        ↓
HintGenerator.generateSocraticHint(word, level)  ← NEW METHOD
        ↓
    ├── Level 1: MorphologicalClueGenerator  ← NEW CLASS
    ├── Level 2: SocraticQuestionGenerator   ← NEW CLASS
    └── Level 3: Existing vowel masking
        ↓
HintResult with hintText, hintLevel, penalty flag
        ↓
UI displays hint in HintCard
```

### 4.2 Modified HintGenerator Interface

```kotlin
class HintGenerator {
    // EXISTING METHOD (unchanged)
    fun generateHint(word: String, level: Int): String { ... }

    // NEW METHOD - Socratic hints
    fun generateSocraticHint(
        word: Word,  // Changed from String to Word
        level: Int,
        context: HintContext  // NEW: Translation, examples, etc.
    ): String {
        return when (level) {
            1 -> generateMorphologicalHint(word)
            2 -> generateSocraticQuestion(word, context)
            3 -> generateLevel3Hint(word.word)  // Existing
            else -> ""
        }
    }
}

// NEW DATA CLASS
data class HintContext(
    val translation: String,
    val exampleSentences: List<ExampleSentence>?,
    val relatedWords: List<String>?,
    val partOfSpeech: String?
)
```

### 4.3 New Components

#### 4.3.1 MorphologicalClueGenerator

```kotlin
/**
 * Generates Level 1 hints using root/prefix/suffix data
 */
class MorphologicalClueGenerator {

    fun generate(word: Word): String {
        val parts = mutableListOf<String>()

        // Prefix explanation
        word.prefix?.let { prefix ->
            parts.add("前缀 '$prefix' (${explainPrefix(prefix)})")
        }

        // Root explanation
        word.root?.let { root ->
            parts.add("词根 '$root' (${explainRoot(root)})")
        }

        // Suffix explanation
        word.suffix?.let { suffix ->
            parts.add("后缀 '$suffix' (${explainSuffix(suffix)})")
        }

        return if (parts.isNotEmpty()) {
            "这个词包含 ${parts.joinToString(" 和 ")}"
        } else {
            // Fallback to first letter
            "首字母: ${word.word.first().uppercaseChar()}"
        }
    }

    private fun explainPrefix(prefix: String): String {
        return prefixMeanings[prefix] ?: "未知前缀"
    }

    private fun explainRoot(root: String): String {
        return rootMeanings[root] ?: "未知词根"
    }

    private fun explainSuffix(suffix: String): String {
        return suffixMeanings[suffix] ?: "后缀"
    }

    companion object {
        // Prefix meanings (KET level)
        val prefixMeanings = mapOf(
            "ob" to "向，对着",
            "ap" to "向，加强",
            "e" to "出",
            "re" to "再，又"
        )

        // Root meanings (KET level)
        val rootMeanings = mapOf(
            "serv" to "服务，看，保持",
            "par" to "出现，准备",
            "vid" to "看",
            "merg" to "沉，没",
            "wit" to "知道，看见",
            "star" to "站，看",
            "nor" to "知道，通知",
            "skai" to "切割，分开"
        )

        // Suffix meanings (KET level)
        val suffixMeanings = mapOf(
            "ness" to "状态，性质",
            "or" to "名词后缀",
            "ly" to "副词后缀",
            "ful" to "充满..."
        )
    }
}
```

#### 4.3.2 SocraticQuestionGenerator

```kotlin
/**
 * Generates Level 2 hints using Socratic questioning
 */
class SocraticQuestionGenerator {

    fun generate(word: Word, context: HintContext): String {
        // Strategy selection in priority order
        return when {
            // 1. Use example sentence if available (most effective)
            hasValidExample(context) ->
                generateContextualQuestion(word, context)

            // 2. Use related words for association
            hasRelatedWords(context) ->
                generateAssociationQuestion(word, context)

            // 3. Fallback to definition-based question
            else ->
                generateDefinitionQuestion(word, context)
        }
    }

    private fun hasValidExample(context: HintContext): Boolean {
        return !context.exampleSentences.isNullOrEmpty()
    }

    private fun hasRelatedWords(context: HintContext): Boolean {
        return !context.relatedWords.isNullOrEmpty()
    }

    private fun generateContextualQuestion(
        word: Word,
        context: HintContext
    ): String {
        val example = context.exampleSentences!!.first()

        // Replace target word with blank
        val sentenceWithBlank = example.sentence.replace(
            word.word,
            "_____",
            ignoreCase = true
        )

        return "填空: $sentenceWithBlank\n(${example.translation})"
    }

    private fun generateAssociationQuestion(
        word: Word,
        context: HintContext
    ): String {
        val related = context.relatedWords!!.first()
        return "类似于 '$related'，表示 '${context.translation}'"
    }

    private fun generateDefinitionQuestion(
        word: Word,
        context: HintContext
    ): String {
        val posLabel = when (context.partOfSpeech) {
            "verb" -> "哪个动作词"
            "noun" -> "哪个名词"
            "adjective" -> "哪个形容词"
            else -> "哪个词"
        }

        return "$posLabel 表示 '${context.translation}'?"
    }
}
```

### 4.4 Updated UseHintUseCaseEnhanced

```kotlin
@Singleton
class UseHintUseCaseEnhanced @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val wordRepository: WordRepository,
    private val hintGenerator: HintGenerator,  // Now has generateSocraticHint
    private val hintManager: HintManager,
    private val behaviorAnalyzer: BehaviorAnalyzer,
    private val morphologicalGenerator: MorphologicalClueGenerator,  // NEW
    private val socraticGenerator: SocraticQuestionGenerator  // NEW
) {

    suspend operator fun invoke(
        userId: String,
        wordId: String,
        levelId: String
    ): Result<HintResult> {
        return try {
            val (canUse, reason) = hintManager.canUseHint(wordId)
            if (!canUse) {
                return Result.Error(HintLimitException(reason ?: "提示不可用"))
            }

            val word = wordRepository.getWordById(wordId)
                ?: return Result.Error(IllegalStateException("Word not found: $wordId"))

            val hintLevel = hintManager.useHint(wordId)

            // NEW: Create hint context
            val context = HintContext(
                translation = word.translation,
                exampleSentences = parseExampleSentences(word.exampleSentences),
                relatedWords = parseRelatedWords(word.relatedWords),
                partOfSpeech = word.partOfSpeech
            )

            // NEW: Generate Socratic hint
            val hintText = hintGenerator.generateSocraticHint(
                word = word,  // Pass full Word object
                level = hintLevel,
                context = context  // Pass context
            )

            // ... rest unchanged (tracking, stats, penalty)

            Result.Success(HintResult(...))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

---

## 5. Data Requirements

### 5.1 Existing Fields Utilization

| Field | Current Usage | New Usage (Level 1) | New Usage (Level 2) |
|-------|--------------|---------------------|---------------------|
| `root` | ✅ Present (50% coverage) | Morphological hints | - |
| `prefix` | ✅ Present (10% coverage) | Morphological hints | - |
| `suffix` | ✅ Present (5% coverage) | Morphological hints | - |
| `exampleSentences` | ✅ Present (100%) | - | Contextual questions |
| `relatedWords` | ✅ Present (100%) | - | Association questions |
| `partOfSpeech` | ✅ Present (100%) | - | Question templates |
| `translation` | ✅ Present (100%) | - | Definition questions |

### 5.2 Data Coverage Analysis

**Look Island (30 words)**:
- Words with `root`: 13/30 (43%)
  - Examples: "glass" (glas), "color" (col), "light" (leuk), "bright" (bher), "observe" (serv), "view" (vid), etc.
- Words with `prefix`: 3/30 (10%)
  - Examples: "observe" (ob), "appear" (ap), "emerge" (e)
- Words with `suffix`: 2/30 (7%)
  - Examples: "witness" (ness), "color" (or)
- Words with `exampleSentences`: 30/30 (100%)
- Words with `relatedWords`: 30/30 (100%)

**Implication**: Level 2 hints have 100% coverage. Level 1 hints have 50% coverage with fallback to first letter.

---

## 6. User Experience Design

### 6.1 Hint Display Flow

```
┌─────────────────────────────────────┐
│         SPELL BATTLE GAME           │
├─────────────────────────────────────┤
│  Translation: 观察                  │
│                                      │
│  [ _  _  _  _  _  _  _ ]           │
│                                      │
│  [ Q W E R T Y U I O P ]           │
│  [  A S D F G H J K L ]            │
│  [    Z X C V B N M ]              │
│                                      │
│  💡 HINT  (2/3 remaining)           │
└─────────────────────────────────────┘

User taps 💡 HINT
        ↓
┌─────────────────────────────────────┐
│         💡 LEVEL 1 HINT             │
├─────────────────────────────────────┤
│  这个词包含词根 'serv' (服务/看)    │
│  和前缀 'ob' (向)                   │
│                                      │
│  [ 下一提示  ]  [ 我知道了  ]      │
└─────────────────────────────────────┘

User taps "下一提示"
        ↓
┌─────────────────────────────────────┐
│         💡 LEVEL 2 HINT             │
├─────────────────────────────────────┤
│  填空: _____ the animals.           │
│  (观察这些动物。)                   │
│                                      │
│  [ 下一提示  ]  [ 我知道了  ]      │
└─────────────────────────────────────┘

User taps "下一提示"
        ↓
┌─────────────────────────────────────┐
│         💡 LEVEL 3 HINT             │
├─────────────────────────────────────┤
│  完整单词（元音隐藏）: _bs_rv_      │
│                                      │
│  [ 我知道了  ]                      │
└─────────────────────────────────────┘
```

### 6.2 UI States

**Before Hint**:
- Button shows: "💡 提示"
- Subtitle shows: "(3 remaining)"

**After Level 1 Hint**:
- Button shows: "💡 提示 ①"
- Subtitle shows: "(2 remaining)"

**After Level 2 Hint**:
- Button shows: "💡 提示 ②"
- Subtitle shows: "(1 remaining)"

**After Level 3 Hint**:
- Button shows: "💡 提示 ③"
- Subtitle shows: "(0 remaining)"
- Button disabled (grayed out)

### 6.3 Accessibility Considerations

**Visual Design**:
- High contrast text for hints
- Larger font size for Level 1 (morphological)
- Bullet points for multi-part hints

**Cognitive Load**:
- One concept per hint level
- Progressive disclosure (never overwhelm)
- Clear hierarchy: Level 1 → Level 2 → Level 3

---

## 7. Scoring and Penalties

### 7.1 Existing Penalty System (Maintained)

From `UseHintUseCaseEnhanced.kt`:
```kotlin
data class HintResult(
    val shouldApplyPenalty: Boolean = true  // Always true
)
```

**Penalty Rules**:
- Use 1 hint: -1 star (max 2★ instead of 3★)
- Use 2 hints: -1 star (max 2★ instead of 3★)
- Use 3 hints: -2 stars (max 1★ instead of 3★)
- Memory strength growth: 50% reduction

### 7.2 Rationale for Socratic Penalties

Even though Socratic hints require more active thinking, penalties are maintained because:

1. **Fairness**: All hints (including Socratic) provide assistance
2. **Motivation**: Penalties encourage attempting without hints first
3. **Progress tracking**: Accurate measure of independent recall

---

## 8. Testing Strategy

### 8.1 Unit Tests

**MorphologicalClueGeneratorTest** (24 tests):
- ✅ Hint with root only
- ✅ Hint with prefix only
- ✅ Hint with suffix only
- ✅ Hint with root + prefix
- ✅ Hint with root + suffix
- ✅ Hint with prefix + root + suffix
- ✅ Fallback when no morphology data
- ✅ Explanation lookup for known prefixes/roots/suffixes
- ✅ Special characters in morphology (hyphens, etc.)

**SocraticQuestionGeneratorTest** (32 tests):
- ✅ Contextual question from example sentence
- ✅ Association question from related words
- ✅ Definition question for verbs
- ✅ Definition question for nouns
- ✅ Definition question for adjectives
- ✅ Fallback when no examples/related words
- ✅ Special characters in sentences
- ✅ Sentence blank replacement (case insensitive)

**HintGeneratorSocraticTest** (16 tests):
- ✅ Integration: Word object → Level 1 hint
- ✅ Integration: Word object → Level 2 hint
- ✅ Integration: Word object → Level 3 hint
- ✅ Fallback chain: Level 1 no data → Level 2 → Level 3

### 8.2 Integration Tests

**UseHintUseCaseEnhancedIntegrationTest** (8 tests):
- ✅ Full flow: User ID + Word ID → HintResult
- ✅ Tracking record created
- ✅ Penalty flag set correctly
- ✅ Hint limit enforced

### 8.3 Manual Testing Scenarios

**Scenario 1: Word with full morphology**
```
Word: "observe" (root: "serv", prefix: "ob", examples: ["Observe the animals"])
Level 1 → "这个词包含词根 'serv' (服务/看) 和前缀 'ob' (向)"
Level 2 → "填空: _____ the animals. (观察这些动物。)"
Level 3 → "_bs_rv_"
```

**Scenario 2: Word without morphology**
```
Word: "look" (no root/prefix/suffix, examples: ["Look at the blackboard"])
Level 1 → "首字母: L" (fallback)
Level 2 → "填空: _____ at the blackboard. (看黑板。)"
Level 3 → "l__k"
```

**Scenario 3: Short word (≤3 letters)**
```
Word: "eye" (3 letters)
Level 1 → Skip to Level 2
Level 2 → "哪个名词表示 '眼睛'?"
Level 3 → "_y_"
```

---

## 9. Migration Path

### 9.1 Phase 1: Backend Implementation (Week 1)

**Tasks**:
1. Create `MorphologicalClueGenerator.kt`
2. Create `SocraticQuestionGenerator.kt`
3. Add `generateSocraticHint()` to `HintGenerator`
4. Update `UseHintUseCaseEnhanced` to use new generators
5. Write unit tests (72 tests total)

**Acceptance Criteria**:
- All unit tests pass
- Code review approved by android-architect

### 9.2 Phase 2: Data Enhancement (Week 2)

**Tasks**:
1. Audit `root/prefix/suffix` coverage across all words
2. Add missing morphology data for high-frequency words
3. Validate `exampleSentences` JSON format
4. Validate `relatedWords` JSON format

**Target Coverage**:
- `root`: 60%+ (currently 43%)
- `prefix`: 15%+ (currently 10%)
- `suffix`: 10%+ (currently 7%)
- `exampleSentences`: 100% (maintained)
- `relatedWords`: 100% (maintained)

### 9.3 Phase 3: UI Integration (Week 3)

**Tasks**:
1. Update `HintCard` component to show multi-level hints
2. Add hint level indicator (①②③)
3. Add "Next Hint" button for progressive disclosure
4. Update `LearningViewModel` to handle new hint flow
5. Manual testing on device

**Acceptance Criteria**:
- All 3 hint levels display correctly
- Hint limits enforced (max 3 per word)
- Penalties applied correctly
- No crashes or visual bugs

### 9.4 Phase 4: Analytics and Optimization (Week 4)

**Tasks**:
1. Track hint usage patterns (which levels most used)
2. Measure learning effectiveness (post-hint correctness)
3. A/B test: Socratic vs. letter hints
4. Refine question templates based on data

**Success Metrics**:
- Hint usage rate: <30% (indicates appropriate difficulty)
- Post-hint correctness: >80% (indicates hints are effective)
- Student satisfaction: >4.0/5.0 (survey)

---

## 10. Future Enhancements

### 10.1 Adaptive Socratic Hints

**Idea**: Adjust hint strategy based on user behavior

```kotlin
class AdaptiveSocraticStrategy {
    fun selectHintStrategy(
        userId: String,
        wordId: String,
        userHistory: UserHistory
    ): HintStrategy {
        return when {
            userHistory.prefersContextual -> ContextualQuestion
            userHistory.prefersMorphology -> MorphologyFocus
            userHistory.strugglesWithSpelling -> SpellingAssist
            else -> BalancedSocratic
        }
    }
}
```

### 10.2 Multilingual Socratic Questions

**Idea**: Support hints in multiple languages

```
Level 2 Hint (English): "Complete this sentence: 'I _____ TV.'"
Level 2 Hint (Chinese): "填空: '我_____电视。'"
Level 2 Hint (Spanish): "Completa: 'Yo _____ televisión.'"
```

### 10.3 Voice-Activated Socratic Hints

**Idea**: Use TTS for auditory learners

```
User taps hint → TTS speaks:
"Think about it: What verb means 'to watch carefully'?
This word has the root 'serv' meaning 'to keep or watch'."
```

---

## 11. References

### 11.1 Educational Research

1. **Socratic Method**:
   - Paul, R., & Elder, L. (2007). *The Art of Socratic Questioning*. Foundation for Critical Thinking.

2. **Morphological Instruction**:
   - Bowers, P. N., & Kirby, J. R. (2010). "Effects of morphological instruction on vocabulary acquisition." *Reading and Writing*, 23(5), 515-537.

3. **Active Recall**:
   - Roediger, H. L., & Karpicke, J. D. (2006). "Test-enhanced learning." *Psychological Science*, 17(3), 249-255.

### 11.2 KET Vocabulary Research

1. **Cambridge English: Key (KET) Wordlist**:
   - Official vocabulary list for A2 level
   - 1,500 words total
   - Focus on high-frequency, practical vocabulary

2. **Vocabulary Learning Strategies**:
   - Nation, I. S. P. (2001). *Learning Vocabulary in Another Language*. Cambridge University Press.

---

## 12. Appendix: Complete Hint Examples

### Example 1: "observe" (Full morphology)

| Level | Type | Hint Content |
|-------|------|--------------|
| 1 | Morphological | 这个词包含词根 'serv' (服务/看) 和前缀 'ob' (向) |
| 2 | Contextual | 填空: _____ the animals. (观察这些动物。) |
| 3 | Partial | _bs_rv_ (元音隐藏) |

### Example 2: "witness" (Suffix only)

| Level | Type | Hint Content |
|-------|------|--------------|
| 1 | Morphological | 这个词包含词根 'wit' (知道/看见) 和后缀 '-ness' (状态) |
| 2 | Definition | 哪个动词表示 '目击，见证'? |
| 3 | Partial | w_tn_ss (元音隐藏) |

### Example 3: "look" (No morphology)

| Level | Type | Hint Content |
|-------|------|--------------|
| 1 | Fallback | 首字母: L |
| 2 | Contextual | 填空: _____ at the blackboard. (看黑板。) |
| 3 | Partial | l__k (元音隐藏) |

### Example 4: "color" (Root + suffix)

| Level | Type | Hint Content |
|-------|------|--------------|
| 1 | Morphological | 这个词包含词根 'col' (覆盖/色) 和后缀 '-or' (名词后缀) |
| 2 | Definition | 哪个名词表示 '颜色'? |
| 3 | Partial | c_l_r (元音隐藏) |

### Example 5: "visible" (No morphology, but has related words)

| Level | Type | Hint Content |
|-------|------|--------------|
| 1 | Fallback | 首字母: V |
| 2 | Association | 类似于 'clear' 和 'noticeable'，表示 '可见的' |
| 3 | Partial | v_s_bl_ (元音隐藏) |

---

## 13. Coordination with android-architect

### 13.1 Handoff Checklist

**From Education Specialist to android-architect**:

- [x] Educational rationale documented
- [x] Hint architecture designed
- [x] Component interfaces specified
- [x] Data requirements analyzed
- [x] Test scenarios defined
- [ ] Implementation code review (pending)
- [ ] Integration testing (pending)
- [ ] UI/UX validation (pending)

### 13.2 Open Questions for android-architect

1. **Performance**: Will parsing `exampleSentences` JSON on every hint request impact performance? Should we cache parsed objects?

2. **Localization**: Should Socratic questions support multiple languages (English/Chinese) or Chinese only?

3. **Migration**: Should we maintain backward compatibility with the old hint system during rollout, or switch all users to Socratic hints simultaneously?

4. **Data Migration**: How do we handle existing user progress when introducing the new hint system?

---

## 14. Conclusion

The Socratic Hint System represents a **significant pedagogical upgrade** from passive letter reveals to **active, metacognitive learning**. By leveraging existing data fields (`root`, `prefix`, `suffix`, `exampleSentences`, `relatedWords`), we can implement this system with **minimal data changes** while maximizing educational impact.

**Next Step**: Review with android-architect and begin Phase 1 (backend implementation).

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Status**: ✅ Design Complete - Ready for Implementation
**Assigned To**: android-architect (implementation coordination)
