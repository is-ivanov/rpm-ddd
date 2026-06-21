---
name: design-preview
description: Preview the planned design for a scenario before writing tests. Shows domain model changes, usecase patterns, and key design choices. Use before red-usecase to get user approval on the implementation approach. If the user rejects the design, offers to discuss inline or escalate to /architecture for a full ADR.
---

# /design-preview - Scenario Design Preview

## Usage
```
/design-preview 10 "Filter by date range"    # Story 10, named scenario
/design-preview 10 9                          # Story 10, scenario 9
/design-preview                               # Detect from progress.md
```

## Purpose

Before writing any test code, present the planned implementation design to the user for approval. This catches design disagreements early — before red-usecase locks in the approach.

## Workflow

### 1. Gather Context

1. **Read the story spec** — full story document for business context, requirements, constraints, business rules
2. **Read the scenario** from `tests/01_API_Tests.md`
3. **Read `ProductSpecification/ExpectedLoad.md`** — for scale assumptions (use real numbers, never guess)
4. **Read existing code** — domain entities, usecases, ports, request/response DTOs
5. **Read ADRs** — check `decisions/*-decision.md` files in the story directory that may constrain the design
6. **Read the acceptance test** (if `red-acceptance` is done) — understand expected API behavior

### 2. Generate Options

Think through 2-3 viable approaches. For each option, capture:
- **Summary** — one line describing the approach (domain changes, ports, sequence)
- **Pros** — 2-3 bullets
- **Cons** — 2-3 bullets

Mark one option as **Recommended** with a one-line rationale ("why this over the others"). For trivial scenarios (validation, error handling, mechanical CRUD) where alternatives would be contrived, present a single option labelled "Only viable approach" instead of forcing artificial alternatives.

### 3. Present Options

Show each option as a labelled block (title, summary, pros, cons) with the recommendation rationale below the recommended one. Depth scales with complexity — see `.claude/templates/workflow/design-preview-formats.md`.

### 4. Get Option Choice

Ask with `AskUserQuestion`: recommended option first (suffixed "(Recommended)"), then alternatives, last answer "Reject all — escalate to `/architecture`". If user rejects all → invoke `/architecture` and STOP (do not proceed to the ADR question). Answer-layout details: see `.claude/templates/workflow/design-preview-formats.md`.

### 5. Get ADR Decision

After an option is chosen, ask separately with `AskUserQuestion` whether to capture the decision as an ADR ("Write ADR" vs "Skip ADR"). Mark "(Recommended)" based on the choice the user just made in step 4. Recommendation criteria: see `.claude/templates/workflow/design-preview-formats.md`.

### 6. Completion

Mark the `design` step per the outcome (no ADR / write ADR / rejected → `/architecture`). See the completion-outcome table in `.claude/templates/workflow/design-preview-formats.md`.

## Rules

- Never write code — this is a design step
- Never create files — design lives in the conversation (unless escalated to `/architecture`)
- Keep it concise — the goal is alignment, not a design document
- If trivially similar to a previous scenario, say so and ask to approve
- **Scope of progress.md changes:** design-preview may ONLY mark the `design` checkbox as `[x]`. It must NOT modify, skip, or rewrite any other steps (usecase, adapter, green-acceptance). Skip recommendations go in the design output text only — `/continue` applies approved skips to progress.md after the design work unit completes, validating each skip against the "zero production files modified" rule in tdd-rules.md.
