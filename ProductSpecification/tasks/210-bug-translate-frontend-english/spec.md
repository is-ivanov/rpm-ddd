# Task 210: Translate frontend UI text from Russian to English

Type: bug
Issue: #210

## Problem

The product interface language is **English** (decided during Story 4 / User management). Already-merged `main` still renders **Russian** UI text in production Vue components, so the live app is in the wrong language.

## Solution

Replace every Russian user-visible string with its English equivalent and set `lang="en"` where applicable. Update the existing tests that assert the Russian strings to expect the English text. **Text only — no logic changes.**

Verify with `rg "\p{Cyrillic}" frontend/src` that no Russian remains in production code; `npm run lint` and the frontend test suite must pass.

## Key Files

Production components:
- `frontend/src/features/home/components/UserMenu.vue`
- `frontend/src/features/home/components/DashboardShell.vue`
- `frontend/src/features/home/components/WelcomeView.vue`
- `frontend/src/app/components/AppLoading.vue`

Tests asserting Russian strings (flip expected text to English):
- `frontend/src/features/home/__tests__/home.smoke.test.ts`
- `frontend/src/app/__tests__/dashboard-user.logic.test.ts`
- `frontend/src/app/__tests__/current-user.api.test.ts`
- `frontend/src/app/__tests__/fetch.api.test.ts`
- `frontend/src/app/stores/__tests__/auth.store.test.ts`

> Note: `LoginPage.vue` / `ActivationPage.vue` were checked and contain no Cyrillic — login/activation are already English. Scope is the home/dashboard chrome + `AppLoading` only. Re-confirm the full file list at fix time with `rg "\p{Cyrillic}" frontend/src`, as more Russian may have merged since.

## Reproduction

1. Run the frontend and sign in; observe Russian labels in the dashboard sidebar/topbar/user menu, the welcome screen, and the app-loading text.
2. `rg "\p{Cyrillic}" frontend/src` returns matches in production components.

## Notes

This is a mechanical text change, not a logic defect: the TDD cycle is **flip the existing test expectations to English (red) → translate the components so they pass (green) → verify no Cyrillic remains (green-playwright) → demo**. No new tests are written; existing assertions are updated. If any genuinely new test is added, tag it with `#210` per the tech binding.
