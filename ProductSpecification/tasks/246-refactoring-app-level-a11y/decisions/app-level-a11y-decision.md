# Decision: App-level accessibility support (lint gate + framework rules)

**Date**: 2026-07-09 **Scenarios**: Task 246

Story 4's Users-grid filter input shipped with no accessible name and was caught only by an IDE-only
SonarLint hint — accessibility is enforced nowhere (no lint rule in the CI `npm run lint` gate, no
a11y item in `align-design`/`design-review`, no a11y section in the frontend rules), so any component
can ship inaccessible markup silently.

| Rejected | Why |
|----------|-----|
| `oxlint` jsx-a11y rules | Does not parse Vue SFC templates — the markup that needs checking |
| `vuejs-accessibility` at stock `flat/recommended` | Fails CI on 17 findings, 7 of them false positives: `label-has-for` defaults to `required:{every:[nesting,id]}`, so already-correct `<label for>` + `<input id>` pairs (LoginPage, ActivationPage) are flagged |
| Hand-picked minimal rule subset | Silently drops the rest of the recommended set; future components regress on rules nobody chose to exclude |
| `no-static-element-interactions` off | Removes wrapper noise but also stops catching the genuine `<div @click>` menu-trigger defect this task exists to prevent |
| Restructure wrapper `@keydown.esc` onto the inner button | Regression: Esc stops working once focus moves into the popup panel, which no longer bubbles to the handler |
| Baseline + ratchet (PMD `maxAllowedViolations` pattern) | Surface is only 5 of 29 files; leaves 3 known keyboard-access defects live in the product |
| Propagate rules to `.opencode/` | Stale, unmaintained in-repo mirror (last touched 2026-06-14 vs `.claude/` 2026-07-06) |
| Author the missing `align-design-checklist.md` | Repairing that dangling reference is a framework bug well beyond a11y scope — raise separately |

**Chosen**: `eslint-plugin-vuejs-accessibility` in the CI lint gate at `flat/recommended`, with two
rules tuned to remove inverted/false-positive verdicts; fix all 10 real findings now (no
suppressions); encode the rules in `frontend-rules.md` and enforce them via a new mandatory
`design-review` check.

## Model

**Lint gate** (`frontend/eslint.config.ts`, runs inside the existing `npm run lint`):

- `vuejs-accessibility/label-has-for` → `required: { some: ['nesting','id'] }`. Accept *either*
  association form; the default demands both and flags correct markup.
- `vuejs-accessibility/no-static-element-interactions` → **cannot be tuned.** Step 2 verified the
  rule declares `schema: []` (no options) and treats a hardcoded handler list — `keydown` included —
  as interactive. The documented fallback applies: a targeted `eslint-disable-next-line` plus a
  written justification at the two wrapper sites.
- Remaining `flat/recommended` rules stay at default.

Both `no-static-element-interactions` and `click-events-have-key-events` skip elements carrying
`role="presentation"`. That is not a loophole but the correct markup for a modal backdrop: the
overlay is presentational, and the card it wraps is the actual `role="dialog"`.

**Framework source-of-truth** is the project's `.claude/` — confirmed no external master copy
(`~/.claude/rules/` holds only `context7.md`; `~/.claude/skills/` only plugin skills; no submodules).
`.opencode/` is not updated.

- `.claude/rules/frontend-rules.md` — new **Accessibility (a11y)** section: every interactive control
  is a natively interactive element (`<button>`, `<a>`, `<input>`); every form control has an
  accessible name via `<label for>`/nesting/`aria-label`; a pointer affordance carries a keyboard
  equivalent; disclosure triggers expose `aria-expanded`; decorative icons are `aria-hidden`.
- `.claude/agents/design-review-agent.md` — new **Check C — Accessibility**, alongside the existing
  Check A (placeholder data) and Check B (control completeness). `design-review` is already MANDATORY
  in the frontend scenario sequence, so a11y gets a per-story human/agent pass for what lint cannot
  see (focus order, semantics, contrast).

**Audit** — 10 findings across 5 of 29 components, all fixed in Step 2:

| Component | Finding | Fix |
|-----------|---------|-----|
| `UserMenu` L32 | `<div @click>` menu trigger — keyboard users cannot open the menu | native `<button>` + `aria-expanded` / `aria-haspopup` |
| `TimeCell` L35 | `<span @mouseenter/@mouseleave>` tooltip — keyboard-unreachable | pair with `@focusin`/`@focusout` on a focusable element |
| `RegisterUserModal` L110 | `<label>` decorates a static read-only timezone `<div>`, not a control | drop `<label>`; it is a caption, not a label |
| `RegisterUserModal` L80 | backdrop `@click.self` dismiss; modal has **no** Esc-to-close at all | `role="presentation"` backdrop + `role="dialog"`/`aria-modal` card; Esc-to-close via a window `keydown` listener |
| `UsersStatusFilter` L27, `UsersDateRangeFilter` L24 | wrapper `@keydown.esc` with a real `<button>` nested inside | targeted `eslint-disable-next-line` + justification (rule takes no options) |

## Edge Cases

| Case | Behavior |
|------|----------|
| `label-has-for` tuning hides a genuinely unassociated label | Still caught: `some` requires at least one of nesting/`id` — zero association fails |
| Esc-to-close while focus sits outside the modal (e.g. on the trigger button that opened it) | A `keydown` handler on the overlay would never fire; the listener is registered on `window` and removed on unmount |
| Tooltip trigger must be focusable for `@focusin`/`@focusout` to fire | `<span>` is never an interactive element, so `TimeCell`'s trigger becomes a `<button>` and links to the tooltip via `aria-describedby` |
| A future wrapper needs a genuine pointer handler on a static element | Rule fires by design — convert to a native interactive element, do not suppress |
| Contrast / focus order / reading order | Out of lint's reach — covered by `design-review` Check C, not the gate |
| `UsersGrid` filter input (the trigger example) | Already hot-fixed with `:aria-label` in `4d969ea`; the gate now prevents recurrence |
