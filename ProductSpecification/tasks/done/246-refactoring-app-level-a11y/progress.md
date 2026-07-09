# Task 246: App-level a11y support — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

> Architectural improvement — **discussion/ADR first**, then implementation.
> The steps below are design+implementation, not a per-layer TDD cycle (a11y is a cross-cutting
> concern enforced by lint config + framework rules, not domain logic).

### Step 1: Architectural discussion & decision (ADR)
- [x] discuss a11y scope — tool, rule set, wrapper-pattern handling, audit posture
- [x] resolve the location open-question — no master copy outside the project; `.opencode/` is a stale mirror, not propagated
- [x] write ADR `app-level-a11y` — see `decisions/app-level-a11y-decision.md`

### Step 2: App-level — a11y lint gate
- [x] add `eslint-plugin-vuejs-accessibility` to `npm run lint` (CI gate); `label-has-for` tuned, `no-static-element-interactions` untunable → ADR amended
- [x] fix the 10 audited violations (`UserMenu`, `TimeCell`, `RegisterUserModal`, incl. modal Esc-to-close)

### Step 3: Framework-level — encode a11y rules
- [x] add an Accessibility section to `.claude/rules/frontend-rules.md`
- [x] add `Check C — Accessibility` to `.claude/agents/design-review-agent.md`
- [S] propagate to master copy (none exists; `.opencode/` is a stale unmaintained mirror — ADR)
