# Open Issues & Tasks — Dependency Map

Cross-reference of all **open** GitHub issues and their ProductSpecification tasks, with the
dependency edges between them. Maintained alongside the per-task `spec.md` "Dependencies" sections.
Last refreshed: 2026-06-20.

## Legend

- **blocks / blocked by** — hard ordering: the blocked item cannot finish until the blocker is done.
- **enables / underpins** — soft: the downstream work becomes cleaner/correct once the upstream lands.
- **related** — overlapping files or a shared decision; sequence to avoid churn, but no hard order.

## FE-audit family (from `2026-06-20-frontend-audit.md`)

| Issue | Task | Kind | Depends on | Blocks / enables |
|-------|------|------|------------|------------------|
| #191 | `191-…client-validation-library` | decision | — | **blocks** #190 Step 2; **blocks/underpins** #189 |
| #190 | `190-…frontend-code-quality` | refactor (4 steps) | Step 2 ← **#191** | — (Steps 1✅, 3, 4 independent) |
| #189 | _(no task; tracked in `stories/01-user-login/improvements.md`)_ | promote-or-backlog | **#191** (password validation) | — |
| #187 | `187-…extended-tests-pipeline-gate` | process | — | **enables** #189 (root-cause gate) |
| #192 | `192-…pinia-state-store` | decision | linked to **I1** / **Story 3** | moves #162 401-redirect to a guard |
| #193 | `193-…component-naming` | decision | — | related to #190 Step 4 (same files) |

### Recommended order (FE-audit)

1. **#191 first** — it is the only blocker; unblocks #190 Step 2 and #189.
2. #190 Steps 1/3/4 (Step 1 done), #187, #192, #193 — independent, any order.
3. #190 Step 2 + #189 password work — after #191 decides the library (folded into Task 191 Step 2 if
   a library is adopted; otherwise #190 Step 2 lands as custom type guards in Task 190).

> **Why #191 before #190:** the audit flagged blind `as` casts on network JSON (#190 Step 2). How we
> validate the boundary depends on whether we adopt a schema library (#191). Doing #190 Step 2 first
> would pick the library by accident. So #191 (the decision) precedes #190 Step 2 (the application).

## Other open issues (independent of the FE-audit family)

| Issue | Task | Kind | Depends on | Notes |
|-------|------|------|------------|-------|
| #138 | _(no task yet)_ | backend bug | — | Same class as #130 (done, Task 13) / PR #137; 401 entry point must emit RFC-9457 ProblemDetail. |
| #166 | _(no task yet)_ | infra decision | — | dev Postgres publishes no host port; **#164 references this decision.** |
| #164 | _(no task yet)_ | skills/prompt | **related to #166** | conditional backend startup in green-playwright depends on the #166 dev-Postgres outcome. |
| #165 | _(no task yet)_ | skill improvement | — | `/demo` URL overlay + mock delay. Independent. |
| #97  | _(no task yet)_ | tech debt (YAGNI) | trigger-based | extract shared email transport; do only when a 2nd email type or HTTP-API provider appears. |

### Edge: #164 ↔ #166

#164 (E2E run-skill drift) explicitly defers to the "companion dev-Postgres issue" (#166): the
conditional `/run-backend` skip in `green-playwright` is correct regardless, but the wording about
the local real-stack path should align with whatever #166 decides. Sequence #166 → #164, or land
#164's mocked-tier carve-out independently and revisit the real-stack wording after #166.
