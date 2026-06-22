import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { AuthBackendStatements } from '../../statements/backend/auth-backend.statements';

test.describe('Welcome Page', () => {
  let homePage: HomePageStatements;
  let authBackend: AuthBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    authBackend = new AuthBackendStatements(page);
  });

  test(
    'UI Test Scenario 1.1: Unauthenticated home shows welcome with logo, tagline, and login button - ' +
      'Given the user is not authenticated, ' +
      'When the user navigates to the home page, ' +
      'Then the page displays the "RPM" logo, ' +
      'And the page displays the tagline "Удалённый мониторинг пациентов", ' +
      'And the page displays a button with text "Войти", ' +
      'And the dashboard shell is not displayed',
    async () => {
      // RED: HomePage still renders the placeholder (home-title/home-subtitle) — no welcome-logo
      // testid yet, so assertWelcomeLogoIsVisible() below times out. Pinned by that assertion.
      test.fail();

      await authBackend.givenUnauthenticated();
      await homePage.navigateToHomePage();

      await homePage.assertWelcomeLogoIsVisible();
      await homePage.assertTaglineIsVisible();
      await homePage.assertLoginButtonIsVisible();
      await homePage.assertDashboardShellIsAbsent();
    },
  );
});
