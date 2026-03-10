#!/bin/bash
# Epic #5 Star Rating Test - Logcat Monitor
# Start this BEFORE launching the app!

echo "=========================================="
echo "Epic #5 Star Rating Test Logcat Monitor"
echo "=========================================="
echo ""
echo "Monitoring logs for:"
echo "  - StarRatingCalculator"
echo "  - LearningViewModel"
echo "  - SubmitAnswerUseCase"
echo "  - GuessingDetector"
echo "  - ComboManager"
echo ""
echo "Press Ctrl+C to stop monitoring"
echo "=========================================="
echo ""

# Clear old logs
echo "Clearing old logs..."
adb logcat -c

# Start monitoring
adb logcat -v time | grep --line-buffered -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager|wordland.*Star|wordland.*combo|wordland.*guess"
