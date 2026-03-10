# Wordland Android Architecture Refactoring Plan

## Context

### Problem Statement
The Wordland Android project has architectural violations that deviate from Clean Architecture principles:

1. **Misplaced Business Logic**: Core algorithms (`GuessingDetector`, `MemoryStrengthAlgorithm`) reside in root package instead of Domain Layer
2. **Constant Confusion**: `AppConstants` contains both infrastructure and business constants, causing Domain Models to depend on Core layer
3. **Empty Directories**: Several empty directories clutter the structure (`repository/`, `usecase/`, `model/`)
4. **Missing UseCase Layer**: ViewModels (6) have no corresponding UseCases, violating separation of concerns

### Why This Change
- **Align with Clean Architecture**: Ensure clear layer boundaries and dependency inversion
- **Improve Testability**: Business logic should be independent of Android framework
- **Enhance Maintainability**: Clear structure makes onboarding easier and reduces bugs
- **Support Workflow Integration**: Our skills (`android-architect`, `android-engineer`) assume proper structure

### Intended Outcome
A project structure that strictly follows Clean Architecture with:
- Clear layer separation (UI → Domain → Data)
- Business logic in Domain Layer (algorithms, use cases, constants)
- No dependency violations (Domain never depends on Core/Data)
- Empty directories removed
- Foundation for implementing UseCase layer

---

## Current Architecture Issues

### Critical Files

1. **`app/src/main/java/com/wordland/algorithm/`** ❌ WRONG LAYER
   - `GuessingDetector.kt` - Business logic algorithm (173 lines)
   - `MemoryStrengthAlgorithm.kt` - Business logic algorithm
   - **Dependency**: Used by `TrackingRepository.kt` (line 3-4)

2. **`app/src/main/java/com/wordland/core/constants/AppConstants.kt`** ⚠️ MIXED CONCERNS
   - Contains infrastructure constants (DB name, prefs)
   - **ALSO** contains business constants (mastery thresholds, memory strength)
   - **Dependencies**: Used by 3 files:
     - `IslandMasteryRepository.kt` (Data Layer) ✅
     - `IslandMastery.kt` (Domain Model) ❌ VIOLATION
     - `UserWordProgress.kt` (Domain Model) ❌ VIOLATION

3. **Empty Directories** (remove all):
   - `/repository/` (empty)
   - `/usecase/` (empty)
   - `/model/` (empty)
   - `/data/local/database/` (empty)
   - `/data/local/preferences/` (empty)

### Layer Distribution
```
UI Layer:     29 files ✅
Domain Layer:   8 files  ⚠️  (missing UseCases)
Data Layer:    24 files ✅
```

---

## Refactoring Plan

### Phase 1: Remove Empty Directories (Safe, 5 min)

**Objective**: Clean up clutter

**Actions**:
```bash
# Remove empty directories
git rm -r app/src/main/java/com/wordland/repository/
git rm -r app/src/main/java/com/wordland/usecase/
git rm -r app/src/main/java/com/wordland/model/
git rm -r app/src/main/java/com/wordland/data/local/database/
git rm -r app/src/main/java/com/wordland/data/local/preferences/
git rm -r app/src/main/java/com/wordland/data/local/  # becomes empty after above
```

**Verification**:
```bash
./gradlew assembleDebug
./gradlew test
```

**Risk**: None (only removing empty dirs)

---

### Phase 2: Move Algorithm to Domain (Medium, 15 min)

**Objective**: Business logic belongs in Domain Layer

**Step 2.1**: Create `domain/algorithm/` directory
```bash
mkdir -p app/src/main/java/com/wordland/domain/algorithm
```

**Step 2.2**: Move files using `git mv` (preserves history)
```bash
git mv app/src/main/java/com/wordland/algorithm/GuessingDetector.kt \
        app/src/main/java/com/wordland/domain/algorithm/GuessingDetector.kt

git mv app/src/main/java/com/wordland/algorithm/MemoryStrengthAlgorithm.kt \
        app/src/main/java/com/wordland/domain/algorithm/MemoryStrengthAlgorithm.kt
```

**Step 2.3**: Update package declarations
```kotlin
// GuessingDetector.kt - Line 1
- package com.wordland.algorithm
+ package com.wordland.domain.algorithm

// MemoryStrengthAlgorithm.kt - Line 1
- package com.wordland.algorithm
+ package com.wordland.domain.algorithm
```

