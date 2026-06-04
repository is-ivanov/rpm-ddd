import { test } from '@playwright/test';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';

test.describe('Login Page', () => {
  let loginPage: LoginPageStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginPage = new LoginPageStatements(page, baseURL!);
  });

  test('UI Test Scenario 1.1: Login page shows login and password fields and submit button - Given the user navigates to the login page, Then the page displays a login input field, And the page displays a password input field, And the password field masks entered text, And the page displays a submit button with text "Sign In"', async () => {
    await loginPage.navigateToLoginPage();

    await loginPage.assertLoginFieldIsVisible();
    await loginPage.assertPasswordFieldIsVisible();
    await loginPage.assertPasswordFieldMasksText();
    await loginPage.assertSubmitButtonIsVisible();
  });

  test('UI Test Scenario 2.1: Password visibility toggle shows and hides password - Given the user is on the login page, When the user enters text into the password field, Then the password field masks the entered text, When the user clicks the password visibility toggle, Then the password field reveals the entered text in plain form, When the user clicks the password visibility toggle again, Then the password field masks the entered text again', async () => {
    test.skip(); // TDD Red Phase - password-toggle does not exist yet (built in align-design)

    await loginPage.navigateToLoginPage();

    await loginPage.enterPasswordText('s3cr3t-pass');
    await loginPage.assertPasswordFieldMasksValue('s3cr3t-pass');

    await loginPage.clickPasswordVisibilityToggle();
    await loginPage.assertPasswordFieldRevealsValue('s3cr3t-pass');

    await loginPage.clickPasswordVisibilityToggle();
    await loginPage.assertPasswordFieldMasksValue('s3cr3t-pass');
  });
});
