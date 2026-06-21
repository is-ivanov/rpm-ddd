---
name: checkstyle-review
description: Review the open "update google_checks.xml from upstream" PR created by the monthly checkstyle-updates workflow. Diffs the upstream google_checks.xml against our google_checks.xml, maps each change to our active my_checks.xml, classifies and recommends accept/reject per change, verifies formatting-related changes don't conflict with Palantir Java Format + Spotless, then on user confirmation ports accepted rules into my_checks.xml and annotates rejected ones as documented deviations. Use when the user mentions /checkstyle-review, asks to review the checkstyle update PR, or wants to decide which upstream google style changes to adopt.
---

# Checkstyle Upstream Update Review

The `.github/workflows/checkstyle-updates.yml` workflow runs monthly. It fetches the latest `google_checks.xml` from `checkstyle/checkstyle` master, overwrites our reference copy at `code-quality-config/checkstyle/google_checks.xml`, and opens a PR (label `checkstyle-update`, branch `chore/update-google-checks-*`).

That PR only updates the **reference** copy. The build actually uses `code-quality-config/checkstyle/my_checks.xml` — a hand-customized derivative of the google config. Upstream changes must be **hand-ported** into `my_checks.xml` after review. This skill does that review and porting.

## Inputs

- The open checkstyle update PR. Find it via the GitHub MCP server: list open PRs on `is-ivanov/rpm-ddd`, pick the one with label `checkstyle-update` (or head branch `chore/update-google-checks-*`). If multiple, use the newest. If none open, tell the user there is nothing to review and stop.
- Optional PR number argument: `/checkstyle-review 100` targets a specific PR.

## Procedure

1. **Get the upstream diff** — fetch the PR diff and split it into logical changes.
2. **Map each change to my_checks.xml** — classify as Present / Absent / New module with line ranges.
3. **Classify each change** — Bugfix / Coverage / Style opinion / Cosmetic / Version-incompatible.
4. **Formatter-conflict gate (MANDATORY for layout checks)** — verify layout rules don't fight Spotless + Palantir, gating against the target-branch checkstyle version.
5. **Present the review** — one table of changes with classification, gate result, and recommendation.
6. **Collect the user's decision** — wait for explicit per-change accept/reject before applying.
7. **Apply** — port accepted changes into my_checks.xml; annotate rejected ones as documented deviations.
8. **Verify** — run `./mvnw checkstyle:check -B` and report pass/fail with counts.

See `.claude/tech/{backend}/templates/checkstyle/upstream-review-procedure.md` for the full procedure.

## Output

- The review table + reasoning (step 5).
- After confirmation: summary of what was ported and what was annotated as a deviation, plus the `checkstyle:check` result.

## Notes

- This skill edits `my_checks.xml` only. It does **not** merge the PR (merging the PR just updates the reference `google_checks.xml`) and does **not** commit — the user commits per their branch policy (never directly to `main`; see `AGENTS.md`).
- Keep the formatter gate honest: a layout rule that passes only because no current source triggers it can still break a future PR. When unsure, prefer Optional/Reject and say why.
