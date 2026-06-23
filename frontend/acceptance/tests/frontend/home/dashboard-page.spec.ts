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
      'Given an authenticated user with first name "Иван" and last name "Петров", ' +
      'When the user navigates to the home page, ' +
      'Then the page displays the top bar with the "RPM" logo, ' +
      'And the top bar displays the user\'s avatar with initials "ИП", ' +
      'And the top bar displays the user\'s name "Иван Петров", ' +
      'And the page displays the navigation sidebar, ' +
      'And the main area displays the page title "Главная", ' +
      'And the main area displays placeholder dashboard content',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'Иван', lastName: 'Петров' });
      await homePage.navigateToHomePage();

      await homePage.assertDashboardShellIsVisible();
      await homePage.assertTopbarLogoIsVisible();
      await homePage.assertUserAvatarShowsInitials('ИП');
      await homePage.assertUserNameIsVisible('Иван Петров');
      await homePage.assertSidebarIsVisible();
      await homePage.assertPageTitleIsVisible();
      await homePage.assertPlaceholderContentIsVisible();
    },
  );
});
