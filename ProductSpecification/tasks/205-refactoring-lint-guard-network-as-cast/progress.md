# Task 205: Lint guard — forbid blind `as` on network response data -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Choose lint approach (A oxlint-broad / B oxlint-plugin / C eslint-selector)
- [x] design (present A/B/C with trade-offs, get user decision; record it in spec.md) — chose **(C)** ESLint `no-restricted-syntax` selector

### Step 2: Add the lint rule + fixture
- [~] refactor (add the chosen rule to the lint config; add a fixture that casts `response.json()` with `as` to prove the rule fires, and a `schema.parse(...)` fixture to prove it stays green)

### Step 3: Verify the CI gate
- [ ] refactor (run `npm run lint`; confirm the rule fails on the blind-`as` fixture and passes on `schema.parse`; confirm the whole repo is still clean)
