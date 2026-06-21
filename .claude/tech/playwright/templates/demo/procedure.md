# /demo — Full Per-Step Procedure (Playwright / Selenium)

The full per-step procedure for the `/demo` skill. Resolve the `browser-testing` concern (config file, run command) and the `backend` concern (run/stop/health) from `ProductSpecification/technology.md` before starting.

## 1. Apply Demo Changes

**Playwright stack (`browser-testing: playwright`):** temporarily edit the `use` block in `frontend/playwright.config.ts` to enable video recording and slow the run down. Do **not** add `--headed` or `--start-maximized` — the run stays headless and writes a video file instead:

```ts
use: {
  baseURL: appUrl,
  trace: 'on-first-retry',
  video: { mode: 'on', size: { width: 1280, height: 720 } },
  launchOptions: { slowMo: 2000 },
},
```

- `video: { mode: 'on', ... }` records every test in the run to `frontend/test-results/<test-dir>/video.webm`. `test-results/` is gitignored, so recordings never pollute the working tree.
- `slowMo: 2000` adds a 2000ms pause before each action so the steps are clearly visible in the recording (user preference for the recorded flow — the slower pace is fine since it's a file, not a live window).
- The `.webm` opens in any browser (drag it into a Chrome/Edge tab) or media player. `ffmpeg` is not assumed to be installed, so do not attempt gif conversion unless `ffmpeg -version` succeeds.

**Selenium/Java base-class stack:** enable the framework's video/screen-recording option for the run (do not switch to a visible window); add a 2000ms demo delay constant + method in the Browser statements class and call it at the start of navigation / find-element / find-elements methods.

## 2. Ensure Clean Environment

- Stop any backend you started earlier via the `/stop-backend` skill (never kill Java by name)
- Clear test email inbox via infrastructure HTTP API
- Start backend fresh via the `/run-backend` skill (background)
- Wait for backend to be UP: poll the health endpoint (see backend tech binding → "Health Check")
- Verify frontend is running (start with `/run-frontend` if not)
- **FE-only scenarios** (no backend dependency — mocked via `page.route`, or pure UI) skip the backend steps entirely; only the frontend dev server is required.

## 3. Run the Test

Resolve the argument to a test filter using the acceptance test command pattern from Conventions table. Run headless (no `--headed`).

| Argument | Filter |
|----------|--------|
| `should_method_name` | Filter to `*.ClassName.should_method_name` (search test files to resolve class) |
| `ClassName.should_method_name` | Filter to `*.ClassName.should_method_name` |
| `ClassName` | Filter to `*.ClassName` |
| *(none)* | Run all frontend acceptance tests |

Delete `frontend/test-results/` before the run so the recording is fresh. Use a generous timeout (180s) since slowMo delays add up.

## 4. Surface the Recording

After the run, locate the produced `video.webm` under `frontend/test-results/<test-dir>/` and **move** it (`mv`, not `cp`) to a friendly, predictable name in the same gitignored directory, e.g. `frontend/test-results/demo-<arg-slug>.webm`, then remove the now-empty `<test-dir>` so only one file remains (copying leaves a duplicate — the original deep-named `video.webm` plus the friendly copy). Report the **absolute path** so the user can open it directly and delete it when satisfied.

## 5. Revert All Changes (ALWAYS)

After the test finishes (pass or fail), revert the config to its original state — undo all changes from step 1 (the `playwright.config.ts` `use` edits, or the base-class/statements changes for the Selenium stack). The recording itself stays (it is gitignored); only the config edits are reverted.

## 6. Report Result

Report whether the test passed or failed and the absolute path to the recording. If it failed, include the error output.
