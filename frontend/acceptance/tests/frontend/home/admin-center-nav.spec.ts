import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('Admin Center Navigation', () => {
  let homePage: HomePageStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    currentUserBackend = new CurrentUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 1.1: Sidebar shows an Admin Center group with a Users item - ' +
      'Given an authenticated user is on the dashboard, ' +
      'Then the sidebar displays an "Admin Center" group, ' +
      'And the group contains a "Users" item',
    async () => {
      // RED: DashboardShell.vue sidebar renders only a placeholder — no "Admin Center"
      // group label and no "Users" nav item, and no admin-center-group / users-nav-item
      // test-ids. assertAdminCenterGroupIsVisible() fails (locator resolves to 0 elements,
      // toBeVisible times out). Cleared in align-design when the sidebar groups are built.
      test.fail();
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await homePage.navigateToHomePage();

      await homePage.assertSidebarIsVisible();
      await homePage.assertAdminCenterGroupIsVisible();
      await homePage.assertUsersNavItemIsVisible();
    },
  );
});
