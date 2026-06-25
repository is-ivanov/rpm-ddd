import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('Welcome to Login Navigation', () => {
  let homePage: HomePageStatements;
  let loginPage: LoginPageStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    loginPage = new LoginPageStatements(page, baseURL!);
    currentUserBackend = new CurrentUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.1: Clicking "Sign in" on the welcome page opens the login page - ' +
      'Given the user is not authenticated, ' +
      'And the user is on the welcome page, ' +
      'When the user clicks the "Sign in" button, ' +
      'Then the user is navigated to the login page',
    async () => {
      // RED — WelcomeView still renders the Russian "Войти" button (Task 210); GREEN translates it to "Sign in"
      test.fail();
      await currentUserBackend.givenUnauthenticated();
      await homePage.navigateToHomePage();
      await homePage.assertLoginButtonIsVisible();

      await homePage.clickLoginButton();

      await loginPage.assertLoginPageIsVisible();
    },
  );
});
