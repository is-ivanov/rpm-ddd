---
name: test-spec
description: Generate BDD test specifications for story in 6 categories (API, UI, Load, Infrastructure, Security, Integration). Use when user wants to create test cases or mentions /test-spec command.
---

# Generate Test Specifications

Generate BDD-style test specifications for a story in 6 categories. Each category produces two files: **main** (critical tests) and **extended** (nice-to-have tests).

## Usage
```
/test-spec "Story name"
/test-spec 5              # By MVP story number
/test-spec                # Interactive selection
```

## Workflow

### Phase 1: Context & Story Selection

Read before generating: `ProductSpecification/BriefProductDescription.txt`, `ProductSpecification/stories.md`, `ProductSpecification/ExpectedLoad.txt`, story folder (`stories/*/`): mockups, `*.md`, `endpoints.md`, `interview.md`.

Parse input: by name (`"Login/Logout"`), by number (`5`), or interactive (list and ask).

If `interview.md` exists, extract:
- Business rules and constraints → map to API test scenarios
- Explicit edge cases (column transitions, task ordering, concurrent edits) → map to extended tests
- External API error modes → map to integration tests
- Rate limits and performance constraints → map to load tests

**Prerequisite analysis** (mandatory): Read the story's Prerequisites section and Validation Rules table. For each prerequisite (Board exists, Column exists, etc.), generate guard scenarios in BOTH API and UI tests following the Prerequisite Guard Checklist in `test-spec-format.md`. Cross-reference existing stories (e.g., Story 5 `tests/01_API_Tests.md` sections 0-1, `tests/02_UI_Tests.md` section 0) for established blocker patterns.

### Phase 2: Generate Test Files

Load `.claude/templates/spec/test-spec-format.md` for category formats, ordering principles, and BDD rules.

Create files in `ProductSpecification/stories/NN-story-name/tests/`:

**Main files (critical ~27-34 total tests):**
- `01_API_Tests.md`, `02_UI_Tests.md`, `03_Load_Tests.md`
- `04_Infrastructure_Tests.md`, `05_Security_Tests.md`, `06_Integration_Tests.md`

**Extended files in `extended/` subfolder (nice-to-have edge cases):**
- `extended/01_API_Tests_Extended.md` through `extended/06_Integration_Tests_Extended.md`

**Full-stack journey verdict (always — see Phase 2b):**
- `07_FullStack_Journey.md` (extend / new / no-impact verdict — not a test category, a recorded decision)

Add this header to extended files:
```markdown
> These are additional edge case tests. Implement after core tests pass.
```

### Phase 2a: Level & overlap classification (mandatory)

For **every backend-style scenario** (API, Security, Integration, Infrastructure), run the overlap pass and stamp a `**Level:**` tag per `test-spec-format.md` → "Per-scenario Level tag + overlap pass". For each scenario, check what is already covered (by a lower level, or an existing scenario in this or an earlier story) and tag the **cheapest** level that recovers the new behavior: error / validation / per-status categories default to **L2 web-slice** (their domain rule is already tested at L3/L4 — only the HTTP mapping is new); reserve **L1 acceptance** for the happy path. This tag is what the `/continue` bootstrap reads to pick the step sequence (`workflow.md` → Bootstrapping) — an untagged scenario is the root cause of a validation case wrongly bootstrapped as a Level-1 acceptance test.

### Phase 2b: Full-Stack Journey Verdict (mandatory)

Assess this story's impact on the top-tier full-stack journey and ALWAYS write the verdict to `tests/07_FullStack_Journey.md` — one of **extend**, **new**, or **no-impact**. The file is never omitted: a backend-only or non-lifecycle story records `no-impact` with a one-line rationale. See `tdd-rules.md` → "Top-Tier Full-Stack Journey Assessment" for the principle and the browser-testing tech binding (`.claude/tech/{browser-testing}/tdd.md`) for the journey location, suffix, and extend-vs-new mechanics. Use the `07_FullStack_Journey.md` format in `test-spec-format.md`.

Decision guide:
- Does the story drive an existing critical-lifecycle step through real UI for the first time, or add a new happy-path leg (auth/onboarding/checkout/cross-service hand-off)? → **extend** the existing journey (default), naming the journey and the changed step.
- Is the new path an independent lifecycle that doesn't belong on the existing journey's spine? → **new**.
- No rendered critical-path change (backend-only, infra, pure data)? → **no-impact** + reason.

### Phase 3: Summary

Report: folder path, files created (including `07_FullStack_Journey.md` and its verdict), test counts per file.

## Rules

- English, Gherkin in Markdown, DSL only (no technical details in steps)
- Main files: critical path (~27-34 total), Extended files: edge cases
- Reference ExpectedLoad.txt for load tests
- **Security**: generate stack-aware scenarios only — see relevance filtering and checklist in `test-spec-format.md`. Skip technologies not in the stack (NoSQL, LDAP, XXE). Skip cross-cutting concerns tested globally (security headers, CORS, HTTPS). Include IDOR for resource-by-ID endpoints (`tasks/{id}`, `boards/{id}`), JWT security for auth stories, input validation for task fields. Merge related scenarios (e.g., one SQL injection test covering all fields). Target 6-10 focused scenarios per story.
