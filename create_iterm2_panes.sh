#!/bin/bash
# Create iTerm2 Native Panes for Wordland Team
# Uses iTerm2's split-pane mechanism (not tmux)

PROJECT_DIR="/Users/panshan/git/ai/ket"

MEMBERS=(
    "android-architect"
    "android-engineer"
    "compose-ui-designer"
    "android-test-engineer"
    "game-designer"
    "education-specialist"
    "android-performance-expert"
)

EMOJIS=(
    "🏗️"
    "⚙️"
    "🎨"
    "🧪"
    "🎮"
    "📚"
    "⚡"
)

echo "Creating iTerm2 native panes for Wordland team..."
echo ""

# Use iTerm2's split-pane via escape codes or AppleScript
# Method: Use iTerm2 Shell Integration if available, or AppleScript

if [ -n "$ITERM_SESSION_ID" ]; then
    # Running inside iTerm2, can use escape codes
    echo "Detected iTerm2 session, using escape codes..."

    # Split vertically and horizontally to create 7 panes
    # Layout: 3 columns x 2 rows + 1 bottom full-width pane

    # Start by splitting into right pane
    printf '\033]1337;SplitSplitPane horizontally\007'

    # Split left pane into 3 vertical
    printf '\033]1337;SplitSplitPane vertically\007'
    sleep 0.2
    printf '\033]1337;SplitSplitPane vertically\007'

    # Split right pane into 3 vertical
    printf '\033]1337;SelectSplitPane\007\033]1337;SplitSplitPane vertically\007'
    sleep 0.2
    printf '\033]1337;SplitSplitPane vertically\007'

else
    echo "Not in iTerm2, using AppleScript..."

    # Use AppleScript to split current iTerm2 window
    osascript << 'EOF'
    tell application "iTerm"
        tell current window
            tell current session
                -- Split into 7 panes
                -- First split (horizontal)
                split horizontally with default profile

                -- Split left pane into 3 vertical
                select split pane 1
                split vertically with default profile
                select split pane 1
                split vertically with default profile

                -- Split right pane into 3 vertical
                select split pane 2
                split vertically with default profile
                select split pane 1
                split vertically with default profile
                select split pane 1
                split vertically with default profile
            end tell
        end tell
    end tell
EOF
fi

echo "✓ Panes created!"
