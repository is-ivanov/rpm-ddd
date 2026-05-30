# Vue 3/TypeScript TDD Conventions

## Testing Framework

- Logic tests: Vitest, pure functions, no DOM, no Vue.
- API client tests: Vitest + MSW (Mock Service Worker).

## Content / Snapshot Approval (Native Vitest)

For approval/snapshot verification — the universal "rendered-content verification" rule. Use Vitest's **built-in** snapshots; do NOT add an external approval library (selfie has no released JS port).

- `toMatchInlineSnapshot()` — small deterministic outputs (view-model objects, request payloads). Auto-populates on first run, updated with `vitest -u` (mirrors the literal-snapshot DX).
- `toMatchFileSnapshot('./__snapshots__/x.html')` — larger rendered text (e.g. an HTML fragment).
- **Scope to deterministic DATA only** — logic/view-model outputs and request bodies. Do NOT snapshot component markup: it is brittle (churns on every style/markup tweak) and can freeze the hardcoded placeholder data that `design-review` is meant to catch. Visual/markup fidelity stays with `align-design` + `design-review`, not snapshots.
- **Determinism (mandatory):** fix all variable inputs (no `Date.now()`/random ids) so snapshots don't flake.
- **CI:** snapshots must be read-only on CI (fail on mismatch); never run `-u` in CI.

## Test Skip Marker

- `.skip` is the test skip marker. Comment above `.skip` documents failure reason.

## Base URL Configuration

- Base URL resolved via `import.meta.env.VITE_API_URL`.
- Vitest sets `VITE_API_URL` dynamically from `BACKEND_PORT` via `vite.config.ts`.
- Production API clients: `const BASE_URL = import.meta.env.VITE_API_URL ?? ''`.
- MSW tests: `const BASE = import.meta.env.VITE_API_URL`.
