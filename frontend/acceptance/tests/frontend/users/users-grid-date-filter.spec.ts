import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersPageStatements } from '../../statements/frontend/users-page.statements';
import { UsersGridDateFilterStatements } from '../../statements/frontend/users-grid-date-filter.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';

test.describe('Users Grid Created Date-Range Filter', () => {
  let homePage: HomePageStatements;
  let usersPage: UsersPageStatements;
  let dateFilter: UsersGridDateFilterStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersPage = new UsersPageStatements(page);
    dateFilter = new UsersGridDateFilterStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
    adminUsersBackend = new AdminUsersBackendStatements(page);
  });

  test(
    'UI Test Scenario 3.7: Created date-range filter narrows by the underlying instant - ' +
      'Given the Users page shows users created across several days, ' +
      'When the user sets a Created from–to date range, ' +
      'Then only rows whose underlying created instant falls within the range remain visible, ' +
      'And the range filter operates on the absolute instant, not the relative label',
    async () => {
      await currentUserBackend.givenAuthenticatedUser();
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.assertGridIsVisible();

      await dateFilter.assertCreatedRangeFilterIsVisible();

      await dateFilter.openCreatedRangeFilter();
      await dateFilter.enterCreatedRange();
      await dateFilter.assertOnlyRowsInCreatedRangeRemain();
    },
  );
});
