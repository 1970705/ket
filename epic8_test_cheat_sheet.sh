#!/bin/bash
# Epic #8 Task #8.2 - Real Device Testing Cheat Sheet
# Quick reference commands for testing
#
# Updated: 2026-02-26
# Purpose: Quick command reference for Epic #8 testing
#
# Prerequisites:
#   - P0-BUG-010 must be fixed before testing
#   - Run unit tests first: ./gradlew test
#   - All 57 tests should pass

# ========================================
# PRE-TEST SETUP
# ========================================

echo "=========================================="
echo "Epic #8 Real Device Testing - Cheat Sheet"
echo "=========================================="
echo ""

# Check device connection
echo "1. Checking device connection..."
adb devices -l
echo ""

# Build and install (if needed)
echo "2. Build and install (only if needed):"
echo "   ./gradlew clean assembleDebug"
echo "   adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo ""

# Start logcat monitor
echo "3. Start logcat monitor (in separate terminal):"
echo "   ./epic8_test_logcat_monitor.sh"
echo ""

# ========================================
# PER-SCENARIO COMMANDS
# ========================================

echo "=========================================="
echo "SCENARIO EXECUTION COMMANDS"
echo "=========================================="
echo ""

echo "Clear app data and launch (run before EACH scenario):"
echo "  adb shell pm clear com.wordland && sleep 2 && adb shell am start -n com.wordland/.ui.MainActivity"
echo ""

echo "Capture screenshot (after EACH scenario):"
echo "  adb shell screencap -p > docs/screenshots/epic8/scenario[X].png"
echo ""

echo "Save logs (after EACH scenario):"
echo "  adb logcat -d | grep -E 'StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager' > docs/reports/testing/epic8_scenario[X].log"
echo ""

# ========================================
# SCENARIO-SPECIFIC SCREENSHOTS
# ========================================

echo "=========================================="
echo "SCREENSHOT FILENAMES"
echo "=========================================="
echo ""

echo "Scenario 1 (Perfect):"
echo "  docs/screenshots/epic8/scenario1_perfect_3stars.png"
echo ""

echo "Scenario 2 (All Hints):"
echo "  docs/screenshots/epic8/scenario2_hints_2stars.png"
echo ""

echo "Scenario 3 (Mixed):"
echo "  docs/screenshots/epic8/scenario3_mixed_2stars.png"
echo ""

echo "Scenario 4 (Guessing):"
echo "  docs/screenshots/epic8/scenario4_guessing_1star.png"
echo ""

echo "Scenario 5 (High Combo):"
echo "  docs/screenshots/epic8/scenario5_combo_3stars.png"
echo ""

echo "Scenario 6 (Slow):"
echo "  docs/screenshots/epic8/scenario6_slow_3stars.png"
echo ""

echo "Scenario 7 (One Wrong):"
echo "  docs/screenshots/epic8/scenario7_onewrong_2stars.png"
echo ""

echo "Scenario 8 (Multiple Wrong):"
echo "  docs/screenshots/epic8/scenario8_multiplewrong_1star.png"
echo ""

# ========================================
# EXPECTED RESULTS
# ========================================

echo "=========================================="
echo "EXPECTED STAR RATINGS"
echo "=========================================="
echo ""

echo "Scenario 1 (Perfect 6/6, 0 hints, 24s):     ★★★ (score ~3.8, >=2.5 threshold)"
echo "Scenario 2 (All Hints 6/6, 6 hints, 30s):    ★★  (score ~2.5, per-word penalty)"
echo "Scenario 3 (Mixed 4/6, 0 hints, 2 errors):   ★★  (score ~1.8, >=1.5 threshold)"
echo "Scenario 4 (Guessing 6/6, <1.5s/word):      ★   (score ~2.4, guessing penalty)"
echo "Scenario 5 (High Combo 5/6, combo=5):       ★★★ (score ~2.9, combo bonus)"
echo "Scenario 6 (Slow 6/6, 20s/word):            ★★★ (score ~2.8, slow penalty minimal)"
echo "Scenario 7 (One Wrong 5/6, 1 error):        ★★  (score ~2.4, <2.5 threshold)"
echo "Scenario 8 (Multiple Wrong 3/6, 3 errors):  ★   (score ~1.2, >=0.5 threshold)"
echo ""
echo "THRESHOLDS (after P0-BUG-010 fix):"
echo "  3 Stars: score >= 2.5 (was 3.0)"
echo "  2 Stars: score >= 1.5 (was 2.0)"
echo "  1 Star:  score >= 0.5 (was 1.0)"
echo ""

# ========================================
# LEVEL 1 WORDS (LOOK ISLAND)
# ========================================

echo "=========================================="
echo "LEVEL 1 WORDS (LOOK ISLAND)"
echo "=========================================="
echo ""

echo "1. look      (看)"
echo "2. see       (看见)"
echo "3. watch     (观看)"
echo "4. eye       (眼睛)"
echo "5. glass     (玻璃)"
echo "6. find      (寻找)"
echo ""

# ========================================
# NAVIGATION PATH
# ========================================

echo "=========================================="
echo "NAVIGATION PATH"
echo "=========================================="
echo ""

echo "Home → Island Map (岛屿地图) → Look Island (看岛) → Level 1 (关卡 1)"
echo ""

# ========================================
# POST-TEST
# ========================================

echo "=========================================="
echo "POST-TEST COLLECTION"
echo "=========================================="
echo ""

echo "List all screenshots:"
echo "  ls -lh docs/screenshots/epic8/"
echo ""

echo "Save all logs:"
echo "  adb logcat -d | grep -E 'StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager' > docs/reports/testing/epic8_all_scenarios.log"
echo ""

echo "View test report:"
echo "  open docs/reports/testing/EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md"
echo ""

echo "View manual testing guide:"
echo "  open docs/guides/testing/EPIC8_MANUAL_TESTING_GUIDE.md"
echo ""

# ========================================
# QUICK REFERENCE
# ========================================

echo "=========================================="
echo "QUICK REFERENCE CARD"
echo "=========================================="
echo ""

echo "Scenario 1: 6/6 correct, ~4s/word, no hints"
echo "Scenario 2: 6/6 correct, use hint on EVERY word"
echo "Scenario 3: 4 correct, 2 wrong, normal pace"
echo "Scenario 4: 6/6 correct, <1.5s per word (FAST!)"
echo "Scenario 5: 5 correct, 1 wrong, build 5-combo"
echo "Scenario 6: 6/6 correct, wait 20s before each word"
echo "Scenario 7: 5 correct, 1 wrong, normal pace"
echo "Scenario 8: 3 correct, 3 wrong, normal pace"
echo ""

echo "=========================================="
echo "End of Cheat Sheet"
echo "=========================================="
