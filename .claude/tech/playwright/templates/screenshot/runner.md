# Screenshot Runner (Puppeteer)

The centralized Puppeteer runner lives in `.screenshots-temp/` and is driven by `node take-screenshots.js`. This file holds the runner environment, commands, examples, and design constraints for the `/screenshot` skill.

### Phase 2: Ensure Puppeteer Environment

The centralized Puppeteer runner is in `.screenshots-temp/`:

1. Check if `.screenshots-temp/node_modules` exists:
   - If not: run `cd .screenshots-temp && npm install`
2. This only needs to happen once, not per-mockup-directory

### Phase 3: Run Screenshot Script

Execute from the centralized location:

```bash
cd .screenshots-temp && node take-screenshots.js [target]
```

**Examples:**
```bash
# All mockups in default directory
cd .screenshots-temp && node take-screenshots.js

# Specific file (relative to repo root)
cd .screenshots-temp && node take-screenshots.js ../ProductSpecification/stories/01-create-task/mockups/desktop/create-task.html

# Directory with HTML files
cd .screenshots-temp && node take-screenshots.js ../ProductSpecification/stories/01-create-task/mockups/desktop/

# Filter by filename pattern
cd .screenshots-temp && node take-screenshots.js ../ProductSpecification/stories/01-create-task/mockups/ mobile
```

## Example Invocations

```
/screenshot                           # All mockups
/screenshot login.html                # Specific file
/screenshot mobile                    # Files containing "mobile"
```

## Design Constraints

- **Mobile detection**: Files in a `mobile/` parent directory use mobile viewport (390x844)
- **Desktop viewport**: 1400x900
- **Element selector**: Crop to `.mockup-card` element when present
- **Fallback**: Full page screenshot if `.mockup-card` not found
- **Format**: PNG
- **Output**: `screenshots/` subfolder next to each HTML file (per-directory, not centralized)
- **Skip**: `node_modules/`, `screenshots/`, and `screenshots-live/` directories