**Step 2.4**: Update imports in dependent files

**File**: `app/src/main/java/com/wordland/data/repository/TrackingRepository.kt`
```kotlin
// Lines 3-4 - Update imports
- import com.wordland.algorithm.GuessingDetector
- import com.wordland.algorithm.GuessingDetector.ResponsePattern
+ import com.wordland.domain.algorithm.GuessingDetector
+ import com.wordland.domain.algorithm.GuessingDetector.ResponsePattern
```

**Step 2.5**: Remove old algorithm directory
```bash
git rm -r app/src/main/java/com/wordland/algorithm/
```

**Verification**:
```bash
./gradlew assembleDebug

# Verify no old imports remain
grep -r "com.wordland.algorithm" app/src/main/java/
# Should return: (no results)

# Verify new location
grep -r "com.wordland.domain.algorithm" app/src/main/java/
# Should return: TrackingRepository.kt
```

**Risk**: Medium (affects 1 file)
**Mitigation**: Use `git mv`, compile after each step

---

### Phase 3: Split Constants by Layer (High, 20 min)

**Objective**: Separate infrastructure from business constants

#### Step 3.1: Create DomainConstants

**File**: `app/src/main/java/com/wordland/domain/constants/DomainConstants.kt` (NEW)

```kotlin
package com.wordland.domain.constants

/**
 * Domain-layer constants for business logic
 * Used by domain models and use cases
 */
object DomainConstants {

    // Difficulty Settings
    const val DIFFICULTY_EASY = 1
    const val DIFFICULTY_NORMAL = 2
    const val DIFFICULTY_HARD = 3

    // Mastery Thresholds (Domain business rules)
    const val MASTERY_PERCENTAGE_TO_UNLOCK_NEXT = 60.0
    const val MASTERY_PERCENTAGE_TO_UNLOCK_NEXT_ISLAND = 60.0
    const val MASTERY_PERCENTAGE_COMPLETE = 100.0

    // Memory Strength (Domain algorithm parameters)
    const val MEMORY_STRENGTH_INITIAL = 10
    const val MEMORY_STRENGTH_MAX = 100
    const val CORRECT_BONUS = 10
    const val INCORRECT_PENALTY = 15

    // Guessing Detection (Domain business rules)
    const val GUESSING_THRESHOLD_FAST = 2000  // 2 seconds
    const val GUESSING_THRESHOLD_VERY_FAST = 1500  // 1.5 seconds

    // Cross Scene Validation (Domain business rules)
    const val CROSS_SCENE_CORRECT_RATE_THRESHOLD = 0.7  // 70%

    // Energy System (Domain business rules)
    const val ENERGY_MAX = 100
    const val ENERGY_RECOVERY_RATE_MINUTES = 1.5
    const val ENERGY_PER_QUESTION = 5

    // Time Limits (Domain business rules)
    const val SESSION_DURATION_THRESHOLD_GUESSING = 2000  // 2 seconds

    // Progress (Domain business rules)
    const val LEVEL_STARS_TO_UNLOCK_NEXT = 3

    // Content (Domain business rules)
    const val WORDS_PER_ISLAND_MVP = 60
    const val LEVELS_PER_ISLAND_MVP = 8
}
```

#### Step 3.2: Refactor AppConstants

**File**: `app/src/main/java/com/wordland/core/constants/AppConstants.kt`

```kotlin
package com.wordland.core.constants

/**
 * Application-wide infrastructure constants
 * Used by Data layer for technical configuration
 */
object AppConstants {

    // Database
    const val DATABASE_NAME = "wordland_database"
    const val DATABASE_VERSION = 1

    // Shared Preferences
    const val PREFS_NAME = "wordland_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_PARENT_MODE = "parent_mode"
    const val KEY_FIRST_LAUNCH = "first_launch"

    // Note: Business logic constants moved to DomainConstants
    // import com.wordland.domain.constants.DomainConstants
}
```

#### Step 3.3: Update Domain Models

**File**: `app/src/main/java/com/wordland/domain/model/IslandMastery.kt`
```kotlin
// Line 6 - Update import
- import com.wordland.core.constants.AppConstants
+ import com.wordland.domain.constants.DomainConstants

// Update references (line 61+)
- AppConstants.MASTERY_PERCENTAGE_TO_UNLOCK_NEXT_ISLAND
+ DomainConstants.MASTERY_PERCENTAGE_TO_UNLOCK_NEXT_ISLAND
```

