import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('Dashboard Page', () => {
  let homePage: HomePageStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    currentUserBackend = new CurrentUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 2.1: Authenticated home shows the dashboard shell with the current user - ' +
      'Given an authenticated user with first name "John" and last name "Doe", ' +
      'When the user navigates to the home page, ' +
      'Then the page displays the top bar with the "RPM" logo, ' +
      'And the top bar displays the user\'s avatar with initials "JD", ' +
      'And the top bar displays the user\'s name "John Doe", ' +
      'And the page displays the navigation sidebar, ' +
      'And the main area displays the page title "Home", ' +
      'And the main area displays placeholder dashboard content',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await homePage.navigateToHomePage();

      await homePage.assertDashboardShellIsVisible();
      await homePage.assertTopbarLogoIsVisible();
      await homePage.assertUserAvatarShowsInitials('JD');
      await homePage.assertUserNameIsVisible('John Doe');
      await homePage.assertSidebarIsVisible();
      await homePage.assertPageTitleIsVisible();
      await homePage.assertPlaceholderContentIsVisible();
    },
  );
});
