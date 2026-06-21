---
name: screenshot
description: Take screenshots of HTML mockups from any version. Use when user wants to capture mockups as PNG images, regenerate screenshots, or mentions /screenshot command.
---

# Screenshot HTML Mockups

Automate taking screenshots of HTML mockups using Puppeteer from a centralized runner location.

## Usage
```
/screenshot [folder/file]
```

## Mockups Location

Default location: `ProductSpecification/stories/*/mockups/`

## Workflow

### Phase 1: Determine Scope

Parse user input to determine target. Folder/file filtering:
- `/screenshot` — All mockups in default location
- `/screenshot login.html` — Single file
- `/screenshot 01_auth` — Files matching pattern

### Phase 2: Ensure Puppeteer Environment

Ensure `.screenshots-temp/node_modules` exists (install once if not).

### Phase 3: Run Screenshot Script

Run `take-screenshots.js` from `.screenshots-temp/` against the target.

### Phase 4: Output Summary

Report screenshots taken, output location, and any failures.

See `.claude/tech/{browser-testing}/templates/screenshot/runner.md` for the runner environment, commands, examples, and design constraints.

## Notes

- Puppeteer is pre-installed in `.screenshots-temp/`
- No npm installation needed in mockup directories
- Screenshots are deterministic for visual regression testing
- Re-running overwrites existing screenshots
