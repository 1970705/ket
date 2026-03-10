#!/bin/bash

# Test Script: Star Breakdown UI Integration
# Tests the navigation flow from Level Complete to Star Breakdown screen
#
# Prerequisites:
# - App installed on device/emulator
# - Look Island Level 1 available
#
# Usage: ./test_star_breakdown_integration.sh

set -e

echo "🧪 Star Breakdown Integration Test"
echo "=================================="
echo ""

# Launch the app
echo "📱 Launching Wordland app..."
adb shell am start -n com.wordland/.ui.MainActivity
sleep 2

echo "✅ App launched successfully"
echo ""
echo "📋 Test Instructions:"
echo "1. Navigate to Island Map"
echo "2. Select Look Island"
echo "3. Select and complete Level 1 (6 words)"
echo "4. On the Level Complete screen, verify:"
echo "   - ✅ '继续探险' (Continue) button exists"
echo "   - ✅ '查看星级详情' (View Star Details) button exists"
echo "5. Tap '查看星级详情' button"
echo "6. Verify Star Breakdown screen:"
echo "   - ✅ Screen displays with star rating"
echo "   - ✅ Accuracy percentage shown"
echo "   - ✅ Hints used count shown"
echo "   - ✅ Time taken shown"
echo "   - ✅ Error count shown"
echo "   - ✅ Progress bars are animated"
echo "   - ✅ Color coding (green/yellow/red) applied"
echo "   - ✅ Back button (←) in top bar"
echo "7. Tap back button"
echo "8. Verify navigation returns to Level Select screen"
echo ""
echo "🎯 Expected Navigation Flow:"
echo "   LearningScreen → LevelCompleteScreen → StarBreakdownScreen → LevelSelectScreen"
echo ""
echo "📊 Data Flow Verification:"
echo "   - Star rating (1-3) passed correctly"
echo "   - Accuracy (0-100) passed correctly"
echo "   - Hints used (0+) passed correctly"
echo "   - Time taken (seconds) passed correctly"
echo "   - Error count (0+) passed correctly"
echo ""
echo "🔍 Logcat Monitoring (optional):"
echo "   Run: adb logcat | grep -E '(Navigation|StarBreakdown|LevelComplete)'"
echo ""
echo "✅ Test script initialized. Please follow the manual test instructions above."
echo ""
echo "❌ To report issues, note:"
echo "   - Device/emulator details"
echo "   - Step number where failure occurred"
echo "   - Expected vs Actual behavior"
echo "   - Screenshot (if possible)"
