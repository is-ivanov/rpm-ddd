# Task 191: Decide — Client Validation Library vs Custom Validation

Type: refactoring
Issue: #191

> **Discussion task.** Decide *whether* to apply before writing any code. Step 1 produces the
> decision (and an ADR if the change is adopted); implementation steps are added only if adopted.

## Problem

The FE audit notes our client-side validation is hand-rolled, while the current industry standard is
schema validation libraries (zod / valibot / vee-validate) with a dedicated place for schemas. As the
app grows, hand-rolled validators scattered across `.logic.ts` files risk inconsistency and
duplication.

## Our current approach

- Validation lives in pure `.logic.ts` functions (Humble Object pattern, `frontend-rules.md`):
  e.g. `validateEmail`, `isLoginFormValid`. Components only reflect the logic.
- Tested as pure functions with Vitest, no DOM.
- No schema layer, no dedicated schema-storage location.

## Constraints from `.claude` rules

- `frontend-rules.md`: pure logic in `.logic.ts` (no side effects); components are thin wrappers;
  FORBIDDEN: validation regex / business logic in component files.
- Testing: logic tests are pure-function Vitest; no new external approval libraries.
- Any library must fit the layered structure (logic vs API client vs component) and the file-size
  limit (200 lines).

## Senior's remark

> "Задействована кастомная валидация. Стандартом сейчас является использование библиотек валидации
> типа zod, схем, vee-validate. Хранение данных валидации — отдельное место в папках."

## Pros / Cons

**Adopt a library (zod/valibot):**
- + Single source of truth (schema), runtime + compile-time types from one definition.
- + Directly enables #190 step 2 (runtime response validation) — same schemas validate network payloads.
- + Less hand-rolled regex; consistent error shapes.
- − New dependency + learning curve; must define a schema-storage convention; some boilerplate for trivial cases.

**Keep custom `.logic.ts`:**
- + Zero deps, full control, already passes our Humble Object rules.
- − Scales poorly; duplication risk; no runtime contract for network data (the #188 / #190.2 gap).

## Decision

_To be filled in Step 1._ If adopted: pick the library, define where schemas live, and add
implementation steps (and likely fold #190 step 2 into it).

## Key Files

- `frontend/src/features/auth/logic/*.logic.ts` (current validators)
- `frontend/package.json`, `frontend-rules.md` (if a convention is added)
