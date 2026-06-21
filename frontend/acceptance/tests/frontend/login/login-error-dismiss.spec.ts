import { test } from '@playwright/test';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';
import { LoginErrorDismissStatements } from '../../statements/frontend/login-error-dismiss.statements';
import { AuthBackendStatements } from '../../statements/backend/auth-backend.statements';

test.describe('Login Error Banner Dismiss', () => {
  let loginPage: LoginPageStatements;
  let errorDismiss: LoginErrorDismissStatements;
  let authBackend: AuthBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginPage = new LoginPageStatements(page, baseURL!);
    errorDismiss = new LoginErrorDismissStatements(page);
    authBackend = new AuthBackendStatements(page);
  });

  test(
    'UI Test Scenario 3.3: Error banner dismiss button closes the banner - ' +
      'Given an error banner is visible on the page, ' +
      'When the user clicks the dismiss button on the banner, ' +
      'Then the error banner is no longer visible',
    async () => {
      await authBackend.givenRegisteredUser('ivan', 'correct-pass');
      await loginPage.navigateToLoginPage();
      await errorDismiss.submitWrongCredentialsAndSeeErrorBanner('ivan', 'wrong-pass');

      await errorDismiss.clickDismissButton();

      await errorDismiss.assertErrorBannerIsNoLongerVisible();
    },
  );
});
