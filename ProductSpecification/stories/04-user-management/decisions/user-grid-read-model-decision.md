# Decision: Admin user grid is a read-model view-entity; the ORM resolves actor names

**Date**: 2026-06-27 **Scenarios**: 1.1

> **Correction (2026-06-27, during red-adapter db):** the original draft assumed the synthetic
> `SYSTEM_USER_ID` (`0000…`) has **no** `iam_user` row and relied on a LEFT-JOIN-null → Null-Object.
> That is false: migration `2026-04-10-04-insert-system-user` **inserts** that row (named "System /
> User") and it is the `created_by` FK anchor for every seed user. The fixture also **excludes** the
> `0000` row from the grid (6 rows, not 7) and renders actor `0000` as `{System,"",""}` — which is
> neither the join-resolved "System User" nor a JOIN null. The mechanism is corrected below to:
> **exclude `SYSTEM_USER_ID` from the listing, and special-case actor `id == SystemActors.SYSTEM_USER_ID`
> to the constant `{System,"",""}` in the adapter mapper** (regular `@ManyToOne`, no LEFT-JOIN-null).
> Outcome unchanged; only the reason and mechanism are fixed. Migration/FK are NOT touched.

How to serve `GET /api/admin/users` — every user row plus its `createdBy`/`updatedBy`
**names** (not raw UUIDs), in one query, ordered `createdAt DESC, id DESC` — without
loading the write aggregate or hand-writing join SQL.

Two "System" identities exist and must both render as a name, never a UUID:
1. The real `admin` row (`…a532`) is literally named `System/System/System` — resolved through the join.
2. The synthetic `SYSTEM_USER_ID` (`0000…`) **does have** an `iam_user` row (named "System / User"), but
   it is **excluded** from the listing and, when it is an actor, is special-cased to the constant
   read-model name `{firstName:"System", middleName:"", lastName:""}` — keyed on
   `SystemActors.SYSTEM_USER_ID`, not derived from the row. This name is **not** a domain `PersonName`
   (it requires a non-blank `lastName`), so actor names are a read-model projection, not a domain value object.

| Rejected | Why |
|----------|-----|
| **B — explicit JPQL/native projection with two self JOINs + `CASE WHEN cb.id = :system`** into a constructor-expression row DTO | Hand-written join SQL is exactly what the storage rule discourages ("delegate mapping to the ORM; never manual grouping"); adds a throwaway row DTO; more brittle than letting the ORM resolve associations. The system special-case is handled just as cleanly by a single id check in the adapter mapper (chosen A) without pushing presentation logic into SQL. |
| **C — load `User` aggregates + a second batched name lookup** in a domain service | Violates "fetch everything upfront / one storage port"; two queries; forces `updatedAt`/`updatedBy` onto the write aggregate **now** (scenario 1.1 is read-only) instead of deferring them to the create/activate scenarios that actually mutate them. |

> **Correction (2026-06-27, during red-adapter db):** the draft mapped TWO `@Entity` classes
> (`UserSummaryView` + a lightweight `ActorView`) onto `iam_user`. Hibernate rejects that with
> `DuplicateMappingException` — and a single `@Entity` on `iam_user` also collides with the write
> aggregate `User`, which already maps that table. Fix: `UserSummaryView` is mapped with
> `@org.hibernate.annotations.@Subselect` (a read-only derived "table" that coexists with `User`
> and is skipped by `ddl-auto: validate`), and `createdBy`/`updatedBy` are resolved through a
> **self-referencing** `@ManyToOne UserSummaryView` — there is no separate `ActorView`. The
> ORM-resolves-the-join intent and the outcome are unchanged.

**Chosen (A)**: A dedicated read-only view `UserSummaryView` (`@Subselect @Immutable` over
`iam_user`) with a **self-referencing** `@ManyToOne UserSummaryView` for both `createdBy` and
`updatedBy` (a separate `ActorView` is not possible — see the correction above).
The ORM resolves the joins; the find method stays trivial (`findByIdNot(SYSTEM_USER_ID, Sort)`
→ map). The synthetic `SYSTEM_USER_ID` is **excluded** from the listing by the derived
`findByIdNot`, and when it appears as an actor it is special-cased to the constant
`{System,"",""}` name **at the adapter boundary** (a single `id == SystemActors.SYSTEM_USER_ID`
check in the mapper) — the only layer that owns such boundary rules. This keeps the read concern
fully separated from the write aggregate, so `updatedAt`/`updatedBy`/`timeZone` are added to
`User` only when the create/activate scenarios (3.1) need them.

## Model

- `UserSummaryView` (`@Subselect @Immutable` over `iam_user`, `infrastructure.persistence`) —
  `id`, name parts, `login`, `email`, `status`, `created_at`, `updated_at`,
  self-referencing `@ManyToOne UserSummaryView createdBy`, `@ManyToOne UserSummaryView updatedBy`.
  A second `@Entity` on `iam_user` is impossible (collides with the write aggregate `User` and
  with itself) — the actor is resolved through the view's own self-join, not a separate `ActorView`.
- Domain read-model `UserSummary` (`iam.user.domain`, no persistence annotations) — the rich
  projection the query port returns: identity, status, audit timestamps, and resolved actor
  names. Actor/own name as a presentation-neutral read-model name triple (first/middle/last) —
  middle nullable, the system actor carries the constant `{System, "", ""}`.
- Query port `UserSummaryQuery.findAllForGrid()` (`iam.user.domain`) → `List<UserSummary>`
  ordered `created_at DESC, id DESC`, excluding the synthetic system user.
- Adapter `JpaUserSummaryQuery` (`infrastructure.persistence`) — wraps a Spring Data repository
  over `UserSummaryView`, `findByIdNot(SystemActors.SYSTEM_USER_ID.id(), Sort.by(DESC, "createdAt").and(DESC, "id"))`,
  maps each view to `UserSummary`; an actor whose `id` equals `SystemActors.SYSTEM_USER_ID` →
  constant `{System,"",""}` name, otherwise the joined `ActorView` name parts.
- Application service `ListUsersService` (`@ApplicationService`, new top-level entry point) —
  calls the one port, returns `List<UserSummary>`. No other application service is injected.
- `UserResource.listUsers()` — replaces the `UnsupportedOperationException` stub: calls
  `ListUsersService`, maps each `UserSummary` to the already-defined `UserSummaryResponse`
  (nested `PersonNameResponse` + `AuditResponse`), returns the bare array with 200.

## Foundation (this scenario)

- Migration adds `updated_at` + `updated_by` columns to `iam_user`, seeded equal to
  `registered_at` / `created_by` for existing rows (the fixture expects `updatedAt == createdAt`,
  `updatedBy == createdBy` for all seed users). `time_zone` is **out of scope** here — it arrives
  with the create/activate scenarios.
- The write aggregate `User` is **not** modified in scenario 1.1.

## Test layering

- Level 1 acceptance (`UserGridIntegrationTest`): seed-only, whole-body JSON-fixture match —
  resolved names, both "System" identities, status, audit quartet, deterministic order.
- `red/green-usecase`: **`[S]`** — `ListUsersService` is a pure pass-through (ordering + actor
  exclusion + system special-case all live in the adapter), so a usecase unit test would be a
  trivial `output == input` identity test. Covered by Level 1 acceptance + Level 3 db adapter.
- `red/green-adapter (db)`: `JpaUserSummaryQuery` — the self-join name resolution, exclusion, and
  ordering. This is the only legitimate TDD phase to author the view-entity storage code (the
  reason the step runs); `green-acceptance` is remove-marker-only and cannot create storage
  mapping. The `@DataJpaTest` is a **candidate for deletion** once green, as it duplicates the
  acceptance happy path — keep it narrow (both System identities in isolation, ordering/tie-break).
- `red/green-domain`: `[S]` unless coverage finds a branch — the system special-case mapping lives
  at the adapter boundary, not in a domain method.
