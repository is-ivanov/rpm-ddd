# Vue 3/TypeScript Infrastructure Idioms

Tech binding for `infrastructure.md`. Load alongside the universal rules.

## Commands

- Dev server: `npm run dev`
- Run tests: `npx vitest run`

## Static Analysis (Pre-Commit)

- Run before every commit that touches frontend files: `npm run lint` (from the `frontend/` directory). It runs three linters in order — `npm run lint:oxlint` (the fast oxlint gate), then `eslint .`, then `prettier --check .` — the same gate the CI "Frontend Lint" job enforces.
- Two-linter split: **oxlint** owns the JS/TS logic-layer correctness/suspicious rules (runs in ms, configured in `.oxlintrc.json`); **ESLint** keeps the Vue-template (`eslint-plugin-vue`) and type-aware rules. `eslint-plugin-oxlint` (`buildFromOxlintConfigFile` in `eslint.config.js`) turns off the ESLint rules oxlint already covers, so the two don't double-report. **Prettier** stays the only formatter.
- For the fast first-pass check alone, run `npm run lint:oxlint` (`oxlint --max-warnings=0`, so `suspicious` warnings also fail).
- If violations are found, auto-fix with `npm run lint:fix` (`oxlint --fix && eslint . --fix && prettier --write .`), then re-run `npm run lint` to confirm it passes before committing.
- ESLint warnings do not fail `eslint` by itself, but still fix them (`lint:fix` handles most) — a clean `npm run lint` is the bar.

## Environment Variables

- `VITE_API_URL` — backend base URL, injected via `vite.config.ts` from `BACKEND_PORT`.
- Config fallback syntax: `process.env.VAR || 'fallback'` (JS syntax).

## Process Safety

- Dangerous commands for Node: `taskkill //IM node.exe`, `pkill node` -- these kill ALL instances system-wide.
