import { test } from '@playwright/test';
import { issue } from 'allure-js-commons';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';

test.describe('Session Lost Mid-Page Redirect', () => {
  let homePage: HomePageStatements;
  let loginPage: LoginPageStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    loginPage = new LoginPageStatements(page, baseURL ?? '');
    currentUserBackend = new CurrentUserBackendStatements(page);
    adminUsersBackend = new AdminUsersBackendStatements(page);
  });

  test(
    'UI Bug #251: the app redirects to /login when an API call returns 401 mid-page - ' +
      'Given an authenticated user is on the Users page, ' +
      'When GET /api/admin/users returns 401 because the session was lost, ' +
      'Then the app reactively redirects to the /login page',
    async () => {
      await issue('251');
      // RED (#251): no reactive watcher on isAuthenticated exists yet. The 401 resets the
      // auth store but the app stays on /users instead of redirecting to /login, so the URL
      // assertion below times out (Received .../users, Expected .../login).
      test.fail();

      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await adminUsersBackend.givenAdminUserListUnauthorized();
      await homePage.navigateToHomePage();

      await homePage.clickUsersNavItem();

      await loginPage.assertNavigatedToLoginUrl();
    },
  );
});
