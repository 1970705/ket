#!/bin/bash
# Start Team Panes Script
# Creates iTerm2/tmux panes for Wordland dev team members

set -e

PROJECT_DIR="/Users/panshan/git/ai/ket"
SESSION_NAME="wordland-team"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Wordland Team Panes Launcher${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if tmux is installed
if ! command -v tmux &> /dev/null; then
    echo -e "${RED}Error: tmux is not installed${NC}"
    echo "Install with: brew install tmux"
    exit 1
fi

# Check if session already exists
if tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
    echo -e "${YELLOW}Session '$SESSION_NAME' already exists${NC}"
    echo "Options:"
    echo "  1. Attach to existing session"
    echo "  2. Kill session and create new one"
    echo ""
    read -p "Choose (1/2): " choice
    case $choice in
        1)
            echo -e "${GREEN}Attaching to existing session...${NC}"
            tmux attach-session -t "$SESSION_NAME"
            exit 0
            ;;
        2)
            echo -e "${YELLOW}Killing existing session...${NC}"
            tmux kill-session -t "$SESSION_NAME"
            sleep 1
            ;;
        *)
            echo -e "${RED}Invalid choice. Exiting.${NC}"
            exit 1
            ;;
    esac
fi

# Team member definitions
# Format: "Name|Pane Title|Working Directory"
MEMBERS=(
    "android-architect|🏗️  Architect|$PROJECT_DIR"
    "android-engineer|⚙️  Engineer|$PROJECT_DIR"
    "compose-ui-designer|🎨 UI Designer|$PROJECT_DIR"
    "android-test-engineer|🧪 Test Engineer|$PROJECT_DIR"
    "game-designer|🎮 Game Designer|$PROJECT_DIR"
    "education-specialist|📚 Education|$PROJECT_DIR"
    "android-performance-expert|⚡ Performance|$PROJECT_DIR"
)

echo -e "${GREEN}Creating tmux session: $SESSION_NAME${NC}"
echo ""

# Create new session with first member
FIRST_MEMBER="${MEMBERS[0]}"
IFS='|' read -r NAME TITLE DIR <<< "$FIRST_MEMBER"

echo "  [$TITLE] $NAME"
tmux new-session -d -s "$SESSION_NAME" -n "$NAME" -c "$DIR"
tmux send-keys -t "$SESSION_NAME:$NAME" "clear" Enter

# Add remaining members as panes in the same window
for i in "${!MEMBERS[@]}"; do
    if [ $i -eq 0 ]; then
        continue
    fi

    MEMBER="${MEMBERS[$i]}"
    IFS='|' read -r NAME TITLE DIR <<< "$MEMBER"

    echo "  [$TITLE] $NAME"

    # Split window vertically and add pane
    tmux split-window -t "$SESSION_NAME" -c "$DIR"
    tmux select-layout -t "$SESSION_NAME" tiled
    tmux send-keys -t "$SESSION_NAME" "clear" Enter
    sleep 0.1
done

# Set up pane titles
for i in "${!MEMBERS[@]}"; do
    MEMBER="${MEMBERS[$i]}"
    IFS='|' read -r NAME TITLE DIR <<< "$MEMBER"
    tmux select-pane -t "$SESSION_NAME:$i" -T "$TITLE"
done

echo ""
echo -e "${GREEN}✓ Team panes created successfully!${NC}"
echo ""
echo "Team members:"
for MEMBER in "${MEMBERS[@]}"; do
    IFS='|' read -r NAME TITLE DIR <<< "$MEMBER"
    echo "  - $TITLE ($NAME)"
done
echo ""
echo -e "${BLUE}========================================${NC}"
echo "Instructions:"
echo "  1. Switch to iTerm2 app now"
echo "  2. Attach to session: ${YELLOW}tmux attach -t $SESSION_NAME${NC}"
echo "  3. Use Ctrl+B then arrow keys to navigate panes"
echo "  4. Use Ctrl+B then D to detach (keep running)"
echo -e "${BLUE}========================================${NC}"
echo ""
read -p "Attach to session now? (y/n): " attach_choice

if [[ $attach_choice =~ ^[Yy]$ ]]; then
    # Try to open in iTerm2
    if [[ -n "$ITERM_SESSION_ID" ]]; then
        # Already in iTerm2, just attach
        tmux attach-session -t "$SESSION_NAME"
    else
        # Not in iTerm2, try to open
        echo -e "${YELLOW}Switch to iTerm2 and run: tmux attach -t $SESSION_NAME${NC}"
        tmux attach-session -t "$SESSION_NAME"
    fi
else
    echo ""
    echo -e "${GREEN}Session is running in background${NC}"
    echo "Attach anytime with: ${YELLOW}tmux attach -t $SESSION_NAME${NC}"
fi
