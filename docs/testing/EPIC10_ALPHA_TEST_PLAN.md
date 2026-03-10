# Epic #10 Alpha Test Plan

**Document Version**: 1.0
**Created**: 2026-02-25
**Author**: android-test-engineer-4
**Epic**: #10 - Onboarding Experience
**Target**: Alpha Release (Week 2)
**Based on**: `docs/design/system/ONBOARDING_ALPHA_ARCHITECTURE.md`

---

## Executive Summary

### Alpha Scope

**User Flow**: Welcome → Pet Selection → Tutorial (5 words) → First Chest → Completed

**Test Coverage Goals**:
- Domain Layer: >80% code coverage
- UseCases: 100% coverage
- ViewModel: >70% coverage
- UI Screens: Manual testing checklist
- Integration: End-to-end flow validation

### Test Timeline

| Week | Focus | Deliverables |
|------|-------|--------------|
| Week 1 | Unit tests for Domain/UseCases | 13+ unit tests |
| Week 2 | Integration + Real device testing | E2E test + validation report |

---

## Part 1: Domain Layer Tests

### 1.1 TutorialWordConfig Tests

**File**: `app/src/test/java/com/wordland/domain/model/TutorialWordConfigTest.kt`

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `calculatePreFillCount_3LetterWord` | "cat" with 35% ratio | Returns 1 (35% of 3 = 1.05 → 1) |
| `calculatePreFillCount_5LetterWord` | "apple" with 35% ratio | Returns 2 (35% of 5 = 1.75 → 2) |
| `calculatePreFillCount_minPreFill` | "a" with minPreFill=1 | Returns 1 (min enforced) |
| `generatePreFilledIndices_count` | Generated indices count | Matches calculatePreFillCount() |
| `generatePreFilledIndices_unique` | No duplicate indices | All indices are unique |
| `generatePreFilledIndices_range` | Indices within word bounds | 0 <= index < word.length |

**Implementation Skeleton**:
```kotlin
class TutorialWordConfigTest {
    @Test
    fun `calculatePreFillCount for 3-letter word returns 1`() {
        val config = TutorialWordConfig(word = "cat", translation = "猫")
        assertEquals(1, config.calculatePreFillCount())
    }

    @Test
    fun `calculatePreFillCount for 5-letter word returns 2`() {
        val config = TutorialWordConfig(word = "apple", translation = "苹果")
        assertEquals(2, config.calculatePreFillCount())
    }

    @Test
    fun `calculatePreFillCount enforces minimum`() {
        val config = TutorialWordConfig(
            word = "a",
            translation = "A",
            minPreFillLetters = 1
        )
        assertEquals(1, config.calculatePreFillCount())
    }

    @Test
    fun `generatePreFilledIndices returns correct count`() {
        val config = TutorialWordConfig(word = "hello", translation = "你好")
        val indices = config.generatePreFilledIndices()
        assertEquals(2, indices.size) // 35% of 5 = 1.75 → 2
    }

    @Test
    fun `generatePreFilledIndices are unique`() {
        val config = TutorialWordConfig(word = "hello", translation = "你好")
        val indices = config.generatePreFilledIndices()
        assertEquals(indices.size, indices.toSet().size)
    }

    @Test
    fun `generatePreFilledIndices are within bounds`() {
        val config = TutorialWordConfig(word = "hello", translation = "你好")
        val indices = config.generatePreFilledIndices()
        assertTrue(indices.all { it in 0 until 5 })
    }
}
```

### 1.2 OnboardingPhase Tests

**File**: `app/src/test/java/com/wordland/domain/model/OnboardingPhaseTest.kt`

| Test Case | Description |
|-----------|-------------|
| Phase enum has all required values | WELCOME, PET_SELECTION, TUTORIAL, FIRST_CHEST, COMPLETED, NOT_STARTED |
| Phase enum values are ordered | NOT_STARTED < WELCOME < ... < COMPLETED |

### 1.3 PetType Tests

**File**: `app/src/test/java/com/wordland/domain/model/PetTypeTest.kt`

