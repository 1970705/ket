# Star Breakdown Navigation Flow

## Visual Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Learning Screen                             │
│  User completes 6 words → Level Complete State Triggered            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                     Level Complete Screen                            │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  🎉 关卡完成!                                                  │  │
│  │  ⭐⭐⭐ (3 stars)                                               │  │
│  │  完美表现！                                                    │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │         [查看星级详情]  (View Star Details)                   │  │
│  └──────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │         [继续探险]      (Continue Adventure)                  │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ User taps "查看星级详情"
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                   Star Breakdown Screen                              │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  ← 星级详情                                                    │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  ⭐⭐⭐ (3 stars)                                               │  │
│  │  太棒了！继续加油！                                            │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  准确率: 85%  [████████░░]  (Green)                           │  │
│  │  太棒了！准确率很高！                                          │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  提示使用: 2 次  [███████░░]  (Green)                         │  │
│  │  用了一些提示，没关系！                                        │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  用时: 45秒  [████████░░]  (Green)                            │  │
│  │  速度很快，节奏很好！                                          │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  错误次数: 1 次  [███████░░]  (Green)                         │  │
│  │  只有几个错误，做得好！                                        │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  💡 小贴士                                                     │  │
│  │  继续保持这个状态！                                            │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ User taps back arrow (←)
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    Level Select Screen                              │
│  User sees level list with updated star ratings                    │
└─────────────────────────────────────────────────────────────────────┘
```

## Data Flow

```
LearningViewModel
  ↓ (calculates star rating)
LearningUiState.LevelComplete
  ├─ stars: Int (3)
  ├─ score: Int (850)
  ├─ accuracy: Int (85)
  ├─ hintsUsed: Int (2)
  ├─ timeTaken: Int (45)
  ├─ errorCount: Int (1)
  ├─ maxCombo: Int (5)
  ├─ isNextIslandUnlocked: Boolean (false)
  └─ islandMasteryPercentage: Double (66.7)
  ↓
LevelCompleteContent
  ↓ (onViewStarBreakdown callback)
SetupNavGraph
  ↓ (NavRoute.starBreakdown())
StarBreakdownScreen
  ├─ starRating: 3
  ├─ accuracy: 85
  ├─ hintsUsed: 2
  ├─ timeTaken: 45
  └─ errorCount: 1
```

## Navigation Arguments

**Route Pattern**:
```
star_breakdown/{stars}/{accuracy}/{hintsUsed}/{timeTaken}/{errorCount}/{islandId}
```

**Example Route**:
```
star_breakdown/3/85/2/45/1/look_island
```

**Parameter Extraction** (in SetupNavGraph):
```kotlin
val stars = backStackEntry.arguments?.getInt("stars") ?: 0
val accuracy = backStackEntry.arguments?.getInt("accuracy") ?: 0
val hintsUsed = backStackEntry.arguments?.getInt("hintsUsed") ?: 0
val timeTaken = backStackEntry.arguments?.getInt("timeTaken") ?: 0
val errorCount = backStackEntry.arguments?.getInt("errorCount") ?: 0
val islandId = backStackEntry.arguments?.getString("islandId") ?: ""
```

## Button Actions

### Level Complete Screen

| Button | Text | Action | Navigation |
|--------|------|--------|------------|
| Primary | 继续探险 | Return to level select | popBackStack() |
| Secondary | 查看星级详情 | View star breakdown | navigate(STAR_BREAKDOWN) |

### Star Breakdown Screen

| Element | Text | Action | Navigation |
|---------|------|--------|------------|
| Back Button | ← | Return to level select | navigate(LEVEL_SELECT/{islandId}) |

## Color Coding

StarBreakdownScreen uses color coding to indicate performance:

- 🟢 **Green (Tertiary)**: Excellent performance
  - Accuracy ≥ 80%
  - Hints = 0
  - Time < 30s
  - Errors = 0

- 🟡 **Yellow (Secondary)**: Good performance
  - Accuracy ≥ 60%
  - Hints ≤ 2
  - Time < 60s
  - Errors ≤ 2

- 🔴 **Red (Error)**: Needs improvement
  - Accuracy < 60%
  - Hints > 2
  - Time ≥ 60s
  - Errors > 2

## User Experience Flow

1. **Complete Level**: User finishes all 6 words in LearningScreen
2. **Level Complete**: Screen shows stars, score, and two buttons
3. **View Details**: User taps "查看星级详情" to see breakdown
4. **Analyze Performance**: StarBreakdownScreen shows detailed metrics with:
   - Animated progress bars
   - Color-coded performance indicators
   - Child-friendly encouraging messages
   - Tips for improvement
5. **Return**: User taps back arrow to return to LevelSelectScreen
6. **Continue**: User selects next level or replays current level

## Benefits

✅ **Transparency**: Users understand how their star rating was calculated
✅ **Motivation**: Encouraging messages and visual feedback
✅ **Education**: Tips help users improve performance
✅ **Engagement**: Detailed breakdown increases replay value
✅ **Child-Friendly**: Simple language, colorful design, encouraging tone
