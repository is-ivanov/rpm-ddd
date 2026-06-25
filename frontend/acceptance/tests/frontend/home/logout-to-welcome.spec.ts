import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UserMenuStatements } from '../../statements/frontend/user-menu.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('Logout to Welcome Navigation', () => {
  let homePage: HomePageStatements;
  let userMenu: UserMenuStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    userMenu = new UserMenuStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.3: Logging out from the user menu returns to the welcome page - ' +
      'Given an authenticated user is on the dashboard, ' +
      'And the user has opened the user menu, ' +
      'When the user clicks "Log out", ' +
      'Then the session is ended, ' +
      'And the user is shown the welcome page with the "Sign in" button',
    async () => {
      // RED — WelcomeView still renders the Russian "Войти" button after logout (Task 210); GREEN translates it to "Sign in"
      test.fail();
      await currentUserBackend.givenAuthenticatedUserUntilLogout({ firstName: 'John', lastName: 'Doe' });
      await homePage.navigateToHomePage();
      await homePage.clickUserAvatar();

      await userMenu.clickLogout();

      await homePage.assertLoginButtonIsVisible();
      await homePage.assertDashboardShellIsAbsent();
    },
  );
});