| Test Case | Description |
|-----------|-------------|
| All 4 pets are defined | DOLPHIN, CAT, DOG, FOX |
| Pet entries are accessible | PetType.entries.size == 4 |

### 1.4 ChestReward Tests

**File**: `app/src/test/java/com/wordland/domain/model/ChestRewardTest.kt`

| Test Case | Description |
|-----------|-------------|
| PetEmoji has correct rarity | Returns COMMON |
| CelebrationEffect has correct rarity | Returns RARE |
| RewardRarity has 3 values | COMMON, RARE, EPIC |

---

## Part 2: UseCase Tests

### 2.1 StartOnboardingUseCase Tests

**File**: `app/src/test/java/com/wordland/domain/usecase/StartOnboardingUseCaseTest.kt`

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| First launch creates initial state | No existing state | Creates with WELCOME phase, userId="user_001" |
| Existing state is preserved | State exists | Returns existing state unchanged |
| State is persisted | After first launch | Repository.saveOnboardingState() called |

**Mock Setup**:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class StartOnboardingUseCaseTest {
    private lateinit var useCase: StartOnboardingUseCase
    private lateinit var repository: OnboardingRepository

    @Before
    fun setup() {
        repository = mockk()
        useCase = StartOnboardingUseCase(repository)
    }

    @Test
    fun `invoke creates initial state when none exists`() = runTest {
        // Given
        coEvery { repository.getOnboardingState() } returns null
        val initialStateSlot = slot<OnboardingState>()
        coEvery { repository.saveOnboardingState(capture(initialStateSlot)) } just Runs

        // When
        val result = useCase()

        // Then
        assertEquals(OnboardingPhase.WELCOME, result.currentPhase)
        assertEquals("user_001", result.userId)
        assertNull(result.selectedPet)
        assertTrue(initialStateSlot.isCaptured)
    }

    @Test
    fun `invoke returns existing state`() = runTest {
        // Given
        val existingState = OnboardingState(
            userId = "user_001",
            currentPhase = OnboardingPhase.PET_SELECTION,
            selectedPet = null
        )
        coEvery { repository.getOnboardingState() } returns existingState

        // When
        val result = useCase()

        // Then
        assertEquals(existingState, result)
        coVerify(exactly = 0) { repository.saveOnboardingState(any()) }
    }
}
```

### 2.2 SelectPetUseCase Tests

**File**: `app/src/test/java/com/wordland/domain/usecase/SelectPetUseCaseTest.kt`

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| Select pet updates state | Select DOLPHIN | selectedPet=DOLPHIN, phase=TUTORIAL |
| Throws when no state exists | Repository returns null | Throws IllegalStateException |
| State is persisted | After selection | Repository.saveOnboardingState() called |
| updatedAt is updated | After selection | updatedAt > initial value |

### 2.3 CompleteTutorialWordUseCase Tests

**File**: `app/src/test/java/com/wordland/domain/usecase/CompleteTutorialWordUseCaseTest.kt`

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| Increments word count | Complete 1 word | count increases by 1 |
| Adds stars correctly | Earn 3 stars | totalStars increases by 3 |
| Transitions to FIRST_CHEST at 5 words | Complete 5th word | phase = FIRST_CHEST |
| Stays in TUTORIAL before 5 words | Complete 3rd word | phase = TUTORIAL |
| Throws when no state exists | Repository returns null | Throws IllegalStateException |

### 2.4 OpenFirstChestUseCase Tests

**File**: `app/src/test/java/com/wordland/domain/usecase/OpenFirstChestUseCaseTest.kt`

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| Returns PetEmoji reward | Random 0-50% | Returns ChestReward.PetEmoji |
| Returns CelebrationEffect reward | Random 51-80% | Returns ChestReward.CelebrationEffect |
| Returns rare PetEmoji | Random 81-100% | Returns PetEmoji with ✨ |
| Updates state to COMPLETED | After opening | phase = COMPLETED |
| Updates lastOpenedChest | After opening | timestamp > 0 |

### 2.5 GetTutorialWordsUseCase Tests

**File**: `app/src/test/java/com/wordland/domain/usecase/GetTutorialWordsUseCaseTest.kt`

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| Returns 5 words | Repository has words | List size = 5 |
| Uses default config | No custom config | All use 35% pre-fill, 3 hints |
| Maps Word to TutorialWordConfig | Correct mapping | english→word, chinese→translation |

---

## Part 3: ViewModel Tests

### 3.1 OnboardingViewModel Tests

**File**: `app/src/test/java/com/wordland/ui/viewmodel/OnboardingViewModelTest.kt`

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| startOnboarding sets Welcome state | First launch | uiState = Welcome |
| selectPet loads tutorial words | After pet selection | uiState = Tutorial with question |
| Tutorial shows correct progress | On 3rd word | currentWordIndex=2, progress=0.6 |
| submitAnswer advances to next word | Correct answer | word count +1 |
| submitAnswer opens chest at word 5 | Complete 5th word | uiState = OpeningChest |
| Error handling | Repository throws | uiState = Error |
| Incorrect answer gives 1 star | Wrong answer | stars=1 |
| Correct answer gives 3 stars | Right answer | stars=3 |

**Test Skeleton**:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var startOnboarding: StartOnboardingUseCase
    private lateinit var selectPet: SelectPetUseCase
    // ... other usecases

    @Before
    fun setup() {
        // Mock all usecases
        startOnboarding = mockk()
        selectPet = mockk()
        // ...

        viewModel = OnboardingViewModel(
            startOnboarding, selectPet, // ...
        )
    }

    @Test
    fun `startOnboarding sets Welcome state`() = runTest {
        // Given
        val state = OnboardingState(
            userId = "user_001",
            currentPhase = OnboardingPhase.WELCOME,
            selectedPet = null
        )
        coEvery { startOnboarding() } returns state

        // When
        viewModel.startOnboarding()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is OnboardingUiState.Welcome)
    }

    @Test
    fun `selectPet loads tutorial words`() = runTest {
        // Given
        val petSelectedState = OnboardingState(
            userId = "user_001",
            currentPhase = OnboardingPhase.TUTORIAL,
            selectedPet = PetType.DOLPHIN,
            completedTutorialWords = 0
        )
        coEvery { selectPet(any()) } returns petSelectedState
        coEvery { getTutorialWords() } returns listOf(
            TutorialWordConfig("cat", "猫")
        )

        // When
        viewModel.selectPet(PetType.DOLPHIN)
        advanceUntilIdle()

        // Then
        val tutorialState = viewModel.uiState.value
        assertTrue(tutorialState is OnboardingUiState.Tutorial)
    }

    @Test
    fun `submitAnswer with correct answer gives 3 stars`() = runTest {
        // Setup tutorial state
        // ...

        // When
        viewModel.submitAnswer("cat")
        advanceUntilIdle()

        // Then - verify completeTutorialWord called with stars=3
        coVerify { completeTutorialWord(3) }
    }
}
```

