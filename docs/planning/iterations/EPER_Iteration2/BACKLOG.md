# Sprint Backlog

**Last Updated**: 2026-02-22
**Status**: Pending Sprint Planning Review

---

## Overview

This document contains tasks that have been identified but **not yet assigned to a specific sprint**. These tasks should be reviewed during Sprint Planning and prioritized based on:
- Team capacity
- Business value
- Dependencies
- Risk assessment

---

## Backlog Items

### Task #20: Add UI Component Screenshot Testing

**Type**: Testing Infrastructure
**Priority**: P1 (High)
**Estimated Time**: 3-4 hours
**Assigned To**: TBD (Sprint Planning)

**Description**:
Add automated screenshot testing for UI components to catch visual regression issues.

**Key Work**:
- Evaluate and integrate screenshot testing library (Paparazzi recommended)
- Configure test environment and CI integration
- Write initial tests for HintCard component
- Create reference screenshots

**Value**:
- Automated visual regression detection
- Prevents issues like P1-BUG-002 (text truncation) from reaching production
- Enables faster UI development feedback loop

**Dependencies**: None
**Blockers**: None

**Milestone**: Complete when HintCard has passing screenshot tests in CI

---

### Task #21: Improve UI Layer Test Coverage to 60%

**Type**: Testing
**Priority**: P0 (Critical)
**Estimated Time**: 16-20 hours
**Assigned To**: TBD (Sprint Planning)

**Description**:
Increase UI layer test coverage from current 5% to target 60%.

**Targets**:
| Module | Current | Target | Priority |
|--------|---------|--------|----------|
| `ui.components` | 0% | 50% | P0 |
| `ui.screens` | 0% | 60% | P0 |
| `ui.viewmodel` | 88% | 90% | P1 |

**Key Components to Test** (in priority order):
1. HintCard (≥80% coverage)
2. SpellBattleGame (≥70% coverage)
3. LevelProgressBarEnhanced (≥60% coverage)
4. WordlandButton (≥60% coverage)
5. WordlandCard (≥60% coverage)

**Value**:
- Prevents issues like P0-BUG-003 (layout overflow) from slipping through
- Improves code quality and maintainability
- Enables refactoring with confidence

**Dependencies**: None
**Blockers**: None

**Milestone**: Complete when UI layer overall coverage ≥ 60%

---

### Task #22: Establish Real Device Testing Matrix

**Type**: Process/Documentation
**Priority**: P1 (High)
**Estimated Time**: 2-3 hours
**Assigned To**: TBD (Sprint Planning)

**Description**:
Define and document a testing matrix for real device validation.

**Key Work**:
1. Define required device types:
   - Small screen phone (< 6")
   - Large screen phone (> 6")
   - Tablet (≥ 8")

2. Define required brands:
   - Samsung (represents OneUI)
   - Xiaomi (represents MIUI)
   - Google Pixel (stock Android)

3. Define required Android versions:
   - API 26 (minimum supported)
   - API 34 (target version)

4. Create real device test checklist

5. Establish device borrowing/procurement process

**Value**:
- Ensures app works on diverse device ecosystem
- Prevents device-specific bugs in production
- Standardizes testing process across team

**Dependencies**: None
**Blockers**: Access to physical devices

**Milestone**: Complete when device testing matrix document is approved

---

## Sprint Planning Considerations

### Resource Requirements

**Total Estimated Effort**:
- Task #20: 3-4 hours
- Task #21: 16-20 hours
- Task #22: 2-3 hours
- **Total**: 21-27 hours

**Recommended Assignment**:
- **android-test-engineer**: All three tasks (primary owner)
- **compose-ui-designer**: Support Task #21 (UI component tests)
- **android-architect**: Review and approve testing strategy

### Suggested Sprint Allocation

**Option A - Aggressive** (One Sprint):
- Task #20: Week 1 (Screenshot testing)
- Task #21: Week 1-2 (UI coverage) - Focus on critical components only
- Task #22: Week 2 (Device matrix)
- **Risk**: High workload, may impact quality

**Option B - Balanced** (Two Sprints):
- **Sprint N**: Task #20 + Task #22
- **Sprint N+1**: Task #21 (start with high-priority components)
- **Risk**: Low, sustainable pace

**Option C - Conservative** (Three Sprints):
- **Sprint N**: Task #22 (Device matrix - low hanging fruit)
- **Sprint N+1**: Task #20 (Screenshot testing)
- **Sprint N+2**: Task #21 (UI coverage - phased approach)
- **Risk**: Very low, but delayed value

### Dependencies and Risks

**Dependencies**:
- None of these tasks block each other
- Can be executed in parallel if team capacity allows

**Risks**:
- **Task #21** is large (16-20 hours) - consider breaking into smaller subtasks
- **Task #22** requires physical devices - may have procurement lead time
- All tasks compete with feature development for team time

### Recommended Priority Order

1. **Task #22** (Device Testing Matrix) - Do first
   - Quick win (2-3 hours)
   - Establishes process for future testing
   - Low effort, high value

2. **Task #20** (Screenshot Testing) - Do second
   - Prevents visual regressions
   - Enables faster UI development
   - Moderate effort (3-4 hours)

3. **Task #21** (UI Coverage) - Do last
   - Largest effort (16-20 hours)
   - Break into phases by component priority
   - Can be ongoing across multiple sprints

---

## Discussion Questions for Sprint Planning

1. **Team Capacity**: How much time can android-test-engineer dedicate to these tasks vs. feature testing?

2. **Value Proposition**: Should we prioritize Task #21 (coverage) over feature development? What's the ROI threshold?

3. **Phasing Approach**: Should Task #21 be broken into smaller chunks (e.g., "Cover HintCard and SpellBattleGame" as one task)?

4. **Device Access**: Do we have access to Samsung, Xiaomi, and Pixel devices for Task #22? If not, what's the procurement plan?

5. **Sprint Goals**: Do these tasks align with current Sprint goals? Should they be part of a "Quality Improvement" sprint?

---

## Related Documents

- Test Coverage Baseline: `docs/reports/testing/TEST_COVERAGE_BASELINE_20260220.md`
- Visual QA Checklist: `docs/testing/checklists/VISUAL_QA_CHECKLIST.md`
- Bug Discovery Summary: `docs/reports/testing/REAL_DEVICE_UI_BUG_DISCOVERY_SUMMARY_2026-02-22.md`
- Testing Strategy: `docs/testing/strategy/TEST_STRATEGY.md`

---

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-02-22 | Tasks #20-22 moved to backlog | Pending Sprint Planning review |

---

**Next Action**: Review and prioritize these tasks in upcoming Sprint Planning meeting.
