import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersPageStatements } from '../../statements/frontend/users-page.statements';
import { UsersGridSortStatements } from '../../statements/frontend/users-grid-sort.statements';
import { UsersGridTimeStatements } from '../../statements/frontend/users-grid-time.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';
import { FIXED_NOW_INSTANT } from '../../statements/support/users-grid-time.fixture';

test.describe('Users Grid', () => {
  let homePage: HomePageStatements;
  let usersPage: UsersPageStatements;
  let usersSort: UsersGridSortStatements;
  let usersTime: UsersGridTimeStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersPage = new UsersPageStatements(page);
    usersSort = new UsersGridSortStatements(page);
    usersTime = new UsersGridTimeStatements(page);
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

  test(
    'UI Test Scenario 3.1: Typing in a column filter narrows the rows client-side - ' +
      'Given the Users page shows multiple users, ' +
      'When the user types text into the Full name column filter, ' +
      'Then only rows whose Full name contains that text remain visible, ' +
      'And no additional network request is made',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.assertGridIsVisible();
      await usersPage.assertFullNameFilterIsVisible();

      await usersPage.enterFullNameFilter();

      await usersPage.assertOnlyMatchingFullNamesRemain();
      adminUsersBackend.assertAdminUserListRequestedOnce();
    },
  );

  test(
    'UI Test Scenario 3.2: Clicking a column header sorts the rows - ' +
      'Given the Users page shows multiple users, ' +
      'When the user clicks the Login column header, ' +
      'Then the rows are sorted ascending by Login, ' +
      'When the user clicks the Login column header again, ' +
      'Then the rows are sorted descending by Login, ' +
      'And the Status column sorts by lifecycle order (Pending, Active, Locked, Inactive), not alphabetically',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.assertGridIsVisible();

      await usersSort.clickLoginHeader();
      await usersSort.assertLoginsSortedAscending();

      await usersSort.clickLoginHeader();
      await usersSort.assertLoginsSortedDescending();

      await usersSort.clickStatusHeader();
      await usersSort.assertStatusesSortedByLifecycleOrder();
    },
  );

  test(
    'UI Test Scenario 3.3: Created column shows relative time with an absolute-time hover tooltip - ' +
      'Given the Users page shows a user created in the past, ' +
      'Then the Created column shows a relative time (e.g. "7 days ago"), ' +
      'When the user hovers over the relative time, ' +
      'Then a tooltip shows the full absolute time rendered in the viewer profile timezone, ' +
      'And the tooltip includes the date, time, timezone label, and IANA zone id',
    // RED: the Created cell currently renders the raw ISO timestamp (row.createdAt) and no
    // tooltip element exists anywhere in the app, so both the relative-label assertion and the
    // hover-tooltip assertion fail. The clock is frozen at FIXED_NOW_INSTANT before navigation
    // so the green implementation's relative-time computation is deterministic.
    async ({ page }) => {
      test.fail();
      await page.clock.setFixedTime(FIXED_NOW_INSTANT);
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.assertGridIsVisible();

      await usersTime.assertCreatedCellRelativeLabelAndTooltip();
    },
  );
});
