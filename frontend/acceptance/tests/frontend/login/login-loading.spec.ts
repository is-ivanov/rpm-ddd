import { test } from '@playwright/test';
import { LoginLoadingStatements } from '../../statements/frontend/login-loading.statements';
import { AuthBackendStatements } from '../../statements/backend/auth-backend.statements';

test.describe('Login Page Loading State', () => {
  let loginLoading: LoginLoadingStatements;
  let authBackend: AuthBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginLoading = new LoginLoadingStatements(page, baseURL!);
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
      await loginLoading.navigateToLoginPage();

      await loginLoading.submitValidCredentials('ivan', 'correct-pass');

      await authBackend.whileSlowLoginIsHeld(() => loginLoading.assertLoadingStateIsActive());
    },
  );
});
