import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersPageStatements } from '../../statements/frontend/users-page.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('Users Navigation', () => {
  let homePage: HomePageStatements;
  let usersPage: UsersPageStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersPage = new UsersPageStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 1.2: Clicking Users navigates to the Users page inside the shell - ' +
      'Given an authenticated user is on the dashboard, ' +
      'When the user clicks the "Users" sidebar item, ' +
      'Then the Users page is displayed inside the same top bar and sidebar shell, ' +
      'And the page shows a "Register user" button',
    async () => {
      // RED: the "Users" sidebar item is a static link with no navigation wired, and there
      // is no /users route or Users page component yet. Clicking it leaves the user on the
      // dashboard, so the Users page and its "Register user" button never render.
      // Pinned by assertUsersPageIsVisible() + assertRegisterUserButtonIsVisible() below.
      test.fail();

      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await homePage.navigateToHomePage();

      await homePage.clickUsersNavItem();

      await usersPage.assertUsersPageIsVisible();
      await homePage.assertDashboardShellIsVisible();
      await homePage.assertTopbarLogoIsVisible();
      await homePage.assertSidebarIsVisible();
      await usersPage.assertRegisterUserButtonIsVisible();
    },
  );
});
