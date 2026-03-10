#!/bin/bash
# Real Device Integration Test Script
# Run this after manual APK installation

set -e

echo "========================================"
echo "  Wordland Integration Test Script"
echo "========================================"
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counters
TESTS_PASSED=0
TESTS_FAILED=0

# Helper function
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $2"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}❌ FAIL${NC}: $2"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
}

echo "Step 1: Checking app installation..."
if adb shell pm list packages | grep -q "com.wordland"; then
    echo -e "${GREEN}✓${NC} Wordland is installed"
else
    echo -e "${RED}✗${NC} Wordland is NOT installed"
    echo "Please install the APK manually first:"
    echo "  1. Transfer app/build/outputs/apk/debug/app-debug.apk to device"
    echo "  2. Install via File Manager"
    echo ""
    exit 1
fi
echo ""

echo "Step 2: Launching app..."
adb shell am start -n com.wordland/.ui.MainActivity
sleep 3
echo ""

echo "Step 3: Database Verification"
echo "----------------------------"

# Check if database exists
DB_EXISTS=$(adb shell "run-as com.wordland ls /data/data/com.wordland/databases/wordland.db 2>&1" | grep -c "wordland.db" || true)
print_result $DB_EXISTS "Database file exists"

# Check word count
WORD_COUNT=$(adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland.db 'SELECT COUNT(*) FROM words' 2>/dev/null" || echo "0")
if [ "$WORD_COUNT" -eq 30 ]; then
    print_result 0 "Word count is 30"
else
    print_result 1 "Word count is $WORD_COUNT (expected 30)"
fi

# Check KET words
KET_COUNT=$(adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland.db 'SELECT COUNT(*) FROM words WHERE ket_level=1' 2>/dev/null" || echo "0")
print_result $( [ "$KET_COUNT" -eq 30 ] && echo 0 || echo 1 ) "All 30 words are KET level"

# Check levels
LEVEL_COUNT=$(adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland.db 'SELECT COUNT(DISTINCT level_id) FROM words' 2>/dev/null" || echo "0")
print_result $( [ "$LEVEL_COUNT" -eq 5 ] && echo 0 || echo 1 ) "5 levels exist"

echo ""
echo "Step 4: Level Progress Verification"
echo "-----------------------------------"

# Check Level 1 status
LEVEL1_STATUS=$(adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland.db 'SELECT status FROM level_progress WHERE level_id=\"look_island_level_01\"' 2>/dev/null" || echo "")
print_result $( [ "$LEVEL1_STATUS" = "unlocked" ] && echo 0 || echo 1 ) "Level 1 is unlocked"

# Check Level 2-5 are locked
LOCKED_COUNT=0
for level in level_02 level_03 level_04 level_05; do
    STATUS=$(adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland.db 'SELECT status FROM level_progress WHERE level_id LIKE \"%$level\"' 2>/dev/null" || echo "")
    if [ "$STATUS" = "locked" ]; then
        LOCKED_COUNT=$((LOCKED_COUNT + 1))
    fi
done
print_result $( [ "$LOCKED_COUNT" -eq 4 ] && echo 0 || echo 1 ) "Levels 2-5 are locked ($LOCKED_COUNT/4)"

echo ""
echo "Step 5: Sample Word Data"
echo "------------------------"

# Show first few words
echo "Sample words in database:"
adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland.db 'SELECT word, translation, difficulty FROM words LIMIT 5' 2>/dev/null" | column -t -s '|'

echo ""
echo "Step 6: Logcat Analysis"
echo "-----------------------"

# Check for errors
ERRORS=$(adb logcat -d | grep -i "wordland" | grep -iE "error|crash|fatal" | grep -v "FileNotFoundException" | head -5 || echo "")
if [ -z "$ERRORS" ]; then
    print_result 0 "No errors in logcat"
else
    print_result 1 "Errors found in logcat"
    echo "$ERRORS"
fi

echo ""
echo "========================================"
echo "  Test Summary"
echo "========================================"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ ALL CHECKS PASSED - Ready for manual testing!${NC}"
    echo ""
    echo "Manual tests to perform:"
    echo "  1. Play Level 1 (6 words) - verify hints work"
    echo "  2. Check star ratings (3★ = good, 1★ = too fast/guessing)"
    echo "  3. Verify Level 2 unlocks after completion"
    echo "  4. Kill app and restart - verify progress saved"
    exit 0
else
    echo -e "${RED}❌ SOME CHECKS FAILED - Review above${NC}"
    exit 1
fi
