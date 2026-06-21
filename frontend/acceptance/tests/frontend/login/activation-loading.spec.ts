import { test } from '@playwright/test';
import { ActivationPageStatements } from '../../statements/frontend/activation-page.statements';
import { ActivationLoadingStatements } from '../../statements/frontend/activation-loading.statements';
import { ActivationBackendStatements } from '../../statements/backend/activation-backend.statements';

test.describe('Activation Page Loading State', () => {
  let activationPage: ActivationPageStatements;
  let activationLoading: ActivationLoadingStatements;
  let activationBackend: ActivationBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationPage = new ActivationPageStatements(page, baseURL!);
    activationLoading = new ActivationLoadingStatements(page);
    activationBackend = new ActivationBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.4: Activation page shows loading state during submission - ' +
      'Given the activation page is displayed with a valid token, ' +
      'When the user submits a valid matching password, ' +
      'Then the activate button shows a loading indicator, ' +
      'And the form fields become disabled during submission',
    async () => {
      await activationBackend.givenSlowActivationRequest({ login: 'ivan', email: 'ivan@example.com' });
      await activationPage.navigateToActivationPageWithToken('valid-activation-token');
      await activationPage.assertPasswordFieldIsVisible();

      await activationLoading.submitValidMatchingPassword();

      await activationBackend.whileSlowActivationIsHeld(() => activationLoading.assertLoadingStateIsActive());
    },
  );
});
