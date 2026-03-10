#!/bin/bash
# Start Wordland Team in iTerm2 Native Panes
# This script uses iTerm2's AppleScript interface to create split panes

PROJECT_DIR="/Users/panshan/git/ai/ket"

echo "🚀 Starting Wordland Team in iTerm2..."
echo ""

# Use AppleScript to create iTerm2 panes
osascript << EOF
tell application "iTerm"
    activate

    -- Get current window or create new one
    try
        set currentWindow to current window
    on error
        set currentWindow to (create window with default profile)
    end try

    tell currentWindow
        -- Start with the current session (pane 0)
        tell current session
            write text "cd $PROJECT_DIR"
            write text "clear"
            write text "echo '🏗️ android-architect - Ready'"
        end tell

        -- Split and create pane 1 (top-right)
        tell current session
            set pane1 to (split horizontally with default profile)
            tell pane1
                write text "cd $PROJECT_DIR"
                write text "clear"
                write text "echo '⚙️ android-engineer - Ready'"
            end tell
        end tell

        -- Split pane 0 vertically to create pane 2 (middle-left)
        tell current session
            set pane2 to (split vertically with default profile)
            tell pane2
                write text "cd $PROJECT_DIR"
                write text "clear"
                write text "echo '🎨 compose-ui-designer - Ready'"
            end tell
        end tell

        -- Split pane 2 vertically to create pane 3 (middle)
        tell pane2
            set pane3 to (split vertically with default profile)
            tell pane3
                write text "cd $PROJECT_DIR"
                write text "clear"
                write text "echo '🧪 android-test-engineer - Ready'"
            end tell
        end tell

        -- Split pane 1 vertically to create pane 4 (middle-right)
        tell pane1
            set pane4 to (split vertically with default profile)
            tell pane4
                write text "cd $PROJECT_DIR"
                write text "clear"
                write text "echo '🎮 game-designer - Ready'"
            end tell
        end tell

        -- Split pane 4 vertically to create pane 5 (right)
        tell pane4
            set pane5 to (split vertically with default profile)
            tell pane5
                write text "cd $PROJECT_DIR"
                write text "clear"
                write text "echo '📚 education-specialist - Ready'"
            end tell
        end tell

        -- Split pane 5 vertically to create pane 6 (far-right)
        tell pane5
            set pane6 to (split vertically with default profile)
            tell pane6
                write text "cd $PROJECT_DIR"
                write text "clear"
                write text "echo '⚡ android-performance-expert - Ready'"
            end tell
        end tell
    end tell
end tell

return "Team panes created successfully"
EOF

echo ""
echo "✅ Team panes created in iTerm2!"
echo ""
echo "You should now see 6 split panes with all team members."