---

## Part 4: UI Manual Testing Checklist

### 4.1 Welcome Screen

**File**: `app/src/androidTest/java/com/wordland/ui/screens/OnboardingWelcomeScreenTest.kt`

| Scenario | Steps | Expected Result |
|----------|-------|-----------------|
| Welcome screen displays | Launch app on fresh install | See "Welcome to Wordland" message |
| Start button works | Tap "开始探险" | Navigate to Pet Selection |
| No crashes on rotation | Rotate device | Screen adapts correctly |

### 4.2 Pet Selection Screen

| Scenario | Steps | Expected Result |
|----------|-------|-----------------|
| All 4 pets displayed | Navigate to screen | See 🐬🐱🐶🦊 options |
| Pet selection works | Tap DOLPHIN | Pet highlights, confirmation appears |
| Navigation after selection | Confirm selection | Navigate to Tutorial |
| Selection persists | Restart app after selection | Resume at Tutorial |

### 4.3 Tutorial Screen

| Scenario | Steps | Expected Result |
|----------|-------|-----------------|
| Question displays | Enter Tutorial | See Chinese translation + pre-filled letters |
| Pre-fill visible | Check question | ~35% of letters already filled |
| Hint counter shows | Check UI | "💡 提示 (3/3)" |
| Progress bar updates | After each word | Progress = words_completed / 5 |
| Success animation | Submit correct answer | Stars awarded, celebration shown |
| Navigate to chest | Complete 5th word | Open chest screen appears |

