import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersPageStatements } from '../../statements/frontend/users-page.statements';
import { UsersGridEmptyResultStatements } from '../../statements/frontend/users-grid-empty-result.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';

test.describe('Users Grid Empty-Result State', () => {
  let homePage: HomePageStatements;
  let usersPage: UsersPageStatements;
  let emptyResult: UsersGridEmptyResultStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersPage = new UsersPageStatements(page);
    emptyResult = new UsersGridEmptyResultStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
    adminUsersBackend = new AdminUsersBackendStatements(page);
  });

  test(
    'UI Test Scenario 3.8: Filtering to no matches shows an empty-result message - ' +
      'Given the Users page shows multiple users, ' +
      'When the user types a filter value that matches no rows, ' +
      'Then the grid shows an empty-result message, ' +
      'And clearing the filter restores all rows',
    async () => {
      await currentUserBackend.givenAuthenticatedUser();
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.assertGridIsVisible();

      await emptyResult.enterNoMatchFilter();
      await emptyResult.assertEmptyResultMessageIsVisible();

      await emptyResult.clearFilter();
      await emptyResult.assertAllRowsRestored();
    },
  );
});
