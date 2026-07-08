# Vue 3/TypeScript TDD Conventions

## Testing Framework

- Logic tests: Vitest, pure functions, no DOM, no Vue.
- API client tests: Vitest + MSW (Mock Service Worker).

## Test Data Builders (Object Mother)

Shared test data lives in **`frontend/src/test/builders/`** as `{ ...DEFAULTS, ...overrides }` object-mothers — the FE analog of the backend `UserBuilder` / 3-tier `Scope`. Never re-inline the same literal across test files.

- **One builder per shape, named `aX(overrides?)` / `anX(overrides?)`**, returning private `DEFAULTS` merged with the caller's `overrides` (e.g. `anAuthenticatedUser`, `aCurrentUserResponse`, `anUnauthenticatedProblem`). `userWith()` in `users-grid.logic.test.ts` is the same pattern kept inline.
- **Defaults are the canonical, byte-equal body.** Reproduce the exact values consumers previously inlined (ids, emails, timezones) so swapping a literal for the builder changes no assertion — a whole-object `toEqual(...)` still holds.
- **Override only the fields the assertion depends on.** In the given, pass just what drives the checked behavior — `anAuthenticatedUser({ firstName: 'jane', lastName: 'smith' })` when only the name→initials mapping is under test; the rest stay at defaults and are irrelevant. Stating only what matters is the point of the mother.
- **Given variable vs shared fixture — pick by role:**
  - *Direct SUT input* (each test has its own subject) → a **local** `const` in the given, named for the concept (`const authenticatedUser = anAuthenticatedUser({ ... })`), passed straight into the SUT.
  - *Canonical fixture reused as both seed and expected value across several tests, compared whole* → a **shared module-level** `const` (e.g. `const JOHN_DOE = anAuthenticatedUser()`); localizing it per test would duplicate it and misrepresent the expected value.
- **AAA layout:** separate arrange / act / assert with a **blank line**, not `// given` / `// when` / `// then` label comments — matches the existing suite.

## Content / Snapshot Approval (Native Vitest)

For approval/snapshot verification — the universal "rendered-content verification" rule. Use Vitest's **built-in** snapshots; do NOT add an external approval library (selfie has no released JS port).

- `toMatchInlineSnapshot()` — small deterministic outputs (view-model objects, request payloads). Auto-populates on first run, updated with `vitest -u` (mirrors the literal-snapshot DX).
- `toMatchFileSnapshot('./__snapshots__/x.html')` — larger rendered text (e.g. an HTML fragment).
- **Scope to deterministic DATA only** — logic/view-model outputs and request bodies. Do NOT snapshot component markup: it is brittle (churns on every style/markup tweak) and can freeze the hardcoded placeholder data that `design-review` is meant to catch. Visual/markup fidelity stays with `align-design` + `design-review`, not snapshots.
- **Determinism (mandatory):** fix all variable inputs (no `Date.now()`/random ids) so snapshots don't flake.
- **CI:** snapshots must be read-only on CI (fail on mismatch); never run `-u` in CI.

## Assertion Messages (rationale over comments)

Vitest's `expect(actual, message?)` shows `message` as the failure description in the console — the frontend analog of the backend's AssertJ `as(...)`. The precedent already exists in Playwright Statements (`.claude/tech/playwright/tdd.md`: `expect(locator, 'task form is displayed').toBeVisible()`); apply the same idea to Vitest logic / API-client tests.

- **Per-assertion rationale → into the message arg.** When an assertion needs a "why THIS expected value / what a wrong impl would produce" explanation (a pinned discrimination like AND-vs-OR, an unknown-status-sorts-last invariant), attach it as `expect(actual, 'AND ⇒ 2 rows; OR ⇒ 4 — pinned so an OR impl differs').toEqual(...)` instead of a `//` comment above the assertion. It then prints on failure, when the rationale is most useful, instead of rotting in a comment only a source-reader sees.
- **Block / setup / dataset-level context stays a comment.** Notes that explain a shared fixture, the construction of a dataset, or contract lock-step across files have no single-assertion anchor — keep them as comments above the block.
- **Delete a comment** only when nothing but a restatement of the test title survives.
- Behavior-preserving: the message shows only on failure; `toEqual` / `toBe` outcomes are unchanged.

## RED-Phase Marker (`it.fails`)

