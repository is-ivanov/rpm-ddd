# Task 139: Add oxlint static analysis to the frontend

Type: refactoring
Issue: #139  <- enhancement issue (not a bug); tracks this frontend-tooling change

## Problem

The Vue 3 + TypeScript frontend (`frontend/`) lints today with `eslint . && prettier --check .`
(`npm run lint`). ESLint is thorough but slow, and the JS/TS logic layer (`*.logic.ts`,
API clients) would benefit from a fast first-pass correctness linter run on every
local save and as a cheap CI gate.

## Solution

Introduce [oxlint](https://oxc.rs/docs/guide/usage/linter.html) — a Rust-based linter
~50–100x faster than ESLint — as a fast first-pass gate, **complementing** (not
replacing) ESLint. Division of labor:

- **oxlint** owns the JS/TS logic layer correctness/suspicious rules (runs in ms).
- **ESLint** keeps Vue-template-specific (`eslint-plugin-vue`) and type-aware rules
  it does better; `eslint-plugin-oxlint` disables ESLint rules already covered by
  oxlint to avoid double-reporting.
- **Prettier** stays the sole formatter — oxlint is a linter, not a formatter.

Composition: `lint:oxlint` runs first as a fast gate, then `lint` runs ESLint +
Prettier for the rules ESLint still owns. CI's `frontend-lint` job fails on any
oxlint violation.

## Key Files

- `frontend/package.json` — add `oxlint` (+ `eslint-plugin-oxlint`) dev deps; add
  `lint:oxlint` script; compose with `lint`
- `frontend/.oxlintrc.json` — new config (categories: correctness/suspicious;
  ignores aligned with `eslint.config.js`: `dist/`, `coverage/`, `node_modules/`,
  `test-results/`, `playwright-report/`, `**/*.d.ts`)
- `frontend/eslint.config.js` — append `eslint-plugin-oxlint` flat config to turn
  off ESLint rules oxlint already covers
- `.github/workflows/code-quality.yml` — `frontend-lint` job runs oxlint before
  (or alongside) `npm run lint`
- `frontend/README.md` and/or `AGENTS.md` — document the two-linter workflow
- `.claude/tech/vue-ts/infrastructure.md` — "Static Analysis (Pre-Commit)" note so
  `/continue`'s pre-commit gate knows to run oxlint

## Acceptance

- `npm run lint:oxlint` runs clean (or with a triaged, documented baseline) on the
  current codebase.
- `npm run lint` (ESLint + Prettier) stays clean — no double-reporting after
  `eslint-plugin-oxlint` is wired.
- CI's `frontend-lint` job fails on a new oxlint violation.
- The two-linter workflow is documented.

## Related

- Frontend lint CI job added in Task 2 (#…) — this task extends it.
