# Task Creation Formats

## Usage Examples

```
/task bug "Modal scroll broken"
/task refactoring "TaskBoard aggregate"
/task                                        # Interactive
```

## spec.md Format

```markdown
# Task {N}: {Title}

Type: {bug|refactoring}
Issue: #{N}  <- every task (the task number IS the issue number; number matches the folder); bug tests are tagged with it (per tech binding `tdd.md`), refactoring records it for traceability but does not tag tests

## Problem

{description}

## Solution

{description}

## Key Files

- {file paths}

## Reproduction  <- bug only

{steps}
```

## Task Number = GitHub Issue Number (rationale)

**The task number IS the GitHub issue number.** Issue numbers are allocated atomically and globally by GitHub, so parallel task creation across worktrees/branches can never collide. (The old "max existing folder + 1" scheme raced: each branch only sees its own committed folders, so two parallel branches both pick the same next number — exactly the `14`/`14` clash that motivated this change, #141.) Every task — `bug` AND `refactoring` — gets an issue.

> Legacy tasks 1–14 used the old sequential scheme and keep their numbers. Issue numbers are already far above that range (140s+), so a new issue-numbered folder never clashes with a legacy one.

## progress.md Formats

### Bug (backend)

```markdown
# Task {N}: {Title} -- Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: {bug description}
- [ ] red-acceptance
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance
```

### Bug (frontend)

```markdown
# Task {N}: {Title} -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix: {bug description}
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo
```

### Refactoring

```markdown
# Task {N}: {Title} -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: {description}
- [ ] red-adapter db
- [ ] green-adapter db

### Step 2: {description}
- [ ] refactor usecase
- [ ] refactor (cleanup)
- [ ] green-acceptance
```
