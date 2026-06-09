# rpm-frontend

Vue 3 + Vite + TypeScript frontend for rpm-ddd.

## Scripts

| Command               | What it does                                                   |
| --------------------- | -------------------------------------------------------------- |
| `npm run dev`         | Start the Vite dev server.                                     |
| `npm run build`       | Type-check (`vue-tsc`) and build for production.               |
| `npm run test`        | Run the Vitest unit/component suite once.                      |
| `npm run test:e2e`    | Run the Playwright end-to-end suite.                           |
| `npm run lint`        | Full lint gate: oxlint → ESLint → Prettier (CI enforces this). |
| `npm run lint:oxlint` | Fast first-pass oxlint gate only.                              |
| `npm run lint:fix`    | Auto-fix: `oxlint --fix`, then `eslint --fix`, then Prettier.  |
| `npm run format`      | Format all files with Prettier.                                |

## Linting

The frontend uses **two linters plus a formatter**, run in order by `npm run lint`:

1. **[oxlint](https://oxc.rs/docs/guide/usage/linter.html)** — a fast Rust-based
   linter (the first gate). It owns correctness/suspicious rules on the JS/TS logic
   layer (`*.logic.ts`, API clients, etc.) and runs in milliseconds. Configured in
   [`.oxlintrc.json`](./.oxlintrc.json) with the `correctness` (error) and
   `suspicious` (warn) categories; `lint:oxlint` runs it with `--max-warnings=0` so
   warnings also fail.
2. **ESLint** — keeps the Vue-template rules (`eslint-plugin-vue`) and type-aware
   rules that oxlint does not cover. [`eslint.config.js`](./eslint.config.js) ends
   with `eslint-plugin-oxlint`'s `buildFromOxlintConfigFile('.oxlintrc.json')`, which
   disables the ESLint rules oxlint already owns so the two never double-report.
3. **Prettier** — the sole formatter (oxlint and ESLint do not format).

CI's `frontend-lint` job runs oxlint as an explicit fast gate and then `npm run lint`,
so any violation fails the build. Run `npm run lint` (or `npm run lint:fix`) before
committing any change under `frontend/`.
