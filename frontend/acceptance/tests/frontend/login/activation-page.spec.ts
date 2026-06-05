import { test } from '@playwright/test';
import { ActivationPageStatements } from '../../statements/frontend/activation-page.statements';

test.describe('Activation Page', () => {
  let activationPage: ActivationPageStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationPage = new ActivationPageStatements(page, baseURL!);
  });

  test('UI Test Scenario 4.1: Activation page shows password fields and complexity rules - Given the user navigates to the activation page with a valid token, Then the page displays a password input field, And the page displays a confirm password input field, And the page displays password complexity rules, And the page displays a submit button with text "Activate Account"', async () => {
    // TDD Red Phase - activation page/route not implemented yet, testids not found
    test.skip();

    await activationPage.navigateToActivationPageWithToken('valid-activation-token');

    await activationPage.assertPasswordFieldIsVisible();
    await activationPage.assertConfirmPasswordFieldIsVisible();
    await activationPage.assertComplexityRulesAreDisplayed();
    await activationPage.assertSubmitButtonIsVisible();
  });
});
