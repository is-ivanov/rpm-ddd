# Task 150: Bring frontend/acceptance under strict TS + inspection config

Type: refactoring
Issue: #150

## Problem

The entire `frontend/acceptance/**` tree (Playwright specs + Statements) is outside any tsconfig
`include`: `frontend/tsconfig.json` has `"strict": true` but `"include": ["src/**/*.ts", ...]`.
Consequences:

1. The build's `vue-tsc --noEmit` silently skips acceptance files — the acceptance suite has
   **never** been type-checked.
2. IntelliJ analyses those files with a non-strict fallback → **false-positive** "unnecessary
   non-null assertion" warnings on `match!` / `token!` / `session!` / `csrf!` that are in fact
   REQUIRED under strict.
3. SonarLint S2068 ("hard-coded password") fires on intentional test fixtures (e.g. the full-stack
   journey's test password).

Discovered during Task 7 (#120); logged there as improvement I2.

## Solution

Config-level only — no runtime behaviour change, and do NOT remove the existing `!` assertions
(they are correct under strict):

- Bring `frontend/acceptance/**` under strict TS type-checking (a strict `include` or a dedicated
  `tsconfig.acceptance.json` via project references) and wire a type-check into the lint/CI gate.
  Fix the latent type issues this surfaces across the existing acceptance tree.
- Scope the IDE/SonarLint inspections so secret-detection heuristics (S2068) don't fire on
  intentional test fixtures in the acceptance tree.

**Acceptance criterion:** the false-positive `!` warnings disappear (the `!` are validated as
required under strict), the acceptance suite is type-checked by the gate, and no new lint/IDE errors
remain.

## Key Files

- `frontend/tsconfig.json` (and/or a new `frontend/tsconfig.acceptance.json`)
- `frontend/package.json` — lint / type-check script wiring
- `.github/workflows/build.yml` — frontend type-check gate (if added there)
- `frontend/acceptance/**` — any type fixes surfaced by enabling the check
