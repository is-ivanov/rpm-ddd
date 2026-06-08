import { test } from '@playwright/test';
import { issue } from 'allure-js-commons';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';
import { AuthBackendStatements } from '../../statements/backend/auth-backend.statements';

test.describe('Login Page', () => {
  let loginPage: LoginPageStatements;
  let authBackend: AuthBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginPage = new LoginPageStatements(page, baseURL!);
    authBackend = new AuthBackendStatements(page);
  });

  test(
    'UI Test Scenario 1.1: Login page shows login and password fields and submit button - ' +
      'Given the user navigates to the login page, ' +
      'Then the page displays a login input field, ' +
      'And the page displays a password input field, ' +
      'And the password field masks entered text, ' +
      'And the page displays a submit button with text "Sign In"',
    async () => {
      await loginPage.navigateToLoginPage();

      await loginPage.assertLoginFieldIsVisible();
      await loginPage.assertPasswordFieldIsVisible();
      await loginPage.assertPasswordFieldMasksText();
      await loginPage.assertSubmitButtonIsVisible();
    },
  );

  test(
    'UI Test Scenario 2.1: Password visibility toggle shows and hides password - ' +
      'Given the user is on the login page, ' +
      'When the user enters text into the password field, ' +
      'Then the password field masks the entered text, ' +
      'When the user clicks the password visibility toggle, ' +
      'Then the password field reveals the entered text in plain form, ' +
      'When the user clicks the password visibility toggle again, ' +
      'Then the password field masks the entered text again',
    async () => {
      await loginPage.navigateToLoginPage();

      await loginPage.enterPasswordText('s3cr3t-pass');
      await loginPage.assertPasswordFieldMasksValue('s3cr3t-pass');

      await loginPage.clickPasswordVisibilityToggle();
      await loginPage.assertPasswordFieldRevealsValue('s3cr3t-pass');

      await loginPage.clickPasswordVisibilityToggle();
      await loginPage.assertPasswordFieldMasksValue('s3cr3t-pass');
    },
  );

  test(
    'UI Test Scenario 3.1: Wrong credentials show error banner with "Invalid username or password" - ' +
      'Given the user is on the login page, ' +
      'And a registered user with login "ivan" and password "correct-pass" exists, ' +
      'When the user enters login "ivan", ' +
      'And the user enters password "wrong-pass", ' +
      'And the user clicks the "Sign In" button, ' +
      'Then an error banner appears with text "Invalid username or password", ' +
      'And the login and password fields are cleared',
    async () => {
      await authBackend.givenRegisteredUser('ivan', 'correct-pass');
      await loginPage.navigateToLoginPage();

      await loginPage.enterLoginText('ivan');
      await loginPage.enterPasswordText('wrong-pass');
      await loginPage.clickSubmitButton();

      await loginPage.assertErrorBannerShowsInvalidCredentials();
      await loginPage.assertLoginAndPasswordFieldsAreCleared();
    },
  );

  test(
    'UI Test Scenario 3.2: Inactive account shows error banner with activation message - ' +
      'Given the user is on the login page, ' +
      'And an inactive user with login "pending" and password "some-pass" exists, ' +
      'When the user enters login "pending", ' +
      'And the user enters password "some-pass", ' +
      'And the user clicks the "Sign In" button, ' +
      'Then an error banner appears with text indicating the account requires activation, ' +
      'And the error banner contains a link to request a new activation email',
    async () => {
      await authBackend.givenInactiveUser('pending', 'some-pass');
      await loginPage.navigateToLoginPage();

      await loginPage.enterLoginText('pending');
      await loginPage.enterPasswordText('some-pass');
      await loginPage.clickSubmitButton();

      await loginPage.assertErrorBannerShowsActivationRequired();
      await loginPage.assertErrorBannerContainsActivationLink();
    },
  );

  test(
    'UI Bug #127: Unexpected login failure shows a generic error banner - ' +
      'Given the user is on the login page, ' +
      'And the login endpoint will fail unexpectedly, ' +
      'When the user enters login "ivan", ' +
      'And the user enters password "correct-pass", ' +
      'And the user clicks the "Sign In" button, ' +
      'Then an error banner appears with text "Something went wrong. Please try again."',
    async () => {
      await issue('127');
      await authBackend.givenLoginRequestFails();
      await loginPage.navigateToLoginPage();

      await loginPage.enterLoginText('ivan');
      await loginPage.enterPasswordText('correct-pass');
      await loginPage.clickSubmitButton();

      await loginPage.assertErrorBannerShowsGenericError();
    },
  );

  test(
    'UI Bug #131: Sign In is disabled until both username and password are filled - ' +
      'Given the user is on the login page, ' +
      'Then the Sign In button is disabled, ' +
      'When the user enters only a username, ' +
      'Then the Sign In button remains disabled, ' +
      'When the user also enters a password, ' +
      'Then the Sign In button becomes enabled',
    async () => {
      await issue('131');
      await loginPage.navigateToLoginPage();

      await loginPage.assertSubmitButtonIsDisabled();

      await loginPage.enterLoginText('ivan');
      await loginPage.assertSubmitButtonIsDisabled();

      await loginPage.enterPasswordText('correct-pass');
      await loginPage.assertSubmitButtonIsEnabled();
    },
  );

  test(
    'UI Bug #131: Forgot-password placeholder is absent from the login page - ' +
      'Given the user navigates to the login page, ' +
      'Then no "Forgot password" element is present',
    async () => {
      await issue('131');
      await loginPage.navigateToLoginPage();

      await loginPage.assertForgotPasswordIsAbsent();
    },
  );

  // RED: implementation already complete (Fix 3 align-design); test passes on first run.
  // green-playwright removes this .skip marker and confirms.
  test.skip(
    'UI Bug #131: Field-validation errors render under their inputs, not in the global banner - ' +
      'Given the user is on the login page, ' +
      'And the login endpoint returns a 422 with per-field validation errors for login and password, ' +
      'When the user fills both the username and password fields, ' +
      'And the user clicks the "Sign In" button, ' +
      'Then the login field error message appears under the username input, ' +
      'And the password field error message appears under the password input, ' +
      'And the global error banner is not present',
    async () => {
      await issue('131');
      await authBackend.givenLoginReturnsFieldValidationErrors();
      await loginPage.navigateToLoginPage();

      await loginPage.enterLoginText('ivan');
      await loginPage.enterPasswordText('short');
      await loginPage.clickSubmitButton();

      await loginPage.assertLoginFieldErrorShown();
      await loginPage.assertPasswordFieldErrorShown();
      await loginPage.assertErrorBannerIsAbsent();
    },
  );
});
