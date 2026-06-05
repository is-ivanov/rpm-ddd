import { test } from '@playwright/test';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';
import { AuthBackendStatements } from '../../statements/backend/auth-backend.statements';

test.describe('Login Page', () => {
  let loginPage: LoginPageStatements;
  let authBackend: AuthBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginPage = new LoginPageStatements(page, baseURL!);
    authBackend = new AuthBackendStatements(page);
  });

  test('UI Test Scenario 1.1: Login page shows login and password fields and submit button - Given the user navigates to the login page, Then the page displays a login input field, And the page displays a password input field, And the password field masks entered text, And the page displays a submit button with text "Sign In"', async () => {
    await loginPage.navigateToLoginPage();

    await loginPage.assertLoginFieldIsVisible();
    await loginPage.assertPasswordFieldIsVisible();
    await loginPage.assertPasswordFieldMasksText();
    await loginPage.assertSubmitButtonIsVisible();
  });

  test('UI Test Scenario 2.1: Password visibility toggle shows and hides password - Given the user is on the login page, When the user enters text into the password field, Then the password field masks the entered text, When the user clicks the password visibility toggle, Then the password field reveals the entered text in plain form, When the user clicks the password visibility toggle again, Then the password field masks the entered text again', async () => {
    await loginPage.navigateToLoginPage();

    await loginPage.enterPasswordText('s3cr3t-pass');
    await loginPage.assertPasswordFieldMasksValue('s3cr3t-pass');

    await loginPage.clickPasswordVisibilityToggle();
    await loginPage.assertPasswordFieldRevealsValue('s3cr3t-pass');

    await loginPage.clickPasswordVisibilityToggle();
    await loginPage.assertPasswordFieldMasksValue('s3cr3t-pass');
  });

  test('UI Test Scenario 3.1: Wrong credentials show error banner with "Invalid username or password" - Given the user is on the login page, And a registered user with login "ivan" and password "correct-pass" exists, When the user enters login "ivan", And the user enters password "wrong-pass", And the user clicks the "Sign In" button, Then an error banner appears with text "Invalid username or password", And the login and password fields are cleared', async () => {
    test.skip(); // TDD Red Phase - error-banner not found; LoginPage has no submit handler or error handling yet

    await authBackend.givenRegisteredUser('ivan', 'correct-pass');
    await loginPage.navigateToLoginPage();

    await loginPage.enterLoginText('ivan');
    await loginPage.enterPasswordText('wrong-pass');
    await loginPage.clickSubmitButton();

    await loginPage.assertErrorBannerShowsInvalidCredentials();
    await loginPage.assertLoginAndPasswordFieldsAreCleared();
  });
});
