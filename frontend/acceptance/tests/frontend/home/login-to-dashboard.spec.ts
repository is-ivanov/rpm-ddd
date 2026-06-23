import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';
import { AuthBackendStatements } from '../../statements/backend/auth-backend.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';

test.describe('Login to Dashboard Navigation', () => {
  let homePage: HomePageStatements;
  let loginPage: LoginPageStatements;
  let authBackend: AuthBackendStatements;
  let currentUserBackend: CurrentUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    loginPage = new LoginPageStatements(page, baseURL!);
    authBackend = new AuthBackendStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.2: Successful login redirects to the dashboard - ' +
      'Given a registered ACTIVE user with login "ipetrov" and password "correct-pass" exists, ' +
      'And the user is on the login page, ' +
      'When the user signs in with login "ipetrov" and password "correct-pass", ' +
      'Then the user is navigated to the home page, ' +
      "And the dashboard shell is displayed with the user's name in the top bar",
    async () => {
      // RED: LoginPage.submitLogin() does not redirect on success — the browser
      // stays on /login, pinned by assertNavigatedToHomeUrl() below.
      test.fail();

      await authBackend.givenRegisteredUser('ipetrov', 'correct-pass');
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'Иван', lastName: 'Петров' });
      await loginPage.navigateToLoginPage();

      await loginPage.enterLoginText('ipetrov');
      await loginPage.enterPasswordText('correct-pass');
      await loginPage.clickSubmitButton();

      await homePage.assertNavigatedToHomeUrl();
      await homePage.assertDashboardShellIsVisible();
      await homePage.assertUserNameIsVisible('Иван Петров');
    },
  );
});
