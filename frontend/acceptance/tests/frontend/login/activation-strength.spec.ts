import { test } from '@playwright/test';
import { ActivationStrengthStatements } from '../../statements/frontend/activation-strength.statements';
import { ActivationBackendStatements } from '../../statements/backend/activation-backend.statements';

test.describe('Activation Page Password Complexity Rules', () => {
  let activationStrength: ActivationStrengthStatements;
  let activationBackend: ActivationBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationStrength = new ActivationStrengthStatements(page, baseURL!);
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
      // RED: per-rule complexity indicators do not exist yet. The component currently renders the
      // rules with a single shared data-testid="password-complexity-rule" and NO per-key testid and
      // NO data-met attribute. The per-key locators (complexity-rule-{key}) + data-met="true|false"
      // are net-new for #189 and will be built in align-design. The pinned assertion below
      // (complexity-rule-lowercase has data-met="true") fails because the element/attribute is absent.
      test.fail();
      await activationBackend.givenPendingAccountForToken({ login: 'ivan', email: 'ivan@example.com' });
      await activationStrength.navigateToActivationPageWithToken('valid-activation-token');

      await activationStrength.typePartiallySatisfyingPassword();
      await activationStrength.assertOnlySatisfiedRulesAreMet();

      await activationStrength.typeFullySatisfyingPassword();
      await activationStrength.assertAllRulesAreMet();
    },
  );
});