**File**: `app/src/main/java/com/wordland/domain/model/UserWordProgress.kt`
```kotlin
// Line 6 - Update import
- import com.wordland.core.constants.AppConstants
+ import com.wordland.domain.constants.DomainConstants

// Update references
- AppConstants.MEMORY_STRENGTH_INITIAL
+ DomainConstants.MEMORY_STRENGTH_INITIAL

- AppConstants.MASTERY_PERCENTAGE_COMPLETE
+ DomainConstants.MASTERY_PERCENTAGE_COMPLETE

- AppConstants.CROSS_SCENE_CORRECT_RATE_THRESHOLD
+ DomainConstants.CROSS_SCENE_CORRECT_RATE_THRESHOLD
```

**File**: `app/src/main/java/com/wordland/data/repository/IslandMasteryRepository.kt`
```kotlin
// Keep both imports if needed
import com.wordland.core.constants.AppConstants
+ import com.wordland.domain.constants.DomainConstants

// Update business logic constant
- AppConstants.MASTERY_PERCENTAGE_TO_UNLOCK_NEXT_ISLAND
+ DomainConstants.MASTERY_PERCENTAGE_TO_UNLOCK_NEXT_ISLAND
```

**Verification**:
```bash
./gradlew assembleDebug

# Verify no domain imports from core
grep -r "import com.wordland.core" app/src/main/java/com/wordland/domain/
# Should return: (no results)

# Verify domain constants usage
grep -r "DomainConstants\." app/src/main/java/com/wordland/domain/
# Should return: IslandMastery.kt, UserWordProgress.kt

grep -r "AppConstants\." app/src/main/java/com/wordland/data/
# Should return: IslandMasteryRepository.kt (for DB constants only)
```

**Risk**: High (updating 4+ files)
**Mitigation**: Create constants first, update one file at a time, compile after each

---

## Target Architecture

### Final Structure
```
com.wordland/
├── core/
│   └── constants/
│       └── AppConstants.kt           # Infrastructure constants only
├── data/
│   ├── assets/
│   ├── converter/
│   ├── dao/
│   ├── database/
│   ├── repository/                    # Data Layer implementations
│   └── seed/
├── domain/
│   ├── algorithm/                    # ✅ Business logic algorithms
│   │   ├── GuessingDetector.kt
│   │   └── MemoryStrengthAlgorithm.kt
│   ├── constants/                    # ✅ Domain constants
│   │   └── DomainConstants.kt
│   ├── model/
│   │   ├── Word.kt
│   │   ├── UserWordProgress.kt
│   │   ├── IslandMastery.kt
│   │   ├── LevelProgress.kt
│   │   └── BehaviorTracking.kt
│   └── usecase/
│       ├── LearnWordResult.kt
│       └── ReviewWordItem.kt
├── navigation/
├── ui/
│   ├── components/
│   ├── screens/
│   ├── theme/
│   ├── uistate/
│   └── viewmodel/
└── di/
```

### Dependency Flow (Clean Architecture)
```
UI Layer (ViewModels)
    ↓ depends on
Domain Layer (Models, Algorithms, Constants)
    ↓ depends on
Data Layer (Repositories, DAOs)
    ↓ depends on
Core Layer (Infrastructure Constants)
```

---

## Critical Files to Modify

### High Priority
1. **`app/src/main/java/com/wordland/domain/constants/DomainConstants.kt`** (CREATE)
   - All business logic constants

2. **`app/src/main/java/com/wordland/core/constants/AppConstants.kt`**
   - Remove business constants

3. **`app/src/main/java/com/wordland/domain/model/IslandMastery.kt`**
   - Update imports to DomainConstants

4. **`app/src/main/java/com/wordland/domain/model/UserWordProgress.kt`**
   - Update imports to DomainConstants

5. **`app/src/main/java/com/wordland/data/repository/IslandMasteryRepository.kt`**
   - Update imports to DomainConstants

6. **`app/src/main/java/com/wordland/data/repository/TrackingRepository.kt`**
   - Update imports to domain.algorithm

### Medium Priority
7. **`app/src/main/java/com/wordland/domain/algorithm/GuessingDetector.kt`** (MOVE)
   - Update package declaration

8. **`app/src/main/java/com/wordland/domain/algorithm/MemoryStrengthAlgorithm.kt`** (MOVE)
   - Update package declaration

---

## Implementation Steps

