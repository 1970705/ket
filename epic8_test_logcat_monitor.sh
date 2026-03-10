#!/bin/bash
# Epic #8 Task #8.2 Real Device Validation Logcat Monitor
# Captures all star rating and game behavior logs
#
# Updated: 2026-02-26
# Purpose: Monitor logs for P0-BUG-010 threshold fix validation
#
# Usage:
#   1. Connect device (adb devices)
#   2. Run: ./epic8_test_logcat_monitor.sh
#   3. Execute test scenarios
#   4. Press Ctrl+C to stop
#
# Key patterns to watch:
#   - "score=X.XX → N stars" - Verify threshold (2.5/1.5/0.5)

echo "=========================================="
echo "Epic #8 Real Device Test - Logcat Monitor"
echo "=========================================="
echo "Starting logcat monitoring..."
echo "Press Ctrl+C to stop monitoring"
echo ""
echo "Monitoring for:"
echo "  - StarRatingCalculator"
echo "  - LearningViewModel"
echo "  - SubmitAnswerUseCase"
echo "  - GuessingDetector"
echo "  - ComboManager"
echo "  - StarBreakdownScreen"
echo ""

# Clear old logs
adb logcat -c

# Start monitoring with key tags
adb logcat -v time | grep --line-buffered -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager|StarBreakdownScreen|Wordland.*submitAnswer|Wordland.*calculateStars"
