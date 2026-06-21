# Frontend Rules

## Humble Object Pattern

- Pure logic in logic files: validation, state computation, request building, data mapping. No side effects.
- HTTP client in API client files: fetch calls, response mapping, error handling.
- Component files are thin wrappers: call logic + API, translate UI state from logic files, render markup.
- FORBIDDEN in component files: business logic, validation regex, direct fetch calls, request building.

## Async Action Buttons (Loading State)

- **Any control that triggers a network request (or any async operation) MUST reflect its in-flight state.** While the request is pending: show a loading indicator on the control AND disable the control so it cannot be re-triggered (prevents double-submit). For form submissions, also disable the form's input fields during the in-flight request. Drive this from a reactive `submitting`/`pending` state set true before the call and reset in a `finally` block (presentational component state — not `.logic.ts`).
- **Verification gate — confirm with the user.** Whenever you add or modify a control that calls the backend (or any async action) and it has NO loading state, STOP and confirm with the user whether one is needed before leaving it without. Loading state is the default expectation; the exceptions (instant/optimistic UI, fire-and-forget) are deliberate choices the user makes, not omissions you decide silently.
- **Extract a shared loading-button when 2+ controls need it.** When two or more components have async-action buttons with the same spinner/disabled/label treatment, extract a shared component (props: label, loading label, loading flag, disabled, test-ids) into the shared UI directory instead of duplicating the pattern per page.

## Mockup Placeholder Data

Mockups contain placeholder values (`user@example.com`, fake dates, sample prices). NEVER copy these into components as hardcoded strings. User-specific data (email, name, company) must come from auth context or API responses. If a value is different per user or per session, it must be dynamic.

## Component Size

- When a component file exceeds ~70-100 lines, extract sub-components (views, sections, cards) into their own files in the same `components/` directory.
- Page components should be thin routers/orchestrators -- fetch data, route between views, render child components.
- Helper components used by only one view live in that view's file. When a helper is shared across views, give it its own file.

## Feature Structure

- Features are organized in self-contained directories with subdirectories for components, logic, API clients, and tests.
- Feature-specific components stay in the feature's components directory.
- Reusable components shared across features live in a dedicated shared UI directory.

## Naming

- Logic functions: verb+noun (`validateEmail`, `buildRegistrationRequest`, `isFormValid`).
- API functions: verb+noun matching endpoint (`registerUser`, `verifyEmail`).
- Types: `{Feature}Request`, `{Feature}Response`, `{Feature}FormState`.
- Test blocks: use the test runner's block and case syntax (see tech binding for specific conventions).

## Testing

- Logic tests: pure functions, no DOM, no framework rendering. Use the frontend test runner (see technology.md Conventions).
- API client tests: frontend test runner + HTTP mock library.
- The test skip marker (see technology.md Conventions) is the frontend equivalent of the backend test disable marker; it marks a known-failing (RED) test that still **runs** every build — it stays green while it fails and fails the build once it passes, enforcing the GREEN transition. A comment above it documents the failure reason. Because the marker cannot pin an error type, **pin the RED reason via a specific assertion inside the test** so an incidental failure isn't absorbed as "expected fail".
- **Snapshots/approval: deterministic data only, native runner.** When approval/snapshot testing fits (rendered text, view-model outputs, request payloads), use the frontend test runner's built-in snapshots — never add an external approval library. Snapshot only deterministic DATA (fix all variable inputs); do NOT snapshot component markup — it is brittle and can freeze hardcoded placeholder data that design-review must catch. Visual fidelity stays with align-design + design-review. See the tech binding for the runner's snapshot API.
- Use the native `fetch` API (not axios). Base URL from the backend URL environment variable.
- **NEVER hardcode `http://localhost:8080`** in HTTP mock handlers or production code. Use the backend URL environment variable -- the test runner sets it dynamically from the backend port. Production API clients read the variable with a fallback to empty string. HTTP mock tests read the variable for handler URLs.

## Playwright Tests

- 2-tier DSL: Test Class (thin, reads like English) + Statements Class (locators, actions, assertions).
- Use `data-testid` attributes for locators. Components MUST include them.
- FORBIDDEN locator strategies: class-based selectors, tag-based selectors, raw CSS class selectors. These break when styling changes. Always use `data-testid`.
- FORBIDDEN in-app navigation via URL: never use direct URL navigation to move between pages. Tests must navigate through UI interactions (clicking buttons, links, menu items). Allowed direct URL uses: (1) app root as test entry point, (2) external entry points where users genuinely arrive via URL -- deep links, shared links.
- Page Statements own browser interactions only (`navigate*`, `enter*`, `click*`, `assert*`). All infrastructure and backend setup (mock stubs, API calls, mock configuration) goes through backend Statements that the test injects directly -- never delegated through page Statements.
- **One page object per page -- no duplicated navigation across Statements.** When 2+ Statements classes navigate to or drive the same page, the navigation (`navigate*`, the page URL/`page.goto` path) and any shared per-page locators belong in ONE page-object Statements class (`{Page}PageStatements`) that tests inject directly. NEVER copy a `navigate*` method, a URL, or a `page.goto(...)` path into multiple per-scenario Statements -- per-scenario Statements hold only that scenario's distinct actions/assertions and reuse the page object for navigation. This is the browser-test analog of the backend Acceptance Test Client Layer (`{Feature}Api`/`{Feature}Client`), which centralizes per-endpoint transport so it is defined once (see `tdd-rules.md`). Tests inject the page object **directly** alongside the scenario Statements -- it is not reached by forwarding through another Statements, so it is not a middleman delegator. The trigger is repetition: the first scenario for a page may keep navigation inline; extract the page object the moment a second scenario needs the same navigation.
- Full conventions in red-playwright and green-playwright skill templates.
- **Mass failure diagnosis:** When all E2E browser tests fail uniformly (connection errors, timeouts), the cause is infrastructure -- not browser/driver versions. Re-verify backend is alive (health endpoint) before investigating individual tests. A dead backend causes frontend pages to error out, the browser driver to time out, and connections to reset -- which looks like a browser compatibility issue but isn't.
- **Assertion detail level:** Assertions must match spec detail level -- when the spec says "cards with title, status, assignee, and priority", verify each sub-element within each card and assert visible + non-empty. A count-only check loses the spec's intent. Read the DSL Technical Reference table in the test spec.

## Conditional Class Logic

- When the same conditional class expression (ternary, logical AND, or template literal with conditions) appears in 2+ elements, extract it into a helper function (e.g., `getStatusClassName(status)`) above the component. The helper takes the condition value and returns the class string.
- This applies to any repeated branching over the same variable to produce class strings.
- Single-use conditional classes are fine inline. The trigger is repetition.

## CSS Utility Extraction

Extract inline CSS utilities to semantic extracted CSS classes in the theme stylesheet when either condition is met:
- **Repeated pattern**: 2+ components share the same utility combination (deduplication)
- **Opaque chain**: any utility with an arbitrary value (`[#hex]`, `[Npx]`, `[N.Nrem]`) that obscures visual intent -- a semantic name is more readable even if used once

Leave simple, self-documenting utilities inline. The test: can a developer understand the visual intent without mentally decoding the values? If not, extract.

## Icons

- ALWAYS use the icon library (see tech binding) -- never write inline SVG paths or elements. Claude generates broken/unrecognizable SVG paths.
- Use the icon library's components which render correct, tested SVGs.
- Standard sizes: small for inline text, medium for nav items, large for prominent display.
