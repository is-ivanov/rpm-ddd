---
name: demo
description: Run a Playwright test in visible (non-headless) mode with slowdown so the user can watch it. Use when user wants to demo or visually watch a Playwright test or mentions /demo command.
---

# /demo - Run Playwright Test in Visible Slow-Motion Mode

## Usage
```
/demo should_display_board_with_columns
/demo CreateTaskPageTest.should_display_create_task_form
/demo CreateTaskPageTest                          # Run all tests in class
/demo                                             # Run all frontend Playwright tests
```

## What It Does

Temporarily modifies the browser test configuration (headed mode + slowMo + maximized window) to run browser tests visibly with delays, so the user can watch the test execute in real time. All changes are reverted after the test completes (even on failure).

## Setup

Read `ProductSpecification/technology.md`:
- `tech-profile` block → resolve the `browser-testing` concern (e.g. `playwright`) for the test runner, config file, and run command.
- Resolve the `backend` concern for the clean-environment step (run/stop/health commands) — only needed when the demo exercises backend-dependent scenarios.
- **Acceptance/E2E test command** from the Conventions table.

## Workflow

### 1. Apply Demo Changes

**Playwright stack (`browser-testing: playwright`):** temporarily edit the `use` block in `frontend/playwright.config.ts` to add visible, watchable settings, and run the test with `--headed`:

```ts
use: {
  baseURL: appUrl,
  trace: 'on-first-retry',
  viewport: null,                                            // use the maximized window size
  launchOptions: { slowMo: 1200, args: ['--start-maximized'] },
},
```

- `--start-maximized` + `viewport: null` make the Chrome window open maximized and **come to the foreground** (in front of the IDE). Playwright cannot steal OS focus on Windows on its own, so without `--start-maximized` the window opens hidden behind the active terminal/IDE and the user can't watch the demo.
- Keep `slowMo` at **1200ms** — do NOT slow further (2000ms felt too slow).

**Selenium/Java base-class stack:** set the UI test base class close-browser-after-tests flag to false and comment out headless mode; add a 1200ms demo delay constant + method in the Browser statements class and call it at the start of navigation / find-element / find-elements methods.

### 2. Ensure Clean Environment

- Stop any backend you started earlier via the `/stop-backend` skill (never kill Java by name)
- Clear test email inbox via infrastructure HTTP API
- Start backend fresh via the `/run-backend` skill (background)
- Wait for backend to be UP: poll the health endpoint (see backend tech binding → "Health Check")
- Verify frontend is running (start with `/run-frontend` if not)

### 3. Run the Test

Resolve the argument to a test filter using the acceptance test command pattern from Conventions table.

| Argument | Filter |
|----------|--------|
| `should_method_name` | Filter to `*.ClassName.should_method_name` (search test files to resolve class) |
| `ClassName.should_method_name` | Filter to `*.ClassName.should_method_name` |
| `ClassName` | Filter to `*.ClassName` |
| *(none)* | Run all frontend acceptance tests |

Use a generous timeout (180s) since delays add up.

### 4. Revert All Changes (ALWAYS)

After the test finishes (pass or fail), revert the config to its original state — undo all changes from step 1 (the `playwright.config.ts` `use` edits, or the base-class/statements changes for the Selenium stack).

### 5. Report Result

Report whether the test passed or failed. If it failed, include the error output.

## Rules

- ALWAYS revert changes, even if the test fails or times out
- Do NOT commit any demo changes
- Do NOT leave the backend process running after — it was only restarted for a clean DB
- The working tree must be clean after `/demo` completes
