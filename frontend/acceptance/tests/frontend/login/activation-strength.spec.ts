import { test } from '@playwright/test';
import { ActivationStrengthStatements } from '../../statements/frontend/activation-strength.statements';
import { ActivationBackendStatements } from '../../statements/backend/activation-backend.statements';

test.describe('Activation Page Password Strength', () => {
  let activationStrength: ActivationStrengthStatements;
  let activationBackend: ActivationBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationStrength = new ActivationStrengthStatements(page, baseURL!);
    activationBackend = new ActivationBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.2: Activation page shows password strength indicator updating in real-time - ' +
      'Given the activation page is displayed, ' +
      'When the user types a weak password, ' +
      'Then the password strength indicator shows weak, ' +
      'When the user updates the password to a strong value, ' +
      'Then the password strength indicator updates to strong in real-time',
    async () => {
      // RED: the password strength indicator (data-testid="password-strength") does not exist
      // yet. It is net-new for #189 and will be built in align-design. The pinned assertion below
      // (strength indicator is visible) fails because the element is absent.
      test.fail();
      await activationBackend.givenPendingAccountForToken({ login: 'ivan', email: 'ivan@example.com' });
      await activationStrength.navigateToActivationPageWithToken('valid-activation-token');

      await activationStrength.typePassword('weak');
      await activationStrength.assertStrengthIndicatorShowsWeak();

      await activationStrength.typePassword('Str0ng-P@ssw0rd!');
      await activationStrength.assertStrengthIndicatorUpdatesToStrong();
    },
  );
});
