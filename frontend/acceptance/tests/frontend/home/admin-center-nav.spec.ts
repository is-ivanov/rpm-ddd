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
      await currentUserBackend.givenAuthenticatedUser();
      await homePage.navigateToHomePage();

      await homePage.assertSidebarIsVisible();
      await homePage.assertAdminCenterGroupIsVisible();
      await homePage.assertUsersNavItemIsVisible();
    },
  );
});
