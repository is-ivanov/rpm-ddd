import { test } from '@playwright/test';
import { issue } from 'allure-js-commons';
import { ActivationPageStatements } from '../../statements/frontend/activation-page.statements';
import { ActivationFailureStatements } from '../../statements/frontend/activation-failure.statements';
import { ActivationBackendStatements } from '../../statements/backend/activation-backend.statements';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';

test.describe('Activation Page', () => {
  let activationPage: ActivationPageStatements;
  let activationFailure: ActivationFailureStatements;
  let activationBackend: ActivationBackendStatements;
  let loginPage: LoginPageStatements;

  test.beforeEach(({ page, baseURL }) => {
    activationPage = new ActivationPageStatements(page, baseURL!);
    activationFailure = new ActivationFailureStatements(page);
    activationBackend = new ActivationBackendStatements(page);
    loginPage = new LoginPageStatements(page, baseURL!);
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
      await activationBackend.givenExpiredToken();
      await activationPage.navigateToActivationPageWithToken('expired-activation-token');

      await activationPage.assertErrorIconIsVisible();
      await activationPage.assertErrorMessageIsDisplayed();
      await activationPage.assertRequestNewLinkButtonIsVisible();
    },
  );

  test(
    'UI Test Scenario 6.1: Clicking "Go to Sign In" navigates to login page - ' +
      'Given the user has completed account activation, ' +
      'And the success screen is displayed with button "Go to Sign In", ' +
      'When the user clicks the "Go to Sign In" button, ' +
      'Then the user is navigated to the login page',
    async () => {
      await activationBackend.givenPendingAccountForToken({ login: 'ivan', email: 'ivan@example.com' });
      await activationPage.completeActivationAndReachSuccessScreen('Str0ng-P@ssw0rd!');

      await activationPage.clickGoToSignInButton();

      await activationPage.assertNavigatedToLoginPage();
      await loginPage.assertLoginFieldIsVisible();
      await loginPage.assertPasswordFieldIsVisible();
      await loginPage.assertPasswordFieldMasksText();
      await loginPage.assertSubmitButtonIsVisible();
    },
  );

  test(
    'UI Bug #188: Activation page must not show success when the backend rejects the submitted password - ' +
      'Given the user is on the activation page with a valid token, ' +
      'When the user submits a password that the backend rejects with a 422 ProblemDetail, ' +
      'Then the "Account Activated!" success screen is NOT shown, ' +
      'And a server error message with the backend detail is displayed instead',
    async () => {
      await issue('188');
      // RED — ActivationPage shows the success screen on a 4xx POST and renders no
      // server-error element (activateAccount discards the Response, activated=true is
      // set unconditionally). assertServerErrorIsDisplayed fails: element not found.
      test.fail();
      await activationBackend.givenValidTokenButActivationRejectsWeakPassword({
        login: 'ivan',
        email: 'ivan@example.com',
      });
      await activationPage.navigateToActivationPageWithToken('valid-activation-token');

      await activationPage.enterPassword('weak');
      await activationPage.enterConfirmPassword('weak');
      await activationPage.clickActivateButton();

      await activationFailure.assertServerErrorIsDisplayed('Password does not meet the complexity requirements.');
      await activationFailure.assertSuccessScreenIsNotVisible();
    },
  );
});
