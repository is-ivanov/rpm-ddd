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

_To be filled in Step 1._ If adopted: pin the suffix convention in `frontend-rules.md`, decide the
`name` mechanism (e.g. lint rule / second script block), and rename the inconsistent cards.

## Key Files

- `frontend/src/features/auth/components/{ActivationResultCard,ActivationSuccess,ActivationExpired}.vue`
- `frontend-rules.md` (Naming section)
