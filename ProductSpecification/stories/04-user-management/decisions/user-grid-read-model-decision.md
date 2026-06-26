# Decision: Admin user grid is a read-model view-entity; the ORM resolves actor names

**Date**: 2026-06-27 **Scenarios**: 1.1

How to serve `GET /api/admin/users` — every user row plus its `createdBy`/`updatedBy`
**names** (not raw UUIDs), in one query, ordered `createdAt DESC, id DESC` — without
loading the write aggregate or hand-writing join SQL.

Two "System" identities exist and must both render as a name, never a UUID:
1. The real `admin` row (`…a532`) is literally named `System/System/System` — resolved through the join.
2. The synthetic `SYSTEM_USER_ID` (`0000…`) has **no** `iam_user` row — it must resolve to the
   Null-Object name `{firstName:"System", middleName:"", lastName:""}`. This name is **not**
   a domain `PersonName` (it requires a non-blank `lastName`), so actor names are a read-model
   projection, not a domain value object.

| Rejected | Why |
|----------|-----|
| **B — explicit JPQL/native projection with two self LEFT JOINs** into a constructor-expression row DTO | Hand-written join SQL is exactly what the storage rule discourages ("delegate mapping to the ORM; never manual grouping"); adds a throwaway row DTO; more brittle than letting the ORM resolve associations. The Null-Object-from-LEFT-JOIN convenience does not outweigh the rule violation. |
| **C — load `User` aggregates + a second batched name lookup** in a domain service | Violates "fetch everything upfront / one storage port"; two queries; forces `updatedAt`/`updatedBy` onto the write aggregate **now** (scenario 1.1 is read-only) instead of deferring them to the create/activate scenarios that actually mutate them. |

**Chosen (A)**: A dedicated read-only view-entity `UserSummaryView` (`@Entity @Immutable`,
mapped to `iam_user`) with a `@ManyToOne` to a lightweight `ActorView` (`@Immutable`,
mapped to `iam_user`, exposing only `id` + name parts) for both `createdBy` and `updatedBy`.
The ORM resolves the joins; the find method stays trivial (`findAll(Sort)` → map). A missing
actor association (the synthetic `SYSTEM_USER_ID` has no row) is mapped to the Null-Object
"System" name **at the adapter boundary** — the only layer that touches null. This keeps the
read concern fully separated from the write aggregate, so `updatedAt`/`updatedBy`/`timeZone`
are added to `User` only when the create/activate scenarios (3.1) need them.

## Model

- `UserSummaryView` (`@Entity @Immutable`, `@Table("iam_user")`, `infrastructure.persistence`) —
  `id`, name parts, `login`, `email`, `status`, `created_at`, `updated_at`,
  `@ManyToOne ActorView createdBy`, `@ManyToOne ActorView updatedBy`.
- `ActorView` (`@Entity @Immutable`, `@Table("iam_user")`) — `id` + name parts only; used solely
  to resolve an actor UUID to a name through the association.
- Domain read-model `UserSummary` (`iam.user.domain`, no persistence annotations) — the rich
  projection the query port returns: identity, status, audit timestamps, and resolved actor
  names. Actor name as a presentation-neutral name triple (first/middle/last) — middle nullable,
  the system Null-Object carries `{System, "", ""}`.
- Query port `UserSummaryQuery.findAllForGrid()` (`iam.user.domain`) → `List<UserSummary>`
  ordered `created_at DESC, id DESC`.
- Adapter `JpaUserSummaryQuery` (`infrastructure.persistence`) — wraps a Spring Data repository
  over `UserSummaryView`, `findAll(Sort.by(DESC, "createdAt").and(DESC, "id"))`, maps each view to
  `UserSummary`; a null `ActorView` → Null-Object "System" name.
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
- `red/green-usecase`: `ListUsersService` over a `FakeUserSummaryQuery` — ordering + actor-name
  resolution (real actor vs system Null-Object) as corner cases.
- `red/green-adapter (db)`: `JpaUserSummaryQuery` — the self-join name resolution + ordering is a
  non-trivial query, so the adapter test is **not** skippable.
- `red/green-domain`: `[S]` unless coverage finds a branch — the Null-Object mapping lives at the
  adapter boundary, not in a domain method.
