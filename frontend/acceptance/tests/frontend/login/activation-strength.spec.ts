import { test } from '@playwright/test';
import { ActivationPageStatements } from '../../statements/frontend/activation-page.statements';
import { ActivationStrengthStatements } from '../../statements/frontend/activation-strength.statements';
import { ActivationBackendStatements } from '../../statements/backend/activation-backend.statements';

test.describe('Activation Page Password Complexity Rules', () => {
  let activationPage: ActivationPageStatements;
  let activationStrength: ActivationStrengthStatements;
  let activationBackend: ActivationBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationPage = new ActivationPageStatements(page, baseURL!);
    activationStrength = new ActivationStrengthStatements(page);
    activationBackend = new ActivationBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.2: Activation page shows password complexity rules updating in real-time - ' +
      'Given the activation page is displayed, ' +
      'When the user types a password that satisfies only some complexity rules, ' +
      'Then only the satisfied complexity rules are marked as met and the rest remain unmet, ' +
      'When the user updates the password to satisfy all complexity rules, ' +
      'Then all complexity rules update to met in real-time',
    async () => {
      await activationBackend.givenPendingAccountForToken({ login: 'ivan', email: 'ivan@example.com' });
      await activationPage.navigateToActivationPageWithToken('valid-activation-token');
      await activationPage.assertPasswordFieldIsVisible();

      await activationStrength.typePartiallySatisfyingPassword();
      await activationStrength.assertOnlySatisfiedRulesAreMet();

      await activationStrength.typeFullySatisfyingPassword();
      await activationStrength.assertAllRulesAreMet();
    },
  );
});
