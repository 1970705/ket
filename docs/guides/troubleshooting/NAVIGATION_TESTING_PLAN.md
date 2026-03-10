# Wordland MVP - Navigation Testing Plan

## Purpose
Comprehensive testing of navigation flows for all 7 islands

## Test Environment Setup

### Prerequisites
- [ ] Android Studio project synced and built
- [ ] Device or emulator running Android 8.0+
- [ ] App installed on test device
- [ ] Developer options enabled (if available)
- [ ] ADB (Android Debug Bridge) ready

### Recommended Test Devices
1. **Emulator**: Pixel 5 API 33 (Android 13)
2. **Physical Device**: Any Android 8-13 device
3. **Tablet**: If available (10" recommended)

---

## Test Scenarios

### 1. Basic Navigation Flow (All Islands)

#### Scenario 1.1: Home → Island Map
**Steps**:
1. Launch app from home screen
2. Tap "Islands" button
3. Verify Island Map screen opens
4. Press back button
5. Verify return to Home screen

**Expected Results**:
- Island Map displays all 7 islands
- All islands show correct colors:
  - Look Island: Green
  - Move Valley: Blue
  - Say Mountain: Purple
  - Feel Garden: Orange
  - Think Forest: Teal
  - Make Lake: Cyan
  - Go Volcano: Red
- Only Look Island shows as unlocked (60% mastery not met)
- Other islands show as locked (locked icon)
- Mastery percentages show 0% (fresh install)
- Back button returns to Home

**Checklist**:
- [ ] All 7 islands visible
- [ ] Correct theme colors
- [ ] Unlock/lock status correct
- [ ] Mastery percentages display
- [ ] Back navigation works

---

### 2. Level Selection Flow (Each Island)

#### Scenario 2.1: Look Island Level Select
**Steps**:
1. From Island Map, tap Look Island
2. Verify Level Select screen opens
3. Verify 10 levels displayed
4. Verify Level 1 shows as UNLOCKED
5. Verify Levels 2-10 show as LOCKED
6. Tap Level 1
7. Verify Learning screen opens
8. Complete back navigation: Learning → Level Select → Island Map

**Expected Results**:
- 10 level cards displayed (6 words each)
- Level 1 has "UNLOCKED" badge
- Levels 2-10 have "LOCKED" badge
- Green theme throughout
- Level numbers: 01, 02, 03... 10
- Tap on Level 1 navigates to learning

**Checklist**:
- [ ] 10 levels displayed
- [ ] Correct unlock/lock badges
- [ ] Level 1 tap navigates correctly
- [ ] Green theme applied
- [ ] Back navigation flow works

#### Scenario 2.2: Move Valley Level Select
**Steps**:
1. From Island Map, tap Move Valley
2. Verify Level Select screen opens
3. Verify 10 levels displayed
4. Verify all levels show as LOCKED (Look Island not 60%)
5. Verify navigation works correctly

**Expected Results**:
- 10 level cards displayed
- All levels show LOCKED (island locked)
- Blue theme throughout
- No levels selectable (locked state)

**Checklist**:
- [ ] 10 levels displayed
- [ ] All levels locked
- [ ] Blue theme applied
- [ ] Cannot navigate to learning (locked)

#### Scenario 2.3: Say Mountain Level Select
**Expected Results**:
- Purple theme throughout
- All levels locked (island locked)

#### Scenario 2.4: Feel Garden Level Select
**Expected Results**:
- Orange theme throughout
- All levels locked

#### Scenario 2.5: Think Forest Level Select
**Expected Results**:
- Teal theme throughout
- All levels locked

#### Scenario 2.6: Make Lake Level Select
**Expected Results**:
- Cyan theme throughout
- All levels locked

#### Scenario 2.7: Go Volcano Level Select
**Expected Results**:
- Red theme throughout
- All levels locked

---

### 3. Learning Screen Navigation (All Islands)

#### Scenario 3.1: Look Island Learning
**Steps**:
1. Navigate: Home → Island Map → Look Island → Level 1 → Learning
2. Verify Learning screen loads with Level 1 words (6 words)
3. Verify word displays with:
   - Word text
   - Chinese translation
   - Pronunciation
   - Part of speech
   - Example sentence
4. Tap a word
5. Verify word detail view appears
6. Tap back button
7. Verify return to Level Select

**Expected Results**:
- 6 words loaded for Level 1
- Green-themed UI elements
- Word cards display correctly
- Example sentences in JSON format
- Audio placeholder or player visible
- Back navigation returns to Level Select

**Checklist**:
- [ ] 6 words loaded
- [ ] Word metadata displays correctly
- [ ] Word detail view works
- [ ] Back navigation works
- [ ] Green theme consistent

#### Scenario 3.2: Move Valley Learning
**Expected Results**:
- Blue-themed UI
- 6 words loaded per level
- Move Valley vocabulary words

#### Scenario 3.3: Say Mountain Learning
**Expected Results**:
- Purple-themed UI
- Speaking/communication vocabulary

#### Scenario 3.4: Feel Garden Learning
**Expected Results**:
- Orange-themed UI
- Emotion/sensory vocabulary

#### Scenario 3.5: Think Forest Learning
**Expected Results**:
- Teal-themed UI
- Cognitive/learning vocabulary

#### Scenario 3.6: Make Lake Learning
**Expected Results**:
- Cyan-themed UI
- Creation/making vocabulary

#### Scenario 3.7: Go Volcano Learning
**Expected Results**:
- Red-themed UI
- Action/energy vocabulary

---

### 4. Deep Linking (All Islands)

#### Scenario 4.1: Deep Link to Look Island Level 1
**Steps**:
1. Generate deep link: `wordland://learning/look_island_level_01/look_island`
2. Tap link from external app (email, browser, etc.)
3. Verify app opens to Look Island Level 1

**Expected Results**:
- App opens to correct learning screen
- 6 words from Level 1 loaded
- Back navigation works

**Checklist**:
- [ ] Deep link opens app
- [ ] Correct island and level loaded
- [ ] Navigation functional

#### Scenario 4.2: Deep Link to Move Valley Level 5
**Expected Results**:
- Opens Move Valley Level 5
- All words loaded correctly

#### Scenario 4.3: Deep Link to Say Mountain Level 3
**Expected Results**:
- Opens Say Mountain Level 3
- Purple theme applied

#### Scenario 4.4: Deep Link to Feel Garden Level 7
**Expected Results**:
- Opens Feel Garden Level 7
- Orange theme applied

#### Scenario 4.5: Deep Link to Think Forest Level 10
**Expected Results**:
- Opens Think Forest Level 10 (final level)
- Teal theme applied

#### Scenario 4.6: Deep Link to Make Lake Level 4
**Expected Results**:
- Opens Make Lake Level 4
- Cyan theme applied

#### Scenario 4.7: Deep Link to Go Volcano Level 2
**Expected Results**:
- Opens Go Volcano Level 2
- Red theme applied

---

### 5. Island Progression (Unlock Chain)

#### Scenario 5.1: Complete Look Island Level 10
**Steps**:
1. Play through all 10 levels of Look Island
2. Master enough words to reach 60%
3. Return to Island Map
4. Verify Move Valley now shows as unlocked
5. Navigate to Move Valley Level Select

**Expected Results**:
- Move Valley unlock achievement notification
- Move Valley card shows "UNLOCKED" badge
- Can navigate to Move Valley Level 1

**Checklist**:
- [ ] 60% mastery unlocks Move Valley
- [ ] Achievement notification displayed
- [ ] Badge status updates
- [ ] Navigation to unlocked island works

#### Scenario 5.2: Progressive Unlock Through All Islands
**Steps**:
1. Complete Look Island → Unlocks Move Valley
2. Complete Move Valley → Unlocks Say Mountain
3. Complete Say Mountain → Unlocks Feel Garden
4. Complete Feel Garden → Unlocks Think Forest
5. Complete Think Forest → Unlocks Make Lake
6. Complete Make Lake → Unlocks Go Volcano

**Expected Results**:
- Islands unlock progressively as mastery increases
- Final island (Go Volcano) accessible after 6 islands
- Each island requires 60% mastery of previous island

**Checklist**:
- [ ] All islands unlock progressively
- [ ] Unlock order correct
- [ ] 60% threshold works
- [ ] Final island accessible

---

### 6. State Persistence

#### Scenario 6.1: App Restart After Progress
**Steps**:
1. Complete Level 1 of Look Island
2. Exit app completely (swipe away from recent apps)
3. Relaunch app
4. Navigate to Island Map
5. Verify progress saved

**Expected Results**:
- Look Island shows 6/60 (10%) mastery
- Level 2 shows as UNLOCKED
- Other islands remain locked

**Checklist**:
- [ ] Progress persists across app restart
- [ ] Mastery percentage saves
- [ ] Level unlock status saves
- [ ] User progress data persists

#### Scenario 6.2: Island Map State Refresh
**Steps**:
1. Navigate to Island Map
2. Pull to refresh (if implemented)
3. Verify mastery percentages update

**Expected Results**:
- Latest mastery percentages displayed
- Island states update correctly

**Checklist**:
- [ ] Pull-to-refresh works
- [ ] Mastery updates reflect current data
- [ ] No data loss on refresh

---

### 7. Navigation Edge Cases

#### Scenario 7.1: Rapid Navigation
**Steps**:
1. Home → Island Map
2. Tap Look Island
3. Tap Level 1 (immediately)
4. Immediately tap back
5. Tap Move Valley
6. Tap Level 1
7. Tap back
8. Tap Say Mountain
9. Rapid tap between islands

**Expected Results**:
- No crashes on rapid navigation
- No duplicate screens in back stack
- Smooth transitions between screens
- All interactions responsive

**Checklist**:
- [ ] No crashes on rapid navigation
- [ ] Back stack manages correctly
- [ ] Transitions smooth (60fps)
- [ ] Memory stable

#### Scenario 7.2: System Back Button
**Steps**:
1. Navigate to: Home → Island Map → Level 1 → Learning
2. Press system back button
3. Verify return to Level Select
4. Press system back again
5. Verify return to Island Map
6. Press system back again
7. Verify return to Home

**Expected Results**:
- System back navigates up correctly
- Final system back exits app (optional)
- No navigation stuck in infinite loop

**Checklist**:
- [ ] System back works through navigation stack
- [ ] Exits app on final back
- [ ] No navigation loops

#### Scenario 7.3: Configuration Changes (Rotation)
**Steps**:
1. Open Level 1 in portrait
2. Rotate device to landscape
3. Verify UI adapts
4. Rotate back to portrait
5. Verify state preserved

**Expected Results**:
- UI reflows for orientation
- Word cards rearrange correctly
- Progress state maintained
- No crashes on rotation

**Checklist**:
- [ ] Portrait mode displays correctly
- [ ] Landscape mode adapts
- [ ] State preserved on rotation
- [ ] No layout breakage

---

### 8. Performance Testing

#### Scenario 8.1: Memory Usage
**Steps**:
1. Open Android Studio Profiler
2. Navigate through all 7 islands
3. Check memory allocation
4. Verify no memory leaks

**Expected Results**:
- Memory usage < 150MB during navigation
- No memory leaks detected
- Garbage collection runs regularly

**Checklist**:
- [ ] Memory within acceptable range
- [ ] No unbounded growth
- [ ] GC runs without stutters
- [ ] LeakCanary reports no leaks

#### Scenario 8.2: Navigation Speed
**Measurement**:
- Time from Home → Island Map: <500ms
- Time from Island Map → Level Select: <300ms
- Time from Level Select → Learning: <500ms

**Expected Results**:
- Navigation feels instantaneous
- No noticeable lag
- Smooth frame rate (60fps)

**Checklist**:
- [ ] Navigation within time targets
- [ ] Animations smooth (60fps)
- [ ] No frame drops
- [ ] UI responsive

---

### 9. ADB Commands for Testing

#### Build and Install
```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Install on emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Clear Data for Fresh Testing
```bash
# Clear app data
adb shell pm clear com.wordland
adb shell rm -rf /data/data/com.wordland/databases/*

# Relaunch app
adb shell am start -n com.wordland/.ui.MainActivity
```

#### View Logs
```bash
# Real-time logcat
adb logcat | grep -E "wordland|AndroidRuntime|Navigation"

# Save logs to file
adb logcat > navigation_test.txt
```

#### Screen Record
```bash
# Record navigation flow (requires API 21+)
adb shell screenrecord /sdcard/navigation.mp4

# Pull recording from device
adb pull /sdcard/navigation.mp4
```

---

## Test Execution Checklist

### Pre-Test Setup
- [ ] Build successful
- [ ] APK installed
- [ ] App launches successfully
- [ ] Fresh install (clear data)

### All Islands Navigation
- [ ] Look Island navigation tested
- [ ] Move Valley navigation tested
- [ ] Say Mountain navigation tested
- [ ] Feel Garden navigation tested
- [ ] Think Forest navigation tested
- [ ] Make Lake navigation tested
- [ ] Go Volcano navigation tested

### Level Selection
- [ ] All 7 islands level select tested
- [ ] Unlock/lock status correct
- [ ] Level navigation works

### Learning Screen
- [ ] All 7 islands learning tested
- [ ] Word loading works
- [ ] Back navigation works
- [ ] Theme colors correct

### Deep Linking
- [ ] Deep links to all islands tested
- [ ] Correct screen opens on link
- [ ] Parameters parse correctly

### Performance
- [ ] Memory usage acceptable
- [ ] Navigation speed within targets
- [ ] No crashes or ANRs
- [ ] No memory leaks

### Orientation
- [ ] Portrait mode works
- [ ] Landscape mode works
- [ ] Rotation preserves state

### Edge Cases
- [ ] Rapid navigation stable
- [ ] System back works
- [ ] No navigation loops

---

## Bug Report Template

If you find issues, document them:

### Bug Report Format
```
**Bug ID**: NAV-001
**Title**: [Brief description]
**Severity**: Critical / Major / Minor
**Island**: [Which island affected]
**Steps to Reproduce**:
1. Step one
2. Step two
3. etc.

**Expected Behavior**: [What should happen]
**Actual Behavior**: [What actually happens]
**Frequency**: [Always / Sometimes / Intermittent]
**Device**: [Device/emulator used]
**Android Version**: [OS version]
**App Version**: [Build number]

**Screenshot**: [Path to screenshot]
**Logcat**: [Relevant logcat output]
**Priority**: [P1 (Fix Immediately) / P2 / P3 / P4]
```

---

## Success Criteria

### Navigation Testing Complete When:
- [x] All 7 islands accessible from Island Map
- [x] All 7 islands show correct theme colors
- [x] Level Select works for all islands
- [x] Learning screen loads for all islands
- [x] Back navigation works at all levels
- [x] Deep linking works to all islands
- [x] No crashes or ANRs
- [x] Performance acceptable
- [x] Orientation changes handled

### Sign-Off Criteria
**Ready for Next Phase**: All above checked and passing

---

## Next Steps After Testing

1. **Fix Bugs**: Address any issues found during testing
2. **Optimize**: Improve performance based on profiling
3. **Final Polish**: UI/UX improvements based on test findings
4. **Asset Procurement**: Begin recording audio and creating images
5. **TTS Fallback**: Implement text-to-speech for missing audio

---

## Notes

- Test on both emulator and physical device
- Test on at least 2 different Android versions
- Test in both portrait and landscape orientations
- Use actual device testing for best validation
