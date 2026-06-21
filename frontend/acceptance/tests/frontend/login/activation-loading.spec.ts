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
      // RED: ActivationPage.vue has no in-flight loading state for the activate
      // button yet (data-testid="activate-loading" does not exist, and the
      // password/confirm fields are not disabled while the activate POST is in
      // flight) -> the loading indicator never renders. Pinned by the exact
      // activate-loading visibility assertion proving the loading state appears
      // while the activate request is held in flight.
      test.fail();
      await activationBackend.givenSlowActivationRequest({ login: 'ivan', email: 'ivan@example.com' });
      await activationPage.navigateToActivationPageWithToken('valid-activation-token');
      await activationPage.assertPasswordFieldIsVisible();

      await activationLoading.submitValidMatchingPassword();

      await activationBackend.whileSlowActivationIsHeld(() => activationLoading.assertLoadingStateIsActive());
    },
  );
});
