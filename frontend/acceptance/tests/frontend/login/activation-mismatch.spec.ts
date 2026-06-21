import { test } from '@playwright/test';
import { ActivationMismatchStatements } from '../../statements/frontend/activation-mismatch.statements';
import { ActivationBackendStatements } from '../../statements/backend/activation-backend.statements';

test.describe('Activation Page Password Mismatch', () => {
  let activationMismatch: ActivationMismatchStatements;
  let activationBackend: ActivationBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationMismatch = new ActivationMismatchStatements(page, baseURL!);
    activationBackend = new ActivationBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.3: Activation page shows error when passwords do not match - ' +
      'Given the activation page is displayed, ' +
      'When the user enters different values in the password and confirm password fields, ' +
      'Then an error message is displayed indicating the passwords do not match',
    async () => {
      // RED: ActivationPage.vue has no password/confirm mismatch check yet
      // (data-testid="password-mismatch-error" does not exist) -> the mismatch
      // error never renders. Pinned by the exact-text assertion proving the
      // "Passwords do not match" message appears when password !== confirm.
      test.fail();
      await activationBackend.givenPendingAccountForToken({ login: 'ivan', email: 'ivan@example.com' });
      await activationMismatch.navigateToActivationPageWithToken('valid-activation-token');

      await activationMismatch.enterMismatchedPasswords();

      await activationMismatch.assertMismatchErrorIsDisplayed();
    },
  );
});
