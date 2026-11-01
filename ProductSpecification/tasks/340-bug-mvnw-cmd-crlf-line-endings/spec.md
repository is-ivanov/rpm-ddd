# Task 340: mvnw.cmd CRLF line endings

Type: bug
Issue: #340

## Problem

Dependabot maven-wrapper bump #307 (`bad7b96`, 2026-08-16) committed `mvnw.cmd` with CRLF line endings into the
repository blob. `.gitattributes` declares `*.cmd text eol=crlf`, so the stored blob must be LF-normalized (CRLF only in
the working tree). The non-normalized blob makes git report `mvnw.cmd` as modified after every fresh checkout.

Impact: the monthly `checkstyle-updates` workflow has failed since 2026-09-01 (runs 33515647969, 36887524046,
41322124149) — `peter-evans/create-pull-request` aborts on `git checkout` because `mvnw.cmd` is dirty, so no upstream
`google_checks.xml` update PR has been opened.

## Solution

`git add --renormalize mvnw.cmd` — stores the blob as LF (diff is line-endings only, empty with
`--ignore-cr-at-eol`); the working tree stays CRLF. After merge, re-run `checkstyle-updates` via `workflow_dispatch`.

No production code or test changes — the fix is repository metadata only. Full-stack journey verdict: **no-impact**
(build tooling only, no user-lifecycle path).

## Key Files

- `mvnw.cmd`
- `.gitattributes`
- `.github/workflows/checkstyle-updates.yml`

## Reproduction

1. Fresh clone of `main`.
2. `git status` → `modified: mvnw.cmd`; `git ls-files --eol mvnw.cmd` → `i/crlf ... attr/text eol=crlf`.
3. Run `checkstyle-updates` while upstream `google_checks.xml` differs → fails in the create-pull-request step.
