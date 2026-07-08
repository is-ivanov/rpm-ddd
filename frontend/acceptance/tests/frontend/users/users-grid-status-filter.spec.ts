import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersPageStatements } from '../../statements/frontend/users-page.statements';
import { UsersGridStatusFilterStatements } from '../../statements/frontend/users-grid-status-filter.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';

test.describe('Users Grid Status Filter', () => {
  let homePage: HomePageStatements;
  let usersPage: UsersPageStatements;
  let statusFilter: UsersGridStatusFilterStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersPage = new UsersPageStatements(page);
    statusFilter = new UsersGridStatusFilterStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
    adminUsersBackend = new AdminUsersBackendStatements(page);
  });

  test(
    'UI Test Scenario 3.6: Status column filter is a lifecycle-ordered multi-select - ' +
      'Given the Users page shows users of varied statuses, ' +
      'When the user opens the Status column filter, ' +
      'Then the status options are listed in lifecycle order (Pending, Active, Locked, Inactive), ' +
      'When the user selects Pending and Locked, ' +
      'Then only rows with those statuses remain visible',
    async () => {
      await currentUserBackend.givenAuthenticatedUser();
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.assertGridIsVisible();

      await statusFilter.assertStatusFilterIsVisible();

      await statusFilter.openStatusFilter();
      await statusFilter.assertStatusOptionsInLifecycleOrder();

      await statusFilter.selectStatuses();
      await statusFilter.assertOnlyRowsWithSelectedStatusesRemain();
    },
  );
});
