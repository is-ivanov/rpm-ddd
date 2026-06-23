# Task 205: Lint guard — forbid blind `as` on network response data

Type: refactoring
Issue: #205  <- recorded for traceability; refactoring task, tests are not tagged with the issue number

## Problem

Frontend API clients must validate every network response body at the boundary
(`schema.parse(...)`) instead of trust-casting it with a TypeScript `as` assertion.
This is codified in `.claude/rules/frontend-rules.md` (Humble Object Pattern) and
`.claude/tech/vue-ts/coding.md` § "Schema Validation (zod)", but there is **no
automated guard** — nothing fails the build when someone reintroduces a blind `as`
on `response.json()`.

It recurred in practice: during Story 3 (home page) a green-agent wrote
`(await response.json()) as CurrentUserPayload` in `current-user.api.ts` — the last
remaining blind cast on network JSON (since fixed). The 2026-06-20 frontend audit
(`ProductSpecification/audits/2026-06-20-frontend-audit.md`, lines 35/64/69/77)
flagged blind `as` on network data as a recurring source of silent backend-contract
drift and recommended a CI lint rule.

## Solution

Add a static-analysis rule to the `frontend/` lint pipeline (`npm run lint`, already a
CI gate in the Frontend Lint job) that fails when code casts an unvalidated network
response with `as` (e.g. `(await response.json()) as Foo`), forcing `schema.parse(...)`
at the boundary instead. Preference: implement in **oxlint** (already wired as
`lint:oxlint`, faster than eslint) if feasible.

### Feasibility (verified via Context7 against oxc.rs docs)

- oxlint does **not** support eslint's selector-based `no-restricted-syntax`, so a
  *targeted* "`as` only on `.json()` result" rule cannot be a native oxlint core rule.
- oxlint **does** support `typescript/consistent-type-assertions` with
  `assertionStyle: 'never'` — but that bans **all** `as` project-wide (would flag legit
  DOM casts like `as HTMLInputElement`), too broad without per-line disables.
- oxlint has **JS Plugins (alpha)** with an ESLint-compatible API — a custom rule
  targeting `TSAsExpression` over a `.json()` call could be written natively in oxlint,
  but the plugin API is alpha.
- ESLint `no-restricted-syntax` with an AST selector (e.g.
  `TSAsExpression[expression.callee.property.name='json']`) expresses the targeted rule
  precisely and stably, in the already-present eslint step.

### Open design decision (resolve in Step 1 before implementing)

- **(A)** oxlint broad ban `consistent-type-assertions: 'never'` + per-line disables for
  legit casts.
- **(B)** oxlint alpha JS plugin with a targeted custom rule.
- **(C)** ESLint `no-restricted-syntax` targeted selector (precise, stable, but in eslint).

**Recommendation:** start with **(C)** for a precise, stable guard now; revisit **(B)**
when oxlint JS plugins leave alpha.

### Decision (Step 1 — resolved 2026-06-23)

**Chosen: (C) ESLint `no-restricted-syntax` targeted selector**, added to the existing
`eslint .` step in `npm run lint` (already a CI gate). Rationale: it expresses the
*targeted* "`as` on a `.json()` result" rule precisely and stably, leaves legitimate DOM
casts (`as HTMLInputElement`) untouched, and needs no per-line disables (rules out A) and
no alpha API (rules out B). Revisit (B) once oxlint JS plugins leave alpha.

The exact AST selector will be pinned against a real fixture in Step 2: the recurring
real-world shape is `(await response.json()) as Foo`, where the `.json()` `CallExpression`
sits inside an `AwaitExpression` — so a naive `TSAsExpression > CallExpression` direct-child
selector does not match. Step 2 derives the working selector from the fixture, covering both
the `await`-wrapped and un-`await`-ed forms.

## Affected Layers

Frontend tooling/config only. No production code changes beyond config.

## Key Files

- `frontend/.oxlintrc.json` — oxlint config (approach A/B)
- `frontend/eslint.config.ts` — eslint flat config (approach C)
- `frontend/package.json` — `lint` / `lint:oxlint` scripts (CI gate)
- a fixture proving the rule fires on a blind `as` over `response.json()` and stays
  green on `schema.parse(...)`
- `.claude/rules/frontend-rules.md`, `.claude/tech/vue-ts/coding.md` — the rule this
  guard enforces (reference only, not edited)
