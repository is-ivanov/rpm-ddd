---
name: continue
description: Continue working on a story or task by reading progress.md, executing the next work unit, and updating progress. Use when user wants to resume story/task work or mentions /continue command.
---

# /continue - Resume Development

Central dispatcher: read `progress.md`, execute the next single work unit, update progress, commit, then STOP. All workflow sequences, progress tracking rules, adapter discovery, and task types are defined in `.claude/rules/workflow.md`. Progress file format examples are in `.claude/templates/workflow/progress-format.md`.

See `.claude/templates/workflow/continue-dispatch.md` for the dispatch tables, pre-commit checklist, and stories.md update rules.

## Algorithm

1. **Identify work item** from argument (see Resolving the Argument).
2. **Backlog promotion** -- if the story row is in the **Backlog** table in `ProductSpecification/stories.md`, move it to **In Progress** before proceeding.
3. **Read progress** file, bootstrap if missing (stories only) — bootstrapping also surfaces any `tests/extended/*_Extended.md` cases as `[S]` entries (see `workflow.md` → Bootstrapping, step 7); `/continue` never executes them.
4. **Find next step** -- first `[~]` or `[ ]` entry.
5. **Read journey context** -- read `carryover.md` (story root, if it exists) and the current scenario's summary file (`summaries/{scenario-slug}.md`, if it exists) as additional context. `/continue` only READS these files (the `/handoff` skill is the sole writer).
6. **Load ADR context** -- check for `decisions/*-decision.md` in the story directory; if any exist AND the current step references the ADR, read it for schema changes, edge cases, and implementation guidance.
7. **Execute one work unit** -- dispatch sub-skills per the dispatch tables in the reference template. Within the work unit, never pause between sub-skills.
8. **Adapter discovery** -- when the next step is `[ ] adapters-discovery`, read the usecase constructor to identify ports and map to adapters (see `workflow.md`). Mark `[x] adapters-discovery`, insert concrete steps below it, commit progress.md.
9. **Update progress** -- mark completed, advance next.
10. **Update stories.md** -- for stories only, update the phase columns in `ProductSpecification/stories.md` (per the reference template) and include it in the same commit.
10a. **Story Completion Gate** -- for stories only, when the work unit just completed was the story's **final** scenario (every other checkbox is `[x]`/`[S]`), do NOT move the row to Done yet. **STOP** and run the Story Completion Gate (see `workflow.md` → "Story Completion Gate"): surface every `tests/extended/*_Extended.md` case and every `Open` item in the story's `improvements.md`, each with a one-line promote/defer recommendation, and **ask the user to decide per item**. The agent never auto-promotes or auto-closes — it moves the row to Done only after the user has decided every item and any promoted scenarios are appended to `progress.md`. This is a deliberate exception to the "never pause within a work unit" rule.
11. **Task completion** -- after updating progress, if ALL checkboxes are `[x]` or `[S]`, move the task folder to `ProductSpecification/tasks/done/` and include the move in the commit.
12. **Commit, then STOP.** Always commit after a work unit (include progress.md). A single `/continue` invocation executes exactly ONE work unit — once the commit lands, stop and report (below). Do NOT read the next `[ ]` step and keep going. If a sub-skill fails, stop immediately and do NOT mark the step complete.

## Resolving the Argument

| Argument | Resolution |
|----------|------------|
| `task N` | Find `ProductSpecification/tasks/N-*/progress.md` |
| Bare number or name | Resolve story via `ProductSpecification/stories.md` then `ProductSpecification/stories/NN-story-name/progress.md` |
| No argument | Scan recent git log for `Story N` or `Task N` references; most recent wins |

**File lookup:** Use `find` via Bash (not Glob) when searching for progress files or story folders. Glob is unreliable on Windows/MINGW with large `.gitignore` files. For story resolution, derive the folder name from `ProductSpecification/stories.md` (e.g., story 5 "Create a task" → `05-create-task`) and Read the progress file directly. Use `ls` or `find` via Bash only when the folder name is ambiguous.

**Task folder not found on the current branch — search git BEFORE creating anything.** Task folders are committed on their own `task/N-slug` branches, so the current branch (especially `main`) may simply not contain the folder yet. Before concluding the task doesn't exist or invoking `/task` to create it:

1. **Search branches**: `git branch -a --list "*N*"` (or `git branch -a | grep N`) — look for a `task/N-*` branch, local or remote (run `git fetch` first if remote branches may be stale).
2. **Search history**: `git log --all --oneline --grep "Task N"` and `git log --all -- "ProductSpecification/tasks/N-*"` — the folder may exist on an unmerged branch.
3. **If a task branch exists**: report it to the user and switch to it (after confirming the working tree is clean), then resume the normal workflow.
4. **Only if neither a folder nor a branch nor history mentions task N**: check the GitHub issue #N; if it exists, offer to bootstrap the task from it via `/task` (reusing the issue, never opening a duplicate). Do not silently create a new task without this git search first.

## Stop and Report

STOP after every commit. Report: completed step, test results (pass/fail counts from every test run in the work unit — collect from red-agent, green-agent, test-coverage, refactor, e.g. `Tests: 15 passed, 0 failed`; report each suite separately), next step, progress fraction, how to continue. Task commit prefix: `task:` (e.g., `task: red-adapter db (Task 1, Step 1)`). Mandatory sub-skills per phase: see `workflow.md` sequences.

## Available Templates

- `.claude/templates/workflow/continue-dispatch.md` -- dispatch tables, pre-commit checklist, stories.md update rules
- `.claude/templates/workflow/progress-format.md` -- progress file format for stories, bug tasks, and refactoring tasks
