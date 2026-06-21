import { test } from '@playwright/test';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';
import { LoginLoadingStatements } from '../../statements/frontend/login-loading.statements';
import { AuthBackendStatements } from '../../statements/backend/auth-backend.statements';

test.describe('Login Page Loading State', () => {
  let loginPage: LoginPageStatements;
  let loginLoading: LoginLoadingStatements;
  let authBackend: AuthBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginPage = new LoginPageStatements(page, baseURL!);
    loginLoading = new LoginLoadingStatements(page);
    authBackend = new AuthBackendStatements(page);
  });

  test(
    'UI Test Scenario 2.2: Login page shows loading state during submission - ' +
      'Given the login page is displayed, ' +
      'When the user submits valid credentials, ' +
      'Then the login button shows a loading indicator, ' +
      'And the form fields become disabled during submission',
    async () => {
      await authBackend.givenSlowLoginRequest('ivan', 'correct-pass');
      await loginPage.navigateToLoginPage();

      await loginLoading.submitValidCredentials('ivan', 'correct-pass');

      await authBackend.whileSlowLoginIsHeld(() => loginLoading.assertLoadingStateIsActive());
    },
  );
});
