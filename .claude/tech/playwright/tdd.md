# Playwright TDD Idioms

Tech binding for `tdd-rules.md`. Load alongside the universal rules.

## Test Disable Marker

- Playwright: `test.fail()` as the first line inside the test body — an **expected-failure** marker, NOT a skip.
  Unlike `test.skip()`, the test still RUNS every build: it stays green while it fails (RED), and fails the build the moment it starts passing (enforcing the GREEN transition).
  There is no `test.fails` in Playwright — that is the Vitest API; using it dies with `TypeError: test.fails is not a function`.
- In RED: add `test.fail()` after validating prediction. The marker cannot pin an error type, so pin the RED reason two ways: a comment above the marker documenting the failure, AND a specific assertion inside the test — so an incidental failure is not absorbed as "expected fail".
- In GREEN: remove `test.fail()` (the only test modification allowed)

## Test Description

- Use `test.describe('...')` to group scenarios by feature/page
- Test name must include "UI Test Scenario N:" prefix referencing `02_UI_Tests.md`
- Test name must include full BDD gherkin from the test spec
- **Bug-task tests are the exception**: a test written for a `bug` task is NOT a story scenario — do NOT give it a `UI Test Scenario N:` prefix (that namespace belongs to `02_UI_Tests.md`). Prefix it with the bug instead: `UI Bug #N:` where `N` is the GitHub issue number, followed by the BDD gherkin. See "Bug Test Tagging" below.

## Bug Test Tagging (GitHub Issue)

When a Playwright test is written in a **bug task's** TDD cycle, tag it with the bug's GitHub issue number (see `.claude/rules/workflow.md` → "Bug Tasks → GitHub Issues"). Use the `allure-js-commons` runtime API — the same import as Vitest:

```ts
import { issue } from 'allure-js-commons';

test('UI Bug #127: unexpected login failure shows a generic error banner - Given ...', async () => {
  await issue('127');
  // ...given / when / then via Statements
});
```

- Call `await issue('127')` as the **first line** of the body — before `test.fail()` in RED, so the link attaches when the test runs.
- Pass the bare number; the report links it via `links.issue.urlTemplate` configured in `playwright.config.ts`'s `allure-playwright` reporter.
- `allure-js-commons` is a direct devDependency. Only tag bug-task tests; story-scenario tests are NOT tagged.

## Full-Stack Journey (top-tier E2E)

Concrete mechanism for the `fullstack-journey` step (`workflow.md` → "Full-Stack Journey Step") and the verdict (`tdd-rules.md` → "Top-Tier Full-Stack Journey Assessment").

- **Location & suffix:** `frontend/acceptance/tests/fullstack/*.fullstack.spec.ts`. The `.fullstack.spec.ts` suffix is what isolates the tier — the default `npm run test:e2e` (`--project=chromium`) excludes it; the `fullstack` Playwright project (`retries: 2`) runs only it.
- **Real stack, no mocks:** real frontend + backend + Postgres + Mailpit, **no `page.route`**. It verifies the actual frontend↔backend HTTP contract on the critical lifecycle path. Run recipe (infra compose, `fullstack` backend profile, seed script, Vite, `npm run test:e2e:fullstack`) is in `frontend/acceptance/tests/fullstack/README.md`; ADR in `ProductSpecification/tasks/7-refactoring-full-stack-contract-e2e/decisions/`.
- **Cadence:** nightly only (`.github/workflows/nightly-fullstack-e2e.yml`), never per-PR.
- **One growing journey:** prefer **extend** — weave the story's critical step into the existing `account-lifecycle.fullstack.spec.ts` spine, reusing the page Statements from `../statements/` built during the frontend phase (do not duplicate navigation/locators — one page object per page, same rule as the mocked suite). Add a **new** `*.fullstack.spec.ts` only for an independent lifecycle that doesn't sit on the existing spine. Edge cases NEVER go here — they stay in the fast mocked suite.
- **Unique-per-run data:** the journey mints a unique identity per run (e.g. `fsuser_<unique>`) so `retries: 2` never collides on "already exists". Read tokens/links from Mailpit via the backend Statements, not by reaching into the DB.
- **No `test.fail()` / no issue tagging here:** the journey is a real passing E2E, not a RED scenario and not a per-scenario story slot.

## Statements Assertions (Playwright Built-in)

- Real `await expect(locator).toBeVisible()` calls, real `await expect(locator).toHaveText(...)` assertions
- Even when `toBeVisible()` is checked implicitly by Playwright auto-wait, the explicit assertion must stay
- Use descriptive assertion messages via `expect(locator, 'task form is displayed').toBeVisible()`

## Route Intercepts

- Backend Statements handle `page.route()` intercept configuration — never in page Statements
- Tests call backend Statements directly for all setup

## Statements Dependencies

- Statements are plain TypeScript classes — instantiated in test fixtures or `beforeEach`
- Constructor receives `page` (and `appUrl` when needed)
- Reuse `AuthenticationStatements`: `login(page)` for API session; `LoginPageStatements.loginAsTestUser(page, appUrl)` for browser login flow

## Test Verification

Run via: `Skill tool: skill="test-acceptance", args="frontend {TestClassName}"`
