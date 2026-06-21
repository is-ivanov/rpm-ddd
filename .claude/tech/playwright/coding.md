# Playwright Browser Testing Conventions

Tech binding for `frontend-rules.md` Playwright section. Shared section structure: `.claude/templates/coding/coding-sections.md`.

## 2-Tier DSL Architecture

1. **Test Class** -- receives `page` and `appUrl` from test fixture. Reads like English.
2. **Statements Class** -- encapsulates locators, navigation, and assertions.

## Test Class Rules

- Delegates ALL Playwright interaction to Statements class.
- NO `page.locator`, `page.getByTestId`, or `expect` calls in test class.
- NO infrastructure calls -- delegate to backend Statements.

## Statements Rules

- Locators: built via `page.getByTestId('...')` inside methods (lazy, no static constants needed).
- Methods: `navigate*`, `enter*`, `click*`, `assert*`.
- Page Statements own browser interactions only.
- `page.goto()` only for app root (`appUrl`) and external entry points.

## Page Object (shared navigation) -- no duplicated `page.goto`

Universal rule: `frontend-rules.md` Playwright section, "One page object per page". A page's navigation/URL is defined ONCE, not copied across per-scenario Statements.

- One `{Page}PageStatements` class per page (e.g. `LoginPageStatements`, `ActivationPageStatements`) owns the page's `navigate*` methods, the `page.goto(\`${appUrl}/path\`)` call, and any locators/actions shared by more than one scenario on that page.
- Per-scenario Statements (`ActivationMismatchStatements`, `ActivationStrengthStatements`, ...) hold only that scenario's distinct actions/assertions. They do NOT define their own `navigate*` or repeat the `page.goto` path.
- Tests inject BOTH the page object and the scenario Statements via the fixture and call each directly:
  ```ts
  await activationPage.navigateToActivationPageWithToken('valid-activation-token'); // shared page object
  await activationMismatch.enterMismatchedPasswords();                              // scenario Statements
  await activationMismatch.assertMismatchErrorIsDisplayed();
  ```
- Do NOT add a `navigate*` to a scenario Statements that just calls the page object's `navigate*` -- that is a forbidden middleman delegator (`tdd-rules.md`). Inject the page object directly instead.
- Construct page objects in the same test fixture that builds the scenario Statements (both receive `page` + `appUrl`); a page object never wraps or forwards to another Statements class.

## Element Locators

- Primary: `page.getByTestId('...')`.
- Fallback: `page.getByRole(...)` if natural ARIA role exists.
- Last resort: `page.getByText('...')`.
- FORBIDDEN: `page.locator('.class')`, `page.locator('div')`, CSS class/tag selectors.

## Component data-testid Attributes

Components MUST include `data-testid`. See `templates/playwright-test.md` for markup examples.

## Prerequisites

Backend running, frontend running, all logic + API tests pass.
