import { test } from '@playwright/test';
import { LoginErrorDismissStatements } from '../../statements/frontend/login-error-dismiss.statements';
import { AuthBackendStatements } from '../../statements/backend/auth-backend.statements';

test.describe('Login Error Banner Dismiss', () => {
  let errorDismiss: LoginErrorDismissStatements;
  let authBackend: AuthBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    errorDismiss = new LoginErrorDismissStatements(page, baseURL!);
    authBackend = new AuthBackendStatements(page);
  });

  test(
    'UI Test Scenario 3.3: Error banner dismiss button closes the banner - ' +
      'Given an error banner is visible on the page, ' +
      'When the user clicks the dismiss button on the banner, ' +
      'Then the error banner is no longer visible',
    async () => {
      // RED: LoginErrorBanner.vue has no dismiss button yet (data-testid="error-banner-dismiss"
      // does not exist) -> clickDismissButton() times out on a missing locator. Pinned by the
      // not-visible assertion that proves the dismiss actually hides the banner.
      test.fail();
      await authBackend.givenRegisteredUser('ivan', 'correct-pass');
      await errorDismiss.givenErrorBannerIsVisible('ivan', 'wrong-pass');

      await errorDismiss.clickDismissButton();

      await errorDismiss.assertErrorBannerIsNoLongerVisible();
    },
  );
});
