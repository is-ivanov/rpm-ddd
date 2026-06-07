# Playwright TDD Idioms

Tech binding for `tdd-rules.md`. Load alongside the universal rules.

## Test Disable Marker

- Playwright: `test.skip()` as first line inside the test body
- In RED: add `test.skip()` after validating prediction
- In GREEN: remove `test.skip()` (the only test modification allowed)

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

- Call `await issue('127')` as the **first line** of the body — before `test.skip()` in RED, so the link still attaches while the test is skipped.
- Pass the bare number; the report links it via `links.issue.urlTemplate` configured in `playwright.config.ts`'s `allure-playwright` reporter.
- `allure-js-commons` is a direct devDependency. Only tag bug-task tests; story-scenario tests are NOT tagged.

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
