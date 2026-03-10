#!/bin/bash
# Monitor Agent Output in tmux Pane
# Usage: ./monitor_agent.sh <agent-name> <output-file>

AGENT_NAME="$1"
OUTPUT_FILE="$2"

if [ -z "$AGENT_NAME" ] || [ -z "$OUTPUT_FILE" ]; then
    echo "Usage: $0 <agent-name> <output-file>"
    echo "Example: $0 android-engineer /private/tmp/.../output"
    exit 1
fi

echo "========================================"
echo "  Monitoring: $AGENT_NAME"
echo "========================================"
echo ""
echo "Output file: $OUTPUT_FILE"
echo ""
echo "Press Ctrl+C to stop monitoring"
echo "========================================"
echo ""

# Check if file exists
if [ ! -f "$OUTPUT_FILE" ]; then
    echo "Waiting for output file to be created..."
    echo "Agent may not have started yet..."
    echo ""
fi

# Follow the output file
# Use tail -f to continuously display new content
tail -f "$OUTPUT_FILE" 2>/dev/null || {
    echo "Output file not found. Agent may still be starting..."
    echo "Will check every 2 seconds..."
    while true; do
        if [ -f "$OUTPUT_FILE" ]; then
            clear
            echo "Output file detected! Starting monitor..."
            tail -f "$OUTPUT_FILE"
            break
        fi
        sleep 2
        echo -n "."
    done
}
