# Decision: Adopt zod for client-side + network-boundary validation

**Date**: 2026-06-20 **Scenarios**: Task 191 (folds in #190 Step 2)

The FE audit flagged hand-rolled validators and, more importantly, blind `as ProblemDetail` /
`as ActivationTokenResponse` casts of server payloads with no runtime contract — one schema source
fixes both and unblocks #189/#190.2.

| Rejected | Why |
|----------|-----|
| Keep custom `.logic.ts` | No runtime contract for network data (the #188/#190.2 gap); duplication grows as #189+ add rules |
| valibot | Same one-schema model but smaller ecosystem/docs; bundle saving not worth the trade for this app's size |
| vee-validate | Form-state + Vue-component coupled; conflicts with the Humble Object rule (pure `.logic.ts`, thin components) |

**Chosen**: zod — one schema yields runtime validation (`.parse`) and the compile-time type
(`z.infer`). Schemas validate both form input (in `.logic.ts`) and network payloads (in `.api.ts`,
replacing the blind casts).

## Model

Schema-storage convention (added to `frontend-rules.md`):

- **Cross-feature schemas** → `src/app/schemas/*.schema.ts`. First entry: `problem-detail.schema.ts`
  (RFC 9457 shape emitted by every endpoint).
- **Feature schemas** → `src/features/{feature}/schemas/*.schema.ts`
  (e.g. `auth/schemas/activation.schema.ts`, `auth/schemas/login.schema.ts`).
- Types are **derived** from schemas via `z.infer<typeof X>` — the schema is the single source of
  truth; do not hand-write a parallel `interface`. Migrate `types.ts` interfaces to inferred types
  only as each schema is introduced (minimal — what the touched code references).
- `.api.ts` clients call `schema.parse(await response.json())` instead of `as` casts. A parse failure
  is a contract violation surfaced as an error, not silently accepted.
- Schemas are pure data declarations — they live in the logic layer, never in component files
  (consistent with `frontend-rules.md`).

## Edge Cases

| Case | Behavior |
|------|----------|
| Server returns a payload the schema rejects (missing/extra/wrong-typed field) | `.parse` throws → mapped to the feature error (`LoginError`/`ActivationError`), not surfaced as a fake success |
| Trivial validator (`isLoginFormValid` — non-empty) | A zod schema adds no behavior over the 3-line check; refactor only if it improves clarity, otherwise leave as-is |
| `#189` password-rules / confirm-password | Out of scope here — this decision only unblocks it; the schema lands in Task #189 |