- **Marker:** Vitest's [`it.fails` / `test.fails`](https://vitest.dev/api/#test-fails) — replaces `.skip` for committing a known-failing (RED) test. Referenced as "test skip marker" in universal rules. The frontend analog of the backend's `@ExpectedToFail`.
- **No `withExceptions` analog.** `it.fails(name, fn)` takes only a name + function — you cannot pin a specific error type the way junit-pioneer's `withExceptions` does. **Pin the RED reason via the assertion inside the test** — a specific `expect(...)` that fails for the predicted reason — so an unrelated failure (a typo, an import error) isn't silently absorbed as "expected fail". This assertion is the compensating control; without it the marker is as blind as the backend would be with `withExceptions` omitted.
- **Reason comment:** keep a comment above the marker documenting why it is RED (the predicted failure), e.g. `// RED — isForgotPasswordVisible not implemented yet`.
- Requires **Vitest ≥ 4.1** (this project: `vitest@^4.1.8`) so `fails`-marked tests appear in the test summary.

### Behavior

| Event | `it.fails` behavior |
|-------|---------------------|
| Test runs and fails | **expected fail** — reported as passing, build stays green (`N expected fail`) |
| Test runs and passes | **build FAILS**: `Error: Expect test to fail` — forces marker removal at GREEN |

Unlike `.skip`, the test **runs on every build** — RED state is continuously machine-verified, and GREEN is enforced (a passing test fails the build until the marker is removed).

### RED / GREEN mechanics

- **RED:** after running the test and validating the prediction (the assertion fails for the predicted reason), change `it(...)` / `test(...)` to `it.fails(...)` / `test.fails(...)`. RED commits include the `it.fails` test.
- **GREEN:** remove the `.fails` modifier (back to `it(...)`/`test(...)`) — the only test modification allowed. The implementation makes the test pass; with `.fails` still present the build fails (`Expect test to fail`), forcing removal.
- Applies to the frontend **logic** and **API client** Vitest tests. Playwright E2E uses its own skip mechanism — out of scope.

### Limitations

- **No type pinning** — any failure inside the body counts as "expected fail". The in-test assertion is the only thing keeping the RED reason precise; write it deliberately (assert the exact predicted value), never leave the body able to fail for an incidental reason.
- A genuinely-skipped test (not RED — e.g. quarantined, environment-gated) still uses `.skip`; `.fails` is exclusively the RED-phase marker.

## Bug Test Tagging (GitHub Issue)

When a Vitest test (logic or API client) is written in a **bug task's** TDD cycle, tag it with the bug's GitHub issue number (see `.claude/rules/workflow.md` → "Bug Tasks → GitHub Issues"). Use the `allure-js-commons` runtime API — the same import works for Vitest and Playwright:

```ts
import { issue } from 'allure-js-commons';

it('maps an unexpected login failure to the generic error view', async () => {
  await issue('127');
  // ...arrange / act / assert
});
```

- `issue()` is `await`-able and must be called inside the test body (first line). Pass the bare number (`'127'`); the report links it via the `links.issue.urlTemplate` configured in `vite.config.ts`'s `allure-vitest/reporter`.
- `allure-js-commons` is a direct devDependency. Other runtime tags exist (`tags`, `label`, `link`) but bug tests use `issue`.
- Only tag tests created for a bug task. Story-scenario tests are NOT tagged.

## Test Review Grep Patterns

Grep patterns for the test-review-agent checklist (the frontend counterpart of the java-spring table). Each entry maps to a checklist item number in `.claude/templates/workflow/test-review-checklist.md`.

| # | Check | Grep pattern |
|---|-------|-------------|
| 21 | Calculated expected values | `\.sort(\|\.toSorted(\|\.filter(\|\.map(\|\.reduce(\|\.reverse(\|localeCompare\|Date\.parse` (these catch re-applying the sort/filter/map/transform under test to build the expected array — the disguised, arithmetic-free form of a calculated expectation) |

## Base URL Configuration

- Base URL resolved via `import.meta.env.VITE_API_URL`.
- Vitest sets `VITE_API_URL` dynamically from `BACKEND_PORT` via `vite.config.ts`.
- Production API clients: `const BASE_URL = import.meta.env.VITE_API_URL ?? ''`.
- MSW tests: `const BASE = import.meta.env.VITE_API_URL`.
