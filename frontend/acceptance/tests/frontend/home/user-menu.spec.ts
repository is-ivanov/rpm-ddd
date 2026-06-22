import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UserMenuStatements } from '../../statements/frontend/user-menu.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('User Menu', () => {
  let homePage: HomePageStatements;
  let userMenu: UserMenuStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    userMenu = new UserMenuStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
  });

  test(
    "UI Test Scenario 3.1: Opening the user menu shows the user's name, email, and logout action - " +
      'Given an authenticated user with name "Иван Петров" and email "i.petrov@rpm.local", ' +
      'And the user is on the dashboard, ' +
      'When the user clicks the avatar in the top bar, ' +
      'Then a menu opens displaying the name "Иван Петров", ' +
      'And the menu displays the email "i.petrov@rpm.local", ' +
      'And the menu displays an action with text "Выйти"',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({
        firstName: 'Иван',
        lastName: 'Петров',
        email: 'i.petrov@rpm.local',
      });
      await homePage.navigateToHomePage();

      await homePage.clickUserAvatar();

      await userMenu.assertMenuIsOpen();
      await userMenu.assertMenuShowsName('Иван Петров');
      await userMenu.assertMenuShowsEmail('i.petrov@rpm.local');
      await userMenu.assertMenuShowsLogoutAction();
    },
  );
});
