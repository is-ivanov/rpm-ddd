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

Vitest tests asserting Russian strings (flip expected text to English):
- `frontend/src/features/home/__tests__/home.smoke.test.ts`
- `frontend/src/app/__tests__/dashboard-user.logic.test.ts`
- `frontend/src/app/__tests__/current-user.api.test.ts`
- `frontend/src/app/__tests__/fetch.api.test.ts`
- `frontend/src/app/stores/__tests__/auth.store.test.ts`

Playwright acceptance tests + Statements asserting Russian strings (surfaced at fix time — flip to English):
- `frontend/acceptance/tests/statements/frontend/home-page.statements.ts` (`WELCOME_TAGLINE`, `LOGIN_BUTTON_TEXT`, `PAGE_TITLE_TEXT`)
- `frontend/acceptance/tests/statements/frontend/user-menu.statements.ts` (`LOGOUT_ACTION_TEXT`)
- `frontend/acceptance/tests/frontend/home/welcome-page.spec.ts`
- `frontend/acceptance/tests/frontend/home/welcome-to-login.spec.ts`
- `frontend/acceptance/tests/frontend/home/dashboard-page.spec.ts`
- `frontend/acceptance/tests/frontend/home/user-menu.spec.ts`
- `frontend/acceptance/tests/frontend/home/logout-to-welcome.spec.ts`
- `frontend/acceptance/tests/frontend/home/login-to-dashboard.spec.ts`

Story 03 mockups (frozen artifacts of a completed story — user opted to translate within Task 210 so they stay consistent with the now-English UI; docs edit, no TDD cycle):
- `ProductSpecification/stories/03-home-page/mockups/{desktop,mobile}/{01-welcome,02-dashboard,03-dashboard-user-menu,04-loading}.html` (8 files)

> Note: `LoginPage.vue` / `ActivationPage.vue` were checked and contain no Cyrillic — login/activation are already English. Production-code scope is the home/dashboard chrome + `AppLoading`. Re-confirm the full file list at fix time with `rg "\p{Cyrillic}" frontend/src frontend/acceptance ProductSpecification/stories`, as more Russian may have merged since — this is how the acceptance suite and mockups above were found.

## Reproduction

1. Run the frontend and sign in; observe Russian labels in the dashboard sidebar/topbar/user menu, the welcome screen, and the app-loading text.
2. `rg "\p{Cyrillic}" frontend/src frontend/acceptance ProductSpecification/stories/03-home-page` returns matches in production components, the Playwright acceptance suite, and the Story 03 mockups.

## Full-Stack Journey Verdict

**no-impact.** Text-only change to the home/dashboard chrome. The nightly full-stack journey (`account-lifecycle.fullstack.spec.ts`) exercises the register → activate → login lifecycle via `LoginPageStatements` / `ActivationPageStatements` (already English) and does not reuse `HomePageStatements` / `UserMenuStatements` nor assert any of the translated home/dashboard strings. No journey spec change required.

## Notes

This is a mechanical text change, not a logic defect: the TDD cycle is **flip the existing test expectations to English (red) → translate the components so they pass (green) → verify no Cyrillic remains (green-playwright) → demo**. No new tests are written; existing assertions are updated. If any genuinely new test is added, tag it with `#210` per the tech binding.
