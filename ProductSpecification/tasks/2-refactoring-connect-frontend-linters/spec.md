# Task 2: Connect Frontend Linters

Type: refactoring

## Problem

The frontend (`frontend/`, Vue 3 + TypeScript + Vite) has **no static analysis / linters** connected:

- `frontend/package.json` declares only `vue-tsc --noEmit` (type-check) — no ESLint, Prettier, or Stylelint, and no config files exist in `frontend/`.
- CI (`.github/workflows/build.yml`) runs `mvn verify -B -Pfrontend`, and the `frontend` Maven profile (`pom.xml`) only executes `install-node-and-npm`, `npm ci`, and `npm run build`. No lint step runs anywhere.

As a result, frontend code style and common bug patterns are never enforced, and nothing fails the build on lint violations the way Checkstyle/PMD do for the backend.

## Solution

Connect frontend static analysis and wire it into the Maven `frontend` profile so it runs in GitHub Actions and fails the build on violations, mirroring the backend Checkstyle/PMD gate.

1. Add **ESLint** (flat config) with `@typescript-eslint`, `eslint-plugin-vue`, and Prettier integration (`eslint-config-prettier` + `prettier`). Optionally add **Stylelint** for CSS/Tailwind if deemed valuable.
2. Add a flat ESLint config (`eslint.config.ts`/`.js`) and a Prettier config aligned with the repo `.editorconfig` (4-space indent, 120-char line limit, UTF-8, final newline).
3. Add `lint` (and `lint:fix`, `format`) scripts to `frontend/package.json`.
4. Add an `npm-lint` execution to the `frontend-maven-plugin` profile in `pom.xml` (running `npm run lint`) so `mvn verify -Pfrontend` enforces it in CI.
5. Run the linter, fix or baseline existing violations so the build is green.

Note: this task is **tooling only** — it does NOT need to wire frontend tests (vitest/playwright) into CI or Allure; that gap is tracked separately if the team decides to address it.

## Key Files

- `frontend/package.json` — add lint deps + scripts
- `frontend/eslint.config.*` — new ESLint flat config
- `frontend/.prettierrc*` / `frontend/prettier.config.*` — new Prettier config
- `pom.xml` — add `npm-lint` execution to the `frontend` profile (lines ~526–595)
- `.editorconfig` — source of truth for formatting rules to mirror
- `.github/workflows/build.yml` — already runs `-Pfrontend`; no change needed if lint is wired into the profile
