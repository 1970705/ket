#!/bin/bash
# Diagnose and fix tmux panes display

echo "========================================="
echo "  Team Panes Diagnostic Tool"
echo "========================================="
echo ""

# Check if session exists
if ! tmux has-session -t wordland-team 2>/dev/null; then
    echo "❌ No session found"
    echo "Please run: ./team_manager.sh start"
    exit 1
fi

echo "✅ Session 'wordland-team' exists"
echo ""

# Check panes
pane_count=$(tmux display-message -t wordland-team -p '#{window_panes}')
echo "📊 Pane count: $pane_count"

if [ "$pane_count" -lt 7 ]; then
    echo "⚠️  Expected 7 panes, found $pane_count"
    echo "Recreating session..."
    tmux kill-session -t wordland-team
    ./team_manager.sh start
    exit 0
fi

echo "✅ All 7 panes exist"
echo ""

# Show pane info
echo "=== Pane Layout ==="
tmux list-panes -t wordland-team:0 -F "#{pane_index}: #{pane_title} (#{pane_width}x#{pane_height})"
echo ""

# Check if attached
if tmux display-message -t wordland-team -p '#{session_attached}' 2>/dev/null | grep -q "1"; then
    echo "✅ You are currently attached to the session"
    echo ""
    echo "If you don't see split screens, try:"
    echo "  1. Press Ctrl+B then Space (cycles layouts)"
    echo "  2. Press Ctrl+B then $ (maximize current pane)"
    echo "  3. Make your terminal window LARGER"
    echo ""
else
    echo "ℹ️  Session is running in background"
    echo ""
    echo "To attach and see the panes:"
    echo "  ${GREEN}tmux attach -t wordland-team${NC}"
    echo ""
fi

# Show layout
echo "=== Current Layout ==="
tmux display-message -t wordland-team:0 -p "Layout: #{window_layout}"
echo ""

# Suggest fixes
echo "=== Troubleshooting ==="
echo ""
echo "If you don't see split screens after attaching:"
echo ""
echo "1️⃣  Make terminal window BIGGER"
echo "   - 7 panes need at least 240x60 characters"
echo "   - Try maximizing the window"
echo ""
echo "2️⃣  Cycle through layouts"
echo "   - Press Ctrl+B then Space (multiple times)"
echo "   - Look for 'tiled' layout"
echo ""
echo "3️⃣  Check pane borders"
echo "   - Borders should be visible between panes"
echo "   - If not, press Ctrl+B then : (type 'set pane-border-status top')"
echo ""
echo "4️⃣  Try different layout"
echo "   - Press Ctrl+B then Space to cycle"
echo "   - Or press: Ctrl+B then : (type 'select-layout tiled')"
echo ""
echo "5️⃣  Last resort - recreate session"
echo "   - Press Ctrl+B then D (detach)"
echo "   - Run: ./team_manager.sh stop && ./team_manager.sh start"
echo ""