### 4.4 Chest Screen

| Scenario | Steps | Expected Result |
|----------|-------|-----------------|
| Chest appears | Navigate to chest | See mystery box |
| Open animation | Tap to open | Reveal reward |
| Reward is valid | Check reward | PetEmoji or CelebrationEffect |
| Completion screen | After chest | Summary with pet, words, stars |

---

## Part 5: Integration Tests

### 5.1 End-to-End Flow Test

**File**: `app/src/androidTest/java/com/wordland/integration/OnboardingE2ETest.kt`

```kotlin
@Test
fun testCompleteOnboardingAlphaFlow() {
    // 1. Fresh install - start onboarding
    launchActivity()
    val scenario = Scenario(startOnboardingUseCase)

    // Verify Welcome screen
    onView(withId(R.id.welcome_title)).check(matches(isDisplayed()))
    onView(withId(R.id.start_button)).perform(click())

    // 2. Select pet
    onView(withId(R.id.pet_dolphin)).perform(click())
    onView(withId(R.id.confirm_pet)).perform(click())

    // 3. Complete tutorial (5 words)
    repeat(5) {
        // Type correct answer
        onView(withId(R.id.answer_input)).perform(typeText("cat"))
        onView(withId(R.id.submit_button)).perform(click())
        // Verify star animation
        onView(withId(R.id.star_container)).check(matches(isDisplayed()))
    }

    // 4. Open chest
    onView(withId(R.id.chest_container)).check(matches(isDisplayed()))
    onView(withId(R.id.open_chest_button)).perform(click())

    // 5. Verify completion
    onView(withId(R.id.completion_summary)).check(matches(isDisplayed()))
    onView(withText("5")).check(matches(isDisplayed())) // words learned
}
```

### 5.2 Persistence Test

```kotlin
@Test
fun testOnboardingPersistsAfterRestart() {
    // 1. Start onboarding and select pet
    // ...
    val selectedPet = PetType.DOLPHIN

    // 2. Restart app
    scenario.recreate()

    // 3. Verify state restored
    val state = repository.getOnboardingState()
    assertEquals(selectedPet, state?.selectedPet)
    assertEquals(OnboardingPhase.TUTORIAL, state?.currentPhase)
}
```

---

## Part 6: Real Device Validation Checklist

### Pre-Test Setup

- [ ] Fresh app install (clear data first)
- [ ] Device: Xiaomi 24031PN0DC (5369b23a)
- [ ] Logcat monitoring started
- [ ] Screenshots directory prepared

### Test Scenarios

| ID | Scenario | Steps | Pass/Fail | Notes |
|----|----------|-------|-----------|-------|
| D1 | Fresh install flow | Install → Launch | ⬜ | |
| D2 | Welcome screen renders | Check visuals | ⬜ | |
| D3 | Start button navigation | Tap → Verify next screen | ⬜ | |
| D4 | Pet selection - DOLPHIN | Select → Confirm | ⬜ | |
| D5 | Pet selection - CAT | Select → Confirm | ⬜ | |
| D6 | Pet selection - DOG | Select → Confirm | ⬜ | |
| D7 | Pet selection - FOX | Select → Confirm | ⬜ | |
| D8 | Tutorial word 1 | Complete with correct answer | ⬜ | |
| D9 | Tutorial word 2 | Complete with correct answer | ⬜ | |
| D10 | Tutorial word 3 | Complete with correct answer | ⬜ | |
| D11 | Tutorial word 4 | Complete with correct answer | ⬜ | |
| D12 | Tutorial word 5 | Complete → Trigger chest | ⬜ | |
| D13 | Chest opens | Tap → See reward | ⬜ | |
| D14 | Completion screen | Verify summary | ⬜ | |
| D15 | Persist across restart | Close → Reopen | ⬜ | |
| D16 | Resume from TUTORIAL | After 2 words completed | ⬜ | |
| D17 | Hint counter decrements | Use hint 3 times | ⬜ | |
| D18 | Pre-fill is visible | Check ~35% filled | ⬜ | |
| D19 | Progress bar accurate | After each word | ⬜ | |
| D20 | No crashes | Full flow | ⬜ | |

