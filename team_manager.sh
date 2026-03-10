#!/bin/zsh
# Wordland Team Manager
# Manages team members in tmux panes with real-time monitoring

set -e

PROJECT_DIR="/Users/panshan/git/ai/ket"
SESSION_NAME="wordland-team"

# Color codes
typeset -gr RED='\033[0;31m'
typeset -gr GREEN='\033[0;32m'
typeset -gr YELLOW='\033[1;33m'
typeset -gr BLUE='\033[0;34m'
typeset -gr CYAN='\033[0;36m'
typeset -gr NC='\033[0m'

# Team members list (with emoji and color info)
typeset -a MEMBERS=(
    "android-architect|🏗️|green"
    "android-engineer|⚙️|cyan"
    "compose-ui-designer|🎨|yellow"
    "android-test-engineer|🧪|magenta"
    "game-designer|🎮|blue"
    "education-specialist|📚|purple"
    "android-performance-expert|⚡|red"
)

# Function to print header
print_header() {
    local text="$1"
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  $text${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

# Function to check tmux
check_tmux() {
    if ! command -v tmux &> /dev/null; then
        echo -e "${RED}Error: tmux is not installed${NC}"
        echo "Install with: brew install tmux"
        exit 1
    fi
}

# Function to get member info by index
get_member_info() {
    local index=$1
    local entry="${MEMBERS[$index]}"
    echo "$entry" | IFS='|' read -r name emoji color
    # Export variables
    export MEMBER_NAME="$name"
    export MEMBER_EMOJI="$emoji"
    export MEMBER_COLOR="$color"
}

# Function to create session
create_session() {
    print_header "Creating Team Session"

    if tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
        echo -e "${YELLOW}Session '$SESSION_NAME' already exists${NC}"
        echo ""
        echo "Options:"
        echo "  [1] Attach to existing session (all panes visible)"
        echo "  [2] Kill session and create new one"
        echo "  [3] Cancel"
        echo ""
        echo -n "Choose (1/2/3): "
        read choice
        case $choice in
            1)
                echo ""
                echo -e "${GREEN}Attaching to existing session...${NC}"
                sleep 1
                tmux attach-session -t "$SESSION_NAME"
                exit 0
                ;;
            2)
                echo ""
                echo -e "${YELLOW}Killing existing session...${NC}"
                tmux kill-session -t "$SESSION_NAME"
                sleep 1
                ;;
            3|*)
                echo "Cancelled"
                exit 0
                ;;
        esac
    fi

    echo -e "${GREEN}Creating tmux session: $SESSION_NAME${NC}"
    echo ""
    echo "Layout: Tiled (all 7 panes visible simultaneously)"
    echo ""

    # Get first member
    get_member_info 1
    local first_name="$MEMBER_NAME"
    local first_emoji="$MEMBER_EMOJI"

    # Create session with first pane
    tmux new-session -d -s "$SESSION_NAME" -n "$first_name" -c "$PROJECT_DIR"
    tmux send-keys -t "$SESSION_NAME:0.0" "clear" Enter
    tmux send-keys -t "$SESSION_NAME:0.0" "echo '$first_emoji $first_name - Ready'" Enter

    # Create additional panes
    local total=${#MEMBERS[@]}
    for ((i=2; i<=total; i++)); do
        get_member_info $i
        local name="$MEMBER_NAME"
        local emoji="$MEMBER_EMOJI"

        tmux split-window -t "$SESSION_NAME:0" -c "$PROJECT_DIR"
        tmux send-keys -t "$SESSION_NAME:0.$((i-1))" "clear" Enter
        tmux send-keys -t "$SESSION_NAME:0.$((i-1))" "echo '$emoji $name - Ready'" Enter
    done

    # Apply tiled layout AFTER all panes are created
    tmux select-layout -t "$SESSION_NAME:0" tiled

    # Set up pane titles and colors
    for ((i=1; i<=total; i++)); do
        get_member_info $i
        local name="$MEMBER_NAME"
        local emoji="$MEMBER_EMOJI"
        tmux select-pane -t "$SESSION_NAME:0.$((i-1))" -T "$emoji $name"
        # Make pane borders more visible
        tmux set-pane-border -t "$SESSION_NAME:0.$((i-1))" -fg colour"$i"
    done

    # Set pane border status to show member names
    tmux set-option -t "$SESSION_NAME" pane-border-status 'top'
    tmux set-option -t "$SESSION_NAME" pane-border-format " #{pane_title} "

    echo -e "${GREEN}✓ Session created with $total panes${NC}"
    echo ""
}

# Function to list members
list_members() {
    echo ""
    echo "Team members:"
    local index=1
    for entry in "${MEMBERS[@]}"; do
        echo "$entry" | IFS='|' read -r name emoji color
        echo "  [$index] $emoji $name"
        ((index++))
    done
    echo ""
}

# Function to attach to session
attach_session() {
    if ! tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
        echo -e "${YELLOW}Session not found. Creating...${NC}"
        create_session
    fi

    echo -e "${GREEN}Attaching to $SESSION_NAME...${NC}"
    tmux attach-session -t "$SESSION_NAME"
}

# Function to show status
show_status() {
    print_header "Team Status"

    if ! tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
        echo -e "${YELLOW}No active session${NC}"
        echo "Start with: $0 start"
        exit 0
    fi

    echo -e "${GREEN}Session: $SESSION_NAME${NC}"
    echo ""

    local index=1
    for entry in "${MEMBERS[@]}"; do
        echo "$entry" | IFS='|' read -r name emoji color

        # Get pane content (last few lines)
        local pane_content=$(tmux capture-pane -t "$SESSION_NAME:0.$((index-1))" -p | tail -3)
        local status="💤 Idle"

        if [[ "$pane_content" == *"Starting"* ]] || [[ "$pane_content" == *"Running"* ]] || [[ "$pane_content" == *"Working"* ]]; then
            status="🔄 Active"
        fi

        echo "[$index] $emoji $name - $status"
        ((index++))
    done
    echo ""
}

# Function to stop session
stop_session() {
    print_header "Stopping Team Session"
    if tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
        tmux kill-session -t "$SESSION_NAME"
        echo -e "${GREEN}✓ Session stopped${NC}"
    else
        echo -e "${YELLOW}No active session${NC}"
    fi
}

# Main function
main() {
    check_tmux

    case "${1:-help}" in
        start)
            create_session
            echo ""
            echo -e "${GREEN}✓ Team panes created and arranged in tiled layout${NC}"
            echo -e "${GREEN}✓ All 7 members visible simultaneously${NC}"
            echo ""
            echo -e "${BLUE}Attaching to session now...${NC}"
            echo ""
            sleep 1
            attach_session
            ;;
        attach)
            attach_session
            ;;
        status)
            show_status
            ;;
        stop)
            stop_session
            ;;
        help|*)
            print_header "Wordland Team Manager"
            echo "Usage: $0 <command>"
            echo ""
            echo "Commands:"
            echo "  ${GREEN}start${NC}           - Create team panes and attach"
            echo "  ${GREEN}attach${NC}          - Attach to existing session"
            echo "  ${GREEN}status${NC}          - Show team status"
            echo "  ${GREEN}stop${NC}            - Stop team session"
            echo ""
            echo "Team members:"
            list_members
            echo "Examples:"
            echo "  $0 start                    # Create panes"
            echo "  $0 status                   # Check status"
            echo "  $0 attach                   # Connect to session"
            echo ""
            echo "Note: Use Ctrl+B then arrow keys to navigate panes"
            echo "      Use Ctrl+B then D to detach (keep running)"
            ;;
    esac
}

main "$@"
