# Team Configuration Fix Proposal

**Date**: 2026-02-25
**Issue**: SendMessage permission request mechanism not working
**Status**: Analysis Complete, Fix Proposed

---

## Problem Analysis

### Current State

**Backend Configuration**:
- 30 team members total
- 28 members using `iterm2` backend (all inactive)
- 1 member using `in-process` backend
- 1 member with no backend

**Permission Configuration**:
- ❌ No `allowedPrompts` configured for any member
- ❌ No `planModeRequired` set (all false)
- ❌ No permission boundaries defined

### Root Cause

1. **iTerm2 Dependency**: Team configured for iTerm2 panes but all are inactive
2. **No Permission Gates**: Agents don't know when to request permissions
3. **Missing Protocol**: Agents print to output instead of using SendMessage

### What Happened

```
❌ Current Behavior:
android-engineer → Prints "Shall I proceed?" to output
    → Agent completes (status: completed)
    → Cannot receive messages anymore
    → Work stuck

✅ Expected Behavior:
android-engineer → SendMessage("I need Edit permission")
    → I receive message
    → I ask user for approval
    → User approves
    → SendMessage("Permission granted")
    → android-engineer continues work
```

---

## Solution Options

### Option A: Add Permission Gates (Recommended)

**Approach**: Add `allowedPrompts` to configure Edit tool permissions

**Changes Required**:
1. Add `allowedPrompts` to member configurations
2. Update prompts to instruct agents to use SendMessage
3. Keep `planModeRequired: false` (not needed for this use case)

**Example Configuration**:
```json
{
  "agentId": "android-engineer@wordland-dev-team",
  "name": "android-engineer",
  "agentType": "general-purpose",
  "planModeRequired": false,
  "allowedPrompts": [
    {
      "tool": "Edit",
      "prompt": "Modify Kotlin source files in domain/, data/, and ui/viewmodel/ layers"
    }
  ]
}
```

**Pros**:
- Maintains role boundaries
- Enables permission request flow
- Minimal disruption

**Cons**:
- Requires updating 30 member configs
- iTerm2 backend still unusable

---

### Option B: Remove iTerm2 Dependency (Simplification)

**Approach**: Remove `backendType: "iterm2"` and `tmuxPaneId` from all members

**Changes Required**:
1. Remove `backendType` and `tmuxPaneId` fields
2. Add `allowedPrompts` for role-specific permissions
3. Update prompts to emphasize SendMessage usage

**Before**:
```json
{
  "name": "android-engineer",
  "backendType": "iterm2",
  "tmuxPaneId": "B7EFD09E-E50E-4C2A-BE12-222BE6845382",
  "isActive": false
}
```

**After**:
```json
{
  "name": "android-engineer",
  "allowedPrompts": [
    {"tool": "Edit", "prompt": "Modify Kotlin source files"}
  ]
}
```

**Pros**:
- Removes broken iTerm2 dependency
- Cleaner configuration
- Works with background agents
- Simplifies team management

**Cons**:
- Lose ability to use iTerm2 panes (if fixed in future)
- Requires configuration migration

---

### Option C: Hybrid Approach (Most Flexible)

**Approach**: Keep minimal core team, remove unused members

**Core Team** (7 active members):
1. android-architect
2. android-engineer
3. compose-ui-designer
4. android-test-engineer
5. game-designer
6. education-specialist
7. android-performance-expert

**Changes**:
1. Remove 23 inactive/duplicate members
2. Configure core team without iTerm2 backend
3. Add `allowedPrompts` with role-specific permissions
4. Update prompts with SendMessage instructions

**Pros**:
- Cleaner, manageable team
- Focus on active roles
- Easier to maintain

**Cons**:
- Lose historical member configurations
- Requires recreation if needed later

---

## Recommended Solution: Option B + Enhanced Protocol

### Implementation Steps

**Step 1**: Update android-engineer prompt to include:
```
**Permission Request Protocol**:
When you need Edit tool permission:
1. STOP working
2. Use SendMessage to request permission
3. WAIT for approval before continuing

Example:
SendMessage(
  type: "message",
  recipient: "team-lead",
  content: "I need Edit permission to modify StarRatingCalculator.kt to add isGuessing field. Shall I proceed?"
)
```

**Step 2**: Add allowedPrompts to config:
```json
"allowedPrompts": [
  {
    "tool": "Edit",
    "prompt": "Modify Kotlin source files in domain/ and ui/viewmodel/ layers"
  },
  {
    "tool": "Write",
    "prompt": "Create new test files in test/ directory"
  }
]
```

**Step 3**: Remove iTerm2 configuration:
- Delete `backendType: "iterm2"`
- Delete `tmuxPaneId`
- Delete `isActive: false`

**Step 4**: Test the flow:
1. Start android-engineer
2. Agent requests permission via SendMessage
3. I receive and forward to user
4. User approves
5. I send approval via SendMessage
6. Agent continues work

---

## Permission Matrix by Role

| Role | Edit Permissions | Write Permissions | Restrictions |
|------|------------------|-------------------|--------------|
| android-architect | All layers | Documentation, tests | No UI implementation |
| android-engineer | domain/, data/, ui/viewmodel/ | Tests | No UI screens/components |
| compose-ui-designer | ui/screens/, ui/components/ | None | No business logic |
| android-test-engineer | test/ | Test reports | No production code |
| game-designer | None | Documentation only | No code changes |
| education-specialist | None | Documentation only | No code changes |
| android-performance-expert | All layers (read-only) | Benchmark tests | No feature changes |

---

## Action Items

### Immediate (Today)
- [ ] Decide on solution option (A/B/C)
- [ ] Update team configuration
- [ ] Test permission flow with one agent
- [ ] Complete Epic #5 tasks

### Short-term (This Week)
- [ ] Document team collaboration protocol
- [ ] Update teamagents.md with permission rules
- [ ] Train team on proper SendMessage usage

### Long-term (Future)
- [ ] Consider iTerm2 integration if needed
- [ ] Automate permission configuration
- [ ] Build permission request UI

---

## Questions for User

1. **Which solution option do you prefer?** (A/B/C)
2. **Should I proceed with Option B (remove iTerm2, add permissions)?**
3. **Keep all 30 members or simplify to core 7?**
4. **Any specific permission boundaries to enforce?**

---

**Next Step**: Await user decision, then implement fix.
