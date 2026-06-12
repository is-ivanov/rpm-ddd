import { test } from '@playwright/test';
import { issue } from 'allure-js-commons';
import { ActivationPageStatements } from '../../statements/frontend/activation-page.statements';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';
import { ActivationBackendStatements } from '../../statements/backend/activation-backend.statements';

test.describe('Global 401 Redirect', () => {
  let activationPage: ActivationPageStatements;
  let loginPage: LoginPageStatements;
  let activationBackend: ActivationBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationPage = new ActivationPageStatements(page, baseURL!);
    loginPage = new LoginPageStatements(page, baseURL!);
    activationBackend = new ActivationBackendStatements(page);
  });

  // RED (#162): no global 401 handler exists yet — ActivationPage catches the error locally and
  // stays on /activate showing the expired-link view; assertNavigatedToLoginPage (toHaveURL
  // "/login") times out with received URL "/activate?token=expired-session-token".
  test.fail(
    'UI Bug #162: An API call answered with 401 lands the unauthenticated user on the login page - ' +
      'Given the backend session has expired so API calls return 401 Unauthorized, ' +
      'When the user opens the activation page from an email link, ' +
      'And the page triggers an API call that returns 401, ' +
      'Then the browser is navigated to the login page, ' +
      'And the login page displays the login form',
    async () => {
      await issue('162');
      await activationBackend.givenSessionExpired();

      await activationPage.navigateToActivationPageWithToken('expired-session-token');

      await activationPage.assertNavigatedToLoginPage();
      await loginPage.assertLoginFieldIsVisible();
      await loginPage.assertPasswordFieldIsVisible();
      await loginPage.assertSubmitButtonIsVisible();
    },
  );
});
