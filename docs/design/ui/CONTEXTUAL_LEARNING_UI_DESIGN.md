# Contextual Learning System UI Design Specification

**Document Version**: 1.0
**Created**: 2026-02-17
**Designer**: compose-ui-designer
**Priority**: P0 - Core Learning Experience

---

## 1. Design Overview

### 1.1 Problem Statement
Current learning flow shows isolated word spelling:
```
┌─────────────────┐
│   苹果          │  ← Translation only
├─────────────────┤
│   _ p p _ _    │  ← Spelling area
├─────────────────┤
│  [A][B][C]...   │  ← Keyboard
└─────────────────┘
```

**Issues**:
- No context for the word
- Boring, test-like experience
- Doesn't leverage memory science (contextual encoding)

### 1.2 Solution: Contextual Learning

Add sentence/story context before spelling:
```
┌─────────────────────────────────┐
│  🍎 Context Sentence           │  ← NEW: Context area
│  "I eat an _____"              │
├─────────────────────────────────┤
│   apple                        │  ← Word to spell
│   _ p p _ _                    │  ← Spacing hints
├─────────────────────────────────┤
│  [A][B][C]...                  │  ← Keyboard
└─────────────────────────────────┘
```

---

## 2. Three Learning Modes

### Mode 1: Sentence Mode (Level 1-3)

**Target**: Beginners, foundational vocabulary

**UI Layout**:
```
┌──────────────────────────────────────┐
│  📝 Sentence Practice               │  ← Header
├──────────────────────────────────────┤
│                                       │
│  ┌─────────────────────────────────┐ │
│  │     [Image Area: 200x150]      │ │  ← Context image
│  │         🍎 Apple illustration   │ │
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │  "I eat an _____ for lunch."    │ │  ← Sentence with blank
│  │         ↑                       │ │
│  │      [target]                   │ │  ← Tap to hear audio
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │     Spell: apple               │ │  ← Word hint
│  │     _ _ _ _ _                  │ │  ← Letter count
│  └─────────────────────────────────┘ │
│                                       │
│  [🎵 Audio] [💡 Hint] [⏭️ Skip]     │  ← Actions
│                                       │
│  ┌─────────────────────────────────┐ │
│  │  Q W E R T Y U I O P            │ │
│  │  A S D F G H J K L             │ │  ← Keyboard
│  │  Z X C V B N M ⌫              │ │
│  └─────────────────────────────────┘ │
└──────────────────────────────────────┘
```

**Interaction Flow**:
1. Show sentence with image
2. Highlight blank space
3. User taps blank → hears word pronunciation
4. User spells word
5. On correct: Show complete sentence with celebration

**Data Structure**:
```kotlin
data class SentenceExercise(
    val sentence: String,           // "I eat an _____ for lunch."
    val targetWord: String,         // "apple"
    val blankPosition: Int,         // Index of blank in sentence
    val imageRes: ImageResource,    // Apple illustration
    val audioRes: AudioResource?,   // Optional pronunciation
    val difficulty: Int             // 1-3
)
```

---

### Mode 2: Story Mode (Level 4-6)

**Target**: Intermediate, connected vocabulary

**UI Layout**:
```
┌──────────────────────────────────────┐
│  📖 Story Adventure: "Morning Routine"│
├──────────────────────────────────────┤
│  Scene 1 of 3                         │
│  ████████░░░░░░░░░░░                  │  ← Progress bar
├──────────────────────────────────────┤
│                                       │
│  ┌─────────────────────────────────┐ │
│  │   [Scene Image: 300x200]       │ │  ← Illustrated scene
│  │     🛏️🧑‍🦰🪥                      │ │
│  │   Bathroom, morning light      │ │
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │  🌅 "Good morning!"             │ │  ← Story text
│  │                                 │ │
│  │  "I brush my teeth with a _____" │  ← Context sentence
│  │                                  │ │
│  │  💡 Hint: Something you use... │ │  ← Context hint
│  └─────────────────────────────────┘ │
│                                       │
│  Target: toothbrush                   │
│  _ _ _ _ _ _ _ _ _                  │  ← Letter count (10)
│                                       │
│  [Submit] [🎵 Listen] [💡 Hint]      │
└──────────────────────────────────────┘
```

**Story Progression**:
```
Scene 1 → Scene 2 → Scene 3 → Complete
   ↓           ↓           ↓
toothbrush   breakfast   bus
(起床刷牙)   (吃早餐)     (坐公交)
```

