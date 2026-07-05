# Vue 3/TypeScript Coding Conventions

Tech binding for `frontend-rules.md`. Shared section structure: `.claude/templates/coding/coding-sections.md`.

## File Extensions (Humble Object)

- Logic files: `.logic.ts`
- API client files: `.api.ts`
- Component files: `.vue`

## Feature Structure

- Features live in `frontend/src/features/{feature}/` with subdirectories:
  - `components/` -- Vue SFCs: `{Feature}Page.vue` and extracted sub-components.
  - `logic/` -- pure logic `{feature}.logic.ts`, API client `{feature}.api.ts`, `types.ts`.
  - `schemas/` -- zod schemas `{thing}.schema.ts` (see "Schema Validation").
  - `__tests__/` -- Vitest tests: `{feature}.logic.test.ts`, `{feature}.api.test.ts`.

## Module Function Ordering (newspaper order)

- Within a `.ts` module, place **exported functions first**, then the private (non-exported)
  helpers they call **below** them — read top-down from the public API to the details (Clean Code
  "newspaper order"). A function is "private" simply by not being `export`ed; there is no `private`
  keyword at module scope.
- This is safe because `function` **declarations are hoisted** — a helper defined lower in the file
  is fully available to an exported function above it. (Applies to `function foo() {}` declarations,
  not `const foo = () => {}` arrow assignments, which are not hoisted — declare those before use.)
- When several exports share one helper, the helper goes at the **bottom** of the module.
- No linter enforces this (no `no-use-before-define` configured), so it is a convention, not a
  tooling gate — keep it consistent across the `.api.ts`/`.logic.ts` layer.

## Shared UI Components

- Reusable components live in `frontend/src/app/components/ui/`.
- Examples: `FieldError.vue`, `LoadingSpinner.vue`, `PasswordToggle.vue`, `input-styles.ts`.

## Component Conventions

- Use `<script setup lang="ts">` for all components (Composition API).
- Props via `defineProps<T>()`, emits via `defineEmits<T>()`.
- Reactive state via `ref()` and `reactive()` from `vue`.
- Template expressions use Vue directives: `v-if`, `v-for`, `v-model`, `v-bind`, `v-on` (shorthand `:` and `@`).
- Component display name: do **not** add an explicit `name` option. A `<script setup>` SFC built with `@vitejs/plugin-vue` (Vue ≥ 3.2.34) infers the name from the filename for devtools inspection, warning traces, and `<KeepAlive>` include/exclude — declaring it would force a redundant second `<script>` block. The `vue/multi-word-component-names` ESLint rule (`error` in `eslint.config.ts`) enforces meaningful multi-word names. This is the framework mechanism behind the role-suffix naming rule in `frontend-rules.md`.

## Icon Library

- Vue 3 icon library: `lucide-vue-next` (not `lucide-react`).
- Import: `import { Plus, X } from 'lucide-vue-next'`.
- Usage in template: `<Plus class="w-4 h-4" />`.
- Standard sizes: `w-4 h-4` (small), `w-5 h-5` (medium), `w-6 h-6` (large).

## Schema Validation (zod)

Client validation and network-payload validation use **zod** (`import { z } from 'zod'`). One schema
is the single source of truth — it yields both runtime validation (`.parse`) and the compile-time
type (`z.infer`). Decided in Task 191 (`decisions/client-validation-library-decision.md`).

- **Where schemas live:**
  - Cross-feature schemas → `src/app/schemas/{thing}.schema.ts` (e.g. `problem-detail.schema.ts`,
    the RFC 9457 shape every endpoint emits).
  - Feature schemas → `src/features/{feature}/schemas/{thing}.schema.ts`.
- **Derive types, don't hand-write them:** export the schema and the inferred type
  (`export type ProblemDetail = z.infer<typeof problemDetailSchema>`). Do not maintain a parallel
  `interface`. Migrate `types.ts` interfaces to inferred types as each schema is introduced.
- **Validate network payloads at the boundary:** `.api.ts` clients call
  `schema.parse(await response.json())` instead of an `as` cast. A parse failure is a contract
  violation — map it to the feature error, never accept it as a silent success.
- **Schemas are pure data declarations** — they live in the logic layer (`schemas/`), never in
  component files (consistent with the Humble Object rule in `frontend-rules.md`).
- **v4 API:** optional field `z.string().optional()`; readonly array `z.array(inner).readonly()`;
  guard form `schema.safeParse(value)` returns `{ success, data | error }`.

## Conditional className Syntax

- Ternary in `:class` binding: `:class="isActive ? 'bg-blue-500' : 'bg-gray-200'"`.
- Object syntax: `:class="{ 'bg-blue-500': isActive, 'bg-gray-200': !isActive }"`.
- Array syntax for combining: `:class="[baseClass, isActive ? 'active' : '']"`.