---

## Part 7: Test Execution Schedule

### Week 1 (Parallel with Development)

| Day | Focus | Tasks |
|-----|-------|-------|
| Day 1 | Domain model tests | TutorialWordConfigTest, PetTypeTest |
| Day 2 | UseCase tests (part 1) | StartOnboardingUseCaseTest, SelectPetUseCaseTest |
| Day 3 | UseCase tests (part 2) | CompleteTutorialWordUseCaseTest, OpenFirstChestUseCaseTest |
| Day 4 | ViewModel tests | OnboardingViewModelTest |
| Day 5 | Integration prep | Set up test infrastructure |

### Week 2 (After Implementation)

| Day | Focus | Tasks |
|-----|-------|-------|
| Day 6 | Integration tests | E2E flow test, persistence test |
| Day 7 | UI component tests | Compose UI tests for each screen |
| Day 8 | Real device testing | Run full D1-D20 checklist |
| Day 9 | Bug regression | Re-test failed scenarios |
| Day 10 | Final validation | Sign-off for Alpha |

---

## Part 8: Test Artifacts

### Test Files Structure

```
app/src/test/java/com/wordland/
├── domain/
│   ├── model/
│   │   ├── OnboardingPhaseTest.kt
│   │   ├── PetTypeTest.kt
│   │   ├── TutorialWordConfigTest.kt
│   │   └── ChestRewardTest.kt
│   └── usecase/
│       ├── StartOnboardingUseCaseTest.kt
│       ├── SelectPetUseCaseTest.kt
│       ├── CompleteTutorialWordUseCaseTest.kt
│       ├── OpenFirstChestUseCaseTest.kt
│       └── GetTutorialWordsUseCaseTest.kt
└── ui/
    └── viewmodel/
        └── OnboardingViewModelTest.kt

app/src/androidTest/java/com/wordland/
├── integration/
│   └── OnboardingE2ETest.kt
└── ui/
    └── screens/
        ├── OnboardingWelcomeScreenTest.kt
        ├── OnboardingPetSelectionScreenTest.kt
        ├── OnboardingTutorialScreenTest.kt
        └── OnboardingChestScreenTest.kt
```

### Documentation

- Test plan (this document)
- Test execution report (Week 2)
- Real device validation report (Week 2)
- Bug tracking sheet (as needed)

---

## Part 9: Success Criteria

### Alpha Sign-off Requirements

| Criteria | Target | Measurement |
|----------|--------|-------------|
| Unit test pass rate | 100% | All tests pass |
| Domain layer coverage | >80% | JaCoCo report |
| UseCase coverage | 100% | All branches covered |
| Integration test pass | ✅ | E2E flow completes |
| Real device scenarios | 20/20 pass | D1-D20 checklist |
| No P0 bugs | 0 crashes | Logcat clean |

### Definition of Done

Alpha testing is complete when:
1. All unit tests pass (100%)
2. E2E integration test passes
3. Real device checklist 100% pass
4. No P0 or P1 bugs remain
5. Test report documented and shared

---

## Appendix: Mock Configuration

### Repository Mock Example

```kotlin
fun mockOnboardingRepository(
    initialState: OnboardingState? = null
): OnboardingRepository {
    val repo = mockk<OnboardingRepository>()
    coEvery { repo.getOnboardingState() } returns initialState
    coEvery { repo.saveOnboardingState(any()) } just Runs
    return repo
}
```

### UseCase Mock Example

```kotlin
fun mockStartOnboardingUseCase(
    state: OnboardingState = defaultOnboardingState()
): StartOnboardingUseCase {
    val useCase = mockk<StartOnboardingUseCase>()
    coEvery { useCase() } returns state
    return useCase
}
```

---

**Document Status**: ✅ Ready for implementation
**Next Step**: Begin Week 1 unit testing as Domain layer is implemented
**Contact**: android-test-engineer-4
