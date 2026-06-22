# Task 193: Decide — Component Naming Convention + name Option

Type: refactoring
Issue: #193

> **Discussion task.** Decide *whether* to apply before writing any code. Step 1 produces the
> decision; implementation steps are added only if adopted.

## Dependencies

- **Independent** — no upstream or downstream blocker within the FE-audit family. Pure naming
  convention + `name` decision; can be done at any time.
- Mild overlap with #190 Step 4 (dedup touches the same auth components) — sequence the two to avoid
  edit churn, but neither blocks the other.

## Problem

The FE audit notes inconsistent component naming (two card components carry a `Card` suffix, one does
not — `ActivationResultCard` vs `ActivationSuccess`/`ActivationExpired`) and that components lack an
explicit `name`, so the Vue devtools component tree shows everything as anonymous during manual
debugging.

## Our current approach

- `<script setup>` SFCs — no explicit `name` (Vue infers from filename in devtools, but not always
  reliably for anonymous/inline cases).
- Naming convention in `frontend-rules.md` (Naming section) covers logic/API/types/test blocks but
  does not pin a component-suffix convention (`*Card`, `*View`, `*Page`).

## Constraints from `.claude` rules

- `frontend-rules.md` Naming: logic = verb+noun, types = `{Feature}Request/Response/FormState`,
  components extracted by size (`~70-100` lines) into `components/`.
- Component Size rules already drive extraction; a naming convention should complement, not fight them.

## Senior's remark

> "«Почерк» в коде не человека: 3 одинаковых компонента-карточки названы 2 с суффиксом Card, один без.
> Компоненты не содержат поле name — при ручном дебаге не понять, что за компонент в дереве, все
> подписаны как unknown."

## Pros / Cons

**Adopt (consistent suffix + explicit `name`):**
- + Readable component tree in devtools; faster manual debugging.
- + Consistent, predictable file/component names.
- − With `<script setup>`, `name` needs a second `<script>` block or a convention/lint rule;
  small per-file overhead.

**Keep as-is:**
- + No churn.
- − Inconsistent suffixes; anonymous tree nodes during debugging.

## Decision

### Findings (Step 1 analysis)

**1. The `name`-field complaint is outdated for this codebase.**
Per the Vue docs, since v3.2.34 a `<script setup>` SFC **automatically infers its `name` option from
the filename** — used for DevTools inspection, warning traces, and `<KeepAlive>` include/exclude. This
project runs Vue `^3.5.13` with `@vitejs/plugin-vue@6`, well past that threshold, so the DevTools tree
already shows real names (`ActivationResultCard`, `ActivationSuccess`, …), not "unknown". The
"anonymous tree node" symptom the senior describes applies to pre-3.2.34 / non-`<script setup>` inline
components, not to ours. Declaring an explicit `name` would require an awkward second plain `<script>`
block per SFC for zero behavioural gain. `vue/multi-word-component-names` (`error`, in `eslint.config.ts`)
already enforces meaningful multi-word names.

**2. The suffix "inconsistency" largely predates the dedup.**
`ActivationResultCard.vue` is the single shared presentational card. `ActivationSuccess.vue` and
`ActivationExpired.vue` are thin **state-preset wrappers** that delegate to it with fixed props — they
are not generic, reusable cards, so naming them `*Card` would be misleading. The senior's
"3 cards, 2 with `Card`, 1 without" reflects the pre-extraction state; after the Story 1 / #190 dedup
there is one `*Card` plus two state-named presets. The remaining components already follow consistent
role suffixes: `*Page` (`ActivationPage`, `LoginPage`), `*Banner` (`ActivationErrorBanner`,
`LoginErrorBanner`), `*Field` (`PasswordField`), `*Card` (`ActivationResultCard`).

### Decision: documentation-only adoption (no code churn)

- **Pin the existing role-suffix convention** in `frontend-rules.md` (Naming section) so it is explicit
  going forward: presentational reusable card → `*Card`; full-page route component → `*Page`;
  notification/alert → `*Banner`; single form control → `*Field`. State-specific preset wrappers keep
  semantic state names (`ActivationSuccess`/`ActivationExpired`) — they describe a state, not a type.
- **No explicit `name` mechanism** — auto-inferred by Vue 3.5; the lint rule covers naming.
- **No renames** — current names are already correct/consistent post-dedup.

_Pending user confirmation in the Story Completion / decision gate before Step 2 is finalized._

## Key Files

- `frontend/src/features/auth/components/{ActivationResultCard,ActivationSuccess,ActivationExpired}.vue`
- `frontend-rules.md` (Naming section)
