import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('Welcome Page', () => {
  let homePage: HomePageStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    currentUserBackend = new CurrentUserBackendStatements(page);
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
      await currentUserBackend.givenUnauthenticated();
      await homePage.navigateToHomePage();

      await homePage.assertWelcomeLogoIsVisible();
      await homePage.assertTaglineIsVisible();
      await homePage.assertLoginButtonIsVisible();
      await homePage.assertDashboardShellIsAbsent();
    },
  );
});
