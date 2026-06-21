---
name: demo
description: Run a Playwright test in headless slow-motion mode while recording a video (.webm) of the run, so the user can open and review it afterward. Use when the user wants to demo, watch, or capture a recording of a Playwright test, or mentions /demo command.
---

# /demo - Record a Playwright Test Run as Video

## Usage
```
/demo should_display_board_with_columns
/demo CreateTaskPageTest.should_display_create_task_form
/demo CreateTaskPageTest                          # Run all tests in class
/demo                                             # Run all frontend Playwright tests
```

## What It Does

Temporarily modifies the browser test configuration to run the test **headless with slowMo and video recording on**, producing a small `.webm` file the user can open and review at their own pace (and delete when done). No live browser window is opened — recording a file is more reliable than a live window, which on Windows opens behind the IDE and finishes before the user can switch to it. All config changes are reverted after the test completes (even on failure).

## Setup

Read `ProductSpecification/technology.md`:
- `tech-profile` block → resolve the `browser-testing` concern (e.g. `playwright`) for the test runner, config file, and run command.
- Resolve the `backend` concern for the clean-environment step (run/stop/health commands) — only needed when the demo exercises backend-dependent scenarios.
- **Acceptance/E2E test command** from the Conventions table.

## Workflow

1. **Apply demo changes** — enable headless video recording + slowMo (Playwright `use` block, or Selenium base-class delay).
2. **Ensure clean environment** — restart backend fresh, clear email inbox, verify frontend (FE-only scenarios skip backend).
3. **Run the test** — resolve the argument to a test filter (see table in template), run headless with a generous timeout.
4. **Surface the recording** — move the produced `video.webm` to a friendly name and report its absolute path.
5. **Revert all changes (ALWAYS)** — undo the step-1 config edits, pass or fail (the gitignored recording stays).
6. **Report result** — pass/fail plus the absolute recording path (and error output on failure).

See `.claude/tech/{browser-testing}/templates/demo/procedure.md` for the full per-step procedure (config edits, clean-environment, run, surface recording, revert).

## Rules

- ALWAYS revert config changes, even if the test fails or times out
- Do NOT commit any demo changes — the working tree must be clean after `/demo` completes (the gitignored recording does not affect this)
- Do NOT open a live/headed browser window — record a video file instead
- Use `slowMo: 2000` for the recording
- Do NOT leave the backend process running after — it was only restarted for a clean DB
