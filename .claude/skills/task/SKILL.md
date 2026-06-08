---
name: task
description: Create a new task (bug or refactoring) with spec and progress tracking. Use when user wants to create a task or mentions /task command.
---

# /task - Create Task

## Input

- **type** (optional): `bug` or `refactoring`
- **title** (optional): Short title (2-5 words)

## Workflow

### 1. Determine Type and Title

If arguments provided, parse type (`bug` or `refactoring`) and title from args.
If no arguments, ask interactively: type and short title (2-5 words).

### 2. Allocate the Task Number via a GitHub Issue

**The task number IS the GitHub issue number.** Issue numbers are allocated atomically and globally by GitHub, so parallel task creation across worktrees/branches can never collide. (The old "max existing folder + 1" scheme raced: each branch only sees its own committed folders, so two parallel branches both pick the same next number — exactly the `14`/`14` clash that motivated this change, #141.) Every task — `bug` AND `refactoring` — gets an issue.

1. Resolve the repository from `git remote get-url origin` (e.g. `is-ivanov/rpm-ddd`).
2. **If an issue already exists** for this work (the user passed one, or you are bootstrapping from an existing issue), use its number — do not open a duplicate.
3. **Otherwise create the issue** with the **GitHub MCP server** (`issue_write`, `method: create`) — title from the task title, body from the gathered Problem + Solution (+ Reproduction for bugs), `labels: ["bug"]` for bugs / `["enhancement"]` for refactorings. Fall back to `gh issue create` only if the MCP server is unavailable.
4. Capture the returned issue **number** `N`. This is the task number used everywhere below.

> Legacy tasks 1–14 used the old sequential scheme and keep their numbers. Issue numbers are already far above that range (140s+), so a new issue-numbered folder never clashes with a legacy one.

### 3. Create Folder

Create `ProductSpecification/tasks/{N}-{type}-{slug}/` where `N` is the issue number and slug is lowercase-hyphenated title.

### 4. Interactive Spec (2-3 rounds)

Gather from user:

**All types:** Problem, Solution, Affected Layers (domain, usecase, db, rest, email, frontend), Key Files.

**Bug only:** Reproduction steps.

**Refactoring only:** Numbered steps with clear scope.

### 5. Generate spec.md

Write `ProductSpecification/tasks/{N}-{type}-{slug}/spec.md` using the format in `.claude/templates/task/creation-formats.md`. Include the `Issue: #N` line — every task has one, and the number matches the folder. Every test written during a **bug** task's TDD cycle carries this number (per tech binding `tdd.md`); refactoring tasks record the issue for traceability but do not tag tests.

### 6. Generate progress.md

Select fix profile based on type and affected layers:

| Affected Layers | Section |
|-----------------|---------|
| Backend only | `## Backend` (standard backend sequence with `[ ] adapters-discovery`) |
| Frontend only | `## Frontend` (standard frontend sequence) |
| Both | `## Backend` + `## Frontend` |
| Refactoring | `## Fix` with user-defined steps |

Write `ProductSpecification/tasks/{N}-{type}-{slug}/progress.md` using the matching format from the template. Bug tasks use `[ ] adapters-discovery` -- adapter discovery runs when this step is reached. Refactoring steps are user-defined from step 4.

### 7. Review and Commit

Show spec.md and progress.md to user for review. Commit both files.

## Rules

- Task number = GitHub issue number (globally atomic — no parallel-creation collisions). Legacy tasks 1–14 keep their old sequential numbers.
- Slug uses lowercase-hyphenated title (max 5 words)
- Commit message: `task: spec (Task {N}, {title})`

## Templates

- `.claude/templates/task/creation-formats.md` -- spec.md format, progress.md formats