**On Complete**:
```
┌──────────────────────────────────────┐
│           🎉 Story Complete!         │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │  "Good morning! I brush my       │ │  ← Full story
│  │   teeth with a toothbrush. Then  │ │
│  │   I eat apple for breakfast.     │ │
│  │   Finally, I take the bus to     │ │
│  │   school."                        │ │
│  └─────────────────────────────────┘ │
│                                       │
│  Words learned: 3                    │
│  ⭐⭐⭐ Stars earned                  │
│                                       │
│  [🔄 Replay] [➡️ Next Story]         │
└──────────────────────────────────────┘
```

**Data Structure**:
```kotlin
data class StoryExercise(
    val id: String,
    val title: String,              // "Morning Routine"
    val scenes: List<StoryScene>,
    val totalWords: Int
)

data class StoryScene(
    val sceneNumber: Int,
    val title: String,              // "Waking Up"
    val imageRes: ImageResource,
    val narrativeText: String,      // Story context
    val targetSentence: String,     // Sentence with blank
    val targetWord: String,
    val hint: String,               // Context hint
    val audioRes: AudioResource?
)
```

---

### Mode 3: Dialogue Mode (Advanced - Future)

**Target**: Advanced learners, conversational skills

**UI Layout**:
```
┌──────────────────────────────────────┐
│  💬 Conversation Practice            │
├──────────────────────────────────────┤
│                                       │
│     ┌─────────────────────┐          │
│     │  👋 New Friend      │          │  ← NPC
│     └─────────────────────┘          │
│                                       │
│  NPC: "Hello! What's your name?"     │
│                                       │
│  You: "My name is _____"              │  ← User input
│                                       │
│  ┌─────────────────────────────────┐ │
│  │  Spell: Tom                     │ │
│  │  _ _ _                          │ │
│  └─────────────────────────────────┘ │
│                                       │
│  [🎵 Listen to NPC]                  │
└──────────────────────────────────────┘
```

---

## 3. Shared UI Components

### 3.1 Context Header

```kotlin
@Composable
fun ContextHeader(
    mode: LearningMode,
    title: String,
    progress: Int?, // For story mode
    modifier: Modifier = Modifier
)
```

**Visual**:
- Sentence Mode: 📝 icon + "Sentence Practice"
- Story Mode: 📖 icon + story title + progress
- Dialogue Mode: 💬 icon + "Conversation"

### 3.2 Image Viewer

```kotlin
@Composable
fun ContextImageViewer(
    imageRes: ImageResource,
    description: String,
    modifier: Modifier = Modifier
)
```

**Spec**:
- Size: Flexible, max 300dp width, 200dp height
- Corner radius: 12dp
- Background: PrimaryContainer
- Alt text: Always available for accessibility

### 3.3 Sentence Display

```kotlin
@Composable
fun SentenceDisplay(
    sentence: String,
    blankPosition: Int,
    onTap: () -> Unit,
    showAudio: Boolean = true,
    modifier: Modifier = Modifier
)
```

**Features**:
- Blank space highlighted (underline or background)
- Tap to play audio
- Optional audio icon

### 3.4 Progress Indicator (Story Mode)

```kotlin
@Composable
fun StoryProgressIndicator(
    currentScene: Int,
    totalScenes: Int,
    modifier: Modifier = Modifier
)
```

**Visual**:
```
Scene 2 of 3
████████░░░░░░░░░░░
```

---

## 4. Interaction Design

### 4.1 Hint System

**Level 1 Hint**: Show first letter
```
"I eat an _____"
     ↓
"I eat an a______"
```

**Level 2 Hint**: Show half the word
```
"I eat an _____"
     ↓
"I eat an app___"
```

**Level 3 Hint**: Show all consonants
```
"I eat an _____"
     ↓
"I eat an _ppl_"
```

### 4.2 Audio Support

**Audio elements**:
1. **Context audio**: Full sentence pronunciation
2. **Word audio**: Target word pronunciation
3. **Letter audio**: Individual letter sounds (optional)

**UI controls**:
- 🎵 button in context area
- Auto-play on first load (optional)

### 4.3 Completion Flow

**Correct Answer**:
```
1. Green checkmark animation
2. Complete sentence appears:
   "I eat an apple for lunch." ✅
3. Word highlights in context
4. Confetti/celebration
5. "Next" button appears
```

**Incorrect Answer**:
```
1. Gentle shake animation
2. Hint suggestion: "Not quite. Try again!"
3. After 3 attempts: Show first letter hint
```

---

## 5. Visual Design Specifications

### 5.1 Color Scheme

| Element | Light Mode | Dark Mode |
|---------|------------|-----------|
| Context Card | PrimaryContainer (85%) | PrimaryContainer (85%) |
| Sentence Text | OnSurface (87%) | OnSurface (87%) |
| Blank Space | Primary + underline | Primary + underline |
| Correct Answer | Primary (Green) | Primary (Green) |
| Incorrect Error | Error (Red) | Error (Red) |

