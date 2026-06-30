import { test } from '@playwright/test';
import { issue } from 'allure-js-commons';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersGridErrorStatements } from '../../statements/frontend/users-grid-error.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';

test.describe('Users Grid Load Failure', () => {
  let homePage: HomePageStatements;
  let usersError: UsersGridErrorStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersError = new UsersGridErrorStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
    adminUsersBackend = new AdminUsersBackendStatements(page);
  });

  test(
    'UI Bug #250: Users page shows an error state with a retry button when the list request fails - ' +
      'Given the user is signed in and on the Users page, ' +
      'When GET /api/admin/users fails with a 500 server error, ' +
      'Then the Users page shows an error state with a message and a retry button, ' +
      'And it does not silently render an empty grid',
    async () => {
      await issue('250');
      // RED: admin-users.api.ts uses raw fetch() and UsersPage.vue has try/finally with no
      // catch, so a failed load is silently swallowed and no error UI renders. Pinned by
      // assertErrorStateIsVisible() — getByTestId('users-grid-error') does not exist yet.
      // (401 session-dead → redirect to /login is Task #251, not an error state.)
      test.fail();

      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await adminUsersBackend.givenAdminUserListServerError();
      await homePage.navigateToHomePage();

      await homePage.clickUsersNavItem();

      await usersError.assertLoadFailureStateIsShown();
    },
  );
});
