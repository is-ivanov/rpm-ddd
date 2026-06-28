import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersPageStatements } from '../../statements/frontend/users-page.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';

test.describe('Users Grid', () => {
  let homePage: HomePageStatements;
  let usersPage: UsersPageStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersPage = new UsersPageStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
    adminUsersBackend = new AdminUsersBackendStatements(page);
  });

  test(
    'UI Test Scenario 2.1: Grid renders all columns and rows from the API - ' +
      'Given the admin user list returns several users, ' +
      'When the user opens the Users page, ' +
      'Then the grid displays columns: Full name, Login, Email, Status, Created, Created by, Updated, Updated by, ' +
      'And each row shows the full name, login, and email from the API, ' +
      'And each row shows a status badge (Active, Pending, Locked, or Inactive), ' +
      'And each audit actor is shown abbreviated as "J. Doe", ' +
      'And the seed actor is shown as "System"',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();

      await homePage.clickUsersNavItem();

      await usersPage.assertUsersPageIsVisible();
      await usersPage.assertGridIsVisible();
      await usersPage.assertAllColumnHeadersAreDisplayed();
      await usersPage.assertEachRowShowsNameLoginEmail();
      await usersPage.assertEachRowShowsStatusBadge();
      await usersPage.assertAuditActorsAreAbbreviated();
      await usersPage.assertSeedActorIsShownAsSystem();
    },
  );

  test(
    'UI Test Scenario 2.2: Grid shows a loading state while fetching - ' +
      'Given the admin user list request is in flight, ' +
      'When the user opens the Users page, ' +
      'Then the grid shows a loading state, ' +
      'And the rows render once the response arrives',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await adminUsersBackend.givenAdminUserListInFlight();
      await homePage.navigateToHomePage();

      await homePage.clickUsersNavItem();

      await usersPage.assertLoadingStateIsVisible();

      adminUsersBackend.releaseAdminUserList();

      await usersPage.assertRowsRenderAfterResponse();
    },
  );
});