### 5.2 Typography

| Element | Style | Size |
|---------|-------|------|
| Story Title | HeadlineMedium | 24sp |
| Narrative | BodyLarge | 18sp |
| Sentence | TitleMedium | 20sp |
| Word Hint | LabelLarge | 16sp |

### 5.3 Spacing

```kotlin
val ContextPadding = 16.dp
val ImageToTextSpacing = 12.dp
val TextToKeyboardSpacing = 24.dp
val CardCornerRadius = 16.dp
```

---

## 6. Screen Transitions

### 6.1 Sentence → Complete

```
LearningScreen                    CompletionCard
     │                                   │
     │  [On Correct]                    │
     ├──────────────────────────────────→│
     │                                   │
     │  Sentence card shrinks            │  Full sentence
     │  Complete sentence expands        │  Celebration
     │  Stars appear                     │  Next button
```

### 6.2 Story Scene Progression

```
Scene 1                          Scene 2
    │                                  │
    │  [On Complete]                   │
    ├──────────────────────────────────→│
    │                                  │
    │  "Scene Complete!"               │  New scene loads
    │  [Continue] → [Next Scene]       │  Image slides in
    │                                  │  Progress updates
```

---

## 7. Component Hierarchy

```
ContextualLearningScreen
├── ContextHeader
│   ├── ModeIcon
│   ├── Title
│   └── StoryProgressIndicator (story mode only)
├── ContextCard
│   ├── ContextImageViewer
│   ├── SentenceDisplay
│   │   ├── TextSegment
│   │   ├── BlankSpace (tappable)
│   │   └── AudioButton
│   └── HintArea
├── WordSpellingArea
│   ├── LetterBoxes
│   └── LetterCount
├── ActionButtons
│   ├── HintButton
│   ├── AudioButton
│   └── SkipButton
└── VirtualKeyboard
```

---

## 8. Technical Implementation Notes

### 8.1 State Management

```kotlin
sealed class ContextualLearningState {
    object Loading : ContextualLearningState()

    data class SentenceReady(
        val exercise: SentenceExercise,
        val userSpelling: String,
        val hintLevel: Int
    ) : ContextualLearningState()

    data class StoryReady(
        val story: StoryExercise,
        val currentScene: Int,
        val userSpelling: String,
        val hintLevel: Int
    ) : ContextualLearningState()

    data class Completed(
        val fullSentence: String,
        val stars: Int
    ) : ContextualLearningState()

    data class Error(
        val message: String
    ) : ContextualLearningState()
}
```

### 8.2 Performance

- Use `remember` for image loading
- Lazy load story scenes
- Preload next scene while current is active
- Cache audio resources

### 8.3 Accessibility

- Screen reader support for all text
- Audio alternative for visual content
- High contrast mode support
- Font scaling support

---

## 9. Content Examples

### Sentence Mode Examples

| Level | Sentence | Target Word | Image |
|-------|----------|-------------|-------|
| 1 | "I see a _____ in the sky." | bird | 🐦 Bluebird illustration |
| 1 | "The _____ is red and sweet." | apple | 🍎 Apple |
| 2 | "She wears a _____ on her head." | hat | 👒 Hat |
| 2 | "I read a _____ before sleeping." | book | 📚 Book |
| 3 | "The _____ is shining brightly." | sun | ☀️ Sun |

### Story Mode Example

**Title**: "Morning Routine"
**Target Age**: 8-10 years

```
Scene 1: Waking Up
Narrative: "The sun rises. A new day begins!"
Sentence: "I brush my teeth with a _____"
Target: toothbrush
Hint: "Something you use to clean teeth"

Scene 2: Breakfast Time
Narrative: "Time for a healthy breakfast!"
Sentence: "I eat a _____ and drink milk"
Target: banana
Hint: "A long yellow fruit"

Scene 3: Going to School
Narrative: "Ready for learning!"
Sentence: "I take the _____ to school"
Target: bus
Hint: "A large vehicle that carries people"
```

---

## 10. Success Metrics

- Completion rate increase (target: +30%)
- Time spent per session (target: 5-10 min)
- Hint usage decrease over time (better retention)
- User feedback: "More fun/interesting"

---

## 11. Implementation Roadmap

### Phase 1 (Week 1-2): Sentence Mode
- Basic sentence display
- Image viewer component
- Sentence completion flow
- Audio integration (optional)

### Phase 2 (Week 3-4): Story Mode
- Story container component
- Scene progression
- Progress tracking
- Story completion celebration

### Phase 3 (Week 5+): Polish & Expand
- More sentence/story content
- Advanced animations
- Dialogue mode (if needed)

---

**Status**: Ready for Review
**Awaiting**: Education specialist input on content design
