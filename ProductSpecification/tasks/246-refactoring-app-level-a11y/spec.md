# Task 246: App-level a11y support

Type: refactoring
Issue: #246  <- task number = issue number; refactoring records it for traceability, does NOT tag tests

## Problem

Story 4's Users grid filter `<input>` shipped without an accessible label — no `id` / `<label for>` / `aria-label`; `placeholder` is a hint, not an accessible name. SonarLint flagged it ("Add an 'id' attribute to this input field and associate it with a label"). The field works for sighted users, but screen readers cannot announce its purpose.

This is **not an isolated slip** — accessibility is enforced nowhere in the project:

- **No a11y lint rule** in the CI `npm run lint` gate (no `eslint-plugin-vuejs-accessibility` or equivalent). SonarLint is IDE-only (no Sonar config, no Sonar in CI), so the gap surfaces only if a dev happens to run it locally.
- **No a11y item** in the `align-design` / `/design-review` checklists.
- **No a11y section** in `.claude/rules/frontend-rules.md`.

Net effect: every new component can ship inaccessible markup silently; violations are caught only opportunistically.

**Trigger example:** `frontend/src/features/users/components/UsersGrid.vue` filter input (hot-fixed with `:aria-label` in `4d969ea`; the systemic gap remains).

## Solution (proposed — needs architectural discussion / ADR)

1. **Lint gate** — add an a11y linter (e.g. `eslint-plugin-vuejs-accessibility`) to `npm run lint` so inaccessible markup fails CI, not just the IDE. Audit existing components for violations and fix them.
2. **Framework rules** — add an a11y section to the frontend rules + an a11y checklist item to `align-design` and `/design-review`, so a11y is verified every story instead of opportunistically.
3. **Location decision (OPEN)** — where does the continue-framework source-of-truth live?
   - Investigation so far: the active framework is in the **project's** `.claude/` (`C:\Users\ivan\.claude\rules\` contains only `context7.md`, NOT the framework; `.claude/skills/` there holds plugin skills only — cavecrew/caveman/context7-mcp/ij-debugger).
   - Confirm whether a master/template copy of the framework exists elsewhere (separate repo, a user-level mirror) that must also be updated, so the improvement propagates to other projects built on the same framework.

This is an **architectural improvement** — discuss scope / tool / location before implementing. The first step is an ADR capturing the decisions; implementation follows.

## Key Files

- `frontend/eslint.config.*` — a11y lint plugin integration (CI gate).
- `frontend/src/features/users/components/UsersGrid.vue` — trigger example (already hot-fixed).
- `.claude/rules/frontend-rules.md` — add a11y section (Humble Object / component rules).
- `.claude/skills/align-design/SKILL.md` + align-design checklist template — a11y verification step.
- `.claude/skills/design-review/` — a11y in the mandatory design-review pass.
- (Open) master/template copy of the framework, if one exists outside the project.
