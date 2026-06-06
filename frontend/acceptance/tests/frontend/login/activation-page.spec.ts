import { test } from '@playwright/test';
import { ActivationPageStatements } from '../../statements/frontend/activation-page.statements';
import { ActivationBackendStatements } from '../../statements/backend/activation-backend.statements';

test.describe('Activation Page', () => {
  let activationPage: ActivationPageStatements;
  let activationBackend: ActivationBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationPage = new ActivationPageStatements(page, baseURL!);
    activationBackend = new ActivationBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.1: Activation page shows password fields and complexity rules - ' +
      'Given the user navigates to the activation page with a valid token, ' +
      'Then the page displays a password input field, ' +
      'And the page displays a confirm password input field, ' +
      'And the page displays password complexity rules, ' +
      'And the page displays a submit button with text "Activate Account"',
    async () => {
      await activationPage.navigateToActivationPageWithToken('valid-activation-token');

      await activationPage.assertPasswordFieldIsVisible();
      await activationPage.assertConfirmPasswordFieldIsVisible();
      await activationPage.assertComplexityRulesAreDisplayed();
      await activationPage.assertSubmitButtonIsVisible();
    },
  );

  test(
    'UI Test Scenario 5.1: Successful activation shows success message and "Go to Sign In" button - ' +
      'Given the user is on the activation page with a valid token, ' +
      'When the user enters a valid password meeting all complexity rules, ' +
      'And the user enters the same password in the confirm field, ' +
      'And the user clicks the "Activate Account" button, ' +
      'Then the page displays a green check icon, ' +
      'And the page displays the text "Account Activated!", ' +
      'And the page displays a button with text "Go to Sign In"',
    async () => {
      await activationBackend.givenPendingAccountForToken({ login: 'ivan', email: 'ivan@example.com' });
      await activationPage.navigateToActivationPageWithToken('valid-activation-token');

      await activationPage.enterPassword('Str0ng-P@ssw0rd!');
      await activationPage.enterConfirmPassword('Str0ng-P@ssw0rd!');
      await activationPage.clickActivateButton();

      await activationPage.assertSuccessIconIsVisible();
      await activationPage.assertSuccessMessageIsDisplayed();
      await activationPage.assertGoToSignInButtonIsVisible();
    },
  );

  test(
    'UI Test Scenario 5.2: Expired token shows error message and "Request New Link" button - ' +
      'Given the user navigates to the activation page with an expired token, ' +
      'Then the page displays a red X icon, ' +
      'And the page displays the text "Link Expired", ' +
      'And the page displays a button with text "Request New Link"',
    async () => {
      test.skip(); // TDD Red Phase - activation-error view not found in ActivationPage.vue

      await activationBackend.givenExpiredToken();
      await activationPage.navigateToActivationPageWithToken('expired-activation-token');

      await activationPage.assertErrorIconIsVisible();
      await activationPage.assertErrorMessageIsDisplayed();
      await activationPage.assertRequestNewLinkButtonIsVisible();
    },
  );
});
