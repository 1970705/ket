#!/bin/bash
# Start Wordland Team Panes - Direct iTerm2 Integration
# This script opens iTerm2 and creates tmux panes automatically

set -e

PROJECT_DIR="/Users/panshan/git/ai/ket"
SESSION_NAME="wordland-team"

echo "🚀 Starting Wordland Team Panes..."

# Kill existing session if exists
tmux kill-session -t "$SESSION_NAME" 2>/dev/null || true
sleep 0.5

# Create tmux session with all 7 panes
echo "Creating tmux session..."
tmux new-session -d -s "$SESSION_NAME" -x 240 -y 60 -n "team" -c "$PROJECT_DIR"

# Create all 7 panes
for i in {1..6}; do
    tmux split-window -t "$SESSION_NAME:0" -c "$PROJECT_DIR"
done

# Apply tiled layout
tmux select-layout -t "$SESSION_NAME:0" tiled

# Setup each pane
MEMBERS=(
    "android-architect:🏗️"
    "android-engineer:⚙️"
    "compose-ui-designer:🎨"
    "android-test-engineer:🧪"
    "game-designer:🎮"
    "education-specialist:📚"
    "android-performance-expert:⚡"
)

for i in {0..6}; do
    IFS=':' read -r name emoji <<< "${MEMBERS[$i]}"
    tmux send-keys -t "$SESSION_NAME:0.$i" "clear" Enter
    tmux send-keys -t "$SESSION_NAME:0.$i" "echo '$emoji $name - Ready'" Enter
    tmux select-pane -t "$SESSION_NAME:0.$i" -T "$emoji $name"
done

# Enhance pane borders
tmux set-option -t "$SESSION_NAME" pane-border-status 'top'
tmux set-option -t "$SESSION_NAME" pane-border-format ' #{pane_title} '

echo "✓ Session created with 7 panes"
echo ""

# Open iTerm2 and attach to session
echo "Opening iTerm2..."

if command -v osascript &> /dev/null; then
    # Use AppleScript to open iTerm2 and attach
    osascript << 'EOF'
    tell application "iTerm"
        activate
        set newWindow to (create window with default profile)
        tell current session of newWindow
            write text "cd /Users/panshan/git/ai/ket"
            write text "clear"
            write text "echo '╔══════════════════════════════════════════════════════════╗'"
            write text "echo '║   Wordland Team - All 7 Members Ready! ✅                ║'"
            write text "echo '╚══════════════════════════════════════════════════════════╝'"
            write text "echo ''"
            write text "tmux attach -t wordland-team"
        end tell
    end tell
EOF

    echo "✓ iTerm2 should open automatically with team panes!"
    echo ""
    echo "You will see all 7 team members in tiled layout."
else
    echo "⚠️  osascript not found"
    echo "Please manually run: tmux attach -t $SESSION_NAME"
fi
