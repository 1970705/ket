#!/bin/bash
# Monitor First Launch Test
# This script monitors the app during first launch on a real device
# Run this BEFORE launching the app, then check results after launch

set -e

echo "📱 First Launch Monitoring Script"
echo "=================================="
echo ""
echo "This script will monitor the app after you manually install and launch it."
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counters
TESTS_PASSED=0
TESTS_FAILED=0

# Helper function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $2"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}❌ FAIL${NC}: $2"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
}

echo "INSTRUCTIONS:"
echo "============="
echo ""
echo "1. Install the APK manually on your device:"
echo "   Location: app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "2. Launch the app by clicking the icon"
echo ""
echo "3. When the app is running, press Enter to start monitoring..."
echo ""
read -p "Press Enter when app is launched..."
echo ""

# Clear logcat buffer
echo "Clearing logcat buffer..."
adb logcat -c
echo ""

# Give app time to initialize
echo "Waiting 5 seconds for app initialization..."
sleep 5
echo ""

# Step 1: Verify database is created
echo "Step 1: Verifying database creation..."
adb shell "run-as com.wordland ls -l /data/data/com.wordland/databases/" > /dev/null 2>&1
DB_EXISTS=$?
print_result $DB_EXISTS "Database file created"
echo ""

# Step 2: Verify database has data
echo "Step 2: Verifying database has data..."
WORD_COUNT=$(adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland_database 'SELECT COUNT(*) FROM words' 2>/dev/null" || echo "0")
echo "Words in database: $WORD_COUNT"

if [ "$WORD_COUNT" -gt 0 ]; then
    print_result 0 "Database contains data ($WORD_COUNT words)"
else
    print_result 1 "Database is empty - initialization failed"
fi
echo ""

# Step 3: Verify Level 1 is unlocked
echo "Step 3: Verifying Level 1 status..."
LEVEL1_STATUS=$(adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland_database 'SELECT status FROM level_progress WHERE level_id=\"look_island_level_01\"' 2>/dev/null" || echo "")
echo "Level 1 status: $LEVEL1_STATUS"

if [ "$LEVEL1_STATUS" = "unlocked" ]; then
    print_result 0 "Level 1 is UNLOCKED"
else
    print_result 1 "Level 1 is $LEVEL1_STATUS (should be unlocked)"
fi
echo ""

# Step 4: Check for initialization in logcat
echo "Step 4: Checking initialization logcat..."
LOGCAT_INIT=$(adb logcat -d | grep -i "WordlandApplication\|Wordland.*initializ" | tail -5 || echo "")
if [ -n "$LOGCAT_INIT" ]; then
    print_result 0 "Initialization logs found in logcat"
    echo "Logs:"
    echo "$LOGCAT_INIT"
else
    print_result 1 "No initialization log found"
fi
echo ""

# Step 5: Check for errors in logcat
echo "Step 5: Checking for errors in logcat..."
ERRORS=$(adb logcat -d | grep -i "wordland" | grep -iE "error|crash|fatal|exception" | grep -v "FileNotFoundException" | head -10 || echo "")
if [ -z "$ERRORS" ]; then
    print_result 0 "No critical errors in logcat"
else
    print_result 1 "Errors found in logcat"
    echo "Errors:"
    echo "$ERRORS"
fi
echo ""

# Step 6: Check if app is still running
echo "Step 6: Checking if app process is alive..."
PROCESS_COUNT=$(adb shell "ps | grep com.wordland" | wc -l | tr -d ' ')
if [ "$PROCESS_COUNT" -gt 0 ]; then
    print_result 0 "App is running (process found)"
else
    print_result 1 "App process not found (may have crashed)"
fi
echo ""

# Final summary
echo "=================================="
echo "Test Summary"
echo "=================================="
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ ALL TESTS PASSED - First launch is working correctly!${NC}"
    echo ""
    echo "The app is ready for real device testing!"
    exit 0
else
    echo -e "${RED}❌ TESTS FAILED - First launch has issues${NC}"
    echo ""
    echo "Please review the following:"
    echo "1. Check if WordlandApplication.onCreate() calls initializeAppData()"
    echo "2. Check if AppDataInitializer.initializeAllData() completes"
    echo "3. Check if Level 1 is set to UNLOCKED (not LOCKED)"
    echo "4. Check full logcat: adb logcat | grep wordland"
    exit 1
fi
