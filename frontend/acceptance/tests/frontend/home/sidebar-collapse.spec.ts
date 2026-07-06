import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { SidebarCollapseStatements } from '../../statements/frontend/sidebar-collapse.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('Sidebar Collapse', () => {
  let homePage: HomePageStatements;
  let sidebarCollapse: SidebarCollapseStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    sidebarCollapse = new SidebarCollapseStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 6.1: Collapse toggle persists across reload - ' +
      'Given an authenticated user is on the dashboard with the sidebar expanded, ' +
      'When the user clicks the sidebar collapse toggle, ' +
      'Then the sidebar collapses, ' +
      'When the user reloads the page, ' +
      'Then the sidebar is still collapsed',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await homePage.navigateToHomePage();

      await sidebarCollapse.assertCollapseToggleIsVisible();
      await sidebarCollapse.assertSidebarIsExpanded();

      await sidebarCollapse.clickCollapseToggle();

      await sidebarCollapse.assertSidebarIsCollapsed();

      await sidebarCollapse.reloadPage();

      await sidebarCollapse.assertSidebarIsCollapsed();
    },
  );
});
