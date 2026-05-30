---
name: cleanup-chrome
description: Kill orphaned Playwright browser processes (headless Chromium / node runners) left over from failed Playwright test runs. Use when Playwright tests mass-fail with Connection reset or TimeoutException, when the system feels sluggish during test runs, or when the user mentions /cleanup-chrome. Also use proactively before running frontend acceptance tests if previous runs were interrupted or stopped.
---

# Clean Up Orphaned Playwright Browser Processes

Failed or interrupted Playwright test runs leave behind headless Chromium processes (and sometimes stray `node` test-runner processes). These accumulate and exhaust system resources, causing new Playwright tests to fail with `Connection reset` / `TimeoutException` / `Target page, context or browser has been closed`.

Playwright does **not** use chromedriver — it drives its own bundled browser directly. Its headless Chromium runs as `headless_shell.exe` (newer Playwright) or `chrome.exe` (the bundled Chromium binary under `ms-playwright/`). Prefer killing `headless_shell.exe`: it is specifically the Playwright headless browser and will never be the user's everyday Chrome.

## Action

1. **Count** orphaned processes to assess the situation:
   ```bash
   echo "headless_shell: $(tasklist //FI "IMAGENAME eq headless_shell.exe" 2>/dev/null | grep -c headless_shell)"
   ```

2. **Kill all** Playwright headless browser processes (these are always test-spawned):
   ```bash
   taskkill //IM headless_shell.exe //F 2>&1 | tail -1
   ```

3. **Repeat** the kill command until the count reaches 0 — processes can respawn briefly as child processes exit. Usually 2-3 rounds suffice.

4. **Clean locked test output** if it exists:
   ```bash
   rm -rf frontend/test-results 2>/dev/null
   ```

## Output

Report how many processes were killed and confirm cleanup is complete.

## Note

If Playwright was configured to run a non-headless (headed) browser, the bundled Chromium runs as `chrome.exe` and is indistinguishable by image name from the user's own browser. In that case **warn the user before killing `chrome.exe`** — it would also close their everyday browser with any unsaved work. Per `.claude/rules/infrastructure.md`, never kill processes by a generic executable name when other instances may belong to the user.
