# Checkstyle Upstream Update Review — Full Procedure

This is the full step-by-step procedure for the `checkstyle-review` skill. The skill keeps a compact one-line summary of each step and links here for the detail.

## Procedure

### 1. Get the upstream diff

Fetch the PR diff (GitHub MCP `pull_request_read` method `get_diff`). It contains only `google_checks.xml`. Split it into **logical changes** — group hunks that belong to one module/concern together (e.g. adding an `id` plus a new sibling `<module>` is one logical change, not two).

### 2. Map each change to my_checks.xml

Read `code-quality-config/checkstyle/my_checks.xml`. For each logical change determine:

- **Present** — the same module/check exists in `my_checks.xml` (the change is portable and relevant).
- **Absent** — the affected module isn't in our config (we already diverged or never adopted it; the change usually doesn't apply — note it but default to no-op).
- **New module** — upstream adds a brand-new check we don't have.

Quote the relevant `my_checks.xml` line range for each Present/New change.

### 3. Classify each change

- **Bugfix** — corrects a wrong regex/anchor/token (e.g. anchoring a `checkFormat` to a fully-qualified suffix). Low risk.
- **Coverage** — extends an existing check to more tokens/cases (e.g. adding `LITERAL_DEFAULT` to a curly-brace rule). Behavioral — may newly flag existing code.
- **Style opinion** — a new stylistic rule (e.g. forbidding decorative banner comments). Judge against this repo's own conventions (`AGENTS.md`, `.claude/rules/`).
- **Cosmetic** — `id` attributes added purely so rules can be targeted by suppressions. Always safe.
- **Version-incompatible** — the upstream rule uses a token or property our pinned checkstyle version doesn't accept yet (upstream tracks checkstyle *master*; our `checkstyle.version` lags). Surfaces as a config-load error (`Token "X" was not found in Acceptable tokens list`, `Property 'Y' does not exist`), not a style violation. Verdict: **reject / defer until checkstyle upgrade** — or, if the rule is wanted, bundle it with a `checkstyle.version` bump and re-gate. The formatter gate (step 4) catches these, since `checkstyle:check` fails to even load the config.

### 4. Formatter-conflict gate (MANDATORY for layout checks)

Java formatting in this repo is enforced by **Spotless + Palantir Java Format** (`./mvnw spotless:apply`). A checkstyle rule that governs **layout** can fight the formatter: the formatter produces one layout, checkstyle then rejects it → unfixable build.

A change is **layout-related** if its module is any of: `*Wrap` (Operator/Separator), `*Curly` (Left/Right), `Indentation`, `WhitespaceAround`/`WhitespaceAfter`/`NoWhitespace*`/`GenericWhitespace`, `ParenPad`/`MethodParamPad`, `EmptyLineSeparator`, `AnnotationLocation`, `CommentsIndentation`, `NoLineWrap`. Naming, Javadoc, import-order, and Todo/comment-content checks are **not** layout-related — skip the gate for them.

**Gate against the checkstyle version the BUILD uses — not whatever the PR branch pins.** The monthly PR branch is often cut from a *stale* base and carries an older `checkstyle.version` in its `pom.xml`. Running the gate with that stale version gives wrong verdicts: a rule using a token only added in a newer checkstyle will *falsely* look incompatible, and vice-versa. Before gating, read `checkstyle.version` from the **target branch** (`main`) `pom.xml` — that is the version `./mvnw verify` actually runs. If the worktree/PR-branch pins a different version, override every gate run with `-Dcheckstyle.version=<target-version>` (or rebase the PR branch onto `main` first). Always record which version the verdict was produced against.

For each layout-related candidate, **verify before recommending accept**:

1. On a scratch branch / stash-safe working tree, apply that single change to `my_checks.xml`.
2. Run `./mvnw spotless:apply -q` (re-formats sources to Palantir's canonical layout — no-op if already clean).
3. Run `./mvnw checkstyle:check -B -Dcheckstyle.version=<target-version>`.
4. Interpret the result:
   - **Config fails to load** (`Token "X" was not found in Acceptable tokens list`, unknown property) → **Version-incompatible** (see step 3). Not a formatter verdict — reject/defer or bundle a version bump. A failed load aborts *all* candidates in that run, so re-isolate: drop the incompatible change and re-gate the rest.
   - **Checkstyle reports violations on formatter-owned layout** → **conflict**: the rule contradicts Palantir. Recommend **reject** (or defer) and record the conflicting message.
   - **Clean (exit 0) and `git diff --stat` shows spotless reformatted nothing** → no conflict, safe to accept on merit. (If spotless *did* reformat sources, the new layout the formatter produced still passes checkstyle — also fine — but note it: a future PR will carry that reformat.)
5. Revert the scratch change before moving on (each candidate is tested in isolation; final application happens in step 7). To save maven runs you may gate all layout candidates together first; only isolate when the combined run fails, to attribute the failure.

Note known historical conflicts already documented in `my_checks.xml` (e.g. `TextBlockGoogleStyleFormatting`, Indentation block disabled) — if a change touches an already-disabled-for-Palantir area, flag it and default to reject.

### 5. Present the review

Show one table: change # | summary | present in my_checks (line range) | classification | gate result (n/a, ✅ clean, ❌ conflict, ⛔ version-incompatible) | recommendation (Accept / Reject / Optional — your call). State the checkstyle version the gate ran against. Below the table, one line per change explaining the reasoning. End by asking the user which changes to accept — they decide; recommendations are advice.

### 6. Collect the user's decision

Wait for explicit per-change accept/reject. Do not apply anything before confirmation. If the user accepts a change the gate marked ❌ conflict, warn once and require re-confirmation.

### 7. Apply

For each **accepted** change: edit `my_checks.xml` to add/modify exactly that module/property, matching the upstream form. Preserve our existing deviations and comments (never clobber a customized regex/message with the upstream default unless the user explicitly accepted that too).

For each **rejected** change: leave an XML comment in `my_checks.xml` at the relevant module documenting the deviation, so the next review knows it was a deliberate choice, not an oversight:

```xml
<!-- DEVIATION from google_checks.xml (reviewed {date}, PR #{n}):
     upstream {what changed}. Not adopted — {reason}. -->
```

Use the PR's creation date for `{date}` and the PR number for `{n}`. Place the comment immediately above the affected module (or where the module would go, for a rejected new module).

### 8. Verify

After applying, run `./mvnw checkstyle:check -B` to confirm the config still parses and the codebase still passes. Report pass/fail with counts. If it fails, show the violations and stop — do not "fix" by loosening a just-accepted rule without telling the user.