### Step 1: Backup Current State
```bash
git checkout -b backup-before-architecture-refactor
git commit -am "Snapshot before architecture refactor"
```

### Step 2: Execute Phase 1 (Empty Directories)
```bash
# Remove empty directories
git rm -r app/src/main/java/com/wordland/repository/
git rm -r app/src/main/java/com/wordland/usecase/
git rm -r app/src/main/java/com/wordland/model/
git rm -r app/src/main/java/com/wordland/data/local/

# Verify
./gradlew clean assembleDebug

# Commit if successful
git commit -m "Phase 1: Remove empty directories"
```

### Step 3: Execute Phase 2 (Algorithm Migration)
```bash
# Create domain/algorithm
mkdir -p app/src/main/java/com/wordland/domain/algorithm

# Move files
git mv app/src/main/java/com/wordland/algorithm/GuessingDetector.kt \
        app/src/main/java/com/wordland/domain/algorithm/

git mv app/src/main/java/com/wordland/algorithm/MemoryStrengthAlgorithm.kt \
        app/src/main/java/com/wordland/domain/algorithm/

# Update package declarations (manual edit)
# Update TrackingRepository.kt imports (manual edit)

# Verify
./gradlew clean assembleDebug

# Commit if successful
git commit -m "Phase 2: Move algorithms to domain layer"
```

### Step 4: Execute Phase 3 (Constants Split)
```bash
# Create DomainConstants (manual file creation)

# Update AppConstants (remove business constants)

# Update domain models (change imports)
# - IslandMastery.kt
# - UserWordProgress.kt

# Update repository (change imports)
# - IslandMasteryRepository.kt

# Verify
./gradlew clean assembleDebug

# Commit if successful
git commit -m "Phase 3: Split constants by layer"
```

### Step 5: Final Verification
```bash
# Run all tests
./gradlew test

# Run lint
./gradlew lint

# Verify structure
find app/src/main/java/com/wordland -type d | sort
find app/src/main/java/com/wordland -name "*.kt" | wc -l

# Verify no violations
grep -r "import com.wordland.algorithm" app/src/main/java/ || echo "✅ No old algorithm imports"
grep -r "import com.wordland.core" app/src/main/java/com/wordland/domain/ || echo "✅ No domain imports from core"
```

---

## Testing & Verification

### Manual Testing
1. Launch app on device/emulator
2. Navigate through all screens
3. Verify no crashes
4. Verify learning functionality works
5. Verify progress tracking works

### Automated Testing
```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint
```

### Compilation Verification
After each phase:
```bash
./gradlew clean
./gradlew assembleDebug
./gradlew test
```

---

## Rollback Strategy

If any phase fails:
```bash
# Identify the commit before failed phase
git log --oneline

# Revert to that commit
git reset --hard <commit-id>

# Or revert specific files
git checkout HEAD~1 -- app/src/main/java/com/wordland/
```

---

## Success Criteria

### Phase 1 Success
- [ ] All empty directories removed
- [ ] Project compiles
- [ ] No broken imports

### Phase 2 Success
- [ ] Algorithms in `domain/algorithm/`
- [ ] All imports updated
- [ ] No references to old package
- [ ] All tests pass

### Phase 3 Success
- [ ] Domain models use DomainConstants
- [ ] No domain imports from core
- [ ] AppConstants contains only infrastructure constants
- [ ] No circular dependencies

### Overall Success
- [ ] Clean Architecture compliance
- [ ] Clear dependency direction
- [ ] All tests pass
- [ ] Git history preserved
- [ ] No data loss
- [ ] All features working

---

## Estimated Timeline

| Phase | Duration | Priority |
|-------|----------|----------|
| Phase 1: Empty Directories | 5 min | P0 |
| Phase 2: Algorithm Migration | 15 min | P0 |
| Phase 3: Constants Split | 20 min | P0 |
| **Total** | **40 min** | |

---

## Post-Refactoring Notes

### Next Steps (Future Work)
1. Implement UseCase layer (android-engineer skill)
2. Write unit tests for algorithms (android-test-engineer skill)
3. Update android-architect.md skill documentation with new structure
4. Consider performance profiling (android-performance-expert skill)

### Lessons Learned
- Empty directories cause confusion → Remove immediately
- Business logic in wrong layer → Move early before dependencies grow
- Mixed concerns in constants → Split by layer upfront
- Use `git mv` to preserve history during refactoring
